package at.dcosta.brew.io.gpio;

import at.dcosta.brew.io.AbstractSensor;
import at.dcosta.brew.io.ComponentType;

public class MockSensor extends AbstractSensor {

	private ComponentType componentType;
	private final String id;
	private final String scale;
	private double value = 20.1d;
	private long startTime;

	public MockSensor(ComponentType componentType, String id, String scale) {
		this.componentType = componentType;
		this.id = id;
		this.scale = scale;
	}

	@Override
	public double doGetValue() {
		if (startTime == 0) {
			startTime = System.currentTimeMillis();
		}
		// add 0.5 per Second
		double aktValue = value + (System.currentTimeMillis() - startTime) / 2000;
		// System.out.println(Thread.currentThread().getName() + ": " +
		// aktValue);
		return aktValue;
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
