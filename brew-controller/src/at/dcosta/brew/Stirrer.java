package at.dcosta.brew;

import static at.dcosta.brew.Configuration.STIRRER_MOTOR_PIN;
import static at.dcosta.brew.Configuration.STIRRER_OVERTIME_SECONDS;

import at.dcosta.brew.io.Relay;
import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.util.StoppableRunnable;
import at.dcosta.brew.util.ThreadManager;
import at.dcosta.brew.util.ThreadUtil;

public class Stirrer {

	private final Relay stirrerMotor;
	private final long overtimeMillis;
	private Thread delayedStopper;

	public Stirrer() {
		GpioSubsystem gpioSubsystem = GpioSubsystem.getInstance();
		Configuration config = Configuration.getInstance();
		stirrerMotor = gpioSubsystem.getRelay("Stirrer Motor", config.getInt(STIRRER_MOTOR_PIN));
		overtimeMillis = ThreadUtil.ONE_SECOND * config.getInt(STIRRER_OVERTIME_SECONDS);
	}

	public void start() {
		stirrerMotor.on();
	}

	public void stop() {
		if (delayedStopper != null && delayedStopper.isAlive()) {
			delayedStopper.interrupt();
		}
	}

	public void stopDelayed() {
		StoppableRunnable runnable = new StoppableRunnable() {

			boolean aborted;

			@Override
			public void abort() {
				aborted = true;
				if (stirrerMotor.isOn()) {
					stirrerMotor.off();
				}
			}

			@Override
			public boolean mustComplete() {
				return false;
			}

			@Override
			public void run() {
				int runs = (int) (overtimeMillis / 100);
				for (int i = 0; i < runs && !aborted; i++) {
					ThreadUtil.sleepMillis(100);
				}
				abort();
			}
		};
		delayedStopper = ThreadManager.getInstance().newThread(runnable, "stirrer overtime");
		delayedStopper.start();
	}
}
