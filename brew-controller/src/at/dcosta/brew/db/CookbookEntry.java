package at.dcosta.brew.db;

import java.sql.Timestamp;

import at.dcosta.brew.recipe.Recipe;

public class CookbookEntry {

	private int id;
	private String name;
	private Recipe recipe;
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

	public Recipe getRecipe() {
		return recipe;
	}

	public String getRecipeSource() {
		return recipeSource;
	}

	public void setAddedOn(Timestamp addedOn) {
		this.addedOn = addedOn;
	}

	public void setBrewCount(int brewCount) {
		this.brewCount = brewCount;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}

	public void setRecipeSource(String recipeSource) {
		this.recipeSource = recipeSource;
	}

}
