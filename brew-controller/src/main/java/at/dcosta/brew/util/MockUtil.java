package at.dcosta.brew.util;

import java.util.HashSet;
import java.util.Set;

import at.dcosta.brew.Configuration;
import at.dcosta.brew.io.gpio.MockSensor;

public class MockUtil {

	private static MockUtil INSTANCE = new MockUtil();

	public static MockUtil instance() {
		return INSTANCE;
	}

	private final Set<MockSensor> sensors;
	private final boolean mockPi;

	private MockUtil() {
		sensors = new HashSet<>();
		mockPi = Configuration.getInstance().isMockPi();
	}

	public void addSensor(MockSensor sensor) {
		sensors.add(sensor);
	}

	public boolean isMockPi() {
		return mockPi;
	}

	public void setIncrementValuePerSecond(double incrementPerSecond) {
		if (!isMockPi()) {
			return;
		}
		for (MockSensor sensor : sensors) {
			sensor.setIncrementValuePerSecond(incrementPerSecond);
		}
	}
}
