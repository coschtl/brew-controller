
package at.dcosta.brew.db;

public class Brew extends Database {

	private static final String TABLE_NAME = "BREW";
	private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE " + TABLE_NAME
			+ " BREW_ID int, RECIPE_ID int, BREW_START timestamp, BREW_STATUS int, STEP_ID int, STEP_START timestamp, STEP_STATUS int, BREW_END timestamp";

	public Brew() {
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
