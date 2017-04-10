package at.dcosta.brew;

import static at.dcosta.brew.Configuration.MALT_STORE_OPENER_PIN;
import static at.dcosta.brew.Configuration.MALT_STORE_OPENER_TIMEOUT_SECONDS;
import static at.dcosta.brew.Configuration.MASHING_HEATER_MINIMUM_INCREASE_PER_MINUTE;
import static at.dcosta.brew.Configuration.MASHING_HEATER_PINS;
import static at.dcosta.brew.Configuration.MASHING_THERMOMETER_ADRESSES;
import static at.dcosta.brew.Configuration.MASHING_THERMOMETER_MAXDIFF;
import static at.dcosta.brew.Configuration.STIRRER_RPM_PIN;

import java.util.ArrayList;
import java.util.List;

import at.dcosta.brew.com.NotificationService;
import at.dcosta.brew.com.NotificationType;
import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.io.gpio.MockSensor;
import at.dcosta.brew.io.gpio.Relay;
import at.dcosta.brew.io.w1.W1Bus;
import at.dcosta.brew.util.SensorUtil;
import at.dcosta.brew.util.SensorUtil.SensorStatus;
import at.dcosta.brew.util.SensorUtil.Value;

public class MashingSystem {

	private static final long TEST_TEMPERATURE_INTERVAL = 5000l; // 5 seconds
	private static final long ONE_MINUTE = 1000l * 60l; // 1 minute
	private static final long INCREASE_INTERVAL = ONE_MINUTE;
	private static final long SENSOR_STATUS_INTERVAL = 1000l * 60l * 5l; // 5
																			// minutes

	private final List<Sensor> temperatureSensors;
	private final List<Relay> heaters;
	private final Relay maltStoreOpener;
	private final int maltStoreOpenerTimeoutSeconds;
	private final Sensor rpmSensor;
	private final NotificationService notificationService;
	private final double thermometerMaxDiff;
	private long lastSensorNotification;
	private SensorStatus sensorStatus = SensorStatus.OK;
	private final Stirrer stirrer;

	double lastTemperature = 4d;

	public MashingSystem(NotificationService notificationService) {
		this.notificationService = notificationService;
		Configuration config = Configuration.getInstance();
		W1Bus w1Bus = new W1Bus();
		temperatureSensors = new ArrayList<>();
		for (String address : config.getStringArray(MASHING_THERMOMETER_ADRESSES)) {
			Sensor sensor = w1Bus.getTemperatureSensor(address);
			if (sensor == null) {
				throw new ConfigurationException(
						"Sensor with address '" + address + "' not found! Check configuration/installation!");
			}
			temperatureSensors.add(sensor);
		}
		if (temperatureSensors.size() < 1) {
			throw new ConfigurationException("No valid mashing temperatur sensor(s) configured!)");
		}

		int i = 0;
		GpioSubsystem gpioSubsystem = GpioSubsystem.getInstance();
		heaters = new ArrayList<>();
		for (int pi4jPinNumber : config.getIntArray(MASHING_HEATER_PINS)) {
			heaters.add(gpioSubsystem.getRelay("Heater " + i++, pi4jPinNumber));
		}
		maltStoreOpener = gpioSubsystem.getRelay("Malt Store Opener", config.getInt(MALT_STORE_OPENER_PIN));
		maltStoreOpenerTimeoutSeconds = config.getInt(MALT_STORE_OPENER_TIMEOUT_SECONDS);
		int rpmPin;
		try {
			rpmPin = config.getInt(STIRRER_RPM_PIN);
		} catch (ConfigurationException e) {
			notificationService.sendNotification(NotificationType.WARNING, "Possible configuration error",
					"No RPM sensor configured!");
			rpmPin = -1;
		}
		if (rpmPin > 0) {
			rpmSensor = gpioSubsystem.getRpmSensor("Stirrer RPM Sensor", rpmPin);
		} else {
			rpmSensor = new MockSensor("RPM-Sensor", "u/min").setValue(30);
		}
		thermometerMaxDiff = config.getInt(MASHING_THERMOMETER_MAXDIFF);
		stirrer = new Stirrer();
	}

	public void addMalts() {
		// open malt store for an appropriate time
		maltStoreOpener.on();
		try {
			sleep(maltStoreOpenerTimeoutSeconds * 1000l);
		} finally {
			maltStoreOpener.off();
		}
		sleep(ONE_MINUTE);
	}

	public double getTemperature() {
		Value aktTemperature = SensorUtil.getValue(lastTemperature, temperatureSensors, thermometerMaxDiff);
		handleSensorStatus(aktTemperature);
		return aktTemperature.getValue();
	}

	public void heat(double targetTemperature, boolean useStirrer) throws BrewException {
		if (useStirrer) {
			stirrer.start();
		}
		try {

			// wait maximum 10s to get the stirrer running
			if (useStirrer & !isStirrerRunning(10)) {
				throw new BrewException("Stirrer does not start!");
			}

			double minIncrease = Configuration.getInstance().getDouble(MASHING_HEATER_MINIMUM_INCREASE_PER_MINUTE);
			for (Relay heater : heaters) {
				heater.on();
			}

			long lastMeasureTime = System.currentTimeMillis();
			while (true) {
				double aktTemperature = getTemperature();
				if (aktTemperature >= targetTemperature) {
					// target temperature reached
					return;
				}
				if (lastMeasureTime + INCREASE_INTERVAL < System.currentTimeMillis()) {
					if (lastTemperature + minIncrease > aktTemperature) {
						throw new BrewException(
								"The temperature did not increase by " + minIncrease + "°C during the last minute!");
					}
				}
				lastTemperature = aktTemperature;
				lastMeasureTime = System.currentTimeMillis();
				if (useStirrer && !isStirrerRunning(0)) {
					throw new BrewException("Stirrer does run anymore! --> heating stopped at " + aktTemperature
							+ temperatureSensors.get(0).getScale());
				}
				sleep(TEST_TEMPERATURE_INTERVAL);
			}
		} finally {
			if (useStirrer) {
				stirrer.stop();
			}
			for (Relay heater : heaters) {
				heater.off();
			}
		}
	}

	private void handleSensorStatus(Value value) {
		if (lastSensorNotification + SENSOR_STATUS_INTERVAL < System.currentTimeMillis()) {
			if (value.getSensorStatus() == sensorStatus) {
				return;
			}
			notificationService.sendNotification(NotificationType.WARNING, "Sensor status " + value.getSensorStatus(),
					value.getError());
			lastSensorNotification = System.currentTimeMillis();
			sensorStatus = value.getSensorStatus();
		}
	}

	private boolean isStirrerRunning(int maxWaitSeconds) {
		int done = maxWaitSeconds > 0 ? 10 * maxWaitSeconds : 1;
		for (int i = 0; i < done; i++) {
			if (rpmSensor.getValue() > 0) {
				return true;
			}
			sleep(100);
		}
		return false;
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
