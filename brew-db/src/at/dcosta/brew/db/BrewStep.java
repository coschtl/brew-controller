package at.dcosta.brew.db;

import java.sql.Timestamp;

public class BrewStep {

	public static enum Name {
		HEAT_WATER, ADD_MALTS, HEAT_FOR_REST, REST, BOILING, ADD_HOP, LAUTHERING, LAUTHERING_REST, WHIRLPOOL, COOL;
	}
	
	public static class StepName {
		
		private final String name; 
		
		public StepName(Name name, int instanceNumber) {
			this.name = name.toString() + "_" + instanceNumber;
		}
		public StepName(String stepName) {
			this.name = stepName;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}

	private int id;
	private Brew brew;
	private StepName stepName;
	private String description;
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

	public StepName getStepName() {
		return stepName;
	}

	public BrewStep setStepName(StepName stepName) {
		this.stepName = stepName;
		return this;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public String getDescription() {
		return description;
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

	public BrewStep setDescription(String description) {
		this.description = description;
		return this;
	}

}
