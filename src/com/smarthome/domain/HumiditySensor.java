package com.smarthome.domain;

import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.strategy.AlertStrategies;
import com.smarthome.strategy.AlertStrategy;

public class HumiditySensor extends AbstractSensor {

    private static final int MIN_VALID = 0;

    public HumiditySensor(String id, String name, double currentReading) {
        this(id, name, currentReading, AlertStrategies.defaultHumidity());
    }

    public HumiditySensor(String id, String name, double currentReading, AlertStrategy alertStrategy) {
        super(id, name, SensorType.HUMIDITY, currentReading, alertStrategy);
    }

    @Override
    protected void validateReading(double value) throws SensorReadingException {
        if (value < MIN_VALID) {
            throw new SensorReadingException(getId(), String.format("Humidity %.1f%% must be bigger than %d", value, MIN_VALID));
        }
    }
}
