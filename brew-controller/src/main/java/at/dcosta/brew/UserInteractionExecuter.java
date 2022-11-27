package at.dcosta.brew;

import at.dcosta.brew.db.InteractionDB;
import at.dcosta.brew.db.ManualAction;
import at.dcosta.brew.db.ManualAction.Type;
import at.dcosta.brew.io.Relay;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.util.StoppableRunnable;
import at.dcosta.brew.util.ThreadUtil;

public class UserInteractionExecuter implements StoppableRunnable {

	private boolean keepRunning;
	private final InteractionDB interactionDB;
	private final PauseHandler pauseHandler;

	public UserInteractionExecuter(PauseHandler pauseHandler) {
		this.pauseHandler = pauseHandler;
		interactionDB = new InteractionDB();
	}

	@Override
	public void abort() {
		keepRunning = false;
	}

	@Override
	public boolean mustComplete() {
		return false;
	}

	@Override
	public void run() {
		keepRunning = true;
		GpioSubsystem gpioSubsystem = GpioSubsystem.getInstance();
		while (keepRunning) {
			for (ManualAction action : interactionDB.getUnprocessedActions()) {
				System.out.println(action);
				if ("system".equals(action.getTarget())) {
					if (action.getType() == Type.PAUSE) {
						pauseHandler.startPause();
					} else if (action.getType() == Type.RESUME) {
						pauseHandler.stopPause();
					}
					interactionDB.setProcessed(action);
					continue;
				}
				Relay relay = gpioSubsystem.getRelayById(action.getTarget());
				if (relay == null) {
					System.out.println("No actor '" + action.getTarget() + "' found.");
					continue;
				}

				if (action.getType() == Type.SWITCH_OFF) {
					relay.off();
					relay.setControlManually(true);
				} else if (action.getType() == Type.SWITCH_ON) {
					relay.on();
					relay.setControlManually(true);
				} else if (action.getType() == Type.SWITCH_TO_AUTOMATIC) {
					relay.setControlManually(false);
				}
				interactionDB.setProcessed(action);
			}
			ThreadUtil.sleepMillis(100);
		}
	}

}
