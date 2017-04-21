package at.dcosta.brew.recipe;

public class Rest {
	private final int temperature;
	private final int minutes;

	public Rest(int temperature, int minutes) {
		this.temperature = temperature;
		this.minutes = minutes;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getTemperature() {
		return temperature;
	}

}
