package at.dcosta.brew.db;

import java.sql.Timestamp;

public class CookbookEntry {

	private int id;
	private String name;
	private String recipe;
	private Timestamp addedOn;
	private String recipeSource;

	public Timestamp getAddedOn() {
		return addedOn;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getRecipe() {
		return recipe;
	}

	public String getRecipeSource() {
		return recipeSource;
	}

	public CookbookEntry setAddedOn(Timestamp addedOn) {
		this.addedOn = addedOn;
		return this;
	}

	public CookbookEntry setId(int id) {
		this.id = id;
		return this;
	}

	public CookbookEntry setName(String name) {
		this.name = name;
		return this;
	}

	public void setRecipe(String recipeString) {
		this.recipe = recipeString;
	}

	public CookbookEntry setRecipeSource(String recipeSource) {
		this.recipeSource = recipeSource;
		return this;
	}
	
	

}
