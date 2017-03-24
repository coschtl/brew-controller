package at.dcosta.brew;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public static final String MAIL_ACCOUNT = "mail.account";
	public static final String MAIL_PASSWORD = "mail.password";
	public static final String MAIL_RECIPIENTS = "mail.recipients";

	private static Configuration INSTANCE;
	private static final Pattern PATTERN_VARIABLE = Pattern.compile("\\$\\{([^}]+)}");

	public static Configuration getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("Configuration has not been initialized!");
		}
		return INSTANCE;
	}

	public static void initialize(File configFile) throws IOException {
		InputStream in = new FileInputStream(configFile);
		try {
			initialize(in);
		} finally {
			IOUtils.close(in);
		}
	}

	public static void initialize(InputStream in) throws IOException {
		INSTANCE = new Configuration();
		INSTANCE.readConfigFile(in);
	}

	private final Map<String, String> configuration;

	private Configuration() throws IOException {
		configuration = new HashMap<>();
	}

	public int getInt(String key) {
		String stringValue = getString(key);
		try {
			return Integer.parseInt(stringValue);
		} catch (NumberFormatException e) {
			throw ConfigurationException.createIllegalValueException(key, stringValue);
		}
	}

	public int[] getIntArray(String key) {
		String[] pinStrings = getString(key).split(",");
		int[] pins = new int[pinStrings.length];
		for (int i = 0; i < pinStrings.length; i++) {
			pins[i] = Integer.parseInt(pinStrings[i].trim());
		}
		return pins;
	}

	public String getString(String key) {
		String value = configuration.get(key);
		if (value == null || value.isEmpty()) {
			throw ConfigurationException.createParameterMissingException(key);
		}
		return value;
	}

	public String[] getStringArray(String key) {
		String[] addrStrings = getString(key).split(",");
		String[] addresses = new String[addrStrings.length];
		for (int i = 0; i < addrStrings.length; i++) {
			addresses[i] = addrStrings[i].trim();
		}
		return addresses;
	}

	@Override
	public String toString() {
		return configuration.toString();
	}

	private void readConfigFile(InputStream in) throws FileNotFoundException, IOException {
		Map<String, String> env = null;
		Iterator<Entry<String, String>> it = readFile(in).entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			String value = entry.getValue();
			if (value.indexOf("${") >= 0) {
				if (env == null) {
					File envFile = new File(System.getProperty("user.home") + "/environment.properties");
					if (!envFile.exists()) {
						throw new ConfigurationException(
								"Variables are used inside the configuration are used, but environment file "
										+ envFile.getAbsolutePath() + " not found!");
					}
					env = readFile(envFile);
				}
				Matcher m = PATTERN_VARIABLE.matcher(value);
				if (m.find()) {
					String variable = m.group(1);
					String envValue = env.get(variable);
					if (envValue == null) {
						throw new ConfigurationException(
								"Variable '" + variable + "' not defined inside environment.properties!");
					}
					value = m.replaceFirst(envValue);
				}
			}
			configuration.put(entry.getKey(), value);
		}
	}

	private Map<String, String> readFile(File file) throws FileNotFoundException, IOException {
		InputStream in = new FileInputStream(file);
		try {
			return readFile(in);
		} finally {
			IOUtils.close(in);
		}
	}

	private Map<String, String> readFile(InputStream in) throws FileNotFoundException, IOException {
		Map<String, String> m = new HashMap<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		try {

			while ((line = reader.readLine()) != null) {
				if (line.trim().length() == 0 || line.startsWith("#")) {
					continue;
				}
				int pos = line.indexOf('=');
				if (pos > 0) {
					m.put(line.substring(0, pos).trim(), line.substring(pos + 1).trim());
				}
			}
		} finally {
			IOUtils.close(reader);
		}
		return m;
	}

}
