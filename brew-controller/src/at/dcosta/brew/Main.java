package at.dcosta.brew;

import java.io.File;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.io.gpio.Relay;
import at.dcosta.brew.io.gpio.RpmSensor;
import at.dcosta.brew.io.w1.W1Bus;
import at.dcosta.brew.util.ThreadManager;

public class Main {

	public static void main(String[] args) throws Exception {
		CommandLineParser parser = new DefaultParser();
		Options options = getOptions();
		CommandLine cmdLine = null;
		try {
			cmdLine = parser.parse(options, args);
		} catch (ParseException exp) {
			new HelpFormatter().printHelp("brew-controller", options);
			return;
		}

		if (cmdLine.hasOption("help")) {
			new HelpFormatter().printHelp("brew-controller", options);
			return;
		}
		if (cmdLine.hasOption("scanW1")) {
			new W1Bus().scanW1Bus();
			return;
		}

		File configFile = null;
		if (cmdLine.hasOption("config")) {
			configFile = new File(cmdLine.getOptionValue("config"));
			if (!configFile.exists()) {
				System.err.println("Given configuration file '" + configFile.getAbsolutePath() + "' does not exist!");
				return;
			}
		}
		if (configFile == null) {
			configFile = new File("configuration.properties");
		}
		if (!configFile.exists()) {
			System.err.println("Configuration file missing!");
			return;
		}
		Configuration.initialize(configFile);
		System.out.println(Configuration.getInstance());

		// readTemperatures();
		// toggleRelais();
		// readHallSensor();

		// new
		// MailNotificationService(null).sendNotification("stephan.dcosta@gmail.com",
		// "Gr��e von der Brauerei", "Das Bier ist nun bereit zum Abl�utern");
		ThreadManager.getInstance().waitForAllThreadsToComplete();
		System.out.println("DONE");
	}

	private static Options getOptions() {
		Options options = new Options();
		options.addOption(new Option("help", "print this message"));
		options.addOption(new Option("scanW1", "List all devices connected to the W1 bus"));
		options.addOption(new Option("config", true, "use given config file"));
		return options;
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

	private static void readTemperatures() throws InterruptedException {
		W1Bus w1Bus = new W1Bus();
		Collection<Sensor> tempSensors = w1Bus.getAvailableTemperatureSensors();
		for (int i = 0; i < 10; i++) {
			int j = 1;
			for (Sensor ts : tempSensors) {
				System.out.println("Sensor " + j++ + ": " + ts.getID() + ": " + ts.getValue());
			}
			Thread.sleep(1000);
		}
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
}
