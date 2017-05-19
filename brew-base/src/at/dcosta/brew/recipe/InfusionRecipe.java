package at.dcosta.brew.recipe;

import java.util.ArrayList;
import java.util.List;

public class InfusionRecipe extends Recipe {

	private final List<Rest> rests;

	public InfusionRecipe(String name, FermentationType fermentationType, float wort) {
		super(name, fermentationType, wort);
		this.rests = new ArrayList<>();
	}

	public void addRest(Rest rest) {
		rests.add(rest);
	}

	@Override
	public String getBrewType() {
		return "infusion";
	}

	public List<Rest> getRests() {
		return rests;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(stringUntilMashing());
		b.append("\nRasten: ");
		for (Rest rest : getRests()) {
			if (rest.getMinutes() > 0) {
				b.append(rest.getTemperature()).append("°C/").append(rest.getMinutes()).append(" min ");
			} else {
				b.append("Abmaischen bei ").append(rest.getTemperature()).append("°C");
			}
		}
		b.append(stringFromLautering());
		return b.toString();
	}
}
