package at.dcosta.brew.recipe;

public class Hop extends Ingredient {

	private final float alpha;
	private final int boilingTime;

	public Hop(String name, int amount, float alpha, int boilingTime) {
		super(name, amount);
		this.alpha = alpha;
		this.boilingTime = boilingTime;
	}

	public float getAlpha() {
		return alpha;
	}

	public float getBoilingTime() {
		return boilingTime;
	}

	@Override
	public String toString() {
		return new StringBuilder(super.toString()).append(" [alpha=").append(alpha).append("%]").toString();
	}
}
