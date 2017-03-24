package at.dcosta.brew.io.gpio;

import at.dcosta.brew.io.Actor;

public interface Relay extends Actor {

	public boolean isOn();

	public void off();

	public void on();
}
