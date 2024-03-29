package at.dcosta.brew;

import static at.dcosta.brew.Configuration.COOKING_COOKING_TEMPERATURE;
import static at.dcosta.brew.Configuration.COOKING_COOKING_TEMPERATURE_MIN;
import static at.dcosta.brew.Configuration.COOKING_HEATER_MINIMUM_INCREASE_PER_MINUTE;
import static at.dcosta.brew.Configuration.COOKING_HEATER_MONITOR_STARTUP_DELAY_MINUTES;
import static at.dcosta.brew.Configuration.COOKING_HEATER_PINS;
import static at.dcosta.brew.Configuration.COOKING_THERMOMETER_ADRESSES;

import java.util.Date;

import at.dcosta.brew.com.NotificationService;
import at.dcosta.brew.db.BrewDB;
import at.dcosta.brew.db.Journal;
import at.dcosta.brew.recipe.Recipe;
import at.dcosta.brew.util.ThreadUtil;

public class BoilingSystem extends HeatingSystem {

	private final double cookingTemperature;
	private final double cookingTemperatureMin;

	public BoilingSystem(int brewId, BrewDB brewDb, NotificationService notificationService, Journal journal) {
		super(brewId, brewDb, notificationService, journal);
		Configuration config = Configuration.getInstance();
		cookingTemperature = config.getDouble(COOKING_COOKING_TEMPERATURE);
		cookingTemperatureMin = config.getDouble(COOKING_COOKING_TEMPERATURE_MIN);
	}

	public void cook(int cookingTimeMinutes) throws BrewException {
		System.out.println("Start cooking");
		try {
			heatingMonitor.start();
			heatUntilBoiling();
			long cookingEnd = System.currentTimeMillis() + cookingTimeMinutes * ThreadUtil.ONE_MINUTE;
			while (System.currentTimeMillis() < cookingEnd) {
				double aktTemperature = getTemperature();
				if (aktTemperature < cookingTemperatureMin) {
					System.out.println("re-heating");
					heatUntilBoiling();
				}
				for (int i = 0; i < 10; i++) {
					long pauseTime = pauseHandler.handlePause();
					if (pauseTime > 0) {
						cookingEnd += pauseTime;
						Recipe recipe = getRecipe();
						recipe.setBoilingTime(recipe.getBoilingTime() + (int) (pauseTime / ThreadUtil.ONE_MINUTE));
						update(recipe);
					}
					ThreadUtil.sleepSeconds(1);
				}
			}
			System.out.println("End cooking");
		} finally {
			heatingMonitor.stop();
			switchHeatersOff();
		}
	}

	private void heatUntilBoiling() throws BrewException {
		heatToTemperature(cookingTemperature);
		System.out.println("boiling temperature reached");
	}

	@Override
	protected int[] getHeaterPins() {
		return Configuration.getInstance().getIntArray(COOKING_HEATER_PINS);
	}

	@Override
	protected double getHeatingMonitorStartupDelayMinutes() {
		return Configuration.getInstance().getDouble(COOKING_HEATER_MONITOR_STARTUP_DELAY_MINUTES);
	}

	@Override
	protected double getMinTemperatureIncreasePerMinute() {
		return Configuration.getInstance().getDouble(COOKING_HEATER_MINIMUM_INCREASE_PER_MINUTE);
	}

	@Override
	protected String[] getTemperatureSensorAddresses() {
		return Configuration.getInstance().getStringArray(COOKING_THERMOMETER_ADRESSES);
	}

	@Override
	protected void heatToTemperatureWaiting() {
		System.out.println(new Date() + ": aktTemperature=" + getTemperature() + "°C");
	}

}
