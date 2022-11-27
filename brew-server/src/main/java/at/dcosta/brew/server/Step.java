package at.dcosta.brew.server;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

public class Step implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement
	private String id;

	@XmlElement
	private String name;
	
	@XmlElement
	private boolean active;
	
	@XmlElement
	private boolean finished;
	
	@XmlElement
	private String headerText;
	
	@XmlElement
	private String description;

	public Step() {
		// needed for jaxb
	}

	public Step(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public String getHeaderText() {
		return headerText;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isFinished() {
		return finished;
	}

	public Step setActive(boolean active) {
		this.active = active;
		return this;
	}
	
	public Step setDescription(String description) {
		this.description = description;
		return this;
	}

	public Step setFinished(boolean finished) {
		this.finished = finished;
		return this;
	}

	public Step setHeaderText(String headerText) {
		this.headerText = headerText;
		return this;
	}

	public Step setId(String id) {
		this.id = id;
		return this;
	}

	public Step setName(String name) {
		this.name = name;
		return this;
	}

}
