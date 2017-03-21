
package at.dcosta.brew.db;

import at.dcosta.brew.Configuration;

public class Devices extends Database {
	
	private static final String TABLE_NAME = "DEVICES";
	private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE " + TABLE_NAME
			+ " DEVICE int, RECIPE_NAME varchar(255), RECIPE varchar(65000), ADDED_ON timestamp, BREW_COUNT int, RECIPE_SOURCE varchar(255)";


	protected Devices(Configuration configuration) {
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
