package at.dcosta.brew.db;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import at.dcosta.brew.BrewStatus;
import at.dcosta.brew.recipe.Recipe;
import at.dcosta.brew.recipe.RecipeReader;
import at.dcosta.brew.recipe.RecipeWriter;

public class Brew {

	private int id;
	private final int cookbookEntryId;
	private Timestamp startTime, endTime;
	private BrewStatus brewStatus;
	private Set<BrewStep> steps;
	private BrewStep currentStep;
	private Recipe recipe;

	public Brew(int cookbookEntryId) {
		this.cookbookEntryId = cookbookEntryId;
		steps = new LinkedHashSet<>();
	}

	public void addStep(BrewStep step) {
		step.setBrew(this);
		steps.add(step);
		currentStep = step;
	}

	public BrewStatus getBrewStatus() {
		return brewStatus;
	}

	public int getCookbookEntryId() {
		return cookbookEntryId;
	}

	public BrewStep getCurrentStep() {
		return currentStep;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public int getId() {
		return id;
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public String getRecipeAsXml() {
		if (recipe == null) {
			return null;
		}
		return new RecipeWriter(recipe, false).getRecipeAsXmlString();
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public Set<BrewStep> getSteps() {
		return steps;
	}

	public Brew setBrewStatus(BrewStatus brewStatus) {
		this.brewStatus = brewStatus;
		return this;
	}

	public Brew setEndTime(Timestamp endTime) {
		this.endTime = endTime;
		return this;
	}

	public Brew setId(int id) {
		this.id = id;
		return this;
	}

	public Brew setRecipe(String recipeAsXml) {
		if (recipeAsXml == null) {
			recipe = null;
		} else {
			recipe = RecipeReader.read(recipeAsXml);
		}
		return this;
	}

	public Brew setStartTime(Timestamp startTime) {
		this.startTime = startTime;
		return this;
	}

	@Override
	public String toString() {
		return String.valueOf(id);

	}

}
