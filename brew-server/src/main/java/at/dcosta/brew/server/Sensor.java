package at.dcosta.brew.server;

import javax.xml.bind.annotation.XmlElement;

public class Sensor {

	@XmlElement
	private double value;
	
	@XmlElement
	private String id;

	public Sensor() {
		// needed for jaxb
	}

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

	public void setId(String id) {
		this.id = id;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
