package at.dcosta.brew.server.resources;

import javax.ws.rs.core.Response.Status;

import at.dcosta.brew.server.BrewServerException;

public class AbstractResource {
	
	public void assertNotEmpty(String name, String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new BrewServerException("Parameter '" + name + "' missing or empty!", Status.BAD_REQUEST);
		}
	}

}
