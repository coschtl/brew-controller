package at.dcosta.brew.io;

public interface Sensor {

	public void start();

	public void stop();

	String getID();

	String getScale();

	double getValue();

}
