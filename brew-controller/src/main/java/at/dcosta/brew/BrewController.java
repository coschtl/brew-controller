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
			journal.addEntry(brew.getId(), (Name) null, "brewStarting", recipe.getName());

			switch (brew.getBrewStatus()) {
			case SCHEDULED:
				journal.addEntry(brew.getId(), (Name) null, "startMashing");
				brew.setBrewStatus(BrewStatus.MASHING);
				brewDb.persist(brew);
				doMashing(recipe);
				break;
			case MASHING:
				journal.addEntry(brew.getId(), (Name) null, "continueMashing");
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
			MashingSystem mashingSystem = new MashingSystem(brew.getId(), brewDb, notificationService, journal);
			boolean isNewStep;

			// heat to the mashing temperature
			stepName = stepnames.stepname(Name.HEAT_WATER);
			currentBrewStep = getStepFromDb(stepName);
			isNewStep = false;
			if (currentBrewStep == null) {
				isNewStep = true;
				currentBrewStep = brewDb.addStep(brew.getId(), stepName,
						"Heat water to " + recipe.getMashingTemperature() + "°C");
			}
			if (!currentBrewStep.isFinished()) {
				journal.addEntry(brew.getId(), Name.HEAT_WATER,
						isNewStep ? "startHeatingWater" : "continueHeatingWater");
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
				mashingSystem.addMalts();
				brewDb.complete(currentBrewStep);
			}

			// do the rests
			int count = 1;
			for (Rest rest : recipe.getRests()) {
				stepName = stepnames.stepname(Name.HEAT_FOR_REST);
				currentBrewStep = getStepFromDb(stepName);
				isNewStep = false;
				if (currentBrewStep == null) {
					isNewStep = true;
					currentBrewStep = brewDb.addStep(brew.getId(), stepName,
							"Heat to " + rest.getTemperature() + "°C for rest " + count);
				}
				if (!currentBrewStep.isFinished()) {
					journal.addEntry(brew.getId(), Name.HEAT_WATER,
							isNewStep ? "startHeatingForRest" : "continueHeatingForRest", rest.getTemperature(), count);
					mashingSystem.heat(rest.getTemperature());
					brewDb.complete(currentBrewStep);
				}

				stepName = stepnames.stepname(Name.REST);
				currentBrewStep = getStepFromDb(stepName);
				isNewStep = false;
				if (currentBrewStep == null) {
					isNewStep = true;
					currentBrewStep = brewDb.addStep(brew.getId(), stepName, "Rest " + count);
				}
				if (!currentBrewStep.isFinished()) {
					journal.addEntry(brew.getId(), Name.HEAT_WATER, isNewStep ? "startDoingRest" : "continueDoingRest",
							count);
					mashingSystem.doRest(rest);
					brewDb.complete(currentBrewStep);
				}
				count++;
			}
			notificationService.sendNotification(NotificationType.INFO, "maltingFinished");
		} catch (ClassCastException e) {
			notificationService.sendNotification(NotificationType.ERROR, "recipeImplementationMissing");
		} catch (BrewException e) {
			e.printStackTrace();
			notificationService.sendNotification(e);
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
