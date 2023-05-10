package at.dcosta.brew.io.w1;

import at.dcosta.brew.db.IOData;
import at.dcosta.brew.db.IOLog;
import at.dcosta.brew.util.StoppableRunnable;
import at.dcosta.brew.util.ThreadUtil;
import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.temperature.TemperatureScale;

import java.util.logging.Logger;

public class W1TemperatureUpdater implements StoppableRunnable {

    private static final Logger LOGGER = Logger.getLogger(W1TemperatureUpdater.class.getName());
    private final TemperatureSensor pi4jSensor;
    private final IOLog ioLog;
    private final W1TemperatureSensor w1Sensor;
    boolean active;
    private double lastGoodValue;

    public W1TemperatureUpdater(W1TemperatureSensor w1Sensor, TemperatureSensor pi4jSensor) {
        this.w1Sensor = w1Sensor;
        this.pi4jSensor = pi4jSensor;
        this.ioLog = new IOLog();
        this.lastGoodValue = temperatureRangeActive() ? w1Sensor.getMinValidValue() : 18.0;
        readAndStoreValue();
    }

    @Override
    public void abort() {
        active = false;
    }

    public void logValue() {
        ioLog.addEntry(new IOData().setComponentId(w1Sensor.getID()).setComponentType(w1Sensor.getComponentType())
                .setValue(w1Sensor.getValue()));
    }

    @Override
    public boolean mustComplete() {
        return false;
    }

    public void readAndStoreValue() {
        double temperature;
        try {
            temperature = pi4jSensor.getTemperature(TemperatureScale.CELSIUS);
            if (Double.isNaN(temperature)) {
                LOGGER.warning("sensor " + w1Sensor.getID() + " answered with invalid temperature=" + temperature + " -> returning lastGoodValue=" + lastGoodValue);
                temperature = lastGoodValue;
            } else if (temperatureRangeActive() && (temperature < w1Sensor.getMinValidValue() || temperature > w1Sensor.getMaxValidValue())) {
                LOGGER.warning("sensor " + w1Sensor.getID() + " answered with temperature=" + temperature + " which is outside of range (" + w1Sensor.getMinValidValue() + ", " + w1Sensor.getMaxValidValue() + ") -> returning lastGoodValue=" + lastGoodValue);
                temperature = lastGoodValue;
            }
            lastGoodValue = temperature;
        } catch (Exception e) {
            LOGGER.warning("can not evaluate temperature of sensor " + w1Sensor.getID() +  ": " + e.getMessage() + " -> returning lastGoodValue=" + lastGoodValue);
            temperature = lastGoodValue;
        }
        w1Sensor.setValue(temperature);
    }

    private boolean temperatureRangeActive() {
        return w1Sensor.getMaxValidValue() > w1Sensor.getMinValidValue();
    }

    @Override
    public void run() {
        active = true;
        int count = Integer.MAX_VALUE;
        int maxCount = IOLog.LOG_INTERVAL_MILLIS / ThreadUtil.SLEEP_MILLIS_DEFAULT;
        while (active) {
            if (count++ >= maxCount) {
                count = 0;
                readStoreAndLogValue();
            }
            ThreadUtil.sleepDefaultMillis();
        }
    }

    private void readStoreAndLogValue() {
        readAndStoreValue();
        logValue();
    }
}
