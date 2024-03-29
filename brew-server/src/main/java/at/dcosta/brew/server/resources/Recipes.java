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
import at.dcosta.brew.msg.I18NTexts;
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

	public static enum ReturnType {
		MINIMAL, FULL;
	}

	private final Cookbook cookbook;
	private final BrewDB brewDB;

	public Recipes() {
		cookbook = new Cookbook();
		brewDB = new BrewDB();
	}

	@DELETE
	@Path("recipes/abortRunningBrew")
	@Produces(MediaType.APPLICATION_JSON)
	public Response abortRunningBrew() {
		brewDB.abortRunningBrew();
		return Response.status(Status.OK).build();
	}

	@POST
	@Path("recipes")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response addRecipe(@FormDataParam("recipe") String recipeString) {
		assertNotEmpty("recipe", recipeString);
		at.dcosta.brew.recipe.Recipe recipe;
		try {
			recipe = RecipeReader.read(recipeString);
		} catch (Exception e) {
			throw new BrewServerException("Recipe is not valid: " + e.getMessage(), Status.BAD_REQUEST);
		}
		int id = cookbook.addRecipe(recipe.getName(), recipe.getSource(), recipeString);
		return Response.status(Status.CREATED)
				.header("x-server-message", "Rezept '" + recipe.getName() + "' erfolgreich hinzugefügt.")
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
		return createDto(entry, fetchType, I18NTexts.getDateFormat(DateFormat.MEDIUM), brewDB.isBrewRunning(recipeId));
	}

	@GET
	@Path("recipes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Recipe> getRecipes(@QueryParam("fetchType") FetchType fetchType) {
		if (fetchType == null) {
			fetchType = FetchType.MINIMAL;
		}
		List<Recipe> recipes = new ArrayList<>();
		DateFormat df = I18NTexts.getDateFormat(DateFormat.MEDIUM);
		for (CookbookEntry recipe : cookbook.listRecipes(fetchType)) {
			recipes.add(createDto(recipe, fetchType, df, false));
		}
		return recipes;
	}

	@GET
	@Path("recipes/{recipeId}/steps/{brewStatus}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Step> getSubsteps(@PathParam("recipeId") int recipeId, @PathParam("brewStatus") BrewStatus brewStatus) {
		Brew runningBrew = null;
		InfusionRecipe recipe;
		if (recipeId == -1) {
			// current brew!
			runningBrew = brewDB.getRunningBrew();
			if (runningBrew == null) {
				return Collections.emptyList();
			}
			recipe = (InfusionRecipe) runningBrew.getRecipe();
		} else {
			recipe = (InfusionRecipe) RecipeReader.read(cookbook.getEntryById(recipeId).getRecipe());
		}
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

	@GET
	@Path("recipes/{recipeId}/showBrews")
	@Produces(MediaType.APPLICATION_JSON)
	public List<at.dcosta.brew.server.Brew> showBrews(@PathParam("recipeId") int recipeId) {
		List<Brew> brews = brewDB.getBrewsByRecipe(recipeId);
		List<at.dcosta.brew.server.Brew> dtos = new ArrayList<>(brews.size());
		DateFormat dateTimeFormat = I18NTexts.getDateTimeFormat(DateFormat.MEDIUM);
		for (Brew brew : brews) {
			at.dcosta.brew.server.Brew dto = new at.dcosta.brew.server.Brew();
			String formattedStart = brew.getStartTime() == null ? "" : dateTimeFormat.format(brew.getStartTime());
			String formattedEnd = brew.getEndTime() == null ? "" : dateTimeFormat.format(brew.getEndTime());
			String time = formattedStart;
			if (!formattedEnd.isEmpty()) {
				time += " - " + formattedEnd;
			}
			dto.setDate(time);
			dto.setId(brew.getId());
			dtos.add(dto);
		}
		return dtos;
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
			runningBrew = brewDB.startNewBrew(recipeId, new Timestamp(System.currentTimeMillis()));
		}
		URI statusUri = URI.create(getAppBaseUri(uriInfo, request) + "/app/status.html?brew=" + runningBrew.getId());
		return Response.status(Status.CREATED).location(statusUri).build();
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

		DateFormat timeFormat = I18NTexts.getTimeFormat(DateFormat.MEDIUM);
		if (startTime != null) {
			descr.append("<br/><br/>Start: ").append(timeFormat.format(startTime));
			if (rest != null && stepName.getName() != Name.HEAT_FOR_REST) {
				endTime = new Date(startTime.getTime() + rest.getMinutes() * MINUTE);
			}
			if (endTime != null) {
				descr.append("&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;Ende: ").append(timeFormat.format(endTime));
			}
		}
		step.setHeaderText(header);
		step.setDescription(descr.toString());
		return step;
	}

	private Recipe createDto(CookbookEntry cookbookEntry, FetchType fetchType, DateFormat df, boolean brewRunning) {
		Recipe recipe = new Recipe();
		recipe.setId(cookbookEntry.getId());
		recipe.setAddedOn(df.format(cookbookEntry.getAddedOn()));
		recipe.setBrewCount(brewDB.getBrewsByRecipe(cookbookEntry.getId()).size());
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

	private String getAppBaseUri(UriInfo uriInfo, HttpServletRequest request) {
		String restBaseUri = uriInfo.getBaseUri().toString();
		String contextPath = request.getContextPath();
		String appBaseUri = restBaseUri.substring(0, restBaseUri.indexOf(contextPath) + contextPath.length());
		return appBaseUri;
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

	private CookbookEntry getCookbookEntry(int recipeId) {
		CookbookEntry entry = cookbook.getEntryById(recipeId);
		if (entry == null) {
			throw new BrewServerException("Unknown recipeID: " + recipeId, Status.BAD_REQUEST);
		}
		return entry;
	}
}
