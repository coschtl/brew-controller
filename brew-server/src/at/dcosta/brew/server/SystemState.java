package at.dcosta.brew.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class SystemState implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement
	private List<Relay> heaters;

	@XmlElement
	private List<Sensor> temperatures;

	@XmlElement
	private Relay stirrer;

	@XmlElement
	private Double rotation;

	@XmlElement
	private Sensor avgTemp;

	@XmlElement
	private String timeString;

	@XmlElement
	private boolean brewFinished;

	public SystemState() {
		heaters = new ArrayList<>();
		temperatures = new ArrayList<>();
	}

	public void addHeater(Relay heater) {
		heaters.add(heater);
	}

	public void addTemperature(Sensor temperature) {
		temperatures.add(temperature);
	}

	public Sensor getAvgTemp() {
		return avgTemp;
	}

	public List<Relay> getHeaters() {
		return heaters;
	}

	public Double getRotation() {
		return rotation;
	}

	public Relay getStirrer() {
		return stirrer;
	}

	public List<Sensor> getTemperatures() {
		return temperatures;
	}

	public String getTimeString() {
		return timeString;
	}

	public boolean isBrewFinished() {
		return brewFinished;
	}

	public void setAvgTemp(double avgTemp) {
		this.avgTemp = new Sensor("Average", avgTemp);
	}

	public void setBrewFinished(boolean brewFinished) {
		this.brewFinished = brewFinished;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	public void setStirrerRunning(Relay stirrer) {
		this.stirrer = stirrer;
	}

	public void setTimeString(String timeString) {
		this.timeString = timeString;
	}
}
