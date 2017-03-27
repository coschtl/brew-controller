package at.dcosta.brew;

import static at.dcosta.brew.Configuration.HEATER_MINIMUM_INCREASE_PER_MINUTE;
import static at.dcosta.brew.Configuration.HEATER_PINS;
import static at.dcosta.brew.Configuration.MALT_STORE_OPENER_PIN;
import static at.dcosta.brew.Configuration.MASHING_THERMOMETER_ADRESSES;
import static at.dcosta.brew.Configuration.MASHING_THERMOMETER_MAXDIFF;
import static at.dcosta.brew.Configuration.STIRRER_MOTOR_PIN;
import static at.dcosta.brew.Configuration.STIRRER_RPM_PIN;

import java.util.ArrayList;
import java.util.List;

import at.dcosta.brew.com.NotificationService;
import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.io.gpio.Relay;
import at.dcosta.brew.io.w1.W1Bus;

public class MashingSystem {

	private static final long ONE_MINUTE = 100l * 60l;

	private final List<Sensor> temperatureSensors;
	private final List<Relay> heaters;
	private final Relay stirrerMotor;
	private final Relay maltStoreOpener;
	private final Sensor rpmSensor;
	private final NotificationService notificationService;
	private final double thermometerMaxDiff;

	public MashingSystem(NotificationService notificationService) {
		this.notificationService = notificationService;
		Configuration config = Configuration.getInstance();
		W1Bus w1Bus = new W1Bus();
		temperatureSensors = new ArrayList<>();
		for (String address : config.getStringArray(MASHING_THERMOMETER_ADRESSES)) {
			Sensor sensor = w1Bus.getTemperatureSensor(address);
			if (sensor == null) {
				throw new IllegalArgumentException(
						"Sensor with address '" + address + "' not found! Check configuration/installation!");
			}
			temperatureSensors.add(sensor);
		}

		int i = 0;
		GpioSubsystem gpioSubsystem = GpioSubsystem.getInstance();
		heaters = new ArrayList<>();
		for (int pi4jPinNumber : config.getIntArray(HEATER_PINS)) {
			heaters.add(gpioSubsystem.getRelay("Heater " + i++, pi4jPinNumber));
		}
		maltStoreOpener = gpioSubsystem.getRelay("Malt Store Opener", config.getInt(MALT_STORE_OPENER_PIN));
		stirrerMotor = gpioSubsystem.getRelay("Stirrer Motor", config.getInt(STIRRER_MOTOR_PIN));
		rpmSensor = gpioSubsystem.getRpmSensor("Stirrer RPM Sensor", config.getInt(STIRRER_RPM_PIN));
		thermometerMaxDiff = config.getInt(MASHING_THERMOMETER_MAXDIFF);
	}

	public void addMalts() {
		// open malt store for an appropriate time
	}

	public void heat(double targetTemperature) throws BrewException {
		stirrerMotor.on();
		try {

			// wait maximum 10s to get the stirrer running
			if (!isStirrerRunning(10)) {
				throw new BrewException("Stirrer does not start!");
			}

			double lastTemperature = getTemperature();
			long lastMeasureTime = System.currentTimeMillis();
			double minIncrease = Configuration.getInstance().getDouble(HEATER_MINIMUM_INCREASE_PER_MINUTE);
			for (Relay heater : heaters) {
				heater.on();
			}
			while (true) {
				if (getTemperature() >= targetTemperature) {
					return;
				}
				sleep(100);
				if (lastMeasureTime + ONE_MINUTE < System.currentTimeMillis()) {
					if (lastTemperature + minIncrease > getTemperature()) {
						throw new BrewException(
								"The temperature did not increase by " + minIncrease + "°C during the last minute!");
					}
				}
			}
		} finally {
			stirrerMotor.off();
			for (Relay heater : heaters) {
				heater.off();
			}
		}

	}

	public boolean isStirrerRunning(int maxWaitSeconds) {
		for (int i = 0; i < 10 * maxWaitSeconds; i++) {
			if (rpmSensor.getValue() > 0) {
				return true;
			}
			sleep(100);
		}
		return false;
	}

	private double getTemperature() {
		double sum = 0;
		double min = 0;
		double max = 0;
		List<String> err = new ArrayList<>();
		for (Sensor tempSensor : temperatureSensors) {
			double sensorTemp = tempSensor.getValue();
			;
			if (sum == 0) {
				sum = sensorTemp;
				min = sum;
				max = sum;
			} else {
				if (min + thermometerMaxDiff < sensorTemp || max - thermometerMaxDiff > sensorTemp) {
					err.add("Temperature sensor " + tempSensor.getID() + " measures + " + sensorTemp
							+ "°C which differs too much from the other sensor(s)!");
				}
				/// xx wie soll ich das berechnen??

				// über abweichung vom mitelwert?
				// was ist, wenn der erste sensor defekt ist?
			}

		}
		return 0;
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
