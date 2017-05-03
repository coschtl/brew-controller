package at.dcosta.brew.io;

import at.dcosta.brew.db.IOData;
import at.dcosta.brew.db.IOLog;
import at.dcosta.brew.util.ManagedThread;
import at.dcosta.brew.util.StoppableRunnable;
import at.dcosta.brew.util.ThreadUtil;

public abstract class AbstractSensor implements Sensor {

	private class SensorDataCollector implements StoppableRunnable {

		private final IOLog ioLog;
		private boolean aborted;

		public SensorDataCollector() {
			ioLog = new IOLog();
		}

		@Override
		public void abort() {
			aborted = true;
		}

		public void logValue() {
			ioLog.addEntry(
					new IOData().setComponentId(getID()).setComponentType(getComponentType()).setValue(getValue()));
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
					logValue();
				}
				ThreadUtil.sleepDefaultMillis();
			}
		}
	}

	private ManagedThread<SensorDataCollector> sensorDataCollectorThread;
	private long switchOffTime;

	public AbstractSensor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getValue() {
		if (!sensorDataCollectorIsRunning()) {
			startCollectingSensorData();
		}
		return doGetValue();
	}

	@Override
	public void logValue() {
		if (sensorDataCollectorThread != null) {
			sensorDataCollectorThread.getRunnable().logValue();
		}
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

	protected void startCollectingSensorData() {
		// do wait at least two intervalls before starting the thread again
		// this prevents the automatic re-creation of the SensorDataCollector
		// immediately after switchOf()
		if (switchOffTime > 0 && switchOffTime + IOLog.LOG_INTERVAL_MILLIS * 2 < System.currentTimeMillis()) {
			return;
		}
		synchronized (this) {
			if (!sensorDataCollectorIsRunning()) {
				sensorDataCollectorThread = new ManagedThread<SensorDataCollector>(new SensorDataCollector(),
						"SensorDataCollector_" + getID());
				sensorDataCollectorThread.start();
			}
		}

	}

}
