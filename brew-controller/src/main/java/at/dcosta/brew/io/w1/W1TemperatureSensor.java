package at.dcosta.brew.io.w1;

import com.pi4j.component.temperature.TemperatureSensor;

import at.dcosta.brew.io.ComponentType;
import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.util.ManagedThread;

public class W1TemperatureSensor implements Sensor {

	private final TemperatureSensor sensor;
	private ManagedThread<W1TemperatureUpdater> temperatureUpdater;
	private double sensorValue;
	private double minValidValue;
	private double maxValidValue;
	private double correctionValue;

	public W1TemperatureSensor(TemperatureSensor sensor) {
		this.sensor = sensor;
		startTemperatureUpdater();
	}

	@Override
	public ComponentType getComponentType() {
		return ComponentType.TEMPERATURE_SENSOR;
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
		if (!temperatureUpdaterIsRunning()) {
			startTemperatureUpdater();
		}
		return sensorValue + correctionValue;
	}

	@Override
	public void logValue() {
		if (temperatureUpdater != null) {
			temperatureUpdater.getRunnable().logValue();
		}
	}

	@Override
	public void setCorrectionValue(double correctionValue) {
		this.correctionValue = correctionValue;
	}

	@Override
	public void setMinValidValue(double minValidValue) {
		this.minValidValue = minValidValue;
	}

	@Override
	public void setMaxValidValue(double maxValidValue) {
		this.maxValidValue = maxValidValue;
	}

	public double getMinValidValue() {
		return minValidValue;
	}

	public double getMaxValidValue() {
		return maxValidValue;
	}

	@Override
	public void switchOff() {
		temperatureUpdater.abort();
	}

	private void startTemperatureUpdater() {
		synchronized (this) {
			if (!temperatureUpdaterIsRunning()) {
				String sensorName = sensor.getName();
				temperatureUpdater = new ManagedThread<>(new W1TemperatureUpdater(this, sensor), sensorName);
				temperatureUpdater.start();
			}
		}
	}

	private boolean temperatureUpdaterIsRunning() {
		return temperatureUpdater != null && temperatureUpdater.isAlive();
	}

	void setValue(double value) {
		sensorValue = value;
	}

}
