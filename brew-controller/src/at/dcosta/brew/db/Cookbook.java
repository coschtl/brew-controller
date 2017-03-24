
package at.dcosta.brew.db;

public class Cookbook extends Database {

	private static final String[] TABLE_NAME = new String[] { "RECIPIES" };
	private static final String[] CREATE_TABLE_STATEMENT = new String[] { "CREATE TABLE " + TABLE_NAME
			+ " RECIPE_NAME varchar(255), RECIPE varchar(65000), ADDED_ON timestamp, BREW_COUNT int, RECIPE_SOURCE varchar(255)" };

	public Cookbook() {
		super();
	}

	@Override
	protected String[] getCreateTableStatements() {
		return CREATE_TABLE_STATEMENT;
	}

	@Override
	protected String[] getTableNames() {
		return TABLE_NAME;
	}

}
