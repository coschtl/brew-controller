package at.dcosta.brew.io.gpio.impl;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import at.dcosta.brew.io.AbstractRelay;

public class GpioRelay extends AbstractRelay {

	private final GpioPinDigitalOutput pin;
	private boolean on;

	public GpioRelay(String name, int pi4jPinNumber, GpioController gpio) {
		super(name, pi4jPinNumber);
		pin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByAddress(pi4jPinNumber), name, PinState.HIGH);
		pin.setShutdownOptions(true, PinState.HIGH);
	}

	@Override
	public boolean isOn() {
		return on;
	}

	@Override
	public void off() {
		if (isOn()) {
			super.off();
			pin.high();
			on = false;
		}
	}

	@Override
	public void on() {
		if (isOn()) {
			return;
		}
		super.on();
		pin.low();
		on = true;
	}
}
