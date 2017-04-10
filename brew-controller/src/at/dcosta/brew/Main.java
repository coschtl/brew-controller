package at.dcosta.brew;

import java.io.File;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import at.dcosta.brew.com.MailNotificationService;
import at.dcosta.brew.com.NotificationType;
import at.dcosta.brew.db.Cookbook;
import at.dcosta.brew.db.CookbookEntry;
import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.io.gpio.Relay;
import at.dcosta.brew.io.w1.W1Bus;
import at.dcosta.brew.recipe.Recipe;
import at.dcosta.brew.recipe.RecipeReader;
import at.dcosta.brew.recipe.RecipeWriter;
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
		if (cmdLine.hasOption("shutDown")) {
			new ProcessBuilder().command("sudo", "halt").start();
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

		if (cmdLine.hasOption("scanW1")) {
			new W1Bus().scanW1Bus();
			return;
		}
		if (cmdLine.hasOption("sendTestMail")) {
			new MailNotificationService().sendNotification(NotificationType.INFO, "Grüße von der Brauerei",
					"Das Mailing scheint zu funktionieren.");
		}

		if (cmdLine.hasOption("importRecipe")) {
			File recipeFile = configFile = new File(cmdLine.getOptionValue("importRecipe"));
			if (!recipeFile.exists()) {
				System.err.println("Given recipe file '" + recipeFile.getAbsolutePath() + "' does not exist!");
				return;
			}
			Recipe recipe = RecipeReader.read(recipeFile);
			String recipeSource = cmdLine.hasOption("recipeSource") ? cmdLine.getOptionValue("recipeSource") : null;
			new Cookbook().addRecipe(recipe, recipeSource);
			System.out.println("Recipe added.");
			return;
		}

		if (cmdLine.hasOption("listRecipes")) {
			System.out.println("ID\tRecipe\t\taddedOn\t\tbrew count\trecipe source");
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY);
			for (CookbookEntry entry : new Cookbook().listRecipes()) {
				System.out.println(entry.getId() + "\t" + entry.getName() + "\t" + df.format(entry.getAddedOn())
						+ "\t\t" + entry.getBrewCount() + "\t" + entry.getRecipeSource());
			}
			return;
		}
		if (cmdLine.hasOption("showRecipe")) {
			int id = Integer.valueOf(cmdLine.getOptionValue("showRecipe"));
			CookbookEntry entry = new Cookbook().getEntryById(id);
			if (entry == null) {
				System.out
						.println("No recipe found for id=" + id + "! Use -listRecipes to list all available recipes.");
				return;
			}
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY);
			System.out.println("Recipe: " + entry.getName());
			System.out.println("addedOn: " + df.format(entry.getAddedOn()));
			System.out.println("brew count: " + entry.getBrewCount());
			System.out.println("recipe source: " + entry.getRecipeSource());
			System.out.println();
			System.out.println(new RecipeWriter(entry.getRecipe(), true).getRecipeAsXmlString());
			return;
		}
		// Recipe recipe = RecipeReader.loadSampleRecipe();
		// System.out.println(new RecipeWriter(recipe,
		// true).getRecipeAsXmlString());

		if (cmdLine.hasOption("testTemperature")) {
			readTemperatures();
		}
		if (cmdLine.hasOption("testRelais")) {
			toggleRelais();
		}
		if (cmdLine.hasOption("testRpm")) {
			readHallSensor();
		}

		ThreadManager.getInstance().waitForAllThreadsToComplete();
		System.out.println("DONE");
	}

	private static Options getOptions() {
		Options options = new Options();
		options.addOption(new Option("help", "print this message"));
		options.addOption(new Option("scanW1", "List all devices connected to the W1 bus"));
		options.addOption(new Option("sendTestMail", "sends a test email"));
		options.addOption(new Option("config", true, "use given config file"));
		options.addOption(new Option("importRecipe", true, "import the given recipe"));
		options.addOption(new Option("recipeSource", true, "the source of the recipe just getting imported"));
		options.addOption(new Option("listRecipes", "list all recipes"));
		options.addOption(new Option("showRecipe", true, "output the xml of the recipe"));
		options.addOption(new Option("testRelais", "testRelais"));
		options.addOption(new Option("shutDown", "shutDown"));
		options.addOption(new Option("testRpm", "testRpm"));
		options.addOption(new Option("testTemperature", "output the xml of the recipe"));
		return options;
	}

	private static void readHallSensor() throws InterruptedException {
		System.out.println("Start");
		try {
			Sensor rpmSensor = GpioSubsystem.getInstance().getRpmSensor("Motor 1", 0);
			for (int i = 0; i < 100; i++) {
				System.out.println(rpmSensor.getValue());

				Thread.sleep(200);
			}
		} finally {
			System.out.println("Shutting down");
			GpioSubsystem.getInstance().shutdown();
		}
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
		Relay relay1 = GpioSubsystem.getInstance().getRelay("Heater 1", 1);
		Relay relay2 = GpioSubsystem.getInstance().getRelay("Heater 2", 4);
		try {
			System.out.println("Relay 1 isOn: " + relay1.isOn());
			Thread.sleep(5000);

			System.out.print("Switching Relay 1 on: ");
			relay1.on();
			System.out.println("Relay 1 isOn: " + relay1.isOn());
			Thread.sleep(5000);

			System.out.print("Switching Relay 1 off: ");
			relay1.off();
			System.out.println("Relay 1 isOn: " + relay1.isOn());
			Thread.sleep(5000);

			System.out.print("Switching Relay 1 on: ");
			relay1.on();
			System.out.println("Relay 1 isOn: " + relay1.isOn());
			Thread.sleep(5000);

			// System.out.print("Switching Relay 1 off: ");
			// relay1.off();
			// System.out.println("Relay 1 isOn: " + relay1.isOn());
			// Thread.sleep(5000);

			System.out.print("Switching Relay 2 on: ");
			relay2.on();
			System.out.println("Relay 2 isOn: " + relay1.isOn());
			Thread.sleep(5000);

			System.out.print("Switching Relay 2 off: ");
			relay2.off();
			System.out.println("Relay 2 isOn: " + relay1.isOn());
			Thread.sleep(5000);

			System.out.print("Switching Relay 2 on: ");
			relay2.on();
			System.out.println("Relay 2 isOn: " + relay1.isOn());
			Thread.sleep(5000);

		} finally {
			System.out.println("Shutting down");
			GpioSubsystem.getInstance().shutdown();
		}
		System.out.println("isOn: " + relay1.isOn());

	}
}
