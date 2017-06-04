package at.dcosta.brew;

import at.dcosta.brew.com.NotificationService;
import at.dcosta.brew.com.NotificationType;
import at.dcosta.brew.db.Brew;
import at.dcosta.brew.db.BrewDB;
import at.dcosta.brew.db.BrewStep;
import at.dcosta.brew.db.BrewStep.Name;
import at.dcosta.brew.db.BrewStepNameFactory;
import at.dcosta.brew.db.Cookbook;
import at.dcosta.brew.db.Journal;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.recipe.InfusionRecipe;
import at.dcosta.brew.recipe.RecipeReader;
import at.dcosta.brew.recipe.Rest;
import at.dcosta.brew.util.StoppableRunnable;

public class BrewController implements StoppableRunnable {

	private boolean keepRunning = true;
	private Journal journal;
	private BrewDB brewDb;
	private Brew brew;
	private NotificationService notificationService;

	public BrewController(Brew brew, NotificationService notificationService) {
		this.brew = brew;
		this.notificationService = notificationService;
		this.journal = new Journal();
		this.brewDb = new BrewDB();
	}

	@Override
	public void abort() {
		keepRunning = false;
	}

	@Override
	public boolean mustComplete() {
		return true;
	}

	@Override
	public void run() {
		try {
			InfusionRecipe recipe = (InfusionRecipe) RecipeReader.read(new Cookbook().getEntryById(brew.getCookbookEntryId()).getRecipe());
			System.out.println("starting recipe: " + recipe.getName());
			BrewStepNameFactory stepnames = new BrewStepNameFactory();
			
			// mashing
			MashingSystem mashingSystem = new MashingSystem(notificationService);
			brew.setBrewStatus(BrewStatus.MASHING);
			brewDb.persist(brew);
			System.out.println("start mashing");
			
			// heat to the mashing temperature
			BrewStep currentBrewStep = brewDb.addStep(brew.getId(), stepnames.stepname(Name.HEAT_WATER), "Heat water to " + recipe.getMashingTemperature() + "°C");
			System.out.println("start heating");
			mashingSystem.heat(recipe.getMashingTemperature());
			brewDb.complete(currentBrewStep);

			// add malts
			currentBrewStep = brewDb.addStep(brew.getId(),stepnames.stepname(Name.ADD_MALTS), "Add malts");
			System.out.println("add malts");
			mashingSystem.addMalts();
			brewDb.complete(currentBrewStep);

			// do the rests
			int count = 0;
			for (Rest rest : recipe.getRests()) {
				System.out.println("heating for rest " + count);
				currentBrewStep = brewDb.addStep(brew.getId(), stepnames.stepname(Name.HEAT_FOR_REST),  "Heat to "+ rest.getTemperature()+ "°C for rest " + count);
				mashingSystem.heat(rest.getTemperature());
				brewDb.complete(currentBrewStep);
				
				System.out.println("doing rest " + count);
				currentBrewStep = brewDb.addStep(brew.getId(), stepnames.stepname(Name.REST), "Rest " + count);
				mashingSystem.doRest(rest);
				brewDb.complete(currentBrewStep);
				count++;
			}
			notificationService.sendNotification(NotificationType.INFO, "Malting finised",
					"The malting has finished. Please start lauthering and do not forget to heat the secundary water!");
		} catch (ClassCastException e) {
			notificationService.sendNotification(NotificationType.ERROR, "FATAL Brewing system error",
					"This implementation can only handle InfusionRecipes!");
		} catch (BrewException e) {
			e.printStackTrace();
			notificationService.sendNotification(NotificationType.ERROR, "FATAL Brewing system error", e.getMessage());
		} finally {
			GpioSubsystem.getInstance().shutdown();
		}
	}

}
