package at.dcosta.brew.io.gpio;

import at.dcosta.brew.io.AbstractSensor;
import at.dcosta.brew.io.ComponentType;

public class MockSensor extends AbstractSensor {

	private ComponentType componentType;
	private final String id;
	private final String scale;
	private double value = 20.1d;
	private long lastMeasureTime;
	private double incrementPerSecond;

	public MockSensor(ComponentType componentType, String id, String scale) {
		this.componentType = componentType;
		this.id = id;
		this.scale = scale;
		this.incrementPerSecond = 0.5;
		this.lastMeasureTime = System.currentTimeMillis();
	}

	@Override
	public double doGetValue() {
		// add <incrementPerSecond> per Second
		value = value + (System.currentTimeMillis() - lastMeasureTime) / 1000 * incrementPerSecond*Math.random();
		lastMeasureTime = System.currentTimeMillis();
		return value;
	}

	@Override
	public ComponentType getComponentType() {
		return componentType;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String getScale() {
		return scale;
	}

	public void setIncrementValuePerSecond(double incrementPerSecond) {
		this.incrementPerSecond = incrementPerSecond;
	}

	public MockSensor setValue(double value) {
		this.value = value;
		return this;
	}

	@Override
	protected void startCollectingSensorData() {
		if (ComponentType.DUMMY != getComponentType()) {
			super.startCollectingSensorData();
		}
	}

}
