package at.dcosta.brew.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import at.dcosta.brew.io.ComponentType;

public class IOLog extends Database {

	public static final int LOG_INTERVAL_MILLIS = 5000;

	private static final String TABLE_NAME = "IO_LOG";
	private static final String[] CREATE_TABLE_STATEMENTS = new String[] { "CREATE TABLE " + TABLE_NAME
			+ " (MEASURE_TIME timestamp, COMPONENT_TYPE varchar(32), COMPONENT_ID varchar(255), VALUE real)" };
	private static final String[] CREATE_INDEX_STATEMENTS = new String[] {
			"CREATE INDEX I_COMPONENT_TYPE ON " + TABLE_NAME + " (COMPONENT_TYPE ASC)",
			"CREATE INDEX I_COMPONENT_ID ON " + TABLE_NAME + " (COMPONENT_ID ASC)" };

	private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME
			+ " (MEASURE_TIME, COMPONENT_TYPE, COMPONENT_ID, VALUE) VALUES (?, ?, ?, ?)";
	private static final String SQL_FIND_BY_SENSOR_TYPE = "SELECT MEASURE_TIME, COMPONENT_TYPE, COMPONENT_ID, VALUE FROM "
			+ TABLE_NAME + " WHERE COMPONENT_TYPE=?";
	private static final String SQL_FIND_BY_COMPONENT_ID = "SELECT MEASURE_TIME, COMPONENT_TYPE, COMPONENT_ID, VALUE FROM "
			+ TABLE_NAME + " WHERE COMPONENT_ID=?";
	private static final String SQL_FIND_LATEST_BY_COMPONENT = "SELECT MEASURE_TIME, COMPONENT_TYPE, COMPONENT_ID, VALUE FROM "
			+ TABLE_NAME + " WHERE COMPONENT_ID=? ORDER BY rowid DESC limit 1";
	private static final String SQL_FIND_COMPONENTS = "SELECT DISTINCT COMPONENT_ID FROM " + TABLE_NAME;

	public void addEntry(IOData sensorData) {
		Connection con = getConnection();
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(SQL_INSERT);
			st.setTimestamp(1, sensorData.getMeasureTime() == null ? now() : sensorData.getMeasureTime());
			st.setString(2, sensorData.getComponentType().toString());
			st.setString(3, sensorData.getComponentId());
			st.setDouble(4, sensorData.getValue());
			int rows = st.executeUpdate();
			if (rows != 1) {
				throw new DatabaseException("can not add sensor data entry (no row created)!");
			}
		} catch (SQLException e) {
			throw new DatabaseException("can not add sensor data entry: " + e.getMessage(), e);
		} finally {
			close(st);
			close(con);
		}
	}

	public List<String> getComponents() {
		List<String> components = new ArrayList<String>();
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(SQL_FIND_COMPONENTS);
			rs = st.executeQuery();
			while (rs.next()) {
				components.add(rs.getString(1));
			}
		} catch (SQLException e) {
			throw new DatabaseException("can not receive components: " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
		return components;
	}

	public List<IOData> getEntries(ComponentType componentType) {
		List<IOData> entries = new ArrayList<>();
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(SQL_FIND_BY_SENSOR_TYPE);
			st.setString(1, componentType.toString());
			rs = st.executeQuery();
			while (rs.next()) {
				entries.add(createEntry(rs));
			}
		} catch (SQLException e) {
			throw new DatabaseException(
					"can not receive sensor data entries for componentType=" + componentType + ": " + e.getMessage(),
					e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
		return entries;
	}

	public List<IOData> getEntries(String componentId) {
		List<IOData> entries = new ArrayList<>();
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(SQL_FIND_BY_COMPONENT_ID);
			st.setString(1, componentId);
			rs = st.executeQuery();
			while (rs.next()) {
				entries.add(createEntry(rs));
			}
		} catch (SQLException e) {
			throw new DatabaseException(
					"can not receive sensor data entries for componentId=" + componentId + ": " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
		return entries;
	}

	public List<IOData> getLatestEntries() {
		return getLatestEntries(getComponents());
	}

	public List<IOData> getLatestEntries(List<String> components) {
		List<IOData> entries = new ArrayList<IOData>();
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			for (String component : components) {
				st = con.prepareStatement(SQL_FIND_LATEST_BY_COMPONENT);
				st.setString(1, component);
				rs = st.executeQuery();
				if (rs.next()) {
					entries.add(createEntry(rs));
				}
			}
		} catch (SQLException e) {
			throw new DatabaseException("can not receive latest sensor data entries: " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
		return entries;
	}

	private IOData createEntry(ResultSet rs) throws SQLException {
		IOData entry = new IOData();
		entry.setMeasureTime(rs.getTimestamp(1));
		entry.setComponentType(ComponentType.valueOf(rs.getString(2)));
		entry.setComponentId(rs.getString(3));
		entry.setValue(rs.getDouble(4));
		return entry;
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
