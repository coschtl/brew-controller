package at.dcosta.brew;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import at.dcosta.brew.util.IOUtils;

public class Configuration {

	public static final String DATABASE_LOCATION = "databaseLocation";
	public static final String HEATER_PINS = "heater.pi4jPinNumbers";
	public static final String STIRRER_MOTOR_PIN = "stirrer.motor.pi4jPinNumber";
	public static final String STIRRER_RPM_PIN = "stirrer.rpm.pi4jPinNumber";
	public static final String MALT_STORE_OPENER_PIN = "maltStoreOpener.pi4jPinNumber";
	public static final String MALT_STORE_OPENER_TIMEOUT_SECONDS = "maltStoreOpener.timeoutSeconds";
	public static final String THERMOMETER_ADRESSES = "thermometer.addresses";
	public static final String THERMOMETER_CONNECTION = "thermometer.connection";
	public static final String MAIL_USER = "mail.user";
	public static final String MAIL_PASSWORD = "mail.password";
	public static final String MAIL_RECIPIENTS = "mail.recipients";

	private final Map<String, String> config;

	private String getMandatoryValue(String key) {
		String value = config.get(key);
		if (value == null || value.isEmpty()) {
			throw ConfigurationException.createParameterMissingException(key);
		}
		return value;
	}

	public int getInt(String key) {
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

	public String getString(String propertyName) {
		return config.get(propertyName);
	}

	public int[] getIntArray(String key) {
		String[] pinStrings = getMandatoryValue(key).split(",");
		int[] pins = new int[pinStrings.length];
		for (int i = 0; i < pinStrings.length; i++) {
			pins[i] = Integer.parseInt(pinStrings[i].trim());
		}
		return pins;
	}

	public String[] getStringArray(String key) {
		String[] addrStrings = getMandatoryValue(key).split(",");
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
