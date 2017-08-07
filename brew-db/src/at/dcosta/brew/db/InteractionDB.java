package at.dcosta.brew.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import at.dcosta.brew.db.ManualAction.Type;

public class InteractionDB extends Database {

	private static final String TABLE_NAME = "INTERACTION";
	private static final String[] CREATE_TABLE_STATEMENTS = new String[] { "CREATE TABLE " + TABLE_NAME
			+ " (TIME timestamp, TYPE varchar(255), TARGET varchar(255), ARGUMENTS varchar(255), EXECUTION_TIME timestamp)" };
	private static final String[] CREATE_INDEX_STATEMENTS = new String[] {
			"CREATE INDEX I_INTERACTION_TIME ON " + TABLE_NAME + " (TIME ASC)",
			"CREATE INDEX I_INTERACTION_TYPE ON " + TABLE_NAME + " (TYPE, TIME ASC)" };

	private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME
			+ " (TIME, TYPE, TARGET, ARGUMENTS) VALUES (?, ?, ?, ?)";

	private static final String SELECT_COLS = "ROWID, TIME, TYPE, TARGET, ARGUMENTS, EXECUTION_TIME";

	private static final String SQL_FIND_UNPROCESSED = "SELECT " + SELECT_COLS + " FROM " + TABLE_NAME
			+ " WHERE EXECUTION_TIME is null";
	private static final String SQL_FIND_SYSTEM_ACTIONS = "SELECT " + SELECT_COLS + " FROM " + TABLE_NAME
			+ " WHERE target='system' and TIME > ? order by TIME ASC";
	private static final String SQL_SET_PROCESSED = "UPDATE " + TABLE_NAME + " SET EXECUTION_TIME=? WHERE ROWID=?";

	public void addEntry(ManualAction manualAction) {
		Connection con = getConnection();
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(SQL_INSERT);
			st.setTimestamp(1, manualAction.getTime() == null ? now() : manualAction.getTime());
			st.setString(2, manualAction.getType().toString());
			st.setString(3, manualAction.getTarget());
			st.setString(4, manualAction.getArguments());
			int rows = st.executeUpdate();
			if (rows != 1) {
				throw new DatabaseException("can not add ManualAction entry (no row created)!");
			}
		} catch (SQLException e) {
			throw new DatabaseException("can not add ManualAction entry: " + e.getMessage(), e);
		} finally {
			close(st);
			close(con);
		}
	}

	public List<ManualAction> getSystemActions(Timestamp brewStart) {
		List<ManualAction> actions = new ArrayList<>();
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(SQL_FIND_SYSTEM_ACTIONS);
			st.setTimestamp(1, brewStart);
			rs = st.executeQuery();
			while (rs.next()) {
				actions.add(create(rs));
			}
		} catch (SQLException e) {
			throw new DatabaseException("can not receive system actions: " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
		return actions;
	}

	public List<ManualAction> getUnprocessedActions() {
		List<ManualAction> actions = new ArrayList<>();
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(SQL_FIND_UNPROCESSED);
			rs = st.executeQuery();
			while (rs.next()) {
				actions.add(create(rs));
			}
		} catch (SQLException e) {
			throw new DatabaseException("can not receive actions: " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
		return actions;
	}

	public void setProcessed(ManualAction manualAction) {
		Connection con = getConnection();
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(SQL_SET_PROCESSED);
			st.setTimestamp(1, now());
			st.setInt(2, manualAction.getId());
			int rows = st.executeUpdate();
			if (rows != 1) {
				throw new DatabaseException("can not update ManualAction entry (no row updated)!");
			}
		} catch (SQLException e) {
			throw new DatabaseException("can not update ManualAction entry: " + e.getMessage(), e);
		} finally {
			close(st);
			close(con);
		}
	}

	private ManualAction create(ResultSet rs) throws SQLException {
		ManualAction manualAction = new ManualAction();
		manualAction.setId(rs.getInt(1));
		manualAction.setTime(rs.getTimestamp(2));
		manualAction.setType(Type.valueOf(rs.getString(3)));
		manualAction.setTarget(rs.getString(4));
		manualAction.setArguments(rs.getString(5));
		manualAction.setExecutionTime(rs.getTimestamp(6));
		return manualAction;
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
