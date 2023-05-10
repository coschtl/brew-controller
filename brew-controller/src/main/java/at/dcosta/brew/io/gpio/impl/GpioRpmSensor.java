package at.dcosta.brew.io.gpio.impl;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import at.dcosta.brew.io.AbstractSensor;
import at.dcosta.brew.io.ComponentType;
import jdk.nashorn.internal.runtime.ErrorManager;

import java.util.logging.Logger;

public class GpioRpmSensor extends AbstractSensor {

	private static final Logger LOGGER = Logger.getLogger(GpioRpmSensor.class.getName());

	private final String id;
	private long lastEvent;
	private double rpms;

	private double minValidValue;
	private double maxValidValue;
	private double lastGoodValue;

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

	@Override
	public void setMinValidValue(double minValidValue) {
		this.minValidValue = minValidValue;
	}

	@Override
	public void setMaxValidValue(double maxValidValue) {
		this.maxValidValue = maxValidValue;
	}

	void calculateRpms() {
		long now = System.currentTimeMillis();
		rpms = 1000d / (now - lastEvent) / 60d;
		if ((rpms < minValidValue || rpms > maxValidValue) && lastGoodValue > 0) {
			LOGGER.warning("actual rpms=" + rpms + " which is outside of range (" + minValidValue + ", " + maxValidValue + ") -> returning lastGoodValue=" + lastGoodValue);
			rpms = lastGoodValue;
		}
		lastGoodValue = rpms;
		lastEvent = now;
	}

	private boolean valueRangeActive() {
		return maxValidValue > minValidValue;
	}

}
