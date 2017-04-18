package at.dcosta.brew.io.gpio.impl;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import at.dcosta.brew.io.Sensor;

public class GpioRpmSensor implements Sensor {

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
	public String getID() {
		return id;
	}

	@Override
	public String getScale() {
		return "rpm";
	}

	@Override
	public double getValue() {
		return rpms;
	}

	@Override
	public void start() {
		System.out.println("starting sensor " + getID());
	}

	@Override
	public void stop() {
		System.out.println("stopping sensor " + getID());

	}

	void calculateRpms() {
		long now = System.currentTimeMillis();
		rpms = 1000d / (now - lastEvent) / 60d;
		lastEvent = now;
	}

}
