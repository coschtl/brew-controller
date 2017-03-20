package at.dcosta.brew.recipe;

public class Ingredient {

	private final String name;
	private final int amount;

	public Ingredient(String name, int amount) {
		this.name = name;
		this.amount = amount;
	}

	public String getName() {
		return name;
	}

	public int getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(getAmount()).append("g ").append(getName()).toString();
	}

}
