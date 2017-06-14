package at.dcosta.brew.io.w1;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.temperature.TemperatureScale;

import at.dcosta.brew.db.IOData;
import at.dcosta.brew.db.IOLog;
import at.dcosta.brew.util.StoppableRunnable;
import at.dcosta.brew.util.ThreadUtil;

public class W1TemperatureUpdater implements StoppableRunnable {

	private W1TemperatureSensor w1Sensor;
	private final TemperatureSensor pi4jSensor;
	private final IOLog ioLog;
	boolean active;

	public W1TemperatureUpdater(W1TemperatureSensor w1Sensor, TemperatureSensor pi4jSensor) {
		this.w1Sensor = w1Sensor;
		this.pi4jSensor = pi4jSensor;
		this.ioLog = new IOLog();
		readAndStroreValue();
	}

	@Override
	public void abort() {
		active = false;
	}

	public void logValue() {
		ioLog.addEntry(new IOData().setComponentId(w1Sensor.getID()).setComponentType(w1Sensor.getComponentType())
				.setValue(w1Sensor.getValue()));
	}

	@Override
	public boolean mustComplete() {
		return false;
	}

	public void readAndStroreValue() {
		double temperature = pi4jSensor.getTemperature(TemperatureScale.CELSIUS);
		w1Sensor.setValue(temperature);
	}

	@Override
	public void run() {
		active = true;
		int count = Integer.MAX_VALUE;
		int maxCount = IOLog.LOG_INTERVAL_MILLIS / ThreadUtil.SLEEP_MILLIS_DEFAULT;
		while (active) {
			if (count++ >= maxCount) {
				count = 0;
				readStoreAndLogValue();
			}
			ThreadUtil.sleepDefaultMillis();
		}
	}

	private void readStoreAndLogValue() {
		readAndStroreValue();
		logValue();
	}
}
