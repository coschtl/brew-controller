package at.dcosta.brew.db;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class ManualAction implements Serializable {

	public static enum Type {
		PAUSE("pause"), RESUME("resume"), SWITCH_ON("switchOn"), SWITCH_OFF("switchOff"), SWITCH_TO_AUTOMATIC(
				"switchAuto");

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

	private static final long serialVersionUID = 1L;

	private Timestamp time, executionTime;
	private Type type;
	private String target, arguments;
	private int id;

	public String getArguments() {
		return arguments;
	}

	public Timestamp getExecutionTime() {
		return executionTime;
	}

	public int getId() {
		return id;
	}

	public String getTarget() {
		return target;
	}

	public Timestamp getTime() {
		return time;
	}

	public Type getType() {
		return type;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public void setExecutionTime(Timestamp executionTime) {
		this.executionTime = executionTime;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(type).append(" ").append(target);
		if (arguments != null) {
			b.append(" (").append(arguments).append(")");
		}
		return b.toString();
	}

}
