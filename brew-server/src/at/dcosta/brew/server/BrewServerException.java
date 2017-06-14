package at.dcosta.brew.server;

import javax.ws.rs.core.Response.Status;

public class BrewServerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final Status status;

	public BrewServerException(String message, Status status) {
		super(message);
		this.status = status;
	}

	public BrewServerException(String message, Throwable cause, Status status) {
		super(message, cause);
		this.status = status;
	}

	public Status getHttpReturnCode() {
		return status;
	}

}
