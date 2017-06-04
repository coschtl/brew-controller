package at.dcosta.brew;

import java.lang.Thread.State;

import at.dcosta.brew.com.NotificationType;
import at.dcosta.brew.util.ManagedThread;
import at.dcosta.brew.util.StoppableRunnable;
import at.dcosta.brew.util.ThreadManager;
import at.dcosta.brew.util.ThreadUtil;

public class HeatingMonitor {

	private static class HeatingMonitorMonitor implements StoppableRunnable {

		private final HeatingSystem heatingSystem;
		private boolean active;
		private double lastTemperature;
		private double minIncreasePerMinute;

		public HeatingMonitorMonitor(HeatingSystem heatingSystem) {
			this.heatingSystem = heatingSystem;
			this.minIncreasePerMinute = heatingSystem.getMinTemperatureIncreasePerMinute();
		}

		@Override
		public void abort() {
			active = false;
		}

		@Override
		public boolean mustComplete() {
			return false;
		}

		@Override
		public void run() {
			active = true;
			while (active) {
				if (isHeatingPowerTooLow()) {
					heatingSystem.getNotificationService().sendNotification(NotificationType.WARNING,
							"Heating power too low", "The temperature did not increase by " + minIncreasePerMinute
									+ "°C during the last minute!");
				}
				lastTemperature = heatingSystem.getTemperature();
				for (int i = 0; i < 60; i++) {
					ThreadUtil.sleepSeconds(1);
					if (!active) {
						break;
					}
				}
			}
		}

		private boolean isHeatingPowerTooLow() {
			return lastTemperature + minIncreasePerMinute > heatingSystem.getTemperature();
		}

	}

	private final HeatingSystem heatingSystem;
	private ManagedThread<HeatingMonitorMonitor> monitorThread;

	public HeatingMonitor(HeatingSystem heatingSystem) {
		this.heatingSystem = heatingSystem;
		createMonitorThread();
	}

	public void start() {
		if (monitorThread.isAlive()) {
			return;
		}
		if (monitorThread.getState() == State.TERMINATED) {
			createMonitorThread();
		}
		monitorThread.start();
	}

	public void stop() {
		monitorThread.getRunnable().abort();
	}

	private void createMonitorThread() {
		HeatingMonitorMonitor heatingMonitorMonitor = new HeatingMonitorMonitor(heatingSystem);
		monitorThread = ThreadManager.getInstance().newThread(heatingMonitorMonitor,
				"Heating Monitor");
	}

}
