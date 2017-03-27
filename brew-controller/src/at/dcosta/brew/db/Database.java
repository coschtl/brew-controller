package at.dcosta.brew.db;

import static at.dcosta.brew.Configuration.DATABASE_LOCATION;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import at.dcosta.brew.Configuration;

abstract class Database {

	private static final String SQL_CHECK_TABLE_EXISTS = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?";
	private static final String SQL_LAST_ROW_ID = "SELECT last_insert_rowid()";
	private static final String JDBC_URL_PREFIX = "jdbc:sqlite:";

	private final String jdbcUrl;

	protected Database() {
		jdbcUrl = JDBC_URL_PREFIX + Configuration.getInstance().getString(DATABASE_LOCATION);
		createTablesIfNecessary();
	}

	private void createTablesIfNecessary() {
		String err = "Can not check if table exists: ";
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			for (int i = 0; i < getTableNames().length; i++) {
				st = con.prepareStatement(SQL_CHECK_TABLE_EXISTS);
				st.setString(1, getTableNames()[i]);
				rs = st.executeQuery();
				if (rs.next() && rs.getInt(1) == 1) {
					continue;
				}
				close(rs);
				close(st);
				err = "Can not create non existing table: ";
				String sqlCreateTable = getCreateTableStatements()[i];
				st = con.prepareStatement(sqlCreateTable);
				st.executeUpdate();
				close(st);
			}
		} catch (SQLException e) {
			throw new DatabaseException(err + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
	}

	protected void close(Connection con) {
		try {
			if (con != null) {
				con.close();
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

	protected void close(Statement st) {
		try {
			if (st != null) {
				st.close();
			}
		} catch (SQLException e) {
			// ignore
		}
	}

	protected Connection getConnection() {
		try {
			return DriverManager.getConnection(jdbcUrl);
		} catch (SQLException e) {
			throw new DatabaseException("Can not conect ot database: " + e.getMessage(), e);
		}
	}

	protected abstract String[] getCreateTableStatements();

	protected int getLastInsertedRowId(Connection con) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(SQL_LAST_ROW_ID);
			rs = st.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
			throw new DatabaseException("can not get last inserted rowId!");
		} catch (SQLException e) {
			throw new DatabaseException("can not get last inserted rowId: " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
		}
	}

	protected abstract String[] getTableNames();

	protected Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}

}
