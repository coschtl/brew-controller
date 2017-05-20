package at.dcosta.brew.server.resources;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataParam;

import at.dcosta.brew.db.Cookbook;
import at.dcosta.brew.db.CookbookEntry;
import at.dcosta.brew.db.FetchType;
import at.dcosta.brew.recipe.RecipeReader;
import at.dcosta.brew.recipe.RecipeWriter;
import at.dcosta.brew.server.BrewServerException;
import at.dcosta.brew.server.Recipe;
import at.dcosta.brew.server.Step;

@Path("")
public class Recipes extends AbstractResource {

	private final Cookbook cookbook;

	public Recipes() {
		cookbook = new Cookbook();
	}

	public static enum ReturnType {
		MINIMAL, FULL;
	}

	@POST
	@Path("recipes")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response addRecipe(@FormDataParam("recipeName") String recipeName,
			@FormDataParam("recipeSource") String recipeSource, @FormDataParam("recipe") String recipe) {
		assertNotEmpty("recipeName", recipeName);
		assertNotEmpty("recipeSource", recipeSource);
		assertNotEmpty("recipe", recipe);

		try {
			RecipeReader.read(recipe);
		} catch (Exception e) {
			throw new BrewServerException("Recipe is not valid: " + e.getMessage(), Status.BAD_REQUEST);
		}
		int id = cookbook.addRecipe(recipeName, recipeSource, recipe);
		return Response.status(Status.CREATED)
				.header("x-server-message", "Rezept '" + recipeName + "' erfolgreich hinzugefügt.").entity(Integer.valueOf(id)).build();
	}

	@GET
	@Path("recipes/{recipeId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Recipe getRecipe(@PathParam("recipeId") int recipeId, @QueryParam("fetchType") FetchType fetchType) {
		if (fetchType == null) {
			fetchType = FetchType.MINIMAL;
		}
		CookbookEntry entry = cookbook.getEntryById(recipeId);
		if (entry == null) {
			System.out.println("NO RECIPE!!!");
			return null;
		}
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		return createDto(entry, fetchType, df);
	}

	@GET
	@Path("recipes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Recipe> getRecipes(@QueryParam("fetchType") FetchType fetchType) {
		if (fetchType == null) {
			fetchType = FetchType.MINIMAL;
		}
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		List<Recipe> recipes = new ArrayList<>();
		for (CookbookEntry recipe : cookbook.listRecipes(fetchType)) {
			recipes.add(createDto(recipe, fetchType, df));
		}
		return recipes;
	}

	private Recipe createDto(CookbookEntry cookbookEntry, FetchType fetchType, DateFormat df) {
		Recipe recipe = new Recipe();
		recipe.setId(cookbookEntry.getId());
		recipe.setAddedOn(df.format(cookbookEntry.getAddedOn()));
		recipe.setBrewCount(cookbookEntry.getBrewCount());
		recipe.setName(cookbookEntry.getName());
		recipe.setSource(cookbookEntry.getRecipeSource());
		if (fetchType == FetchType.FULL) {
			String prettyPrintXml = new RecipeWriter(RecipeReader.read(cookbookEntry.getRecipe()), true)
					.getRecipeAsXmlString();
			recipe.setRecipe(prettyPrintXml);
		}
		return recipe;
	}

	@GET
	@Path("recipes/{recipeId}/steps/{step}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Step> getSubsteps(@PathParam("recipeId") int recipeId, @PathParam("step") String step) {
		List<Step> l = new ArrayList<>();
		l.add(new Step("heat1", "aufheizen").setFinished(true));
		l.add(new Step("mashing", "einmaischen").setFinished(true));
		l.add(new Step("rest1", "rasten").setFinished(true));
		l.add(new Step("heat2", "heizen").setFinished(true));
		l.add(new Step("rest2", "rasten").setActive(true));
		l.add(new Step("heat3", "heizen"));
		l.add(new Step("rest3", "rasten"));
		l.add(new Step("heat4", "heizen"));
		return l;
	}

}
