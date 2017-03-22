package at.dcosta.brew.io.w1;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Master;

import at.dcosta.brew.io.Sensor;

public class W1Bus {

	private final Map<String, Sensor> sensors;

	public W1Bus() {
		W1Master w1Master = new W1Master();
		sensors = new HashMap<>();
		for (TemperatureSensor device : w1Master.getDevices(TemperatureSensor.class)) {
			W1TemperatureSensor sensor = new W1TemperatureSensor(device);
			sensors.put(sensor.getID(), sensor);
		}
	}
	
	public Sensor getTemperatureSensor(String address) {
		return sensors.get(address);
	}

	public Collection<at.dcosta.brew.io.Sensor> getAvailableSensors() {
		return Collections.unmodifiableCollection(sensors.values());
	}

}
