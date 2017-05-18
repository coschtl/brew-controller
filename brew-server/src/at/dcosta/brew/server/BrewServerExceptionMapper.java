package at.dcosta.brew.server;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class BrewServerExceptionMapper implements
                                 ExceptionMapper<BrewServerException> {
 
    @Override
    public Response toResponse(BrewServerException ex) {
        return Response.status(ex.getHttpReturnCode())
                .entity(ex.getMessage()).build();
    }
}
