package at.dcosta.brew;

public class BrewException extends Exception {

	private static final long serialVersionUID = 1L;

	public BrewException(String message) {
		super(message);
	}

	public BrewException(String message, Throwable cause) {
		super(message, cause);
	}

	public BrewException(Throwable cause) {
		super(cause);
	}

}
