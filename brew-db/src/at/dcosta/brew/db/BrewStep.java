package at.dcosta.brew.db;

import java.sql.Timestamp;

public class BrewStep {

	private int id;
	private Brew brew;
	private String stepName;
	private Timestamp startTime, endTime;

	public Brew getBrew() {
		return brew;
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

	public String getStepName() {
		return stepName;
	}

	public BrewStep setBrew(Brew brew) {
		this.brew = brew;
		return this;
	}

	public BrewStep setEndTime(Timestamp endTime) {
		this.endTime = endTime;
		return this;
	}

	public BrewStep setId(int id) {
		this.id = id;
		return this;
	}

	public BrewStep setStartTime(Timestamp startTime) {
		this.startTime = startTime;
		return this;
	}

	public BrewStep setStepName(String stepName) {
		this.stepName = stepName;
		return this;
	}

}
