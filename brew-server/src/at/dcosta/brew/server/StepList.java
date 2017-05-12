package at.dcosta.brew.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class StepList implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@XmlElement
	private List<Step> entries;
	
	public StepList() {
		entries = new ArrayList<>();
	}
	
	public void addEntry(Step entry) {
		entries.add(entry);
	}
	
	public List<Step> getEntries() {
		return entries;
	}

}
