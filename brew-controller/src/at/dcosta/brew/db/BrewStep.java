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

	public void setBrew(Brew brew) {
		this.brew = brew;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

}
