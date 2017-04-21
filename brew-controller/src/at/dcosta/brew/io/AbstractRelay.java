package at.dcosta.brew.io;

import at.dcosta.brew.db.IoData;
import at.dcosta.brew.db.IoLog;

public abstract class AbstractRelay implements Relay {

	private final IoLog ioLog;
	private final String id;

	public AbstractRelay(String name, int pi4jPinNumber) {
		this.id = name + "_GPIO_" + pi4jPinNumber;
		ioLog = new IoLog();
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public void off() {
		log(0);
	}

	@Override
	public void on() {
		log(1);
	}

	private void log(int state) {
		ioLog.addEntry(new IoData().setComponentId(getID()).setComponentType(ComponentType.RELAY).setValue(state));
	}

}
