package at.dcosta.brew;

public class ConfigurationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public static ConfigurationException createIllegalValueException(String key, String value) {
		return new ConfigurationException(
				"Configuration error: key '" + key + "' has an unsupported value: " + String.valueOf(value));
	}

	public static ConfigurationException createParameterMissingException(String key, Configuration configuration) {
		return new ConfigurationException("Configuration error: key '" + key + "' is missing or has no value: " + configuration);
	}

	public ConfigurationException(String message) {
		super(message);
	}

}
