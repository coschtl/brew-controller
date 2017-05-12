package at.dcosta.brew.server;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

public class Step implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement
	private String id;

	@XmlElement
	private String name;

	public Step() {
		// needed for jaxb
	}

	public Step(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setText(String name) {
		this.name = name;
	}

}
