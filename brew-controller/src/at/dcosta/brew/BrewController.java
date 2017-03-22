package at.dcosta.brew;

import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.recipe.Recipe;

public class BrewController implements Runnable {
	
	private final Configuration config;

	private boolean keepRunning = true;
	
	public BrewController(Configuration config) {
		this.config = config;
	}

	@Override
	public void run() {
		try {
			Recipe recipe = readRecipe();
			
			// journaling system!!!!
			// error alarm system!!
			
			// mashing
			MashingSystem mashingSystem = new MashingSystem(config);
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

	public void stop() {
		keepRunning = false;
	}

}
