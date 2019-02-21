package ninja;

import org.glassfish.jersey.internal.util.Base64;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

/**
 * This filter verify the access permissions for a user
 * based on username and passowrd provided in request
 */
@Provider
public class AuthenticationFilter implements javax.ws.rs.container.ContainerRequestFilter {

    private Connection connection;

    private Connection getConnection() throws SQLException {
        if (connection == null) {
            try {
                // Tomcat would not, for some reason, auto-discover my driver unless I did this.
                // There is probably a better ServiceFactory-based method to do this.
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            connection = DriverManager.getConnection(DBConfig.DB_URL, DBConfig.DB_USER, DBConfig.DB_PWD);
        }
        return connection;
    }

    @Context
    private ResourceInfo resourceInfo;

    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Basic";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Method method = resourceInfo.getResourceMethod();
        //Get request headers
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();

        //Fetch authorization header
        final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

        //If no authorization information present; block access
        if (authorization == null || authorization.isEmpty()) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("You cannot access this resource").build());
            return;
        }

        //Get encoded username and password
        final String encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");

        //Decode username and password
        String usernameAndPassword = new String(Base64.decode(encodedUserPassword.getBytes()));
        ;

        //Split username and password tokens
        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
        final String username = tokenizer.nextToken();
        final String password = tokenizer.nextToken();

        //Verify user access
        if (method.isAnnotationPresent(RolesAllowed.class)) {
            RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
            Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));

            //Is user valid?
            if (!isUserAllowed(username, password, rolesSet)) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .entity("You cannot access this resource").build());
                return;
            }
        }
    }

    /** Fetch user record from DB and validate against role set. */
    private boolean isUserAllowed(final String username, final String password, final Set<String> rolesSet) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "select rolename from users where username = ? and password = crypt(?, password)"))
        {
            ps.setString(1, username);
            ps.setString(2, password);
            System.out.println("About to run " + ps);
            ResultSet rs = ps.executeQuery();
            if (rs == null || !rs.next()) {
                System.out.println("User " + username + " not found");
                return false;
            }
            return rolesSet.contains(rs.getString(1));
        } catch (final SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    }
}