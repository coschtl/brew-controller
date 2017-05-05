package at.dcosta.brew;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import at.dcosta.brew.com.MailNotificationService;
import at.dcosta.brew.com.Notification;
import at.dcosta.brew.com.NotificationService;
import at.dcosta.brew.com.NotificationType;
import at.dcosta.brew.db.BrewDB;
import at.dcosta.brew.db.Cookbook;
import at.dcosta.brew.db.CookbookEntry;
import at.dcosta.brew.db.Database;
import at.dcosta.brew.db.IOData;
import at.dcosta.brew.db.IOLog;
import at.dcosta.brew.db.Journal;
import at.dcosta.brew.io.Relay;
import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.io.w1.W1Bus;
import at.dcosta.brew.recipe.Recipe;
import at.dcosta.brew.recipe.RecipeReader;
import at.dcosta.brew.recipe.RecipeWriter;
import at.dcosta.brew.recipe.Rest;
import at.dcosta.brew.util.FileUtil;
import at.dcosta.brew.util.ThreadManager;
import at.dcosta.brew.util.ThreadUtil;
import at.dcosta.brew.xml.dom.Document;
import at.dcosta.brew.xml.dom.DomReader;
import at.dcosta.brew.xml.dom.DomWriter;

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

		execute(cmdLine, options);
		ThreadManager.getInstance().waitForAllThreadsToComplete();
		System.out.println("DONE");
	}

	private static void execute(CommandLine cmdLine, Options options) throws Exception {
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
			new MailNotificationService().sendNotification(new Notification(NotificationType.INFO,
					"Grüße von der Brauerei", "Das Mailing scheint zu funktionieren."));
			return;
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
			return;
		}
		if (cmdLine.hasOption("testRelais")) {
			toggleRelais();
			return;
		}
		if (cmdLine.hasOption("testRpm")) {
			readHallSensor();
			return;
		}
		NotificationService notificationService = new NotificationService(0);
		if (cmdLine.hasOption("boil")) {
			int boilingTime = Integer.valueOf(cmdLine.getOptionValue("boil"));
			BoilingSystem boilingSystem = new BoilingSystem(notificationService);
			boilingSystem.cook(boilingTime);
			return;
		}
		if (cmdLine.hasOption("getData")) {
			System.out.println();
			DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
			IOLog ioLog = new IOLog();
			List<String> components = ioLog.getComponents();
			Comparator<IOData> comparator = new Comparator<IOData>() {

				@Override
				public int compare(IOData o1, IOData o2) {
					int cmp = o1.getComponentType().toString().compareTo(o2.getComponentType().toString());
					if (cmp == 0) {
						return o1.getComponentId().compareTo(o2.getComponentId());
					}
					return cmp;
				}
			};
			while (true) {
				List<IOData> entries = ioLog.getLatestEntries(components);
				if (!entries.isEmpty()) {
					entries.sort(comparator);
					StringBuilder b = new StringBuilder(df.format(new Date())).append(":");
					int tempSensorCount = 0;
					for (IOData entry : entries) {
						b.append('\t');
						switch (entry.getComponentType()) {
						case RELAY: {
							if (entry.getComponentId().startsWith("Heater")) {
								b.append("Heater: ").append(entry.getValue() > 0 ? "ON" : "OFF");
							}
							if (entry.getComponentId().startsWith("Stirrer")) {
								b.append("Stirrer: ").append(entry.getValue() > 0 ? "ON" : "OFF");
							}
						}
							break;
						case ROTATION_SPEED_SENSOR:
							b.append(entry.getValue()).append(" rpm");
							break;
						case TEMPERATURE_SENSOR: {
							if (entry.getComponentId().startsWith("Average")) {
								b.append("avg: ");
							} else {
								b.append("T").append(tempSensorCount++).append(": ");
							}
							b.append(entry.getValue()).append("°C");
						}
							break;
						}
					}
					b.append('\r');
					System.out.print(b.toString());
				}
				ThreadUtil.sleepSeconds(3);
			}
		}
		if (cmdLine.hasOption("dump")) {
			String db = cmdLine.getOptionValue("dump");
			Database database = null;
			if ("BrewDB".equals(db)) {
				database = new BrewDB();
			} else if ("Cookbook".equals(db)) {
				database = new Cookbook();
			} else if ("IOLog".equals(db)) {
				database = new IOLog();
			} else if ("Journal".equals(db)) {
				database = new Journal();
			}
			if (database == null) {
				System.out.println("Can not dump: unknown argument: " + db);
			}
			File out = new File(new File("."), FileUtil.getFilename(db + "_", ".xml", false));
			System.out.println("dumping " + db + " to " + out.getAbsolutePath());
			database.dumpToXml(out);
		}
		if (cmdLine.hasOption("importXml")) {
			String file = cmdLine.getOptionValue("importXml");
			File in = new File(new File("."), file);
			DomReader reader = new DomReader();
			Document document = reader.read(new FileInputStream(file));
			DomWriter writer = new DomWriter();
			File out = new File(new File("."), FileUtil.getFilename( "copy_", ".xml", false));
			writer.write(document, out);
			
			Database.importFromXml(in);
			//System.out.println("dumping " + db + " to " + out.getAbsolutePath());
		}

		int temperature = -1;
		if (cmdLine.hasOption("temperature")) {
			temperature = Integer.valueOf(cmdLine.getOptionValue("temperature"));
		}
		if (cmdLine.hasOption("heat")) {
			if (temperature < 0) {
				System.out.println("No temperature given! Use the -temperature argument!");
				return;
			}

			MashingSystem mashingSystem = new MashingSystem(notificationService);
			mashingSystem.heat(temperature);
			mashingSystem.switchOff();
			String msg = "Finished heating to " + temperature + "°C";
			notificationService.sendNotification(NotificationType.INFO, "Heating finished", msg);
			System.out.println(msg);
			return;
		}
		if (cmdLine.hasOption("rest")) {
			if (temperature < 0) {
				System.out.println("No temperature given! Use the -temperature argument!");
				return;
			}
			int restTime = Integer.valueOf(cmdLine.getOptionValue("rest"));
			MashingSystem mashingSystem = new MashingSystem(notificationService);
			mashingSystem.doRest(new Rest(temperature, restTime));
			mashingSystem.switchOff();
			String msg = "Finished a " + restTime + " minutes rest at " + temperature + "°C";
			notificationService.sendNotification(NotificationType.INFO, "Rest finished", msg);
			System.out.println(msg);
			return;
		}
	}

	private static Options getOptions() {
		Options options = new Options();
		options.addOption(new Option("help", "print this message"));
		options.addOption(new Option("config", true,
				"use given config file (if not given use the configuration.properties inside the current dir)"));
		options.addOption(new Option("importRecipe", true, "import the given recipe"));
		options.addOption(new Option("recipeSource", true, "the source of the recipe just getting imported"));
		options.addOption(new Option("listRecipes", "list all recipes"));
		options.addOption(new Option("showRecipe", true, "output the xml of the recipe"));
		options.addOption(new Option("temperature", true, "use this argument with -heat or -rest"));
		options.addOption(new Option("heat", "heat to the temperature given with the -temperature argument"));
		options.addOption(new Option("rest", true,
				"rest for the given minutes at the temperature given with the -temperature argument"));
		options.addOption(new Option("boil", true, "heat and boil for a given time"));
		options.addOption(new Option("sendTestMail", "sends a test email"));
		options.addOption(new Option("scanW1", "List all devices connected to the W1 bus"));
		options.addOption(new Option("getData", "get the actual data for all components"));
		options.addOption(new Option("testTemperature", "output the xml of the recipe"));
		options.addOption(new Option("testRelais", "testRelais"));
		options.addOption(new Option("testRpm", "testRpm"));
		options.addOption(
				new Option("dump", true, "dump database tables [BrewDB | Cookbook | IOLog | Journal] to xml."));
		options.addOption(
				new Option("importXml", true, "import database tables [BrewDB | Cookbook | IOLog | Journal] from xml."));
		options.addOption(new Option("shutDown", "shutDown"));
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
		while (true) {
			int j = 1;
			for (Sensor ts : tempSensors) {
				System.out.println("Sensor " + j++ + ": " + ts.getID() + ": " + ts.getValue());
			}
			ThreadUtil.sleepSeconds(3);
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
