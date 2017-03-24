package at.dcosta.brew.io.gpio;

import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

public class GpioSubsystem {

	private static final GpioSubsystem INSTANCE = new GpioSubsystem();

	public static GpioSubsystem getInstance() {
		return INSTANCE;
	}

	private final GpioController gpio;
	private final Map<Integer, Relay> relays;

	private GpioSubsystem() {
		gpio = GpioFactory.getInstance();
		relays = new HashMap<>();
	}

	public Relay getRelay(String name, int pi4jPinNumber) {
		Relay relay = relays.get(pi4jPinNumber);
		if (relay == null) {
			relay = new Relay(name, pi4jPinNumber, gpio);
			relays.put(pi4jPinNumber, relay);
		}
		return relay;
	}
	
	public RpmSensor getRpmSensor(String name, int pi4jPinNumber) {
		return new RpmSensor(name, pi4jPinNumber, gpio);
	}

	public void shutdown() {
		for (Relay relay : relays.values()) {
			relay.off();
		}
		relays.clear();
		gpio.shutdown();
	}

}
