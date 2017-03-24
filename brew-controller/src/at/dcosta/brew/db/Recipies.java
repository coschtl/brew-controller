
package at.dcosta.brew.db;

public class Recipies extends Database {

	private static final String TABLE_NAME = "RECIPIES";
	private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE " + TABLE_NAME
			+ " RECIPE_ID int, RECIPE_NAME varchar(255), RECIPE varchar(65000), ADDED_ON timestamp, BREW_COUNT int, RECIPE_SOURCE varchar(255)";

	public Recipies() {
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
