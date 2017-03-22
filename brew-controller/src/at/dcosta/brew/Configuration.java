package at.dcosta.brew;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import at.dcosta.brew.util.IOUtils;

public class Configuration {

	private static final String DATABASE_LOCATION = "databaseLocation";
	private static final String HEATER_PINS = "heater.pi4jPinNumbers";
	private static final String STIRRER_MOTOR_PIN = "stirrer.motor.pi4jPinNumber";
	private static final String STIRRER_RPM_PIN = "stirrer.rpm.pi4jPinNumber";
	private static final String MALT_STORE_OPENER_PIN = "maltStoreOpener.pi4jPinNumber";
	private static final String MALT_STORE_OPENER_TIMEOUT = "maltStoreOpener.timeoutSeconds";
	private static final String THERMOMETER_ADRESSES = "thermometer.addresses";
	private static final String THERMOMETER_CONNECTION = "thermometer.connection";

	private final Map<String, String> config;

	private String getMandatoryValue(String key) {
		String value = config.get(key);
		if (value == null || value.isEmpty()) {
			throw ConfigurationException.createParameterMissingException(key);
		}
		return value;
	}

	public int getIntValue(String key) {
		String stringValue = getMandatoryValue(key);
		try {
			return Integer.parseInt(stringValue);
		} catch (NumberFormatException e) {
			throw ConfigurationException.createIllegalValueException(key, stringValue);
		}
	}

	public Configuration(String configFile) throws IOException {
		config = new HashMap<>();
		readConfigFile(configFile);
	}

	public String getProperty(String propertyName) {
		return config.get(propertyName);
	}

	public int[] getHeaterPins() {
		String[] pinStrings = getMandatoryValue(HEATER_PINS).split(",");
		int[] pins = new int[pinStrings.length];
		for (int i = 0; i < pinStrings.length; i++) {
			pins[i] = Integer.parseInt(pinStrings[i].trim());
		}
		return pins;
	}

	public String getThermometerConnection() {
		String connection = config.get(THERMOMETER_CONNECTION);
		if (connection == null || "w1".equalsIgnoreCase(connection)) {
			return "w1";
		}
		throw new ConfigurationException("Thermometer connection-type ' " + connection + "' not supported!");
	}

	public int getStirrerMotorPin() {
		return getIntValue(STIRRER_MOTOR_PIN);
	}

	public int getStirrerRpmPin() {
		return getIntValue(STIRRER_RPM_PIN);
	}

	public int getMaltStoreOpenerPin() {
		return getIntValue(MALT_STORE_OPENER_PIN);
	}
	public int getMaltStoreOpenerTimeoutSeconds() {
		return getIntValue(MALT_STORE_OPENER_TIMEOUT);
	}

	public String getDatabaseLocation() {
		return getMandatoryValue(DATABASE_LOCATION);
	}

	public String[] getThermometerAddresses() {
		String[] addrStrings = getMandatoryValue(THERMOMETER_ADRESSES).split(",");
		String[] addresses = new String[addrStrings.length];
		for (int i = 0; i < addrStrings.length; i++) {
			addresses[i] = addrStrings[i].trim();
		}
		return addresses;
	}

	private void readConfigFile(String configFile) throws FileNotFoundException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(configFile));
		String line;
		try {

			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0 || line.startsWith("#")) {
					continue;
				}
				int pos = line.indexOf('=');
				if (pos > 0) {
					config.put(line.substring(0, pos).trim(), line.substring(pos + 1).trim());
				}
			}
		} finally {
			IOUtils.close(reader);
		}
	}

}
