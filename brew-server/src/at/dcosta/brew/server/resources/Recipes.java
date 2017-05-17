package at.dcosta.brew.server.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import at.dcosta.brew.db.Cookbook;
import at.dcosta.brew.db.CookbookEntry;
import at.dcosta.brew.db.FetchType;
import at.dcosta.brew.recipe.RecipeWriter;
import at.dcosta.brew.server.Recipe;
import at.dcosta.brew.server.Step;

@Path("")
public class Recipes {
	
	private final  Cookbook cookbook;
	
	public Recipes() {
		cookbook = new Cookbook();
	}
	
	public static enum ReturnType {
		MINIMAL,
		FULL;
	}

	@POST
	@Path("recipes")
	public void addRecipe(@QueryParam("recipeName") String recipeName, @QueryParam("recipeSource") String recipeSource,
			@QueryParam("recipe") String recipe) {

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
		return createDto(entry, fetchType);
	}
	
	@GET
	@Path("recipes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Recipe> getRecipes( @QueryParam("fetchType") FetchType fetchType) {
		if (fetchType == null) {
			fetchType = FetchType.MINIMAL;
		}
		List<Recipe> recipes = new ArrayList<>();
		for (CookbookEntry recipe : cookbook.listRecipes(fetchType)) {
			recipes.add(createDto(recipe, fetchType));
		}
		return recipes;
	}
	
	private Recipe createDto(CookbookEntry cookbookEntry, FetchType fetchType) {
		Recipe recipe = new Recipe();
		recipe.setId(cookbookEntry.getId());
		recipe.setAddedOn(cookbookEntry.getAddedOn());
		recipe.setBrewCount(cookbookEntry.getBrewCount());
		recipe.setName(cookbookEntry.getName());
		recipe.setSource(cookbookEntry.getRecipeSource());
		if (fetchType == FetchType.FULL) {
			recipe.setRecipe(new RecipeWriter(cookbookEntry.getRecipe(), true).getRecipeAsXmlString());
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
