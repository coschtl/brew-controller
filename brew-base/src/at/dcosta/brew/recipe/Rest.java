package at.dcosta.brew.recipe;

public class Rest {
	private final int temperature, restNumber;
	private int minutes;

	public Rest(int restNumber, int temperature, int minutes) {
		this.restNumber = restNumber;
		this.temperature = temperature;
		this.minutes = minutes;
	}

	public void addMinutes(int minutesToAdd) {
		this.minutes += minutesToAdd;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getRestNumber() {
		return restNumber;
	}

	public int getTemperature() {
		return temperature;
	}
}
