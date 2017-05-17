package at.dcosta.brew.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import at.dcosta.brew.Configuration;
import at.dcosta.brew.server.resources.Recipes;
import at.dcosta.brew.server.resources.States;

@ApplicationPath("/v1/api/")
public class BrewServerApplication extends Application {
	
	public BrewServerApplication() {
		InputStream cfgIn = getClass().getClassLoader().getResourceAsStream("configuration.properties");
		try {
			Configuration.initialize(cfgIn);
			cfgIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> s = new HashSet<Class<?>>();
		s.add(States.class);
		s.add(Recipes.class);
		return s;
	}

}
