package at.dcosta.brew;

import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;

public class AbstractConfigBasedTest {
	public AbstractConfigBasedTest() {
		try {
			URL url = getClass().getClassLoader().getResource(".");
			File f = new File(url.toURI());
			Configuration.initialize(new File(f, "../configuration.properties"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("can not initialize configuration for test: " + e.getMessage());
		}
	}

}
