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

	public UserInteractionExecuter( ) {
		interactionDB = new InteractionDB();
	}

	@Override
	public void run() {
		keepRunning = true;
		GpioSubsystem gpioSubsystem = GpioSubsystem.getInstance();
		while (keepRunning) {
			for (ManualAction action : interactionDB.getUnprocessedActions()) {
				System.out.println(action);
				Relay relay = gpioSubsystem.getRelayById(action.getTarget());
				if (relay == null) {
					System.out.println("No actor " + action.getTarget() + " found.");
					continue;
				}
				
				boolean processed = false;
				if (action.getType() == Type.SWITCH_OFF) {
					relay.off();
					processed = true;
				} else  if (action.getType() == Type.SWITCH_ON) {
					relay.on();
					processed = true;
				} else  if (action.getType() == Type.SWITCH_TO_AUTOMATIC) {
					relay.setControlManually(0);
					interactionDB.setProcessed(action);
				}
				if (processed) {
					relay.setControlManually(action.getDurationMinutes() * ThreadUtil.ONE_MINUTE);
					interactionDB.setProcessed(action);
				}
			}
			ThreadUtil.sleepMillis(100);
		}
	}

	@Override
	public void abort() {
		keepRunning = false;
	}

	@Override
	public boolean mustComplete() {
		return false;
	}

}
