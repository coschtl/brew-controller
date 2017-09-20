package at.dcosta.brew.db;

import java.sql.Timestamp;

public class JournalEntry {

	private int brewId;
	private String step;
	private String textId;
	private String text;
	private Timestamp timestamp;

	public int getBrewId() {
		return brewId;
	}

	public String getStep() {
		return step;
	}

	public String getText() {
		return text;
	}

	public String getTextId() {
		return textId;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setBrewId(int brewId) {
		this.brewId = brewId;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTextId(String textId) {
		this.textId = textId;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

}
