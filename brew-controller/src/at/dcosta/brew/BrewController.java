package at.dcosta.brew;

import at.dcosta.brew.com.NotificationService;
import at.dcosta.brew.com.NotificationType;
import at.dcosta.brew.db.Brew;
import at.dcosta.brew.db.BrewDB;
import at.dcosta.brew.db.Cookbook;
import at.dcosta.brew.db.Journal;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.recipe.InfusionRecipe;
import at.dcosta.brew.recipe.Rest;
import at.dcosta.brew.util.StoppableRunnable;
import at.dcosta.brew.util.ThreadUtil;

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

	public void doRest(Rest rest, MashingSystem mashingSystem) {
		long restEnd = System.currentTimeMillis() + rest.getMinutes() * ThreadUtil.ONE_MINUTE;
		long aktRestTimeMinutes = 0;
		double minTemp = mashingSystem.getTemperature()
				- Configuration.getInstance().getDouble(Configuration.MASHING_TEMPERATURE_MAX_DROP);
		while (System.currentTimeMillis() < restEnd) {
			ThreadUtil.sleepMinutes(1);
			aktRestTimeMinutes++;
			if (aktRestTimeMinutes == 5) {
				mashingSystem.startStirrer();
			} else if (aktRestTimeMinutes == 6) {
				aktRestTimeMinutes = 0;
				mashingSystem.stoptStirrer();
				if (mashingSystem.getTemperature() < minTemp) {
					mashingSystem.heatToTemperature(rest.getTemperature(),
							(restEnd - System.currentTimeMillis()) / ThreadUtil.ONE_MINUTE);
				}
			}
		}
	}

	@Override
	public boolean mustComplete() {
		return true;
	}

	@Override
	public void run() {
		try {
			InfusionRecipe recipe = (InfusionRecipe) new Cookbook().getEntryById(brew.getCookbookEntryId()).getRecipe();
			// mashing
			MashingSystem mashingSystem = new MashingSystem(notificationService);
			// heat to the mashing temperature
			mashingSystem.heat(recipe.getMashingTemperature(), false);

			// add malts
			mashingSystem.addMalts();

			// do the rests
			for (Rest rest : recipe.getRests()) {
				mashingSystem.heat(rest.getTemperature(), true);
				doRest(rest, mashingSystem);
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

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// ignore
		}
	}

}
