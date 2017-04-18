package at.dcosta.brew.io.w1;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.temperature.TemperatureScale;

import at.dcosta.brew.util.ThreadUtil;

public class W1TemperatureUpdater extends Thread {

	private Value value;
	private final TemperatureSensor sensor;
	boolean active;

	public W1TemperatureUpdater(Value value, TemperatureSensor sensor) {
		this.value = value;
		this.sensor = sensor;
	}

	public void abort() {
		active = false;
	}

	public void readValue() {
		double temperature = sensor.getTemperature(TemperatureScale.CELSIUS);
		value.setValue(temperature);
	}

	@Override
	public void run() {
		active = true;
		while (active) {
			readValue();
			ThreadUtil.sleepSeconds(5);
		}

	}

}
