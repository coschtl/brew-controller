package at.dcosta.brew.server;

import java.io.Serializable;
import java.util.Date;

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
	private Date addedOn;
	
	@XmlElement
	private int  brewCount;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Date getAddedOn() {
		return addedOn;
	}

	public void setAddedOn(Date addedOn) {
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
