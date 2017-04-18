package at.dcosta.brew.io.w1;

import at.dcosta.brew.io.Sensor;

public class W1TemperatureSensor implements Sensor {

	private final String name;
	private final W1TemperatureUpdater temperatureUpdater;
	private final Value value;
	private boolean started;

	public W1TemperatureSensor(com.pi4j.component.temperature.TemperatureSensor sensor) {
		this.name = sensor.getName().trim();
		value = new Value();
		this.temperatureUpdater = new W1TemperatureUpdater(value, sensor);
	}

	@Override
	public String getID() {
		return name;
	}

	@Override
	public String getScale() {
		return "°C";
	}

	@Override
	public double getValue() {
		if (!started) {
			temperatureUpdater.readValue();
			start();
		}
		return value.getValue();
	}

	@Override
	public void start() {
		started = true;
		temperatureUpdater.start();
	}

	@Override
	public void stop() {
		temperatureUpdater.abort();
	}

}
