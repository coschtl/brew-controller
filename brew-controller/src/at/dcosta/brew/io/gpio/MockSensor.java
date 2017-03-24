package at.dcosta.brew.io.gpio;

import at.dcosta.brew.io.Sensor;

public class MockSensor implements Sensor {

	private final String id;
	private final String scale;
	private double value = 20.1d;

	public MockSensor(String id, String scale) {
		this.id = id;
		this.scale = scale;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String getScale() {
		return scale;
	}

	@Override
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
