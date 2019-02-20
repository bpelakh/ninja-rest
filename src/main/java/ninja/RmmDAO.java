package ninja;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides data access for RMM app.
 * <p>
 * Methods here will throw SQLException on failure. In a real system, SQLEXception would be
 * caught locally, and wrapped in an application-specific exception.
 */
public class RmmDAO {

    /**
     * Persistent DB conneciton. In a real application, this would use a connection pool.
     */
    private final Connection connection;

    /**
     * Constructor.
     */
    RmmDAO(final String jdbcUrl) throws SQLException {
        connection = DriverManager.getConnection(jdbcUrl);
    }

    /**
     * Add devices.
     *
     * @param customer Customer ID.
     * @param devices  List of devices. Attempting to insert a duplicate device will reject entire batch.
     * @throws SQLException
     */
    public void addDevices(final String customer, final List<Device> devices) throws SQLException {
        connection.setAutoCommit(false);
        String query = "INSERT INTO devices (customer, id, type, name) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            for (Device record : devices) {
                ps.setString(1, customer);
                ps.setString(2, record.id());
                ps.setString(3, record.type().toString());
                ps.setString(4, record.name());
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Update devices.
     *
     * @param customer Customer ID.
     * @param devices  List of devices. Attempting to insert a duplicate device will reject entire batch.
     * @throws SQLException
     */
    public void updateDevices(final String customer, final List<Device> devices) throws SQLException {
        connection.setAutoCommit(false);
        String query = "UPDATE devices SET type = ?, name = ? WHERE customer = ? AND id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            for (Device record : devices) {
                ps.setString(1, record.type().toString());
                ps.setString(2, record.name());
                ps.setString(3, customer);
                ps.setString(4, record.id());
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Delete devices.
     *
     * @param customer Customer ID.
     * @param device  Device ID
     * @throws SQLException
     */
    public void deleteDevice(final String customer, final String device) throws SQLException {
        connection.setAutoCommit(false);
        String query = "DELETE FROM devices WHERE customer = ? and id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, customer);
            ps.setString(2, device);
            ps.execute();
            connection.commit();
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Retreive a list of devices for the specified customer
     * @param customer Customer ID.
     * @return Device list, if any.
     * @throws SQLException
     */
    public List<Device> getDevices(final String customer) throws SQLException {
        String query = "SELECT id, type, name FROM devices WHERE customer = ? ORDER BY id";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, customer);
        final ResultSet results = ps.executeQuery();
        if (results == null) {
            return Collections.emptyList();
        }

        final List<Device> devices = new ArrayList<>();
        while (results.next()) {
            devices.add(
                    Device.builder()
                            .id(results.getString("id"))
                            .type(Device.Type.valueOf(results.getString("type")))
                            .name(results.getString("name"))
                            .build());
        }
        return devices;
    }

    /**
     * Add services.
     *
     * @param customer Customer ID.
     * @param services  List of services. Attempting to insert a duplicate service will reject entire batch.
     * @throws SQLException
     */
    public void addServices(final String customer, final List<String> services) throws SQLException {
        connection.setAutoCommit(false);
        String query = "INSERT INTO services (customer, service) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            for (String service : services) {
                ps.setString(1, customer);
                ps.setString(2, service);
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Delete services.
     *
     * @param customer Customer ID.
     * @param service  Service
     * @throws SQLException
     */
    public void deleteService(final String customer, final String service) throws SQLException {
        connection.setAutoCommit(false);
        String query = "DELETE FROM services WHERE customer = ? and service = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, customer);
            ps.setString(2, service);
            ps.execute();
            connection.commit();
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Retreive a list of services for the specified customer
     * @param customer Customer ID.
     * @return Service list, if any.
     * @throws SQLException
     */
    public List<String> getServices(final String customer) throws SQLException {
        String query = "SELECT service FROM services WHERE customer = ? ORDER BY service";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, customer);
        final ResultSet results = ps.executeQuery();
        if (results == null) {
            return Collections.emptyList();
        }

        final List<String> services = new ArrayList<>();
        while (results.next()) {
            services.add(results.getString("service"));
        }
        return services;
    }

    public int monthlyCost(final String customer) throws SQLException {
        // Total up service costs for each device in inner query, then add the per-device cost in outer query
        String query = "select sum(costs.per_device + (select price from service_defs where service = 'Device')) as total " +
                "FROM (SELECT d.id, sum(sd.price) as per_device " +
                "FROM devices d, services s " +
                "INNER JOIN service_defs sd ON s.service = sd.service " +
                "WHERE d.customer = ? and s.customer = ? and " +
                "(sd.type = '' OR sd.type = d.type) group by d.id) as costs";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, customer);
            ps.setString(2, customer);
            ResultSet result = ps.executeQuery();
            result.next();
            return result.getInt("total");
        }
    }
}