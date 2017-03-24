package at.dcosta.brew;

import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.recipe.Recipe;

public class BrewController implements Runnable {

	private boolean keepRunning = true;

	@Override
	public void run() {
		try {
			Recipe recipe = readRecipe();

			// journaling system!!!!
			// error alarm system!!

			// mashing
			MashingSystem mashingSystem = new MashingSystem();
			// (1) heat to the mashing temperature
			mashingSystem.heat(recipe.getMashingTemperature());

			// (2) add malts
			// (3) heat and
			while (keepRunning) {

				sleep(100);
			}
		} finally {
			GpioSubsystem.getInstance().shutdown();
		}
	}

	public void stop() {
		keepRunning = false;
	}

	private Recipe readRecipe() {
		// TODO Auto-generated method stub
		return null;
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// ignore
		}
	}

}
