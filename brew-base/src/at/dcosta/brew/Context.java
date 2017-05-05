package at.dcosta.brew;

public class Context {
	
	private static Context INSTANCE = new Context();
	
	private int recipeId = -1;
	private int brewId =-1;
	
	private Context() {
		// use getInstance()!
	}
	
	public static  Context getInstance() {
		return INSTANCE;
	}
	
	public synchronized void initialize(int brewId, int recipeId) {
		this.brewId = brewId;
		this.recipeId = recipeId;
	}
	
	

	public int getRecipeId() {
		return recipeId;
	}

	public int getBrewId() {
		return brewId;
	}
	
	

}
