package at.dcosta.brew.io.w1;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Device;
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

	public Collection<at.dcosta.brew.io.Sensor> getAvailableTemperatureSensors() {
		return Collections.unmodifiableCollection(sensors.values());
	}

	public Sensor getTemperatureSensor(String address) {
		return sensors.get(address);
	}

	public void scanW1Bus() throws IOException {
		W1Master w1Master = new W1Master();
		System.out.println("Scanning W1 Bus:\n");
		for (W1Device device : w1Master.getDevices()) {
			System.out.println("ID=" + device.getId().trim());
			System.out.println("  familyID=" + device.getFamilyId());
			System.out.println("  name=" + device.getName().trim());
			System.out.println("  java class=" + device.getClass().getName());
			System.out.println("  value=" + device.getValue());
			System.out.println();
		}
	}

}
