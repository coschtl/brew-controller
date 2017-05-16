package at.dcosta.brew.server.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import at.dcosta.brew.Configuration;
import at.dcosta.brew.server.Step;
import at.dcosta.brew.server.StepList;

@Path("recipes")
public class Recipes {

	public Recipes() {
		InputStream cfgIn = getClass().getClassLoader().getResourceAsStream("configuration.properties");
		try {
			Configuration.initialize(cfgIn);
			cfgIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@GET
	@Path("{recipeId}/{step}")
	@Produces(MediaType.APPLICATION_JSON)
	public StepList getSubsteps(@PathParam("recipeId") int recipeId, @PathParam("step") String step) {
		System.out.println("returning steps for recipe=" + recipeId + ", step=" + step);
		StepList l = new StepList();
		l.addEntry(new Step("heat1", "aufheizen").setFinished(true));
		l.addEntry(new Step("mashing", "einmaischen").setFinished(true));
		l.addEntry(new Step("rest1", "rasten").setFinished(true));
		l.addEntry(new Step("heat2", "heizen").setFinished(true));
		l.addEntry(new Step("rest2", "rasten").setActive(true));
		l.addEntry(new Step("heat3", "heizen"));
		l.addEntry(new Step("rest3", "rasten"));
		l.addEntry(new Step("heat4", "heizen"));
		return l;
	}

}
