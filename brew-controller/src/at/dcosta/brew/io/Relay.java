package at.dcosta.brew.io;

public interface Relay extends Actor {

	public boolean isOn();

	public void off();

	public void on();
}
