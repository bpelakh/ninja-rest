package ninja;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/** Add JSON serialization support. */
@Provider
@Produces({MediaType.APPLICATION_JSON})
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class JsonProvider implements ContextResolver<ObjectMapper> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return MAPPER;
    }
}
