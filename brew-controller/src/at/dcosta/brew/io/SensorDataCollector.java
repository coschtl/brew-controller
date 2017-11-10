package at.dcosta.brew.io;

import at.dcosta.brew.db.IOData;
import at.dcosta.brew.db.IOLog;
import at.dcosta.brew.util.StoppableRunnable;
import at.dcosta.brew.util.ThreadUtil;

class SensorDataCollector implements StoppableRunnable {

	private final Sensor sensor;
	private final IOLog ioLog;
	private boolean aborted;
	private boolean autoLoggingActive;

	public SensorDataCollector(Sensor sensor) {
		this.sensor = sensor;
		ioLog = new IOLog();
		autoLoggingActive = true;
	}

	@Override
	public void abort() {
		aborted = true;
	}

	public void logValue() {
		ioLog.addEntry(new IOData().setComponentId(sensor.getID()).setComponentType(sensor.getComponentType())
				.setValue(sensor.getValue()));
	}

	@Override
	public boolean mustComplete() {
		return false;
	}

	@Override
	public void run() {
		int count = Integer.MAX_VALUE;
		int maxCount = IOLog.LOG_INTERVAL_MILLIS / ThreadUtil.SLEEP_MILLIS_DEFAULT;
		while (!aborted) {
			if (count++ >= maxCount) {
				count = 0;
				if (autoLoggingActive) {
					logValue();
				}
			}
			ThreadUtil.sleepDefaultMillis();
		}
	}

	public void setAutoLoggingActive(boolean autoLoggingActive) {
		this.autoLoggingActive = autoLoggingActive;
	}
}