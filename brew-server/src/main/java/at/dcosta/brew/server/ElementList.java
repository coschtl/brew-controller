package at.dcosta.brew.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class ElementList<T> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@XmlElement
	private List<T> entries;
	
	public ElementList() {
		entries = new ArrayList<>();
	}
	
	public void addEntry(T entry) {
		entries.add(entry);
	}
	
	public List<T> getEntries() {
		return entries;
	}

}
