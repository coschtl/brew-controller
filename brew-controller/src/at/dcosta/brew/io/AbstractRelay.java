package at.dcosta.brew.io;

import at.dcosta.brew.db.IOData;
import at.dcosta.brew.db.IOLog;

public abstract class AbstractRelay implements Relay {

	private final IOLog ioLog;
	private final String id;
	private boolean controlledManually;

	public AbstractRelay(String name, int pi4jPinNumber) {
		this.id = name + "_GPIO_" + pi4jPinNumber;
		ioLog = new IOLog();
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public boolean isControlledAutomatically() {
		return !controlledManually;
	}

	@Override
	public void off() {
		log(0);
	}

	@Override
	public void on() {
		log(1);
	}

	@Override
	public void setControlManually(boolean controlledManually) {
		this.controlledManually = controlledManually;
	}

	private void log(int state) {
		ioLog.addEntry(new IOData().setComponentId(getID()).setComponentType(ComponentType.RELAY).setValue(state));
	}

}
