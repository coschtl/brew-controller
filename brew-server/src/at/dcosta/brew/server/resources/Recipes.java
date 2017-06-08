package at.dcosta.brew.server.resources;

import java.net.URI;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataParam;

import at.dcosta.brew.BrewStatus;
import at.dcosta.brew.db.Brew;
import at.dcosta.brew.db.BrewDB;
import at.dcosta.brew.db.BrewStep;
import at.dcosta.brew.db.BrewStep.Name;
import at.dcosta.brew.db.BrewStep.StepName;
import at.dcosta.brew.db.BrewStepNameFactory;
import at.dcosta.brew.db.Cookbook;
import at.dcosta.brew.db.CookbookEntry;
import at.dcosta.brew.db.FetchType;
import at.dcosta.brew.recipe.InfusionRecipe;
import at.dcosta.brew.recipe.Ingredient;
import at.dcosta.brew.recipe.RecipeReader;
import at.dcosta.brew.recipe.RecipeWriter;
import at.dcosta.brew.recipe.Rest;
import at.dcosta.brew.server.BrewServerException;
import at.dcosta.brew.server.Recipe;
import at.dcosta.brew.server.Step;

@Path("")
public class Recipes extends AbstractResource {

	private final Cookbook cookbook;
	private final BrewDB brewDB;
	private final DateFormat dateFormat;

	public Recipes() {
		cookbook = new Cookbook();
		brewDB = new BrewDB();
		dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
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
				.header("x-server-message", "Rezept '" + recipeName + "' erfolgreich hinzugefügt.")
				.entity(Integer.valueOf(id)).build();
	}

	@GET
	@Path("recipes/{recipeId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Recipe getRecipe(@PathParam("recipeId") int recipeId, @QueryParam("fetchType") FetchType fetchType) {
		if (fetchType == null) {
			fetchType = FetchType.MINIMAL;
		}
		CookbookEntry entry = getCookbookEntry(recipeId);
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		return createDto(entry, fetchType, df, brewDB.isBrewRunning(recipeId));
	}

	private CookbookEntry getCookbookEntry(int recipeId) {
		CookbookEntry entry = cookbook.getEntryById(recipeId);
		if (entry == null) {
			throw new BrewServerException("Unknown recipeID: " + recipeId, Status.BAD_REQUEST);
		}
		return entry;
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
			recipes.add(createDto(recipe, fetchType, df, false));
		}
		return recipes;
	}

	@PUT
	@Path("recipes/{recipeId}/startBrew")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startBrew(@Context UriInfo uriInfo, @Context HttpServletRequest request,
			@PathParam("recipeId") int recipeId) {
		Brew runningBrew = brewDB.getRunningBrew();
		if (runningBrew != null && runningBrew.getCookbookEntryId() != recipeId) {
			CookbookEntry runningCookbookEntry = cookbook.getEntryById(runningBrew.getCookbookEntryId());
			String cookBookEntryName = runningCookbookEntry == null ? "<unknown>" : runningCookbookEntry.getName();
			throw new BrewServerException(
					"There is already a brew for '" + cookBookEntryName + "' (recipeID="
							+ runningBrew.getCookbookEntryId()
							+ ") running. This brew must get aborted or finished before a new recipe can get brewed!",
					Status.BAD_REQUEST);
		}
		if (runningBrew == null) {
			brewDB.startNewBrew(recipeId, new Timestamp(System.currentTimeMillis()));
		}
		URI statusUri = URI.create(getAppBaseUri(uriInfo, request) + "/app/status.html");
		return Response.status(Status.CREATED).location(statusUri).build();
	}

	@DELETE
	@Path("recipes/abortRunningBrew")
	@Produces(MediaType.APPLICATION_JSON)
	public Response abortRunningBrew() {
		brewDB.abortRunningBrew();
		return Response.status(Status.OK).build();
	}

	private String getAppBaseUri(UriInfo uriInfo, HttpServletRequest request) {
		String restBaseUri = uriInfo.getBaseUri().toString();
		String contextPath = request.getContextPath();
		String appBaseUri = restBaseUri.substring(0, restBaseUri.indexOf(contextPath) + contextPath.length());
		return appBaseUri;
	}

	private Recipe createDto(CookbookEntry cookbookEntry, FetchType fetchType, DateFormat df, boolean brewRunning) {
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
		recipe.setBrewRunning(brewRunning);
		return recipe;
	}

	@GET
	@Path("recipes/{recipeId}/steps/{brewStatus}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Step> getSubsteps(@PathParam("recipeId") int recipeId, @PathParam("brewStatus") BrewStatus brewStatus) {
		Brew runningBrew = null;
		if (recipeId == -1) {
			// current brew!
			runningBrew = brewDB.getRunningBrew();
			if (runningBrew == null) {
				return Collections.emptyList();
			}
			recipeId = runningBrew.getCookbookEntryId();
		}
		InfusionRecipe recipe = (InfusionRecipe) RecipeReader.read(cookbook.getEntryById(recipeId).getRecipe());
		BrewStepNameFactory stepnames = new BrewStepNameFactory();
		List<Step> l = new ArrayList<>();
		switch (brewStatus) {
		case MASHING:
			l.add(createStep(Name.HEAT_WATER, "aufheizen", stepnames, runningBrew, recipe));
			l.add(createStep(Name.ADD_MALTS, "einmaischen", stepnames, runningBrew, recipe));
			for (int i = 0; i < recipe.getRests().size(); i++) {
				l.add(createStep(Name.HEAT_FOR_REST, "heizen", stepnames, runningBrew, recipe));
				l.add(createStep(Name.REST, "rasten", stepnames, runningBrew, recipe));
			}
			break;
		default:
			break;
		}
		return l;
	}

	private Step createStep(Name name, String text, BrewStepNameFactory stepNames, Brew runningBrew,
			InfusionRecipe recipe) {
		StepName stepName = stepNames.stepname(name);
		Step step = new Step(stepName.toString(), text);
		BrewStep brewStep = getBrewStep(stepName, runningBrew);
		Timestamp startTime = null;
		Timestamp endTime = null;
		if (brewStep != null) {
			startTime = brewStep.getStartTime();
			endTime = brewStep.getEndTime();
			step.setFinished(brewStep.isFinished());
			step.setActive(brewStep.isActive());
		}
		return addDescription(stepName, recipe, step, startTime, endTime);
	}

	private Step addDescription(StepName stepName, InfusionRecipe recipe, Step step, Date startTime, Date endTime) {
		String header = null;
		StringBuilder descr = new StringBuilder();
		Rest rest = null;
		switch (stepName.getName()) {
		case HEAT_WATER:
			header = "Aufheizen auf Einmaisch-Temperatur";
			descr.append(recipe.getPrimaryWater()).append(" Liter Brauwasser werden bis zur Einmaischtemperatur von ")
					.append(recipe.getMashingTemperature()).append("°C erhitzt.");
			break;
		case ADD_MALTS:
			header = "Malze hinzufügen";
			descr.append("Folgende Malze werden hinzugefügt:<ul>");
			for (Ingredient malt : recipe.getMalts()) {
				descr.append("<li>").append(malt.getAmount()).append("g ").append(malt.getName()).append("</li>");
			}
			descr.append("</ul>");
			break;
		case REST:
			header = "Rasten";
			rest = recipe.getRests().get(stepName.getInstanceNumber());
			descr.append(stepName.getInstanceNumber() + 1).append(". Rast: ").append(rest.getMinutes())
					.append(" Minuten bei ").append(rest.getTemperature()).append("°C");
			break;
		case HEAT_FOR_REST:
			header = "Maische aufheizen";
			rest = recipe.getRests().get(stepName.getInstanceNumber());
			descr.append("Aufheizen der Maische auf ").append(rest.getTemperature()).append("°C für die ")
					.append(stepName.getInstanceNumber() + 1).append(". Rast.");
			break;
		case LAUTHERING_REST:
			header = "Läuter-Rast";
			descr.append("Umfüllen der Maische in den Läuter-Behälter.<br/>Anschließend muss eine Läuterruhe von ")
					.append(recipe.getLauteringRest()).append(" Minuten eingehalten werden.");
			break;
		}

		if (startTime != null) {
			descr.append("<br/><br/>Start: ").append(dateFormat.format(startTime));
			if (rest != null) {
				endTime = new Date(startTime.getTime() + rest.getMinutes() * MINUTE);
			}
			if (endTime != null) {
				descr.append("&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;Ende: ").append(dateFormat.format(endTime));
			}
		}
		step.setHeaderText(header);
		step.setDescription(descr.toString());
		return step;
	}

	private BrewStep getBrewStep(StepName stepName, Brew runningBrew) {
		if (runningBrew == null) {
			return null;
		}
		for (BrewStep step : runningBrew.getSteps()) {
			if (step.getStepName().equals(stepName)) {
				return step;
			}
		}
		return null;
	}
}
