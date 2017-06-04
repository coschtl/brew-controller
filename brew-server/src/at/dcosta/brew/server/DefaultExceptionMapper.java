package at.dcosta.brew.server;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

	@Override
	public Response toResponse(Throwable t) {
		t.printStackTrace();
		return Response.status(Status.INTERNAL_SERVER_ERROR).header("x-server-error", "Fehler: " + t.getMessage())
				.entity(t.getMessage()).build();
	}

}
