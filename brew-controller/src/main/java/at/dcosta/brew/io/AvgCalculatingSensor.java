package at.dcosta.brew.io;

import at.dcosta.brew.io.gpio.MockSensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AvgCalculatingSensor extends AbstractSensor {

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
    private ComponentType componentType;
    private String scale;
    private final List<Sensor> sensors;
    private SensorStatus sensorStatus;
    private String error;
    private final double lastValue = -1;
    private final double maxDiff;
    private boolean mayStartTemperatureCollection;
    public AvgCalculatingSensor(double maxDiff) {
        sensors = new ArrayList<>();
        this.maxDiff = maxDiff;
    }

    private static double getMedian(List<Sensor> sensors) {
        Collections.sort(sensors, VALUE_COMPARATOR);
        int half = sensors.size() / 2;
        if (sensors.size() % 2 == 0) {
            return (sensors.get(half - 1).getValue() + sensors.get(half).getValue()) / 2d;
        }
        return sensors.get(half).getValue();
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
        List<Sensor> allSensors = new ArrayList<>(sensors.size() + 1);
        List<Sensor> validSensors = new ArrayList<>(sensors.size() + 1);
        allSensors.addAll(sensors);
        validSensors.addAll(sensors);
        if (allSensors.size() == 2) {
            allSensors.add(new MockSensor(ComponentType.DUMMY, "MOCK_ID", sensors.get(0).getScale()).setValue(lastValue));
        }
        double median = getMedian(allSensors);
        StringBuilder err = new StringBuilder();
        for (Sensor sensor : sensors) {
            if (Math.abs(sensor.getValue() - median) > maxDiff) {
                err.append("Temperature sensor ").append(sensor.getID()).append(" measures ").append(sensor.getValue())
                        .append(sensor.getScale()).append(" which differs too much (maxDiff=").append(maxDiff).append(") from the other sensor(s)!\n");
            } else {
                validSensors.add(sensor);
            }
        }
        double average = validSensors.isEmpty() ? lastValue : getAverage(validSensors);
        if (err.length() > 0) {
            error = err.toString();
            sensorStatus = SensorStatus.ERROR;
        } else {
            sensorStatus = SensorStatus.OK;
        }
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

    @Override
    public void setMinValidValue(double ignored) {
    }

    @Override
    public void setMaxValidValue(double ignored) {
    }

    public SensorStatus getSensorStatus() {
        return sensorStatus;
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

    public enum SensorStatus {
        OK, ERROR, UNKNOWN
	}
}
