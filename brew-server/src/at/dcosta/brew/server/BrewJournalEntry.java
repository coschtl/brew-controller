package at.dcosta.brew.server;

import javax.xml.bind.annotation.XmlElement;

public class BrewJournalEntry {

	@XmlElement
	private String timestamp;
	@XmlElement
	private String text;

	public BrewJournalEntry() {
		// BEAN
	}

	public BrewJournalEntry(String timestamp, String text) {
		super();
		this.timestamp = timestamp;
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
