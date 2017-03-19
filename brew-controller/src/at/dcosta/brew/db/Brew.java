
package at.dcosta.brew.db;

import at.dcosta.brew.Configuration;

public class Brew extends Database {

	private static final String TABLE_NAME = "BREW";
	private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE " + TABLE_NAME
			+ " BREW_ID int, RECIPE_ID int, BREW_START timestamp, BREW_STATUS int, STEP_ID int, STEP_START timestamp, STEP_STATUS int";

	protected Brew(Configuration configuration) {
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
