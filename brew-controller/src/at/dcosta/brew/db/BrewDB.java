
package at.dcosta.brew.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import at.dcosta.brew.BrewStatus;

public class BrewDB extends Database {

	private static final String[] TABLE_NAMES = new String[] { "BREW", "BREW_STEPS" };
	private static final String[] CREATE_TABLE_STATEMENTS = new String[] {
			"CREATE TABLE " + TABLE_NAMES[0]
					+ " (COOKBOOK_ENTRY_ID int, BREW_START timestamp, BREW_STATUS varchar(16), BREW_END timestamp)",
			"CREATE TABLE " + TABLE_NAMES[1]
					+ " (BREW_ID int, STEP_NAME varchar(255), STEP_START timestamp, STEP_END timestamp)" };
	private static final String SQL_INSERT_BREW = "INSERT INTO " + TABLE_NAMES[0]
			+ " (COOKBOOK_ENTRY_ID, BREW_START, BREW_STATUS) VALUES (?, ?, ?)";
	private static final String SQL_BREW_BY_ID = "SELECT ROWID, * FROM " + TABLE_NAMES[0] + " WHERE ROWID=?";
	private static final String SQL_STEPS_FOR_BREW = "SELECT ROWID, * FROM " + TABLE_NAMES[1] + " WHERE BREW_ID=?";

	public BrewDB() {
		super();
	}

	public Brew getBrewById(int brewId) {
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(SQL_BREW_BY_ID);
			st.setInt(1, brewId);
			rs = st.executeQuery();
			if (rs.next()) {
				Brew brew = new Brew(rs.getInt("COOKBOOK_ENTRY_ID"));
				brew.setId(brewId);
				brew.setBrewStatus(BrewStatus.valueOf(rs.getString("BREW_STATUS")));
				brew.setStartTime(rs.getTimestamp("BREW_START"));
				brew.setStartTime(rs.getTimestamp("BREW_END"));

				close(rs);
				close(st);
				st = con.prepareStatement(SQL_STEPS_FOR_BREW);
				st.setInt(1, brewId);
				rs = st.executeQuery();
				while (rs.next()) {
					BrewStep step = new BrewStep();
					step.setId(rs.getInt("ROWID"));
					step.setStepName(rs.getString("STEP_NAME"));
					step.setStartTime(rs.getTimestamp("BREW_START"));
					step.setStartTime(rs.getTimestamp("BREW_END"));
					brew.addStep(step);
				}
				return brew;
			}
			return null;
		} catch (SQLException e) {
			throw new DatabaseException("can not load brew with id=" + brewId + ": " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
	}

	public Brew startNewBrew(int cookbookEntryId, Timestamp startTime) {
		Brew brew = new Brew(cookbookEntryId);
		brew.setBrewStatus(BrewStatus.SCHEDULED);
		brew.setStartTime(startTime);
		Connection con = getConnection();
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(SQL_INSERT_BREW);
			st.setInt(1, brew.getCookbookEntryId());
			st.setTimestamp(2, brew.getStartTime());
			st.setString(3, brew.getBrewStatus().toString());
			int rows = st.executeUpdate();
			if (rows != 1) {
				throw new DatabaseException("can not add brew (no row created)!");
			}
			brew.setId(getLastInsertedRowId(con));
		} catch (SQLException e) {
			throw new DatabaseException("can not add brew: " + e.getMessage(), e);
		} finally {
			close(st);
			close(con);
		}
		return brew;
	}

	@Override
	protected String[] getCreateTableStatements() {
		return CREATE_TABLE_STATEMENTS;
	}

	@Override
	protected String[] getTableNames() {
		return TABLE_NAMES;
	}

}
