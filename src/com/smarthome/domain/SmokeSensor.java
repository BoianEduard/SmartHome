package com.smarthome.domain;

import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.strategy.AlertStrategies;
import com.smarthome.strategy.AlertStrategy;

public class SmokeSensor extends AbstractSensor {
    private static final int MIN_VALID = 0;

    public SmokeSensor(String id, String name, double currentReading) {
        this(id, name, currentReading, AlertStrategies.defaultSmoke());
    }

    public SmokeSensor(String id, String name, double currentReading, AlertStrategy alertStrategy) {
        super(id, name, SensorType.SMOKE, currentReading, alertStrategy);
    }

    @Override
    protected void validateReading(double value) throws SensorReadingException {
        if (value < MIN_VALID) {
            throw new SensorReadingException(getId(), String.format("Smoke level %.1f ppm must be bigger than %d", value, MIN_VALID));
        }
    }
}
