package at.dcosta.brew.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import at.dcosta.brew.io.Sensor;
import at.dcosta.brew.io.gpio.MockSensor;

public class SensorUtil {

	public static enum SensorStatus {
		OK, ERROR, UNKNOWN;
	}

	public static class Value {
		private final double value;
		private SensorStatus sensorStatus;
		private final String error;

		public Value(double value, String error) {
			this.value = value;
			this.error = error;
			this.sensorStatus = error == null || error.trim().isEmpty() ? SensorStatus.OK : SensorStatus.ERROR;
		}

		public String getError() {
			return error;
		}

		public SensorStatus getSensorStatus() {
			return sensorStatus;
		}

		public double getValue() {
			return value;
		}

		private Value setSensorStatusUnknown() {
			sensorStatus = SensorStatus.UNKNOWN;
			return this;
		}

	}

	private static final Comparator<Sensor> VALUE_COMPARATOR = new Comparator<Sensor>() {
		@Override
		public int compare(Sensor s1, Sensor s2) {
			if (s1 == null) {
				if (s2 == null) {
					return 0;
				}
				return 1;
			}
			if (s2 == null) {
				return -1;
			}
			return (int) (s2.getValue() - s1.getValue());
		}
	};

	public static Value getValue(double lastValue, List<Sensor> sensors, double maxDiff) {
		long start = System.currentTimeMillis();
		if (sensors.size() == 0) {
			return new Value(lastValue, "no sensors given!");
		}
		if (sensors.size() == 1) {
			return new Value(sensors.get(0).getValue(), null).setSensorStatusUnknown();
		}
		List<Sensor> l = new ArrayList<>(sensors.size() + 1);
		l.addAll(sensors);
		if (l.size() == 2) {
			l.add(new MockSensor("MOCK_ID", sensors.get(0).getScale()).setValue(lastValue));
		}
		double median = getMedian(l);
		StringBuilder err = new StringBuilder();
		for (Sensor sensor : sensors) {
			if (Math.abs(sensor.getValue() - median) > maxDiff) {
				err.append("Temperature sensor ").append(sensor.getID()).append(" measures ").append(sensor.getValue())
						.append(sensor.getScale()).append(" which differs too much from the other sensor(s)!\n");
			}
		}
		double average = getAverage(sensors);
		System.out.println("getValue took " + (System.currentTimeMillis() - start));
		return new Value(average, err.toString());
	}

	private static double getAverage(List<Sensor> sensors) {
		double sum = 0;
		for (Sensor sensor : sensors) {
			sum += sensor.getValue();
		}
		return sum / sensors.size();
	}

	private static double getMedian(List<Sensor> sensors) {
		Collections.sort(sensors, VALUE_COMPARATOR);
		int half = sensors.size() / 2;
		if (sensors.size() % 2 == 0) {
			return (sensors.get(half - 1).getValue() + sensors.get(half).getValue()) / 2d;
		}
		return sensors.get(half).getValue();
	}

}
