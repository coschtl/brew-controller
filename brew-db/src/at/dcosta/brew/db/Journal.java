
package at.dcosta.brew.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Journal extends Database {

	private static final String TABLE_NAME = "JOURNAL";
	private static final String[] CREATE_TABLE_STATEMENTS = new String[] { "CREATE TABLE " + TABLE_NAME
			+ " (BREW_ID int, STEP varchar(255), JOURNAL_TEXT varchar(65000), JOURNAL_DATE timestamp)" };
	private static final String[] CREATE_INDEX_STATEMENTS = new String[] {
			"CREATE INDEX I_BREWID_STEP ON " + TABLE_NAME + " (BREW_ID ASC, STEP ASC)" };

	private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME
			+ " (BREW_ID, STEP, JOURNAL_TEXT, JOURNAL_DATE) values (?, ?, ?, ?, ?)";
	private static final String SQL_FIND_BY_BREW_ID = "SELECT BREW_ID, STEP, JOURNAL_TEXT, JOURNAL_DATE from "
			+ TABLE_NAME + " where BREW_ID=?";
	private static final String SQL_FIND_BY_BREW_ID_AND_STEP = SQL_FIND_BY_BREW_ID + " and STEP=?";

	public Journal() {
		super();
	}

	public void addEntry(int brewId, String step, String text) {
		Connection con = getConnection();
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(SQL_INSERT);
			st.setInt(1, brewId);
			st.setString(2, step);
			st.setString(3, text);
			st.setTimestamp(4, now());
			int rows = st.executeUpdate();
			if (rows != 1) {
				throw new DatabaseException("can not add journal entry (no row created)!");
			}
		} catch (SQLException e) {
			throw new DatabaseException("can not add journal entry: " + e.getMessage(), e);
		} finally {
			close(st);
			close(con);
		}
	}

	public List<JournalEntry> getEntries(int brewId) {
		return getEntries(brewId, null);
	}

	public List<JournalEntry> getEntries(int brewId, String step) {
		List<JournalEntry> entries = new ArrayList<>();
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(step == null ? SQL_FIND_BY_BREW_ID : SQL_FIND_BY_BREW_ID_AND_STEP);
			st.setInt(1, brewId);
			if (step != null) {
				st.setString(2, step);
			}
			rs = st.executeQuery();
			while (rs.next()) {
				entries.add(createEntry(rs));
			}
		} catch (SQLException e) {
			String message = "can not receive journal entries for brewId=" + brewId;
			if (step != null) {
				message += ", step=" + step;
			}
			throw new DatabaseException(message + ": " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
		return entries;
	}

	private JournalEntry createEntry(ResultSet rs) throws SQLException {
		JournalEntry entry = new JournalEntry();
		entry.setBrewId(rs.getInt(1));
		entry.setStep(rs.getString(2));
		entry.setText(rs.getString(3));
		entry.setTimestamp(rs.getTimestamp(4));
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
