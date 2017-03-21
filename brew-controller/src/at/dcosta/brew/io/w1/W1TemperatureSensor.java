package at.dcosta.brew.io.w1;

import com.pi4j.temperature.TemperatureScale;

import at.dcosta.brew.io.TemperatureSensor;

public class W1TemperatureSensor implements TemperatureSensor {

	private final com.pi4j.component.temperature.TemperatureSensor sensor;

	public W1TemperatureSensor(com.pi4j.component.temperature.TemperatureSensor sensor) {
		this.sensor = sensor;
	}

	@Override
	public String getID() {
		return sensor.getName().trim();
	}

	@Override
	public double getTemperature() {
		return sensor.getTemperature(TemperatureScale.CELSIUS);
	}

}
