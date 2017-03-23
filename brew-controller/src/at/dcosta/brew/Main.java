package at.dcosta.brew;

import java.util.Collection;

import at.dcosta.brew.com.MailNotificationService;
import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.io.gpio.Relay;
import at.dcosta.brew.io.gpio.RpmSensor;
import at.dcosta.brew.io.w1.W1Bus;
import at.dcosta.brew.util.ThreadManager;

public class Main {
	public static void main(String[] args) throws Exception {
		System.out.println("hello World");
		// readTemperatures();
		// toggleRelais();
		//readHallSensor();
		
		System.out.println("sending mail");
		new MailNotificationService(null).sendNotification("stephan.dcosta@gmail.com", "Grüße von der Brauerei", "Das Bier ist nun bereit zum Abläutern");
		System.out.println("mail sent.");
		System.out.println("Waiting  for all Threads to complete.");
		ThreadManager.getInstance().waitForAllThreadsToComplete();
		System.out.println("DONE");
	}

	private static void readHallSensor() throws InterruptedException {
		System.out.println("Start");
		try {
			RpmSensor rpmSensor = GpioSubsystem.getInstance().getRpmSensor("Motor 1", 0);
			for (int i = 0; i < 100; i++) {
				System.out.println(rpmSensor.getValue());

				Thread.sleep(200);
			}
		} finally {
			System.out.println("Shutting down");
			GpioSubsystem.getInstance().shutdown();
		}
		System.out.println("DONE");
	}

	private static void toggleRelais() throws InterruptedException {
		Relay relay = GpioSubsystem.getInstance().getRelay("Heater 1", 1);
		try {
			System.out.println("isOn: " + relay.isOn());
			Thread.sleep(5000);

			System.out.print("Switching on: ");
			relay.on();
			System.out.println("isOn: " + relay.isOn());
			Thread.sleep(5000);

			System.out.print("Switching off: ");
			relay.off();
			System.out.println("isOn: " + relay.isOn());
			Thread.sleep(5000);

			System.out.print("Switching on: ");
			relay.on();
			System.out.println("isOn: " + relay.isOn());
			Thread.sleep(5000);

		} finally {
			System.out.println("Shutting down");
			GpioSubsystem.getInstance().shutdown();
		}
		System.out.println("isOn: " + relay.isOn());

		System.out.println("DONE");
	}

	private static void readTemperatures() throws InterruptedException {
		W1Bus w1Bus = new W1Bus();
		Collection<Sensor> tempSensors = w1Bus.getAvailableSensors();
		for (int i = 0; i < 10; i++) {
			int j = 1;
			for (Sensor ts : tempSensors) {
				System.out.println("Sensor " + j++ + ": " + ts.getID() + ": " + ts.getValue());
			}
			Thread.sleep(1000);
		}
	}
}
