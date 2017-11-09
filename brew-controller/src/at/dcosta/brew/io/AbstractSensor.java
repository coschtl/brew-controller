package at.dcosta.brew.io;

import at.dcosta.brew.db.IOLog;
import at.dcosta.brew.util.ManagedThread;

public abstract class AbstractSensor implements Sensor {

	private ManagedThread<SensorDataCollector> sensorDataCollectorThread;
	private long switchOffTime;
	private double correctionValue;

	@Override
	public final double getValue() {
		if (!sensorDataCollectorIsRunning()) {
			startCollectingSensorData();
		}
		return doGetValue() + correctionValue;
	}

	@Override
	public void logValue() {
		if (sensorDataCollectorThread != null) {
			sensorDataCollectorThread.getRunnable().logValue();
		}
	}

	@Override
	public void setCorrectionValue(double correctionValue) {
		this.correctionValue = correctionValue;
	}

	@Override
	public void switchOff() {
		switchOffTime = System.currentTimeMillis();
		System.out.println("switching off sensor " + getID());
		if (sensorDataCollectorIsRunning()) {
			sensorDataCollectorThread.abort();
		}
	}

	private boolean sensorDataCollectorIsRunning() {
		return sensorDataCollectorThread != null && sensorDataCollectorThread.isAlive();
	}

	protected abstract double doGetValue();

	protected boolean mayStartTemperatureCollection() {
		return true;
	}

	protected void startCollectingSensorData() {
		// do wait at least two intervalls before starting the thread again
		// this prevents the automatic re-creation of the SensorDataCollector
		// immediately after switchOf()
		if (switchOffTime > 0 && switchOffTime + IOLog.LOG_INTERVAL_MILLIS * 2 < System.currentTimeMillis()) {
			return;
		}
		if (!mayStartTemperatureCollection()) {
			return;
		}
		synchronized (this) {
			if (!sensorDataCollectorIsRunning()) {
				sensorDataCollectorThread = new ManagedThread<>(new SensorDataCollector(this),
						"SensorDataCollector_" + getID());
				sensorDataCollectorThread.start();
			}
		}

	}

}
