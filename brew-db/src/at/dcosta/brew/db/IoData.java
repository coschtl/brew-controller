package at.dcosta.brew.db;

import java.sql.Timestamp;

import at.dcosta.brew.io.ComponentType;

public class IoData {

	private ComponentType componentType;
	private String componentId;
	private Timestamp measureTime;
	private double value;

	public String getComponentId() {
		return componentId;
	}

	public ComponentType getComponentType() {
		return componentType;
	}

	public Timestamp getMeasureTime() {
		return measureTime;
	}

	public double getValue() {
		return value;
	}

	public IoData setComponentId(String componentId) {
		this.componentId = componentId;
		return this;
	}

	public IoData setComponentType(ComponentType componentType) {
		this.componentType = componentType;
		return this;
	}

	public IoData setMeasureTime(Timestamp measureTime) {
		this.measureTime = measureTime;
		return this;
	}

	public IoData setValue(double value) {
		this.value = value;
		return this;
	}

}
