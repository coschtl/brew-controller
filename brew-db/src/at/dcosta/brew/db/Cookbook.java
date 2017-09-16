
package at.dcosta.brew.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import at.dcosta.brew.recipe.Recipe;
import at.dcosta.brew.recipe.RecipeWriter;

public class Cookbook extends Database {

	private static final String TABLE_NAME = "RECIPIES";
	private static final String[] CREATE_TABLE_STATEMENTS = new String[] { "CREATE TABLE " + TABLE_NAME
			+ " (RECIPE_NAME varchar(255), RECIPE varchar(65000), ADDED_ON timestamp, RECIPE_SOURCE varchar(255))", };
	private static final String SQL_ADD_ENTRY = "INSERT INTO " + TABLE_NAME
			+ " (RECIPE_NAME, RECIPE, ADDED_ON,  RECIPE_SOURCE) VALUES (?, ?, ?, ?)";
	private static final String SQL_GET_ALL = "SELECT ROWID, * FROM " + TABLE_NAME;
	private static final String SQL_GET_BY_ID = SQL_GET_ALL + " WHERE ROWID=?";

	public Cookbook() {
		super();
	}

	public void addRecipe(Recipe recipe, String recipeSource) {
		addRecipe(recipe.getName(), recipeSource, new RecipeWriter(recipe, false).getRecipeAsXmlString());
	}

	public int addRecipe(String recipeName, String recipeSource, String recipeAsXml) {
		Connection con = getConnection();
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(SQL_ADD_ENTRY);
			st.setString(1, recipeName);
			st.setString(2, recipeAsXml);
			st.setTimestamp(3, now());
			st.setString(4, recipeSource);
			int rows = st.executeUpdate();
			if (rows != 1) {
				throw new DatabaseException("can not add Cookbook (no row created)!");
			}
			return getLastInsertedRowId(con);
		} catch (SQLException e) {
			throw new DatabaseException("can not add Cookbook: " + e.getMessage(), e);
		} finally {
			close(st);
			close(con);
		}
	}

	public CookbookEntry getEntryById(int id) {
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(SQL_GET_BY_ID);
			st.setInt(1, id);
			rs = st.executeQuery();
			if (rs.next()) {
				CookbookEntry entry = new CookbookEntry();
				entry.setAddedOn(rs.getTimestamp("ADDED_ON"));
				entry.setId(rs.getInt("ROWID"));
				entry.setName(rs.getString("RECIPE_NAME"));
				entry.setRecipe(rs.getString("RECIPE"));
				entry.setRecipeSource(rs.getString("RECIPE_SOURCE"));
				return entry;
			}
			return null;
		} catch (SQLException e) {
			throw new DatabaseException("can not read Cookbook entry '" + id + "': " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
	}

	public List<CookbookEntry> listRecipes(FetchType fetchType) {
		Connection con = getConnection();
		PreparedStatement st = null;
		ResultSet rs = null;
		List<CookbookEntry> entries = new ArrayList<>();
		try {
			st = con.prepareStatement(SQL_GET_ALL);
			rs = st.executeQuery();
			while (rs.next()) {
				CookbookEntry entry = new CookbookEntry();
				entry.setAddedOn(rs.getTimestamp("ADDED_ON"));
				entry.setId(rs.getInt("ROWID"));
				entry.setName(rs.getString("RECIPE_NAME"));
				if (fetchType == FetchType.FULL) {
					entry.setRecipe(rs.getString("RECIPE"));
				}
				entry.setRecipeSource(rs.getString("RECIPE_SOURCE"));
				entries.add(entry);
			}
		} catch (SQLException e) {
			throw new DatabaseException("can not read Cookbook: " + e.getMessage(), e);
		} finally {
			close(rs);
			close(st);
			close(con);
		}
		return entries;
	}

	@Override
	protected void addAlterTablesStatements(int oldVersion, List<String> alterTableStatements) {
	}

	@Override
	protected String[] getCreateIndexStatements() {
		return new String[0];
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
