package at.dcosta.brew;

import static at.dcosta.brew.Configuration.STIRRER_MOTOR_PIN;
import static at.dcosta.brew.Configuration.STIRRER_OVERTIME_SECONDS;

import at.dcosta.brew.io.gpio.GpioSubsystem;
import at.dcosta.brew.io.gpio.Relay;
import at.dcosta.brew.util.StoppableRunnable;
import at.dcosta.brew.util.ThreadManager;

public class Stirrer {

	private final Relay stirrerMotor;
	private final long overtimeMillis;

	public Stirrer() {
		GpioSubsystem gpioSubsystem = GpioSubsystem.getInstance();
		Configuration config = Configuration.getInstance();
		stirrerMotor = gpioSubsystem.getRelay("Stirrer Motor", config.getInt(STIRRER_MOTOR_PIN));
		overtimeMillis = 1000l * config.getInt(STIRRER_OVERTIME_SECONDS);
	}

	public void start() {
		stirrerMotor.on();
	}

	public void stop() {

		StoppableRunnable runnable = new StoppableRunnable() {

			@Override
			public void abort() {
				stirrerMotor.off();
			}

			@Override
			public boolean mustComplete() {
				return false;
			}

			@Override
			public void run() {
				try {
					Thread.sleep(overtimeMillis);
				} catch (InterruptedException e) {
					// ignore
				}
				abort();
			}
		};
		ThreadManager.getInstance().newThread(runnable).start();
	}
}
