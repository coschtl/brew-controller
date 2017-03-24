package at.dcosta.brew.io.gpio.impl;

import at.dcosta.brew.io.gpio.Relay;

public class MockRelay implements Relay {

	private final String id;
	private boolean on;

	public MockRelay(int pi4jPinNumber) {
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
		System.out.println("Setting relay " + getID() + " to state OFF");
		on = false;
	}

	@Override
	public void on() {
		System.out.println("Setting relay " + getID() + " to state ON");
		on = true;
	}
}
