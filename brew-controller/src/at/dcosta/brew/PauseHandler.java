package at.dcosta.brew;

import at.dcosta.brew.db.Journal;
import at.dcosta.brew.util.ThreadUtil;

public class PauseHandler {

	private static final long SLEEP_TIME = 100;
	private final int brewId;
	private final Journal journal;
	private final HeatingSystem heatingSystem;
	private boolean paused;

	public PauseHandler(int brewId, Journal journal, HeatingSystem heatingSystem) {
		this.brewId = brewId;
		this.journal = journal;
		this.heatingSystem = heatingSystem;
	}

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
		heatingSystem.switchHeatersOff();
		journal.addEntry(brewId, null, "startPause");
	}

	public void stopPause() {
		paused = false;
		journal.addEntry(brewId, null, "stoptPause");
	}

}
