package at.dcosta.brew.io.gpio;

import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import at.dcosta.brew.io.ComponentType;
import at.dcosta.brew.io.Relay;
import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.impl.GpioRelay;
import at.dcosta.brew.io.gpio.impl.GpioRpmSensor;
import at.dcosta.brew.io.gpio.impl.MockRelay;
import at.dcosta.brew.util.MockUtil;

public class GpioSubsystem {

	private static final GpioSubsystem INSTANCE = new GpioSubsystem();

	public static GpioSubsystem getInstance() {
		return INSTANCE;
	}

	private final GpioController gpio;
	private final Map<Integer, Relay> relaysByPi4JPin;
	private final Map<String, Relay> relaysById;

	private GpioSubsystem() {
		if (MockUtil.instance().isMockPi()) {
			gpio = null;
		} else {
			gpio = GpioFactory.getInstance();
		}
		relaysByPi4JPin = new HashMap<>();
		relaysById = new HashMap<>();
	}

	public Relay getRelay(String name, int pi4jPinNumber) {
		Relay relay = getRelayByPi4JPin(pi4jPinNumber);
		if (relay == null) {
			if (MockUtil.instance().isMockPi()) {
				relay = new MockRelay(name, pi4jPinNumber);
			} else {
				relay = new GpioRelay(name, pi4jPinNumber, gpio);
			}
			relaysByPi4JPin.put(pi4jPinNumber, relay);
			relaysById.put(relay.getID(), relay);
		}
		return relay;
	}

	public Relay getRelayById(String id) {
		return relaysById.get(id);
	}

	public Relay getRelayByPi4JPin(int pi4jPinNumber) {
		return relaysByPi4JPin.get(pi4jPinNumber);
	}

	public Sensor getRpmSensor(String name, int pi4jPinNumber) {
		if (MockUtil.instance().isMockPi()) {
			return new MockSensor(ComponentType.ROTATION_SPEED_SENSOR, "GPIO_" + pi4jPinNumber, "rpm");
		}
		return new GpioRpmSensor(name, pi4jPinNumber, gpio);
	}

	public void shutdown() {
		for (Relay relay : relaysByPi4JPin.values()) {
			relay.off();
		}
		relaysByPi4JPin.clear();
		if (!MockUtil.instance().isMockPi()) {
			gpio.shutdown();
		}
	}

}
