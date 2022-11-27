package at.dcosta.brew;

public class Context {
	
	private static Context INSTANCE = new Context();
	
	public static  Context getInstance() {
		return INSTANCE;
	}
	private int recipeId = -1;
	
	private int brewId =-1;
	
	private Context() {
		// use getInstance()!
	}
	
	public int getBrewId() {
		return brewId;
	}
	
	

	public int getRecipeId() {
		return recipeId;
	}

	public synchronized void initialize(int brewId, int recipeId) {
		this.brewId = brewId;
		this.recipeId = recipeId;
	}
	
	

}
