package at.dcosta.brew.recipe;

import java.util.ArrayList;
import java.util.List;

public abstract class Recipe {

	public enum FermentationType {
		TOP, BOTTOM;
	}

	private final String name;
	private final FermentationType fermentationType;
	private final float wort;

	private int mashTemperature;
	private final List<Ingredient> malts;
	private final List<Hop> hops;
	private final List<Hop> coldHops;
	private Ingredient yeast;

	private int boilingTime;
	private int whirlpoolTime;
	private int primaryWater, secondaryWater;
	private int mashingTemperature;
	private int lauteringRest;

	public Recipe(String name, FermentationType fermentationType, float wort) {
		this.name = name;
		this.fermentationType = fermentationType;
		this.wort = wort;
		this.malts = new ArrayList<>();
		this.hops = new ArrayList<>();
		this.coldHops = new ArrayList<>();
	}

	public void addHop(Hop hop) {
		hops.add(hop);
	}

	public void addMalt(Ingredient malt) {
		malts.add(malt);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Recipe other = (Recipe) obj;
		if (boilingTime != other.boilingTime)
			return false;
		if (coldHops == null) {
			if (other.coldHops != null)
				return false;
		} else if (!coldHops.equals(other.coldHops))
			return false;
		if (fermentationType != other.fermentationType)
			return false;
		if (hops == null) {
			if (other.hops != null)
				return false;
		} else if (!hops.equals(other.hops))
			return false;
		if (lauteringRest != other.lauteringRest)
			return false;
		if (malts == null) {
			if (other.malts != null)
				return false;
		} else if (!malts.equals(other.malts))
			return false;
		if (mashTemperature != other.mashTemperature)
			return false;
		if (mashingTemperature != other.mashingTemperature)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (primaryWater != other.primaryWater)
			return false;
		if (secondaryWater != other.secondaryWater)
			return false;
		if (whirlpoolTime != other.whirlpoolTime)
			return false;
		if (Float.floatToIntBits(wort) != Float.floatToIntBits(other.wort))
			return false;
		if (yeast == null) {
			if (other.yeast != null)
				return false;
		} else if (!yeast.equals(other.yeast))
			return false;
		return true;
	}

	public int getBoilingTime() {
		return boilingTime;
	}

	public abstract String getBrewType();

	public List<Hop> getColdHops() {
		return coldHops;
	}

	public FermentationType getFermentationType() {
		return fermentationType;
	}

	public List<Hop> getHops() {
		return hops;
	}

	public int getLauteringRest() {
		return lauteringRest;
	}

	public List<Ingredient> getMalts() {
		return malts;
	}

	public int getMashingTemperature() {
		return mashingTemperature;
	}

	public int getMashTemperature() {
		return mashTemperature;
	}

	public String getName() {
		return name;
	}

	public int getPrimaryWater() {
		return primaryWater;
	}

	public int getSecondaryWater() {
		return secondaryWater;
	}

	public int getWhirlpoolTime() {
		return whirlpoolTime;
	}

	public float getWort() {
		return wort;
	}

	public Ingredient getYeast() {
		return yeast;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + boilingTime;
		result = prime * result + ((coldHops == null) ? 0 : coldHops.hashCode());
		result = prime * result + ((fermentationType == null) ? 0 : fermentationType.hashCode());
		result = prime * result + ((hops == null) ? 0 : hops.hashCode());
		result = prime * result + lauteringRest;
		result = prime * result + ((malts == null) ? 0 : malts.hashCode());
		result = prime * result + mashTemperature;
		result = prime * result + mashingTemperature;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + primaryWater;
		result = prime * result + secondaryWater;
		result = prime * result + whirlpoolTime;
		result = prime * result + Float.floatToIntBits(wort);
		result = prime * result + ((yeast == null) ? 0 : yeast.hashCode());
		return result;
	}

	public void setBoilingTime(int boilingTime) {
		this.boilingTime = boilingTime;
	}

	public void setFermentation(Ingredient yeast, List<Hop> hops) {
		this.yeast = yeast;
		if (hops != null) {
			coldHops.addAll(hops);
		}
	}

	public void setLauteringRest(int lauteringRest) {
		this.lauteringRest = lauteringRest;
	}

	public void setMashingTemperature(int mashingTemperature) {
		this.mashingTemperature = mashingTemperature;
	}

	public void setMashTemperature(int mashTemperature) {
		this.mashTemperature = mashTemperature;
	}

	public void setPrimaryWater(int primaryWater) {
		this.primaryWater = primaryWater;
	}

	public void setSecondaryWater(int secondaryWater) {
		this.secondaryWater = secondaryWater;
	}

	public void setWhirlpoolTime(int whirlpoolTime) {
		this.whirlpoolTime = whirlpoolTime;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(stringUntilMashing());
		b.append(stringFromLautering());
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

	protected String stringUntilMashing() {
		StringBuilder b = new StringBuilder();
		b.append(getName()).append(": ").append(getWort()).append("°P").append("\n");
		b.append("Hauptguss: ").append(getPrimaryWater()).append("l / Nachguss: ").append(getSecondaryWater())
				.append("l\n");
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

}
