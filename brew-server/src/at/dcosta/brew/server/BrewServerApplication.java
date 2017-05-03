package at.dcosta.brew.server;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import at.dcosta.brew.server.resources.States;

@ApplicationPath("/v1/api/")
public class BrewServerApplication extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> s = new HashSet<Class<?>>();
		s.add(States.class);
		return s;
	}

}
