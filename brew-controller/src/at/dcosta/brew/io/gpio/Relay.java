package at.dcosta.brew.io.gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import at.dcosta.brew.io.Actor;

public class Relay implements Actor {

	private final GpioPinDigitalOutput pin;
	private String id;
	private boolean on;

	Relay(String name, int pi4jPinNumber, GpioController gpio) {
		pin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(pi4jPinNumber), name, PinState.HIGH);
		pin.setShutdownOptions(true, PinState.HIGH);
	}

	@Override
	public String getID() {
		return id;
	}

	public void on() {
		pin.low();
		on = true;
	}

	public void off() {
		pin.high();
		on = false;
	}

	public boolean isOn() {
		return on;
	}
}
