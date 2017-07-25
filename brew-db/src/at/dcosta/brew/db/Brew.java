package at.dcosta.brew.db;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import at.dcosta.brew.BrewStatus;

public class Brew {

	private int id;
	private final int cookbookEntryId;
	private Timestamp startTime, endTime;
	private BrewStatus brewStatus;
	private Set<BrewStep> steps;

	public Brew(int cookbookEntryId) {
		this.cookbookEntryId = cookbookEntryId;
		steps = new LinkedHashSet<>();
	}

	public void addStep(BrewStep step) {
		step.setBrew(this);
		steps.add(step);
	}

	public BrewStatus getBrewStatus() {
		return brewStatus;
	}

	public int getCookbookEntryId() {
		return cookbookEntryId;
	}

	public BrewStep getCurrentStep() {
		BrewStep step = null;
		Iterator<BrewStep> it = steps.iterator();
		while (it.hasNext()) {
			step = it.next();
		}
		return step;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public int getId() {
		return id;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public Set<BrewStep> getSteps() {
		return steps;
	}

	public Brew setBrewStatus(BrewStatus brewStatus) {
		this.brewStatus = brewStatus;
		return this;
	}

	public Brew setEndTime(Timestamp endTime) {
		this.endTime = endTime;
		return this;
	}

	public Brew setId(int id) {
		this.id = id;
		return this;
	}

	public Brew setStartTime(Timestamp startTime) {
		this.startTime = startTime;
		return this;
	}

	@Override
	public String toString() {
		return String.valueOf(id);

	}

}
