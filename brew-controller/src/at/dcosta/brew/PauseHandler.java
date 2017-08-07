package at.dcosta.brew;

import at.dcosta.brew.util.ThreadUtil;

public class PauseHandler {

	private static final long SLEEP_TIME = 100;
	private boolean paused;

	public long handlePause() {
		long sleepTime = 0;
		while (paused) {
			ThreadUtil.sleepMillis(SLEEP_TIME);
			sleepTime += SLEEP_TIME;
		}
		return sleepTime;
	}

	public void startPause() {
		paused = true;
	}

	public void stopPause() {
		paused = false;
	}

}
