package at.dcosta.brew.io.gpio;

import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import at.dcosta.brew.Configuration;
import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.impl.GpioRelay;
import at.dcosta.brew.io.gpio.impl.GpioRpmSensor;
import at.dcosta.brew.io.gpio.impl.MockRelay;

public class GpioSubsystem {

	private static final GpioSubsystem INSTANCE = new GpioSubsystem();

	public static GpioSubsystem getInstance() {
		return INSTANCE;
	}

	private final GpioController gpio;
	private final Map<Integer, Relay> relays;
	private final boolean isMockPi;

	private GpioSubsystem() {
		isMockPi = Configuration.getInstance().isMockPi();
		if (isMockPi) {
			gpio = null;
		} else {
			gpio = GpioFactory.getInstance();
		}
		relays = new HashMap<>();
	}

	public Relay getRelay(String name, int pi4jPinNumber) {
		Relay relay = relays.get(pi4jPinNumber);
		if (relay == null) {
			if (isMockPi) {
				relay = new MockRelay(pi4jPinNumber);
			} else {
				relay = new GpioRelay(name, pi4jPinNumber, gpio);
			}
			relays.put(pi4jPinNumber, relay);
		}
		return relay;
	}

	public Sensor getRpmSensor(String name, int pi4jPinNumber) {
		if (isMockPi) {
			return new MockSensor("GPIO_" + pi4jPinNumber, "rpm");
		}
		return new GpioRpmSensor(name, pi4jPinNumber, gpio);
	}

	public void shutdown() {
		for (Relay relay : relays.values()) {
			relay.off();
		}
		relays.clear();
		if (!isMockPi) {
			gpio.shutdown();
		}
	}

}
