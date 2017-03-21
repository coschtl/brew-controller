package at.dcosta.brew;

public class BrewController implements Runnable {
	
	private boolean keepRunning = true;

	@Override
	public void run() {
		while (keepRunning) {
			
			sleep(100);
		}
		
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
