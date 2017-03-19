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

	private final Map<String, String> config;

	public Configuration(String configFile) throws IOException {
		config = new HashMap<>();
		readConfigFile(configFile);
	}
	
	public String getProperty(String propertyName) {
		return config.get(propertyName);
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
