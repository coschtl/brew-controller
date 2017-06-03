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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isBrewRunning() {
		return brewRunning;
	}

	public void setBrewRunning(boolean brewRunning) {
		this.brewRunning = brewRunning;
	}

	public String getRecipe() {
		return recipe;
	}

	public void setRecipe(String recipe) {
		this.recipe = recipe;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getAddedOn() {
		return addedOn;
	}

	public void setAddedOn(String addedOn) {
		this.addedOn = addedOn;
	}

	public int getBrewCount() {
		return brewCount;
	}

	public void setBrewCount(int brewCount) {
		this.brewCount = brewCount;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
