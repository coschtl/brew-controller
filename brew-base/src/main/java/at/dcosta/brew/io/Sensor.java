package at.dcosta.brew.io;

public interface Sensor {

	ComponentType getComponentType();

	String getID();

	String getScale();

	double getValue();

	void logValue();

	void setCorrectionValue(double correctionValue);

	void switchOff();

}
