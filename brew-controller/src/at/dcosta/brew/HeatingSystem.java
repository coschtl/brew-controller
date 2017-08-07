package at.dcosta.brew;

import static at.dcosta.brew.Configuration.MULTIPLE_HEATER_TEMPDIFF;
import static at.dcosta.brew.Configuration.THERMOMETER_CORRECTION_VALUE;
import static at.dcosta.brew.Configuration.THERMOMETER_MAXDIFF;

import java.util.ArrayList;
import java.util.List;

import at.dcosta.brew.com.NotificationService;
import at.dcosta.brew.com.NotificationType;
import at.dcosta.brew.io.AvgCalculatingSensor;
import at.dcosta.brew.io.AvgCalculatingSensor.SensorStatus;
import at.dcosta.brew.io.Relay;
import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.io.w1.W1Bus;
import at.dcosta.brew.util.ThreadManager;
import at.dcosta.brew.util.ThreadUtil;

public abstract class HeatingSystem {

	private static final long SWITCHING_INTERVAL = 1000l * 10l;

	private final double multipleHeaterTempDiff;
	private final NotificationService notificationService;
	private final List<Sensor> temperatureSensors;
	private final AvgCalculatingSensor averageTemperature;
	private final List<Relay> heaters;
	protected final HeatingMonitor heatingMonitor;
	protected final PauseHandler pauseHandler;

	private long lastSwitchingTime;
	private SensorStatus sensorStatus = SensorStatus.OK;
	private String errorStatus;

	public HeatingSystem(NotificationService notificationService) {
		this.notificationService = notificationService;
		Configuration config = Configuration.getInstance();
		this.multipleHeaterTempDiff = config.getDouble(MULTIPLE_HEATER_TEMPDIFF);
		this.temperatureSensors = new ArrayList<>();
		this.heaters = new ArrayList<>();
		this.heatingMonitor = new HeatingMonitor(this);
		this.pauseHandler = new PauseHandler();
		averageTemperature = new AvgCalculatingSensor(config.getDouble(THERMOMETER_MAXDIFF),
				config.getDouble(THERMOMETER_CORRECTION_VALUE));
		W1Bus w1Bus = new W1Bus();
		for (String address : getTemperatureSensorAddresses()) {
			Sensor sensor = w1Bus.getTemperatureSensor(address);
			if (sensor == null) {
				throw new ConfigurationException(
						"Sensor with address '" + address + "' not found! Check configuration/installation!");
			}
			temperatureSensors.add(sensor);
			averageTemperature.addSensor(sensor);
		}
		if (temperatureSensors.size() < 1) {
			throw new ConfigurationException(
					"No valid " + getClass().getSimpleName() + " temperature sensor(s) configured!)");
		}

		GpioSubsystem gpioSubsystem = GpioSubsystem.getInstance();
		int i = 0;
		for (int pi4jPinNumber : getHeaterPins()) {
			heaters.add(gpioSubsystem.getRelay("Heater " + i++, pi4jPinNumber));
		}
		if (heaters.size() < 1) {
			throw new ConfigurationException("No valid " + getClass().getSimpleName() + " heater(s) configured!)");
		}

		ThreadManager.getInstance().newThread(new UserInteractionExecuter(pauseHandler), "UserInteractionExecuter")
				.start();
	}

	public NotificationService getNotificationService() {
		return notificationService;
	}

	public double getTemperature() {
		double temp = averageTemperature.getValue();
		handleSensorStatus();
		return temp;
	}

	public boolean isInErrorStatus() {
		return errorStatus != null;
	}

	public void setErrorStatus(String errorStatus) {
		this.errorStatus = errorStatus;
	}

	public void switchOff() {
		switchOffTemperatureSensors();
		pauseHandler.stopPause();
	}

	private void handleSensorStatus() {
		if (averageTemperature.getSensorStatus() == sensorStatus) {
			return;
		}
		notificationService.sendNotification(NotificationType.WARNING,
				"Sensor status " + averageTemperature.getSensorStatus(), averageTemperature.getError());
		sensorStatus = averageTemperature.getSensorStatus();
	}

	private void switchOffTemperatureSensors() {
		for (Sensor temperatureSensor : temperatureSensors) {
			temperatureSensor.switchOff();
		}
	}

	protected void adjustHeaters(double targetTemperature) {
		if (lastSwitchingTime + SWITCHING_INTERVAL > System.currentTimeMillis()) {
			return;
		}
		double aktTemperature = getTemperature();
		for (int i = 0; i < heaters.size(); i++) {
			Relay heater = heaters.get(i);
			if (heater.isControlledAutomatically()) {
				if (aktTemperature + multipleHeaterTempDiff * i < targetTemperature) {
					heater.on();
				} else {
					heater.off();
				}
			}
		}
	}

	protected abstract int[] getHeaterPins();

	protected abstract double getHeatingMonitorStartupDelayMinutes();

	protected abstract double getMinTemperatureIncreasePerMinute();

	protected abstract String[] getTemperatureSensorAddresses();

	protected List<Sensor> getTemperatureSensors() {
		return temperatureSensors;
	}

	protected void heatToTemperature(double targetTemperature) throws BrewException {
		heatToTemperature(targetTemperature, ThreadUtil.TEN_HOURS);
	}

	protected void heatToTemperature(double targetTemperature, long maxHeatingTimeMinutes) throws BrewException {
		System.out.println("heatToTemperature");
		long heatingEnd = System.currentTimeMillis() + maxHeatingTimeMinutes * ThreadUtil.ONE_MINUTE;
		while (true) {
			if (System.currentTimeMillis() > heatingEnd) {
				break;
			}
			double aktTemperature = getTemperature();
			if (aktTemperature >= targetTemperature) {
				logTemperature();
				return;
			}
			adjustHeaters(targetTemperature);
			heatToTemperatureWaiting();
			for (int i = 0; i < 10; i++) {
				heatingEnd += pauseHandler.handlePause();
				ThreadUtil.sleepSeconds(1);
			}
		}
	}

	protected void heatToTemperatureWaiting() {

	}

	protected void logTemperature() {
		for (Sensor temperatureSensor : temperatureSensors) {
			temperatureSensor.logValue();
		}
	}

	protected void switchHeatersOff() {
		for (Relay heater : heaters) {
			heater.off();
		}
	}

}
