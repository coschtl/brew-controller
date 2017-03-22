package at.dcosta.brew.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import at.dcosta.brew.Configuration;

abstract class Database {

	private static final String SQL_CHECK_TABLE_EXISTS = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?";

	private static final String JDBC_URL_PREFIX = "jdbc:sqlite:";

	private final String jdbcUrl;

	protected Database(Configuration configuration) {
		jdbcUrl = JDBC_URL_PREFIX + configuration.getDatabaseLocation();
		createTableIfNecessary();
	}

	private void createTableIfNecessary() {
		String err = "Can not check if table exists: ";
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(SQL_CHECK_TABLE_EXISTS);
			st.setString(1, getTableName());
			rs = st.executeQuery();
			if (rs.next() && rs.getInt(1)  == 1) {
				return;
			}
			close(rs);
			close(st);
			err = "Can not create non existing table: ";
			st = con.prepareStatement(getCreateTableStatement());
			 st.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseException(err + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
	}

	public Connection getConnection()  {
		try {
			return DriverManager.getConnection(jdbcUrl);
		} catch (SQLException e) {
			throw new DatabaseException("Can not conect ot database: " + e.getMessage(), e);
		}
	}

	protected abstract String getTableName();

	protected abstract String getCreateTableStatement();

	protected void close(Connection con) {
		try {
			if (con != null) {
				con.close();
			}
		} catch (SQLException e) {
			// ignore
		}
	}

	protected void close(Statement st) {
		try {
			if (st != null) {
				st.close();
			}
		} catch (SQLException e) {
			// ignore
		}
	}

	protected void close(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			// ignore
		}
	}

}
