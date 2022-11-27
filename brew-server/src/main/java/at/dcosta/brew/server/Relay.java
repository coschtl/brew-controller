package at.dcosta.brew.server;

import javax.xml.bind.annotation.XmlElement;

public class Relay {

	@XmlElement
	private boolean on;

	@XmlElement
	private String id;

	public Relay() {
		// needed for jaxb
	}

	public Relay(String id, boolean on) {
		this.id = id;
		this.on = on;
	}

	public String getId() {
		return id;
	}

	public boolean isOn() {
		return on;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

}
