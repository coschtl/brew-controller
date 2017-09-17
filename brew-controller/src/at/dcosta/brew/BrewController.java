package at.dcosta.brew;

import java.util.Iterator;

import at.dcosta.brew.com.NotificationService;
import at.dcosta.brew.com.NotificationType;
import at.dcosta.brew.db.Brew;
import at.dcosta.brew.db.BrewDB;
import at.dcosta.brew.db.BrewStep;
import at.dcosta.brew.db.BrewStep.Name;
import at.dcosta.brew.db.BrewStep.StepName;
import at.dcosta.brew.db.BrewStepNameFactory;
import at.dcosta.brew.db.Journal;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.recipe.InfusionRecipe;
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
			InfusionRecipe recipe = (InfusionRecipe) brew.getRecipe();
			System.out.println("starting recipe: " + recipe.getName());
			switch (brew.getBrewStatus()) {
			case SCHEDULED:
				System.out.println("start mashing");
				brew.setBrewStatus(BrewStatus.MASHING);
				brewDb.persist(brew);
				// fall thru!
			case MASHING:
				System.out.println("continue mashing");
				doMashing(recipe);
				break;
			default:
				System.out.println("Can not start/continue brew of " + recipe.getName()
						+ ", because the corresponding brew is in status " + brew.getBrewStatus());
				return;
			}
		} finally {
			GpioSubsystem.getInstance().shutdown();
		}
	}

	private void doMashing(InfusionRecipe recipe) {
		try {
			BrewStepNameFactory stepnames = new BrewStepNameFactory();
			StepName stepName;
			BrewStep currentBrewStep;
			MashingSystem mashingSystem = new MashingSystem(brew.getId(), notificationService);

			// heat to the mashing temperature
			stepName = stepnames.stepname(Name.HEAT_WATER);
			currentBrewStep = getStepFromDb(stepName);
			if (currentBrewStep == null) {
				currentBrewStep = brewDb.addStep(brew.getId(), stepName,
						"Heat water to " + recipe.getMashingTemperature() + "°C");
			}
			if (!currentBrewStep.isFinished()) {
				System.out.println("start heating");
				mashingSystem.heat(recipe.getMashingTemperature());
				brewDb.complete(currentBrewStep);
			}

			// add malts
			stepName = stepnames.stepname(Name.ADD_MALTS);
			currentBrewStep = getStepFromDb(stepName);
			if (currentBrewStep == null) {
				currentBrewStep = brewDb.addStep(brew.getId(), stepName, "Add malts");
			}
			if (!currentBrewStep.isFinished()) {
				System.out.println("add malts");
				mashingSystem.addMalts();
				brewDb.complete(currentBrewStep);
			}

			// do the rests
			int count = 0;
			for (Rest rest : recipe.getRests()) {
				stepName = stepnames.stepname(Name.HEAT_FOR_REST);
				currentBrewStep = getStepFromDb(stepName);
				if (currentBrewStep == null) {
					currentBrewStep = brewDb.addStep(brew.getId(), stepName,
							"Heat to " + rest.getTemperature() + "°C for rest " + count);
				}
				if (!currentBrewStep.isFinished()) {
					System.out.println("heating for rest " + count);
					mashingSystem.heat(rest.getTemperature());
					brewDb.complete(currentBrewStep);
				}

				stepName = stepnames.stepname(Name.REST);
				currentBrewStep = getStepFromDb(stepName);
				if (currentBrewStep == null) {
					currentBrewStep = brewDb.addStep(brew.getId(), stepName, "Rest " + count);
				}
				if (!currentBrewStep.isFinished()) {
					System.out.println("doing rest " + count);
					mashingSystem.doRest(rest);
					brewDb.complete(currentBrewStep);
				}
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
		}
	}

	private BrewStep getStepFromDb(StepName stepName) {
		Iterator<BrewStep> it = brew.getSteps().iterator();
		while (it.hasNext()) {
			BrewStep step = it.next();
			if (step.getStepName().equals(stepName)) {
				return step;
			}
		}
		return null;
	}

	protected Brew getBrew() {
		return brew;
	}

}
