package com.smarthome.domain;

import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.strategy.AlertStrategies;
import com.smarthome.strategy.AlertStrategy;

import java.time.LocalDateTime;

public class TemperatureSensor extends AbstractSensor {

    private static final int MIN_VALID = -50;
    private static final int MAX_VALID = 100;

    public TemperatureSensor(String id, String name, double initialCelsius) {
        this(id, name, initialCelsius, AlertStrategies.defaultTemperature());
    }

    public TemperatureSensor(String id, String name, double initialCelsius, AlertStrategy alertStrategy) {
        super(id, name, SensorType.TEMPERATURE, initialCelsius, alertStrategy);
    }

    @Override
    protected void validateReading(double value) throws SensorReadingException {
        if (value < MIN_VALID || value > MAX_VALID) {
            throw new SensorReadingException(getId(), String.format("Temperature %.1f°C is out of valid range [%d, %d]", value, MIN_VALID, MAX_VALID));
        }
    }
}
