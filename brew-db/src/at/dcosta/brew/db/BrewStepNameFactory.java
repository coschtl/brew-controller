package at.dcosta.brew.db;

import java.util.HashMap;
import java.util.Map;

import at.dcosta.brew.db.BrewStep.Name;
import at.dcosta.brew.db.BrewStep.StepName;

public class BrewStepNameFactory {

	private final Map<Name, Integer> stepnames;

	public BrewStepNameFactory() {
		stepnames = new HashMap<>();
	}

	public StepName stepname(Name name) {
		Integer count = stepnames.get(name);
		int c;
		if (count == null) {
			c = 0;
		} else {
			c = count.intValue() + 1;
		}
		stepnames.put(name, Integer.valueOf(c));
		return new StepName(name, c);
	}

}
