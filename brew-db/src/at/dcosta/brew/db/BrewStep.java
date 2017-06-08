package at.dcosta.brew.db;

import java.sql.Timestamp;

public class BrewStep {

	public static enum Name {
		HEAT_WATER, ADD_MALTS, HEAT_FOR_REST, REST, BOILING, ADD_HOP, LAUTHERING, LAUTHERING_REST, WHIRLPOOL, COOL;
	}

	public static class StepName {

		private final Name name;
		private final int instanceNumber;

		public StepName(String stepName) {
			int pos = stepName.lastIndexOf('_');
			name = Name.valueOf(stepName.substring(0, pos));
			instanceNumber = Integer.parseInt(stepName.substring(pos + 1));
		}

		public StepName(Name name, int instanceNumber) {
			this.name = name;
			this.instanceNumber = instanceNumber;
		}

		public Name getName() {
			return name;
		}

		public int getInstanceNumber() {
			return instanceNumber;
		}

		@Override
		public String toString() {
			return new StringBuilder().append(name.toString()).append('_').append(instanceNumber).toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + instanceNumber;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StepName other = (StepName) obj;
			if (instanceNumber != other.instanceNumber)
				return false;
			if (name != other.name)
				return false;
			return true;
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
	
	public boolean isFinished() {
		return startTime != null && endTime != null;
	}
	
	public boolean isActive() {
		return startTime != null && endTime == null;
	}
	
	@Override
	public String toString() {
		return getStepName().toString();
	}

}
