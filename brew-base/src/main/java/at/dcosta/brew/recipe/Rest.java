package at.dcosta.brew.recipe;

public class Rest {
	private final int temperature, restNumber;
	private int minutes;
	private boolean keepTemperature;

	public Rest(int restNumber, int temperature, int minutes) {
		this.restNumber = restNumber;
		this.temperature = temperature;
		this.minutes = minutes;
		this.keepTemperature = true;
	}

	public void addMinutes(int minutesToAdd) {
		this.minutes += minutesToAdd;
	}
	
	public void setKeepTemperature(boolean keepTemperature) {
		this.keepTemperature = keepTemperature;
	}
	
	public boolean isKeepTemperature() {
		return keepTemperature;
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
