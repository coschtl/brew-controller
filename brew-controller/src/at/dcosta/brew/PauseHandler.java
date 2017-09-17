package at.dcosta.brew;

import at.dcosta.brew.db.Brew;
import at.dcosta.brew.db.BrewDB;
import at.dcosta.brew.recipe.Recipe;
import at.dcosta.brew.recipe.RecipeWriter;
import at.dcosta.brew.util.ThreadUtil;

public class PauseHandler {

	private static final long SLEEP_TIME = 100;
	private final int brewId;
	private BrewDB brewDb;
	private boolean paused;

	public PauseHandler(int brewId) {
		this.brewId = brewId;
	}

	public long handlePause() {
		long sleepTime = 0;
		while (paused) {
			ThreadUtil.sleepMillis(SLEEP_TIME);
			sleepTime += SLEEP_TIME;
		}
		return sleepTime;
	}

	public void startPause() {
		paused = true;
	}

	public void stopPause() {
		paused = false;
	}

	public void updateBrewDb(Recipe recipe) {
		if (brewId < 0) {
			return;
		}
		BrewDB db = getBrewDb();
		Brew brew = db.getBrewById(brewId);
		RecipeWriter writer = new RecipeWriter(recipe, false);
		brew.setRecipe(writer.getRecipeAsXmlString());
		db.persist(brew);
	}

	private synchronized BrewDB getBrewDb() {
		if (brewDb == null) {
			brewDb = new BrewDB();
		}
		return brewDb;
	}

}
