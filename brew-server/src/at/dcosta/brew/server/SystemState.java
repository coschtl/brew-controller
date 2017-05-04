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
	private Double avgTemp;

	@XmlElement
	private String timeString;

	public SystemState() {
		heaters = new ArrayList<>();
		temperatures = new ArrayList<>();
	}

	public List<Relay> getHeaters() {
		return heaters;
	}

	public void addHeater(Relay heater) {
		heaters.add(heater);
	}

	public void addTemperature(Sensor temperature) {
		temperatures.add(temperature);
	}

	public Double getAvgTemp() {
		return avgTemp;
	}

	public Double getRotation() {
		return rotation;
	}

	public String getTimeString() {
		return timeString;
	}

	public void setAvgTemp(double avgTemp) {
		this.avgTemp = avgTemp;
	}

	public List<Sensor> getTemperatures() {
		return temperatures;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	public void setStirrerRunning(Relay stirrer) {
		this.stirrer = stirrer;
	}

	public Relay getStirrer() {
		return stirrer;
	}

	public void setTimeString(String timeString) {
		this.timeString = timeString;
	}

}
