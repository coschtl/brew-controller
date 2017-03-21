package at.dcosta.brew;

import java.util.List;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import at.dcosta.brew.io.TemperatureSensor;
import at.dcosta.brew.io.w1.W1Bus;

public class Main {
	public static void main(String[] args) throws InterruptedException {

		// pins siehe
		// https://entwickler.de/wp-content/uploads/2016/08/mohr_raspberry_2-1.jpg
		System.out.println("hello World");
		// readTemperatures();
		// toggleRelais();

		readHallSensor();

		System.out.println("DONE");
	}

	private static void readHallSensor() throws InterruptedException {
		final GpioController gpio = GpioFactory.getInstance();
		System.out.println("starting");

		// provision gpio pin #01 as an output pin and turn on
		final GpioPinDigitalInput pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, "MyLED", PinPullResistance.OFF);

		pin.addListener(new GpioPinListenerDigital() {

			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				GpioPinDigitalInput pin = (GpioPinDigitalInput) event.getPin();
				System.out.println("Pin changed: " + pin.isHigh() + " - " + pin.isLow());
			}
		});
		for (int i = 0; i < 100; i++) {
			Thread.sleep(100);
		}
		gpio.shutdown();
	}

	private static void toggleRelais() throws InterruptedException {
		final GpioController gpio = GpioFactory.getInstance();
		System.out.println("starting");

		// provision gpio pin #01 as an output pin and turn on
		final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.LOW);

		// set shutdown state for this pin
		pin.setShutdownOptions(true, PinState.HIGH);

		System.out.println("--> GPIO state should be: ON");

		Thread.sleep(5000);

		// turn off gpio pin #01
		pin.high();
		System.out.println("--> GPIO state should be: OFF");

		Thread.sleep(5000);

		// toggle the current state of gpio pin #01 (should turn on)
		pin.toggle();
		System.out.println("--> GPIO state should be: ON");

		Thread.sleep(5000);

		// toggle the current state of gpio pin #01 (should turn off)
		pin.toggle();
		System.out.println("--> GPIO state should be: OFF");

		Thread.sleep(5000);

		gpio.shutdown();
	}

	private static void readTemperatures() throws InterruptedException {
		W1Bus w1Bus = new W1Bus();
		List<TemperatureSensor> tempSensors = w1Bus.getAvailableSensors();
		for (int i = 0; i < 10; i++) {
			int j = 1;
			for (TemperatureSensor ts : tempSensors) {
				System.out.println("Sensor " + j++ + ": " + ts.getID() + ": " + ts.getTemperature());
			}
			Thread.sleep(1000);
		}
	}
}
