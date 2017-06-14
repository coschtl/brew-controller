
package at.dcosta.brew.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import at.dcosta.brew.BrewStatus;
import at.dcosta.brew.db.BrewStep.StepName;

public class BrewDB extends Database {

	private static final String TABLE_BREW = "BREW";
	private static final String TABLE_BREW_STEPS = "BREW_STEPS";
	private static final String[] TABLE_NAMES = new String[] { TABLE_BREW, TABLE_BREW_STEPS };
	private static final String[] CREATE_TABLE_STATEMENTS = new String[] {
			"CREATE TABLE " + TABLE_NAMES[0]
					+ " (COOKBOOK_ENTRY_ID int, BREW_START timestamp, BREW_STATUS varchar(16), BREW_END timestamp)",
			"CREATE TABLE " + TABLE_NAMES[1]
					+ " (BREW_ID int, NAME varchar(255), DESCRIPTION varchar(255), STEP_START timestamp, STEP_END timestamp)" };
	private static final String[] CREATE_INDEX_STATEMENTS = new String[] {};
	private static final String SQL_UPDATE_BREW = "UPDATE " + TABLE_BREW
			+ " SET BREW_STATUS=?, BREW_END=? WHERE ROWID=?";
	private static final String SQL_COMPLETE_STEP = "UPDATE " + TABLE_BREW_STEPS + " SET STEP_END=? WHERE ROWID=?";
	private static final String SQL_INSERT_BREW = "INSERT INTO " + TABLE_BREW
			+ " (COOKBOOK_ENTRY_ID, BREW_START, BREW_STATUS) VALUES (?, ?, ?)";
	private static final String SQL_INSERT_STEP = "INSERT INTO " + TABLE_BREW_STEPS
			+ " (BREW_ID, NAME, DESCRIPTION, STEP_START) VALUES (?,?, ?, ?)";
	private static final String SQL_BREW_BY_ID = "SELECT ROWID, * FROM " + TABLE_BREW + " WHERE ROWID=?";
	private static final String SQL_GET_RUNNING = "SELECT ROWID, * FROM " + TABLE_BREW + " WHERE BREW_END is null";
	private static final String SQL_STEPS_FOR_BREW = "SELECT ROWID, * FROM " + TABLE_BREW_STEPS + " WHERE BREW_ID=?";
	private static final String SQL_BREWS_BY_RECIPE = "SELECT ROWID, * FROM " + TABLE_BREW
			+ " where COOKBOOK_ENTRY_ID=?";

	public BrewDB() {
		super();
	}

	public void abortRunningBrew() {
		Brew runningBrew = getRunningBrew();
		if (runningBrew != null) {
			runningBrew.setBrewStatus(BrewStatus.ABORTED);
			runningBrew.setEndTime(now());
			persist(runningBrew);
		}
	}

	public BrewStep addStep(int brewId, StepName stepName, String description) {
		BrewStep step = new BrewStep();
		step.setBrew(getBrewById(brewId));
		step.setStartTime(now());
		step.setStepName(stepName);
		step.setDescription(description);
		Connection con = getConnection();
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(SQL_INSERT_STEP);
			st.setInt(1, brewId);
			st.setString(2, stepName.toString());
			st.setString(3, description);
			st.setTimestamp(4, step.getStartTime());
			int rows = st.executeUpdate();
			if (rows != 1) {
				throw new DatabaseException("can not add brew step (no row created)!");
			}
			step.setId(getLastInsertedRowId(con));
		} catch (SQLException e) {
			throw new DatabaseException("can not add brew step: " + e.getMessage(), e);
		} finally {
			close(st);
			close(con);
		}
		return step;
	}

	public void complete(BrewStep step) {
		Connection con = getConnection();
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(SQL_COMPLETE_STEP);
			st.setTimestamp(1, now());
			st.setInt(2, step.getId());
			int rows = st.executeUpdate();
			if (rows != 1) {
				throw new DatabaseException("can not update brew step (no row updated)!");
			}
		} catch (SQLException e) {
			throw new DatabaseException("can not update brew step: " + e.getMessage(), e);
		} finally {
			close(st);
			close(con);
		}
	}

	public Brew getBrewById(int brewId) {
		if (brewId < 0) {
			return null;
		}
		Connection con = getConnection();
		PreparedStatement stBrew = null;
		ResultSet rs = null;
		try {
			stBrew = con.prepareStatement(SQL_BREW_BY_ID);
			stBrew.setInt(1, brewId);
			rs = stBrew.executeQuery();
			if (rs.next()) {
				return createDto(con, rs, true);
			}
			return null;
		} catch (SQLException e) {
			throw new DatabaseException("can not load brew with id=" + brewId + ": " + e.getMessage(), e);
		} finally {
			close(rs);
			close(stBrew);
			close(con);
		}
	}

	public List<Brew> getBrewsByRecipe(int cookbookEntryId) {
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		List<Brew> brews = new ArrayList<>();
		try {
			st = con.prepareStatement(SQL_BREWS_BY_RECIPE);
			st.setInt(1, cookbookEntryId);
			rs = st.executeQuery();
			while (rs.next()) {
				brews.add(createDto(con, rs, false));
			}
			return brews;
		} catch (SQLException e) {
			throw new DatabaseException("can not load brews for recipeID=" + cookbookEntryId + ": " + e.getMessage(),
					e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
	}

	public Brew getRunningBrew() {
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(SQL_GET_RUNNING);
			rs = st.executeQuery();
			if (rs.next()) {
				return createDto(con, rs, true);
			}
			return null;
		} catch (SQLException e) {
			throw new DatabaseException("can not load running brew: " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
	}

	public boolean isBrewRunning(int cookbookEntryId) {
		Brew runningBrew = getRunningBrew();
		if (runningBrew == null) {
			return false;
		}
		return runningBrew.getCookbookEntryId() == cookbookEntryId;
	}

	public void persist(Brew brew) {
		Connection con = getConnection();
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(SQL_UPDATE_BREW);
			st.setString(1, brew.getBrewStatus().toString());
			st.setTimestamp(2, brew.getEndTime());
			st.setInt(3, brew.getId());
			int rows = st.executeUpdate();
			if (rows != 1) {
				throw new DatabaseException("can not update brew (no row updated)!");
			}
		} catch (SQLException e) {
			throw new DatabaseException("can not update brew: " + e.getMessage(), e);
		} finally {
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

	private Brew createDto(Connection con, ResultSet rs, boolean includeSteps) throws SQLException {
		Brew brew = new Brew(rs.getInt("COOKBOOK_ENTRY_ID"));
		brew.setId(rs.getInt("ROWID"));
		brew.setBrewStatus(BrewStatus.valueOf(rs.getString("BREW_STATUS")));
		brew.setStartTime(rs.getTimestamp("BREW_START"));
		brew.setEndTime(rs.getTimestamp("BREW_END"));

		if (includeSteps) {
			PreparedStatement stSteps = null;
			ResultSet rsSteps = null;
			try {
				stSteps = con.prepareStatement(SQL_STEPS_FOR_BREW);
				stSteps.setInt(1, brew.getId());
				rsSteps = stSteps.executeQuery();
				while (rsSteps.next()) {
					BrewStep step = new BrewStep();
					step.setId(rsSteps.getInt("ROWID"));
					step.setStepName(new StepName(rsSteps.getString("NAME")));
					step.setDescription(rsSteps.getString("DESCRIPTION"));
					step.setStartTime(rsSteps.getTimestamp("STEP_START"));
					step.setEndTime(rsSteps.getTimestamp("STEP_END"));
					brew.addStep(step);
				}
			} finally {
				close(rsSteps);
				close(stSteps);
			}
		}
		return brew;
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
		return TABLE_NAMES;
	}

}
