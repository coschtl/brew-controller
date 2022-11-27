package at.dcosta.brew.db;

import java.sql.Timestamp;

import at.dcosta.brew.io.ComponentType;

public class IOData {

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

	public IOData setComponentId(String componentId) {
		this.componentId = componentId;
		return this;
	}

	public IOData setComponentType(ComponentType componentType) {
		this.componentType = componentType;
		return this;
	}

	public IOData setMeasureTime(Timestamp measureTime) {
		this.measureTime = measureTime;
		return this;
	}

	public IOData setValue(double value) {
		this.value = value;
		return this;
	}

}
