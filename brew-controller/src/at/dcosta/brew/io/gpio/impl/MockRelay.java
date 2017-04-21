package at.dcosta.brew.io.gpio.impl;

import at.dcosta.brew.io.AbstractRelay;

public class MockRelay extends AbstractRelay {

	private boolean on;

	public MockRelay(String name, int pi4jPinNumber) {
		super(name, pi4jPinNumber);
	}

	@Override
	public boolean isOn() {
		return on;
	}

	@Override
	public void off() {
		if (isOn()) {
			super.off();
			System.out.println("Setting relay " + getID() + " to state OFF");
			on = false;
		}
	}

	@Override
	public void on() {
		if (isOn()) {
			return;
		}
		super.on();
		System.out.println("Setting relay " + getID() + " to state ON");
		on = true;
	}
}
