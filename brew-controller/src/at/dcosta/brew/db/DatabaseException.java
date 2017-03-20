package at.dcosta.brew.db;

import java.sql.SQLException;

public class DatabaseException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DatabaseException(String message, SQLException cause) {
		super(message, cause);
	}
	
	@Override
	public synchronized SQLException getCause() {
		return (SQLException) super.getCause();
	}

}
