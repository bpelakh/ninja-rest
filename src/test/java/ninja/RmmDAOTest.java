package ninja;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class RmmDAOTest {

    private static final String MEMORY_DB = "jdbc:h2:mem:test";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Instance being tested.
     */
    private RmmDAO dao;

    @Before
    public void init() throws SQLException, IOException {
        runScript("/createDB.sql");
        dao = new RmmDAO(MEMORY_DB);
    }

    @After
    public void clean() throws SQLException, IOException {
        runScript("/cleanUp.sql");
    }

    private void runScript(final String script) throws SQLException, IOException {
        Connection connection = DriverManager.getConnection(MEMORY_DB);
        try (InputStream stream = RmmDAO.class.getResourceAsStream(script)) {
            final Scanner scanner = new Scanner(stream).useDelimiter(";");
            while (scanner.hasNext()) {
                try (PreparedStatement ps = connection.prepareStatement(scanner.next())) {
                    ps.execute();
                }
            }
        }
    }

    @Test
    public void testAddDevices() throws SQLException {
        assertEquals(Collections.emptyList(), dao.getDevices("new_customer"));

        final List<Device> devices = Arrays.asList(
                Device.builder().id("a").type(Device.Type.MAC).name("Mac A").build(),
                Device.builder().id("b").type(Device.Type.WINDOWS_SERVER).name("Server").build(),
                Device.builder().id("c").type(Device.Type.WINDOWS_WORKSTATION).name("WS").build()
        );

        dao.addDevices("MegaCorp", devices);

        assertEquals(devices, dao.getDevices("MegaCorp"));
    }

    @Test
    public void testDuplicateDevices() throws SQLException {
        final List<Device> devices = Arrays.asList(
                Device.builder().id("a").type(Device.Type.MAC).name("Mac A").build(),
                Device.builder().id("b").type(Device.Type.WINDOWS_SERVER).name("Server").build(),
                Device.builder().id("c").type(Device.Type.WINDOWS_WORKSTATION).name("WS").build()
        );

        dao.addDevices("MegaCorp", devices);
        thrown.expect(SQLException.class);
        dao.addDevices("MegaCorp", devices);
    }

    @Test
    public void testUpdateDevices() throws SQLException {
        final List<Device> devices = Arrays.asList(
                Device.builder().id("a").type(Device.Type.MAC).name("Mac A").build(),
                Device.builder().id("b").type(Device.Type.WINDOWS_SERVER).name("Server").build(),
                Device.builder().id("c").type(Device.Type.WINDOWS_WORKSTATION).name("WS").build()
        );

        dao.addDevices("MegaCorp", devices);

        dao.updateDevices("MegaCorp",
                Arrays.asList(
                        Device.builder().id("a").type(Device.Type.MAC).name("Mac Again").build(),
                        Device.builder().id("c").type(Device.Type.WINDOWS_SERVER).name("Upgraded").build()
                ));
        final List<Device> modified = Arrays.asList(
                Device.builder().id("a").type(Device.Type.MAC).name("Mac Again").build(),
                Device.builder().id("b").type(Device.Type.WINDOWS_SERVER).name("Server").build(),
                Device.builder().id("c").type(Device.Type.WINDOWS_SERVER).name("Upgraded").build()
        );
        assertEquals(modified, dao.getDevices("MegaCorp"));
    }

    @Test
    public void deleteAddDevice() throws SQLException {
        final List<Device> devices = Arrays.asList(
                Device.builder().id("a").type(Device.Type.MAC).name("Mac A").build(),
                Device.builder().id("b").type(Device.Type.WINDOWS_SERVER).name("Server").build(),
                Device.builder().id("c").type(Device.Type.WINDOWS_WORKSTATION).name("WS").build()
        );

        dao.addDevices("MegaCorp", devices);

        dao.deleteDevice("MegaCorp", "unknown");
        assertEquals(devices, dao.getDevices("MegaCorp"));

        dao.deleteDevice("MegaCorp", "b");
        dao.deleteDevice("MegaCorp", "c");
        assertEquals(devices.subList(0, 1), dao.getDevices("MegaCorp"));

    }

    @Test
    public void testAddServices() throws SQLException {
        assertEquals(Collections.emptyList(), dao.getServices("new_customer"));

        final List<String> services = Arrays.asList("Antivirus", "PSA");

        dao.addServices("MegaCorp", services);

        assertEquals(services, dao.getServices("MegaCorp"));
    }

    @Test
    public void testDuplicateServices() throws SQLException {
        final List<String> services = Arrays.asList("Antivirus", "PSA");

        dao.addServices("MegaCorp", services);
        thrown.expect(SQLException.class);
        dao.addServices("MegaCorp", services);
    }

    @Test
    public void testDeleteServices() throws SQLException {
        final List<String> services = Arrays.asList("Antivirus", "PSA");

        dao.addServices("MegaCorp", services);
        assertEquals(services, dao.getServices("MegaCorp"));

        dao.deleteService("MegaCorp", "PSA");
        assertEquals(Arrays.asList("Antivirus"), dao.getServices("MegaCorp"));
    }

    @Test
    public void testMonthly() throws SQLException {
        assertEquals(0, dao.monthlyCost("new_customer"));

        final List<Device> devices = Arrays.asList(
                Device.builder().id("a").type(Device.Type.MAC).name("Mac A").build(),
                Device.builder().id("b").type(Device.Type.MAC).name("Mac B").build(),
                Device.builder().id("c").type(Device.Type.MAC).name("Mac C").build(),
                Device.builder().id("d").type(Device.Type.WINDOWS_SERVER).name("Server").build(),
                Device.builder().id("e").type(Device.Type.WINDOWS_WORKSTATION).name("WS").build()
        );

        dao.addDevices("MegaCorp", devices);
        dao.addServices("MegaCorp", Arrays.asList("Antivirus", "Cloudberry", "TeamViewer"));

        assertEquals(71, dao.monthlyCost("MegaCorp"));
    }
}
