
package at.dcosta.brew.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Journal extends Database {

	private static final String[] TABLE_NAME = new String[] { "JOURNAL" };
	private static final String[] SQL_CREATE_TABLE = new String[] { "CREATE TABLE " + TABLE_NAME
			+ " RECIPE_ID int, STEP varchar(255), JOURNAL_DATE timestamp, JOURNAL_TEXT varchar(65000)" };

	private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME
			+ " (RECIPE_ID, STEP, JOURNAL_DATE, JOURNAL_TEXT) values (?, ?, ?, ?)";

	public Journal() {
		super();
	}

	public void addEntry(int recipeId, String step, String text) {
		Connection con = getConnection();
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(SQL_INSERT);
			st.setInt(1, recipeId);
			st.setString(2, step);
			st.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			st.setString(4, text);
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

	@Override
	protected String[] getCreateTableStatements() {
		return SQL_CREATE_TABLE;
	}

	@Override
	protected String[] getTableNames() {
		return TABLE_NAME;
	}

}
