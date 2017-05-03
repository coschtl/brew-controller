package at.dcosta.brew.server;

import javax.xml.bind.annotation.XmlElement;

public class Relay {

	@XmlElement
	private final boolean on;

	@XmlElement
	private final String id;

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

}
