package at.dcosta.brew;

import static at.dcosta.brew.Configuration.HEATER_PINS;
import static at.dcosta.brew.Configuration.MALT_STORE_OPENER_PIN;
import static at.dcosta.brew.Configuration.STIRRER_MOTOR_PIN;
import static at.dcosta.brew.Configuration.STIRRER_RPM_PIN;
import static at.dcosta.brew.Configuration.THERMOMETER_ADRESSES;

import java.util.ArrayList;
import java.util.List;

import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.io.gpio.Relay;
import at.dcosta.brew.io.gpio.RpmSensor;
import at.dcosta.brew.io.w1.W1Bus;

public class MashingSystem {

	private final List<Sensor> temperatureSensors;
	private final List<Relay> heaters;
	private final Relay stirrerMotor;
	private final Relay maltStoreOpener;
	private final RpmSensor rpmSensor;

	public MashingSystem() {
		Configuration config = Configuration.getInstance();
		W1Bus w1Bus = new W1Bus();
		temperatureSensors = new ArrayList<>();
		for (String address : config.getStringArray(THERMOMETER_ADRESSES)) {
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
	}

	public void addMalts() {
		// open malt store for an appropriate time
	}

	public void heat(double targetTemperature) {
		// start stirrer
		// heat
	}

}
