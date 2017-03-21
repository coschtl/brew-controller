package at.dcosta.brew.io;

public interface TemperatureSensor extends Sensor {
	
	public String getID();
	
	public double getTemperature();

}
