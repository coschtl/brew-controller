package at.dcosta.brew;

import static at.dcosta.brew.Configuration.MALT_STORE_OPENER_PIN;
import static at.dcosta.brew.Configuration.MALT_STORE_OPENER_TIMEOUT_SECONDS;
import static at.dcosta.brew.Configuration.MASHING_HEATER_MINIMUM_INCREASE_PER_MINUTE;
import static at.dcosta.brew.Configuration.MASHING_HEATER_PINS;
import static at.dcosta.brew.Configuration.MASHING_THERMOMETER_ADRESSES;
import static at.dcosta.brew.Configuration.STIRRER_RPM_PIN;

import at.dcosta.brew.com.NotificationService;
import at.dcosta.brew.com.NotificationType;
import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.io.gpio.MockSensor;
import at.dcosta.brew.io.gpio.Relay;
import at.dcosta.brew.util.ThreadUtil;

public class MashingSystem extends HeatingSystem {

	private final Relay maltStoreOpener;
	private final int maltStoreOpenerTimeoutSeconds;
	private final Sensor rpmSensor;
	private final Stirrer stirrer;

	private double lastTemperature = 4d;
	private boolean useStirrer;

	public MashingSystem(NotificationService notificationService) {
		super(notificationService);
		Configuration config = Configuration.getInstance();
		GpioSubsystem gpioSubsystem = GpioSubsystem.getInstance();
		maltStoreOpener = gpioSubsystem.getRelay("Malt Store Opener", config.getInt(MALT_STORE_OPENER_PIN));
		maltStoreOpenerTimeoutSeconds = config.getInt(MALT_STORE_OPENER_TIMEOUT_SECONDS);
		int rpmPin;
		try {
			rpmPin = config.getInt(STIRRER_RPM_PIN);
		} catch (ConfigurationException e) {
			notificationService.sendNotification(NotificationType.WARNING, "Possible configuration error",
					"No RPM sensor configured!");
			rpmPin = -1;
		}
		if (rpmPin > 0) {
			rpmSensor = gpioSubsystem.getRpmSensor("Stirrer RPM Sensor", rpmPin);
		} else {
			rpmSensor = new MockSensor("RPM-Sensor", "u/min").setValue(30);
		}
		stirrer = new Stirrer();
	}

	public void addMalts() {
		// open malt store for an appropriate time
		maltStoreOpener.on();
		try {
			ThreadUtil.sleepSeconds(maltStoreOpenerTimeoutSeconds);
		} finally {
			maltStoreOpener.off();
		}
		ThreadUtil.sleepMinutes(1);
	}

	public void heat(double targetTemperature, boolean useStirrer) throws BrewException {
		this.useStirrer = useStirrer;
		if (useStirrer) {
			startStirrer();
		}
		heatingMonitor.start();
		try {

			// wait maximum 10s to get the stirrer running
			if (useStirrer & !isStirrerRunning(10)) {
				throw new BrewException("Stirrer does not start!");
			}
			heatToTemperature(targetTemperature);
		} finally {
			if (useStirrer) {
				stoptStirrer();
			}
			heatingMonitor.stop();
			switchHeatersOff();
		}
	}

	public void startStirrer() {
		stirrer.start();
	}

	public void stoptStirrer() {
		stirrer.stop();
	}

	private boolean isStirrerRunning(int maxWaitSeconds) {
		int done = maxWaitSeconds > 0 ? 10 * maxWaitSeconds : 1;
		for (int i = 0; i < done; i++) {
			if (rpmSensor.getValue() > 0) {
				return true;
			}
			ThreadUtil.sleepMillis(100);
		}
		return false;
	}

	@Override
	protected int[] getHeaterPins() {
		return Configuration.getInstance().getIntArray(MASHING_HEATER_PINS);
	}

	@Override
	protected double getLastTemperature() {
		return lastTemperature;
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
		if (useStirrer && !isStirrerRunning(0)) {
			throw new BrewException("Stirrer does run anymore! --> heating stopped at " + getTemperature()
					+ getTemperatureSensors().get(0).getScale());
		}
	}

}
