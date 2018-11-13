package at.dcosta.brew;

import static at.dcosta.brew.Configuration.MALT_STORE_OPENER_PIN;
import static at.dcosta.brew.Configuration.MALT_STORE_OPENER_TIMEOUT_SECONDS;
import static at.dcosta.brew.Configuration.MALT_STORE_TIMEOUT_SECONDS;
import static at.dcosta.brew.Configuration.MASHING_HEATER_MINIMUM_INCREASE_PER_MINUTE;
import static at.dcosta.brew.Configuration.MASHING_HEATER_MONITOR_STARTUP_DELAY_MINUTES;
import static at.dcosta.brew.Configuration.MASHING_HEATER_PINS;
import static at.dcosta.brew.Configuration.MASHING_HEATER_POSTHEAT_INCREASE;
import static at.dcosta.brew.Configuration.MASHING_THERMOMETER_ADRESSES;
import static at.dcosta.brew.Configuration.STIRRER_RPM_PIN;

import at.dcosta.brew.com.NotificationService;
import at.dcosta.brew.com.NotificationType;
import at.dcosta.brew.db.BrewDB;
import at.dcosta.brew.db.BrewStep.Name;
import at.dcosta.brew.io.ComponentType;
import at.dcosta.brew.io.Relay;
import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.io.gpio.MockSensor;
import at.dcosta.brew.recipe.InfusionRecipe;
import at.dcosta.brew.recipe.Rest;
import at.dcosta.brew.util.MockUtil;
import at.dcosta.brew.util.ThreadUtil;

public class MashingSystem extends HeatingSystem {

	private final Relay maltStoreOpener;
	private final int maltStoreTimeoutSeconds;
	private final int maltStoreOpenerTimeoutSeconds;
	private final double postHeatingTempIncrease;
	private final Sensor rpmSensor;
	private final Stirrer stirrer;

	public MashingSystem(int brewId, BrewDB brewDb, NotificationService notificationService) {
		super(brewId, brewDb, notificationService);
		Configuration config = Configuration.getInstance();
		GpioSubsystem gpioSubsystem = GpioSubsystem.getInstance();
		maltStoreOpener = gpioSubsystem.getRelay("Malt Store Opener", config.getInt(MALT_STORE_OPENER_PIN));
		maltStoreTimeoutSeconds = config.getInt(MALT_STORE_TIMEOUT_SECONDS, 60);
		maltStoreOpenerTimeoutSeconds = config.getInt(MALT_STORE_OPENER_TIMEOUT_SECONDS);
		postHeatingTempIncrease = config.getDouble(MASHING_HEATER_POSTHEAT_INCREASE);
		int rpmPin;
		try {
			rpmPin = config.getInt(STIRRER_RPM_PIN);
		} catch (ConfigurationException e) {
			notificationService.sendNotification(NotificationType.WARNING, "noRpmConfigured");
			rpmPin = -1;
		}
		if (rpmPin > 0) {
			rpmSensor = gpioSubsystem.getRpmSensor("Stirrer RPM Sensor", rpmPin);
		} else {
			rpmSensor = new MockSensor(ComponentType.ROTATION_SPEED_SENSOR, "RPM-Sensor", "u/min").setValue(30);
		}
		stirrer = new Stirrer();
		System.out.println(getClass().getSimpleName() + " initialized");
	}

	public void addMalts() {
		// open malt store for an appropriate time
		maltStoreOpener.on();
		try {
			ThreadUtil.sleepSeconds(maltStoreOpenerTimeoutSeconds);
			pauseHandler.handlePause();
		} finally {
			maltStoreOpener.off();
		}
		ThreadUtil.sleepSeconds(maltStoreTimeoutSeconds);
	}

	public void doRest(Rest rest) {
		MockUtil.instance().setIncrementValuePerSecond(-0.01);
		getAverageTemperatureSensor().setAutoLoggingActive(false);
		long restEnd = System.currentTimeMillis() + rest.getMinutes() * ThreadUtil.ONE_MINUTE;
		long aktRestTimeMinutes = 0;
		getTemperature();
		double minTemp = rest.getTemperature()
				- Configuration.getInstance().getDouble(Configuration.MASHING_TEMPERATURE_MAX_DROP);
		while (System.currentTimeMillis() < restEnd) {
			long totalPauseTime = 0;
			for (int i = 0; i < 60; i++) {
				long pauseTime = pauseHandler.handlePause();
				totalPauseTime += pauseTime;
				if (pauseTime > 0) {
					break;
				}
				ThreadUtil.sleepSeconds(1);
			}
			if (totalPauseTime > 0) {
				restEnd += totalPauseTime;
				int pauseMinutes = (int) (totalPauseTime / ThreadUtil.ONE_MINUTE);
				rest.addMinutes(pauseMinutes);
				InfusionRecipe recipe = (InfusionRecipe) getRecipe();
				recipe.getRests().get(rest.getRestNumber()).addMinutes(pauseMinutes);
				recipe.setBoilingTime(recipe.getBoilingTime() + pauseMinutes);
				update(recipe);
			}
			if (totalPauseTime > ThreadUtil.ONE_MINUTE && aktRestTimeMinutes != 5) {
				aktRestTimeMinutes = 5;
			} else {
				aktRestTimeMinutes++;
			}
			if (aktRestTimeMinutes == 5) {
				startStirrer(false);
				MockUtil.instance().setIncrementValuePerSecond(0.1);
			} else if (aktRestTimeMinutes == 6) {
				MockUtil.instance().setIncrementValuePerSecond(-0.02);
				aktRestTimeMinutes = 0;
				stoptStirrer(false);
				logTemperature();
				if (getTemperature() < minTemp && rest.isKeepTemperature()) {
					logTemperature();
					journal.addEntry(getBrewId(), Name.HEAT_WATER, "restTemperatuerTooLow", minTemp);
					heatToTemperature(rest.getTemperature() - postHeatingTempIncrease,
							((restEnd - System.currentTimeMillis()) / ThreadUtil.ONE_MINUTE) - 1);
				}
			}
		}
		getAverageTemperatureSensor().setAutoLoggingActive(true);
		MockUtil.instance().setIncrementValuePerSecond(0.5);
		logTemperature();
	}

	public void heat(double targetTemperature) throws BrewException {
		startStirrer(true);
		heatingMonitor.start();
		try {

			// wait maximum 10s to get the stirrer running
			if (!isStirrerRunning(10)) {
				throw new BrewException("Stirrer does not start!");
			}
			heatToTemperature(targetTemperature - postHeatingTempIncrease);
		} finally {
			stoptStirrerDelayed();
			heatingMonitor.stop();
			switchHeatersOff();
		}
	}

	@Override
	public void switchOff() {
		super.switchOff();
		rpmSensor.switchOff();
		stoptStirrer(true);
	}

	private boolean isStirrerRunning(int maxWaitSeconds) {
		// FIXME: uncomment when rpm-sensor i´has been added (HARDWARE!)
		// int done = maxWaitSeconds > 0 ? 10 * maxWaitSeconds : 1;
		// for (int i = 0; i < done; i++) {
		// if (rpmSensor.getValue() > 0) {
		// return true;
		// }
		// ThreadUtil.sleepMillis(100);
		// }
		// return false;
		return true;
	}

	private void startStirrer(boolean force) {
		if (force || stirrer.isControlledAutomatically()) {
			stirrer.on();
		}
	}

	private void stoptStirrer(boolean force) {
		if (force || stirrer.isControlledAutomatically()) {
			stirrer.off();
		}
	}

	private void stoptStirrerDelayed() {
		stirrer.stopDelayed();
	}

	@Override
	protected int[] getHeaterPins() {
		return Configuration.getInstance().getIntArray(MASHING_HEATER_PINS);
	}

	@Override
	protected double getHeatingMonitorStartupDelayMinutes() {
		return Configuration.getInstance().getDouble(MASHING_HEATER_MONITOR_STARTUP_DELAY_MINUTES);
	}

	@Override
	protected double getMinTemperatureIncreasePerMinute() {
		return Configuration.getInstance().getDouble(MASHING_HEATER_MINIMUM_INCREASE_PER_MINUTE);
	}

	@Override
	protected String[] getTemperatureSensorAddresses() {
		return Configuration.getInstance().getStringArray(MASHING_THERMOMETER_ADRESSES);
	}

	@Override
	protected void heatToTemperatureWaiting() {
		startStirrer(false);
		if (!stirrer.isOn()) {
			return;
		}
		if (!isStirrerRunning(1)) {
			throw new BrewException("Stirrer does run anymore! --> heating stopped at " + getTemperature()
					+ getTemperatureSensors().get(0).getScale());
		}
	}

}
