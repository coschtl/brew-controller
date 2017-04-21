package at.dcosta.brew.io.gpio.impl;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import at.dcosta.brew.io.AbstractSensor;
import at.dcosta.brew.io.ComponentType;

public class GpioRpmSensor extends AbstractSensor {

	private final String id;
	private long lastEvent;
	private double rpms;

	public GpioRpmSensor(String name, int pi4jPinNumber, GpioController gpio) {
		id = "GPIO_" + pi4jPinNumber;
		final GpioPinDigitalInput pin = gpio.provisionDigitalInputPin(RaspiPin.getPinByAddress(pi4jPinNumber), name,
				PinPullResistance.OFF);
		pin.addListener(new GpioPinListenerDigital() {

			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				GpioPinDigitalInput pin = (GpioPinDigitalInput) event.getPin();
				if (pin.isLow()) {
					calculateRpms();
				}
			}
		});
	}

	@Override
	public double doGetValue() {
		return rpms;
	}

	@Override
	public ComponentType getComponentType() {
		return ComponentType.ROTATION_SPEED_SENSOR;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String getScale() {
		return "rpm";
	}

	void calculateRpms() {
		long now = System.currentTimeMillis();
		rpms = 1000d / (now - lastEvent) / 60d;
		lastEvent = now;
	}

}
