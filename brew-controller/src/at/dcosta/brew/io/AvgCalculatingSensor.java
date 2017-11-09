package at.dcosta.brew.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import at.dcosta.brew.io.gpio.MockSensor;

public class AvgCalculatingSensor extends AbstractSensor {

	public static enum SensorStatus {
		OK, ERROR, UNKNOWN;
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

	private static double getMedian(List<Sensor> sensors) {
		Collections.sort(sensors, VALUE_COMPARATOR);
		int half = sensors.size() / 2;
		if (sensors.size() % 2 == 0) {
			return (sensors.get(half - 1).getValue() + sensors.get(half).getValue()) / 2d;
		}
		return sensors.get(half).getValue();
	}

	private ComponentType componentType;
	private String scale;
	private List<Sensor> sensors;
	private SensorStatus sensorStatus;
	private String error;
	private double lastValue = -1;
	private double maxDiff;

	private boolean mayStartTemperatureCollection;

	public AvgCalculatingSensor(double maxDiff) {
		sensors = new ArrayList<>();
		this.maxDiff = maxDiff;

	}

	public void addSensor(Sensor sensor) {
		if (componentType == null) {
			componentType = sensor.getComponentType();
		} else if (sensor.getComponentType() != componentType) {
			throw new IllegalArgumentException(
					"illegal componentType! Can not mix " + componentType + " and " + sensor.getComponentType());
		}
		if (scale == null) {
			scale = sensor.getScale();
		} else if (!sensor.getScale().equals(scale)) {
			throw new IllegalArgumentException("illegal scale! Can not mix " + scale + " and " + sensor.getScale());
		}
		sensors.add(sensor);
		mayStartTemperatureCollection = true;
	}

	@Override
	public double doGetValue() {
		sensorStatus = SensorStatus.UNKNOWN;
		error = null;

		if (sensors.size() == 0) {
			sensorStatus = SensorStatus.ERROR;
			error = "no sensors given!";
			return lastValue;
		}

		if (sensors.size() == 1) {
			Sensor sensor = sensors.get(0);
			// System.out.println(sensor.getID() + ": " + sensor.getValue() +
			// sensor.getScale());
			sensorStatus = SensorStatus.OK;
			return sensor.getValue();
		}
		List<Sensor> l = new ArrayList<>(sensors.size() + 1);
		l.addAll(sensors);
		if (l.size() == 2) {
			l.add(new MockSensor(ComponentType.DUMMY, "MOCK_ID", sensors.get(0).getScale()).setValue(lastValue));
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
		if (err.length() > 0) {
			error = err.toString();
			sensorStatus = SensorStatus.ERROR;
		}
		sensorStatus = SensorStatus.OK;
		return average;
	}

	@Override
	public ComponentType getComponentType() {
		return componentType;
	}

	public String getError() {
		return error;
	}

	@Override
	public String getID() {
		return "Average";
	}

	@Override
	public String getScale() {
		return scale;
	}

	public SensorStatus getSensorStatus() {
		return sensorStatus;
	}

	@Override
	public void setCorrectionValue(double correctionValue) {
		// not needed, the sensor already adds the correctionValue individually
	}

	private double getAverage(List<Sensor> sensors) {
		double sum = 0;
		for (Sensor sensor : sensors) {
			sum += sensor.getValue();
		}
		return (sum / sensors.size());
	}

	@Override
	protected boolean mayStartTemperatureCollection() {
		return mayStartTemperatureCollection;
	}
}
