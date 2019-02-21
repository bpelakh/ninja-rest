package ninja;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Path("/rmm")
public class RmmService {

    /**
     * Connecting to local Postgres by default, modify through rmm.db.url property.
     */
    private final String DB_URL = System.getProperty("rmm.db.url", "jdbc:postgresql://localhost/ninja");

    /**
     * DB username - override through rmm.db.user property.
     */
    private final String DB_USER = System.getProperty("rmm.db.user", "boris");

    /**
     * DB password - override through rmm.db.password property.
     * In a real system, would use secure connection provided by container.
     */
    private final String DB_PWD = System.getProperty("rmm.db.password", "password");

    private RmmDAO dao;

    private RmmDAO getDao() throws SQLException {
        if (dao == null) {
            try {
                // Tomcat would not, for some reason, auto-discover my driver unless I did this.
                // There is probably a better ServiceFactory-based method to do this.
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            dao = new RmmDAO(DB_URL, DB_USER, DB_PWD);
        }
        return dao;
    }

    @Path("/devices/{customer}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @RolesAllowed({"READER","WRITER"})
    public List<Device> getDevices(@PathParam("customer") final String customer) {
        try {
            return getDao().getDevices(customer);
        } catch (final SQLException sqle) {
            return Collections.EMPTY_LIST;
        }
    }

    @Path("/devices/{customer}")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @RolesAllowed({"WRITER"})
    public Response addtDevices(@PathParam("customer") final String customer, @Valid final List<Device> devices) {
        try {
            getDao().addDevices(customer, devices);
            return Response.accepted().build();
        } catch (final SQLException sqle) {
            return Response.status(Response.Status.CONFLICT).entity(sqle.getMessage()).build();
        }
    }

    @Path("/devices/{customer}")
    @Consumes(MediaType.APPLICATION_JSON)
    @PUT
    @RolesAllowed({"WRITER"})
    public Response updateDevices(@PathParam("customer") final String customer, @Valid final List<Device> devices) {
        try {
            getDao().updateDevices(customer, devices);
            return Response.accepted().build();
        } catch (final SQLException sqle) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(sqle.getMessage()).build();
        }
    }

    @Path("/devices/{customer}/{device}")
    @Consumes(MediaType.APPLICATION_JSON)
    @DELETE
    @RolesAllowed({"WRITER"})
    public Response deleteDevice(@PathParam("customer") final String customer, @PathParam("device") final String device) {
        try {
            getDao().deleteDevice(customer, device);
            return Response.accepted().build();
        } catch (final SQLException sqle) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(sqle.getMessage()).build();
        }
    }

    @Path("/services/{customer}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @RolesAllowed({"READER","WRITER"})
    public List<String> getServices(@PathParam("customer") final String customer) {
        try {
            return getDao().getServices(customer);
        } catch (final SQLException sqle) {
            return Collections.EMPTY_LIST;
        }
    }

    @Path("/services/{customer}")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @RolesAllowed({"WRITER"})
    public Response addtServices(@PathParam("customer") final String customer, @Valid final List<String> services) {
        try {
            getDao().addServices(customer, services);
            return Response.accepted().build();
        } catch (final SQLException sqle) {
            return Response.status(Response.Status.CONFLICT).entity(sqle.getMessage()).build();
        }
    }

    @Path("/services/{customer}/{service}")
    @Consumes(MediaType.APPLICATION_JSON)
    @DELETE
    @RolesAllowed({"WRITER"})
    public Response deleteService(@PathParam("customer") final String customer, @PathParam("service") final String service) {
        try {
            getDao().deleteService(customer, service);
            return Response.accepted().build();
        } catch (final SQLException sqle) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(sqle.getMessage()).build();
        }
    }

    @Path("/cost/{customer}")
    @Consumes(MediaType.APPLICATION_JSON)
    @GET
    @RolesAllowed({"READER","WRITER"})
    public Response montlyCost(@PathParam("customer") final String customer) {
        try {
            final int cost = getDao().monthlyCost(customer);
            return Response.ok(cost).build();
        } catch (final SQLException sqle) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(sqle.getMessage()).build();
        }
    }
}
