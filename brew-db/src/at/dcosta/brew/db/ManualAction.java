package at.dcosta.brew.db;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class ManualAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static enum Type {
		SWITCH_ON("switchOn"), SWITCH_OFF("switchOff"), SWITCH_TO_AUTOMATIC ("switchAuto"), ADD_TIME("addTime");

		private static final Map<String, Type> BY_ACTION;
		static {
			BY_ACTION = new HashMap<>();
			for (Type type : Type.values()) {
				BY_ACTION.put(type.getAction(), type);
			}
		}

		public static Type fromAction(String action) {
			return BY_ACTION.get(action);
		}

		private final String action;

		private Type(String action) {
			this.action = action;
		}

		public String getAction() {
			return action;
		}
	}

	private Timestamp time, executionTime;
	private Type type;
	private String target, arguments;
	private int id, durationMinutes;

	public ManualAction() {
		this.durationMinutes = -1;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public Timestamp getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(Timestamp executionTime) {
		this.executionTime = executionTime;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(type).append(" ").append(target);
		if (arguments != null) {
			b.append(" (").append(arguments).append(")");
		}
		if (durationMinutes > 0) {
			b.append(", duration=").append(durationMinutes).append(" minutes");
		}
		return b.toString();
	}

}
