package at.dcosta.brew.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import at.dcosta.brew.Configuration;

abstract class Database {

	private static final String JDBC_URL_PREFIX = "jdbc:sqlite:";

	private final String jdbcUrl;

	protected Database(Configuration configuration) {
		jdbcUrl = JDBC_URL_PREFIX + configuration.getProperty(Configuration.DATABASE_LOCATION);
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(jdbcUrl);
	}

	protected abstract String getTableName();

	protected abstract String getCreateTableStatement();

	protected void close(Connection con) {
		try {
			con.close();
		} catch (SQLException e) {
			// ignore
		}
	}

	protected void close(Statement st) {
		try {
			st.close();
		} catch (SQLException e) {
			// ignore
		}
	}

	protected void close(ResultSet rs) {
		try {
			rs.close();
		} catch (SQLException e) {
			// ignore
		}
	}

}
