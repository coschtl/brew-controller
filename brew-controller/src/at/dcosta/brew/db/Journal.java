
package at.dcosta.brew.db;

public class Journal extends Database {

	private static final String TABLE_NAME = "JOURNAL";
	private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE " + TABLE_NAME
			+ " ID int, RECIPE_ID int, STEP_ID int, JOURNAL_DATE timestamp, JOURNAL_TEXT varchar(65000)";

	public Journal() {
		super();
	}

	@Override
	protected String getCreateTableStatement() {
		return CREATE_TABLE_STATEMENT;
	}

	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

}
