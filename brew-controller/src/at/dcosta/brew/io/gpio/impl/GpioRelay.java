package at.dcosta.brew.io.gpio.impl;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import at.dcosta.brew.io.gpio.Relay;

public class GpioRelay implements Relay {

	private final GpioPinDigitalOutput pin;
	private final String id;
	private boolean on;

	public GpioRelay(String name, int pi4jPinNumber, GpioController gpio) {
		pin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(pi4jPinNumber), name, PinState.HIGH);
		pin.setShutdownOptions(true, PinState.HIGH);
		id = "GPIO_" + pi4jPinNumber;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public boolean isOn() {
		return on;
	}

	@Override
	public void off() {
		if (isOn()) {
			pin.high();
			on = false;
		}
	}

	@Override
	public void on() {
		if (isOn()) {
			return;
		}
		pin.low();
		on = true;
	}
}
