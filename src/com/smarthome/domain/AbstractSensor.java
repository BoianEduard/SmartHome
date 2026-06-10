package com.smarthome.domain;

import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.strategy.AlertLevel;
import com.smarthome.strategy.AlertStrategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSensor implements Sensor {

    private final String id;
    private final String name;
    private final SensorType type;
    private volatile boolean active;
    private volatile double currentReading;
    private volatile LocalDateTime lastUpdated;
    private ArrayList<Double> readingHistory = new ArrayList<>();
    private volatile AlertStrategy alertStrategy;

    public AbstractSensor(String id, String name, SensorType type, double currentReading, AlertStrategy alertStrategy) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Sensor ID may not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Sensor name may not be null or blank");
        }
        if (type == null ) {
            throw new IllegalArgumentException("Sensor type may not be null or blank");
        }
        if (alertStrategy == null) {
            throw new IllegalArgumentException("AlertStrategy may not be null");
        }

        this.id = id;
        this.name = name;
        this.type = type;
        this.active = true;
        this.currentReading = currentReading;
        this.lastUpdated = LocalDateTime.now();
        this.alertStrategy = alertStrategy;
        readingHistory.add(currentReading);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SensorType getType() {
        return type;
    }

    @Override
    public double getCurrentReading() {
        return currentReading;
    }

    @Override
    public synchronized List<Double> getReadingHistory() {
        return Collections.unmodifiableList(readingHistory);
    }

    @Override
    public synchronized void updateReading(double value) throws SensorReadingException {
        if (!active) {
            throw new SensorReadingException(id, "Sensor is not active");
        }
        validateReading(value);
        currentReading = value;
        lastUpdated = LocalDateTime.now();
        readingHistory.add(value);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public synchronized AlertLevel checkAlertLevel() {
        return alertStrategy.evaluate(currentReading);
    }

    public AlertStrategy getAlertStrategy() {
        return alertStrategy;
    }

    public synchronized void setAlertStrategy(AlertStrategy alertStrategy) {
        if (alertStrategy == null) {
            throw new IllegalArgumentException("Alert strategy can not be null");
        }

        this.alertStrategy = alertStrategy;
    }

    public synchronized void setActive(boolean active) {
        this.active = active;
    }

    protected abstract void validateReading(double value) throws SensorReadingException;
}
