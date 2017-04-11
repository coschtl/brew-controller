package at.dcosta.brew.io.w1;

import com.pi4j.temperature.TemperatureScale;

import at.dcosta.brew.io.Sensor;

public class W1TemperatureSensor implements Sensor {

	private static final long VALUE_READ_AGE = 1000l * 5l;
	private static final long VALUE_MAX_AGE = VALUE_READ_AGE * 2l;

	private final com.pi4j.component.temperature.TemperatureSensor sensor;
	private double temperature;
	private long valueReadTime;

	public W1TemperatureSensor(com.pi4j.component.temperature.TemperatureSensor sensor) {
		this.sensor = sensor;
	}

	@Override
	public String getID() {
		return sensor.getName().trim();
	}

	@Override
	public String getScale() {
		return "°C";
	}

	@Override
	public double getValue() {
		if (valueReadTime + VALUE_READ_AGE < System.currentTimeMillis()) {
			readValue();
		}
		return temperature;
	}

	public void readValue() {
		if (valueReadTime + VALUE_MAX_AGE < System.currentTimeMillis()) {
			// read synchronously
			setAktTemperature(sensor.getTemperature(TemperatureScale.CELSIUS));
			setValueReadTime();
		} else {
			new Thread() {
				@Override
				public void run() {
					setAktTemperature(sensor.getTemperature(TemperatureScale.CELSIUS));
					setValueReadTime();
				}
			}.start();
		}
	}

	void setAktTemperature(double aktTemperature) {
		temperature = aktTemperature;
	}

	void setValueReadTime() {
		valueReadTime = System.currentTimeMillis();
	}

}
