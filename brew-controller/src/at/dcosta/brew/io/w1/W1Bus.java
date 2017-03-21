package at.dcosta.brew.io.w1;

import java.util.ArrayList;
import java.util.List;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Master;

public class W1Bus {

	private W1Master w1Master;

	public W1Bus() {
		w1Master = new W1Master();
	}

	public List<at.dcosta.brew.io.TemperatureSensor> getAvailableSensors() {
		List<at.dcosta.brew.io.TemperatureSensor> sensors = new ArrayList<>();
		for (TemperatureSensor device : w1Master.getDevices(TemperatureSensor.class)) {
			sensors.add(new W1TemperatureSensor(device));
		}
		return sensors;
	}

}
