package at.dcosta.brew.server;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

public class Recipe implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement
	private int id;

	@XmlElement
	private String name;

	@XmlElement
	private String recipe;

	@XmlElement
	private String source;

	@XmlElement
	private String addedOn;

	@XmlElement
	private int brewCount;

	@XmlElement
	private boolean brewRunning;

	public String getAddedOn() {
		return addedOn;
	}

	public int getBrewCount() {
		return brewCount;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getRecipe() {
		return recipe;
	}

	public String getSource() {
		return source;
	}

	public boolean isBrewRunning() {
		return brewRunning;
	}

	public void setAddedOn(String addedOn) {
		this.addedOn = addedOn;
	}

	public void setBrewCount(int brewCount) {
		this.brewCount = brewCount;
	}

	public void setBrewRunning(boolean brewRunning) {
		this.brewRunning = brewRunning;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRecipe(String recipe) {
		this.recipe = recipe;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
