package at.dcosta.brew.recipe;

import java.util.ArrayList;
import java.util.List;

public abstract class Recipe {

	private final String name;
	private final float wort;

	private int mashTemperature;
	private final List<Ingredient> malts;
	private final List<Hop> hops;
	private Ingredient yeast;

	
	private int boilingTime;
	private int whirlpoolTime;
	private int primaryWater, secondaryWater;
	private int mashingTemperature;
	private int lauteringRest;
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(stringUntilMashing());
		b.append(stringFromLautering());
		return b.toString();
	}
	protected String stringUntilMashing() {
		StringBuilder b = new StringBuilder();
		b.append(getName()).append(": ").append(getWort()).append("°P").append("\n");
		b.append("Hauptguss: ").append(getPrimaryWater()).append("l / Nachguss: ").append(getSecondaryWater()).append("l\n");
		b.append("Malze: ");
		boolean first = true;
		for (Ingredient malt : getMalts()) {
			if (first) {
				first = false;
			} else {
				b.append(", ");
			}
			b.append(malt.getAmount()).append("g ").append(malt.getName());
		}
		b.append("\nEinmaischen bei ").append(getMashingTemperature()).append("°C");
		return b.toString();
	}
	protected String stringFromLautering() {
		StringBuilder b = new StringBuilder();
		b.append("\nLäuterrast: ").append(getLauteringRest()).append(" min\n");
		b.append("Kochen: ").append(getBoilingTime()).append(" min\n");
		b.append("Hopfung: ");
		for (Hop hop : getHops()) {
			if (hop.getBoilingTime() == getBoilingTime()) {
				b.append("\n\tVorderwürze: ").append(hop);
			} else if (hop.getBoilingTime() == 0) {
				b.append("\n\tzu Kochende: ").append(hop);
			} else {
				b.append("\n\t").append(hop).append(" Kochzeit: ").append(hop.getBoilingTime()).append(" min");
			}
		}
		if (getWhirlpoolTime() > 0) {
			b.append("\nWhirlpool: ").append(getWhirlpoolTime()).append(" min");
		}
		b.append("\nHefe: ").append(getYeast());
		return b.toString();
	}

	public int getLauteringRest() {
		return lauteringRest;
	}

	public void setLauteringRest(int lauteringRest) {
		this.lauteringRest = lauteringRest;
	}

	public int getMashingTemperature() {
		return mashingTemperature;
	}

	public void setMashingTemperature(int mashingTemperature) {
		this.mashingTemperature = mashingTemperature;
	}

	public int getPrimaryWater() {
		return primaryWater;
	}

	public void setPrimaryWater(int primaryWater) {
		this.primaryWater = primaryWater;
	}

	public int getSecondaryWater() {
		return secondaryWater;
	}

	public void setSecondaryWater(int secondaryWater) {
		this.secondaryWater = secondaryWater;
	}

	public Recipe(String name, float wort) {
		this.name = name;
		this.wort = wort;
		this.malts = new ArrayList<>();
		this.hops = new ArrayList<>();
	}

	public float getWort() {
		return wort;
	}

	public int getMashTemperature() {
		return mashTemperature;
	}

	public String getName() {
		return name;
	}

	public void setMashTemperature(int mashTemperature) {
		this.mashTemperature = mashTemperature;
	}

	public Ingredient getYeast() {
		return yeast;
	}

	public void setYeast(Ingredient yeast) {
		this.yeast = yeast;
	}

	public int getBoilingTime() {
		return boilingTime;
	}

	public void setBoilingTime(int boilingTime) {
		this.boilingTime = boilingTime;
	}

	public int getWhirlpoolTime() {
		return whirlpoolTime;
	}

	public void setWhirlpoolTime(int whirlpoolTime) {
		this.whirlpoolTime = whirlpoolTime;
	}

	public void addMalt(Ingredient malt) {
		malts.add(malt);
	}

	public List<Ingredient> getMalts() {
		return malts;
	}

	public void addHop(Hop hop) {
		hops.add(hop);
	}

	public List<Hop> getHops() {
		return hops;
	}

}
