package at.dcosta.brew.server;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class BrewJournal {
	@XmlElement
	private List<BrewJournalEntry> entries;

	public BrewJournal() {
		entries = new ArrayList<>();
	}

	public void addBrewJournalEntry(BrewJournalEntry entry) {
		entries.add(entry);
	}

}
