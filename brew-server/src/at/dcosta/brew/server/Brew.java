package at.dcosta.brew.server;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

public class Brew implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@XmlElement
	private int id;
	
	@XmlElement
	private String date;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	

}
