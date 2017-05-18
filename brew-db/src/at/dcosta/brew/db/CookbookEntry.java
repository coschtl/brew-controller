package at.dcosta.brew.db;

import java.sql.Timestamp;

public class CookbookEntry {

	private int id;
	private String name;
	private String recipe;
	private Timestamp addedOn;
	private int brewCount;
	private String recipeSource;

	public Timestamp getAddedOn() {
		return addedOn;
	}

	public int getBrewCount() {
		return brewCount;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getRecipeSource() {
		return recipeSource;
	}

	public CookbookEntry setAddedOn(Timestamp addedOn) {
		this.addedOn = addedOn;
		return this;
	}

	public CookbookEntry setBrewCount(int brewCount) {
		this.brewCount = brewCount;
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

	public CookbookEntry setRecipeSource(String recipeSource) {
		this.recipeSource = recipeSource;
		return this;
	}

	public String getRecipe() {
		return recipe;
	}

	public void setRecipe(String recipeString) {
		this.recipe = recipeString;
	}
	
	

}
