
package at.dcosta.brew.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ConfigurationDB extends Database {

	public static enum Namespace {
		Version,
	}

	private static final String TABLE_NAME = "CONFIGURATION";
	private static final String[] CREATE_TABLE_STATEMENTS = new String[] {
			"CREATE TABLE " + TABLE_NAME + " (NAMESPACE varchar(255), KEY varchar(255), VALUE varchar(65000))", };
	private static final String[] CREATE_INDEX_STATEMENTS = new String[] {
			"CREATE UNIQUE INDEX I_NAMESPACE_KEY ON " + TABLE_NAME + " (NAMESPACE, KEY ASC)" };
	private static final String SQL_ADD_ENTRY = "INSERT INTO " + TABLE_NAME
			+ " (NAMESPACE, KEY, VALUE) VALUES (?, ?, ?)";
	private static final String SQL_UPDATE_ENTRY = "UPDATE " + TABLE_NAME + " SET VALUE=? where NAMESPACE=? AND KEY=?";
	private static final String SQL_GET_ALL = "SELECT ROWID, * FROM " + TABLE_NAME;
	private static final String SQL_GET_BY_ID = SQL_GET_ALL + " WHERE ROWID=?";
	private static final String SQL_GET_BY_KEY = SQL_GET_ALL + " WHERE NAMESPACE=? AND KEY=?";

	public ConfigurationDB() {
		super();
	}

	public void addEntry(Namespace namespace, String key, String value) {
		Connection con = getConnection();
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(SQL_ADD_ENTRY);
			st.setString(1, namespace.toString());
			st.setString(2, key);
			st.setString(3, value);
			int rows = st.executeUpdate();
			if (rows != 1) {
				throw new DatabaseException("can not add configuration entry for namespace=" + namespace + ", key="
						+ key + " (no row created)!");
			}
		} catch (SQLException e) {
			throw new DatabaseException("can not add configuration entry: " + e.getMessage(), e);
		} finally {
			close(st);
			close(con);
		}
	}

	public String getEntry(Namespace namespace, String key) {
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(SQL_GET_BY_KEY);
			st.setString(1, namespace.toString());
			st.setString(2, key);
			rs = st.executeQuery();
			if (rs.next()) {
				return rs.getString("VALUE");
			}
			return null;
		} catch (SQLException e) {
			throw new DatabaseException("can not read Configuration entry with namespace=" + namespace + ", key =" + key
					+ ": " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
	}

	public String getEntryById(int id) {
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(SQL_GET_BY_ID);
			st.setInt(1, id);
			rs = st.executeQuery();
			if (rs.next()) {
				return rs.getString("VALUE");
			}
			return null;
		} catch (SQLException e) {
			throw new DatabaseException("can not read Configuration entry by id '" + id + "': " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
	}

	public int getIntEntry(Namespace namespace, String key, int defaultValue) {
		String stringValue = getEntry(namespace, key);
		if (stringValue == null || stringValue.isEmpty()) {
			return defaultValue;
		}
		return Integer.valueOf(stringValue);
	}

	public int getProgramVersion(String key) {
		return getIntEntry(Namespace.Version, key, 1);
	}

	public void updateEntry(Namespace namespace, String key, String value) {
		Connection con = getConnection();
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(SQL_UPDATE_ENTRY);
			st.setString(1, value);
			st.setString(2, namespace.toString());
			st.setString(3, key);
			int rows = st.executeUpdate();
			if (rows != 1) {
				throw new DatabaseException("can not update configuration entry for namespace=" + namespace + ", key="
						+ key + " (no row updated)!");
			}
		} catch (SQLException e) {
			throw new DatabaseException("can not update configuration entry: " + e.getMessage(), e);
		} finally {
			close(st);
			close(con);
		}
	}

	public void updateProgramVersion(String key, int version) {
		if (getEntry(Namespace.Version, key) == null) {
			addEntry(Namespace.Version, key, Integer.toString(version));
		} else {
			updateEntry(Namespace.Version, key, Integer.toString(version));
		}
	}

	@Override
	protected void addAlterTablesStatements(int oldVersion, List<String> alterTableStatements) {
	}

	@Override
	protected String[] getCreateIndexStatements() {
		return CREATE_INDEX_STATEMENTS;
	}

	@Override
	protected String[] getCreateTableStatements() {
		return CREATE_TABLE_STATEMENTS;
	}

	@Override
	protected String[] getTableNames() {
		return new String[] { TABLE_NAME };
	}

}
