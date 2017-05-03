package at.dcosta.brew.server;

import javax.xml.bind.annotation.XmlElement;

public class Sensor {

	@XmlElement
	private final double value;

	@XmlElement
	private final String id;

	public Sensor(String id, double value) {
		this.id = id;
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public double getValue() {
		return value;
	}

}
