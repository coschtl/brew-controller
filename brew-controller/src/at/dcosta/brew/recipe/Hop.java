package at.dcosta.brew.recipe;

public class Hop extends Ingredient {

	private final float alpha;
	private final int cookingTime;

	public Hop(String name, int amount, float alpha, int cookingTime) {
		super(name, amount);
		this.alpha = alpha;
		this.cookingTime =cookingTime;
	}

	public float getAlpha() {
		return alpha;
	}
	
	public float getCookingTime() {
		return cookingTime;
	}
	
	@Override
	public String toString() {
		return new StringBuilder(super.toString()).append(" [alpha=").append(alpha).append("%]").toString();
	}
}
