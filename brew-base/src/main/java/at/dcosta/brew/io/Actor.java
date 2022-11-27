package at.dcosta.brew.io;

public interface Actor {
	String getID();

	boolean isControlledAutomatically();

	void setControlManually(boolean controlledManually);
}
