
package at.dcosta.brew.db;

import at.dcosta.brew.Configuration;

public class Journal extends Database {
	
	private static final String TABLE_NAME = "JOURNAL";
	private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE " + TABLE_NAME
			+ " ID int, RECIPE_ID int, STEP_ID int, JOURNAL_DATE timestamp, JOURNAL_TEXT varchar(65000)";

	protected Journal(Configuration configuration) {
		super(configuration);
	}
	
	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	@Override
	protected String getCreateTableStatement() {
		return CREATE_TABLE_STATEMENT;
	}

}
