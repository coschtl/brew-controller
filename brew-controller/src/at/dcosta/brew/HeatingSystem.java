package at.dcosta.brew;

import static at.dcosta.brew.Configuration.MULTIPLE_HEATER_TEMPDIFF;
import static at.dcosta.brew.Configuration.THERMOMETER_MAXDIFF;

import java.util.ArrayList;
import java.util.List;

import at.dcosta.brew.com.NotificationService;
import at.dcosta.brew.com.NotificationType;
import at.dcosta.brew.io.Relay;
import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.io.w1.W1Bus;
import at.dcosta.brew.util.SensorUtil;
import at.dcosta.brew.util.SensorUtil.SensorStatus;
import at.dcosta.brew.util.SensorUtil.Value;
import at.dcosta.brew.util.ThreadUtil;

public abstract class HeatingSystem {

	private static final long SWITCHING_INTERVAL = 1000l * 10l;

	private final double thermometerMaxDiff;
	private final double multipleHeaterTempDiff;
	private final NotificationService notificationService;
	private final List<Sensor> temperatureSensors;
	private final List<Relay> heaters;
	protected final HeatingMonitor heatingMonitor;

	private long lastSwitchingTime;
	private SensorStatus sensorStatus = SensorStatus.OK;
	private String errorStatus;

	public HeatingSystem(NotificationService notificationService) {
		this.notificationService = notificationService;
		Configuration config = Configuration.getInstance();
		this.thermometerMaxDiff = Configuration.getInstance().getInt(THERMOMETER_MAXDIFF);
		this.multipleHeaterTempDiff = config.getDouble(MULTIPLE_HEATER_TEMPDIFF);
		this.temperatureSensors = new ArrayList<>();
		this.heaters = new ArrayList<>();
		this.heatingMonitor = new HeatingMonitor(this);
		W1Bus w1Bus = new W1Bus();
		for (String address : getTemperatureSensorAddresses()) {
			Sensor sensor = w1Bus.getTemperatureSensor(address);
			if (sensor == null) {
				throw new ConfigurationException(
						"Sensor with address '" + address + "' not found! Check configuration/installation!");
			}
			temperatureSensors.add(sensor);
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
	}

	public NotificationService getNotificationService() {
		return notificationService;
	}

	public double getTemperature() {
		Value aktTemperature = SensorUtil.getValue(getLastTemperature(), temperatureSensors, thermometerMaxDiff);
		handleSensorStatus(aktTemperature);
		return aktTemperature.getValue();
	}

	public boolean isInErrorStatus() {
		return errorStatus != null;
	}

	public void setErrorStatus(String errorStatus) {
		this.errorStatus = errorStatus;
	}

	public void switchOff() {
		switchOffTemperatureSensors();
	}

	private void handleSensorStatus(Value value) {
		if (value.getSensorStatus() == sensorStatus) {
			return;
		}
		notificationService.sendNotification(NotificationType.WARNING, "Sensor status " + value.getSensorStatus(),
				value.getError());
		sensorStatus = value.getSensorStatus();
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
			if (aktTemperature + multipleHeaterTempDiff * i < targetTemperature) {
				heater.on();
			} else {
				heater.off();
			}
		}
	}

	protected abstract int[] getHeaterPins();

	protected abstract double getLastTemperature();

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
			ThreadUtil.sleepSeconds(10);
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