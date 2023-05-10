package at.dcosta.brew.io;

public interface Sensor {

	ComponentType getComponentType();

	String getID();

	String getScale();

	double getValue();

	void logValue();

	void setCorrectionValue(double correctionValue);
	void setMinValidValue(double minValidValue);
	void setMaxValidValue(double maxValidValue);

	void switchOff();

}
