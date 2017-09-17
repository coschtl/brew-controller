package at.dcosta.brew.recipe;

public class Rest {
	private final int temperature;
	private int minutes;

	public Rest(int temperature, int minutes) {
		this.temperature = temperature;
		this.minutes = minutes;
	}

	public void addMinutes(int minutesToAdd) {
		this.minutes += minutesToAdd;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getTemperature() {
		return temperature;
	}
}
