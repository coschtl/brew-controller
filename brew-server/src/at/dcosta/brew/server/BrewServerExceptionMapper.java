package at.dcosta.brew.server;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class BrewServerExceptionMapper implements
                                 ExceptionMapper<BrewServerException> {
 
    @Override
    public Response toResponse(BrewServerException ex) {
    	ex.printStackTrace();
        return Response.status(ex.getHttpReturnCode())
        		.header("x-server-error", "Fehler: " + ex.getMessage())
                .entity(ex.getMessage()).build();
    }
    
}
