package com.smarthome.decorator;

import com.smarthome.domain.AbstractSensor;
import com.smarthome.domain.Sensor;
import com.smarthome.domain.SensorType;
import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.strategy.AlertLevel;

import java.util.List;

public abstract  class SensorDecorator implements Sensor {
    protected final Sensor wrapped;

    public SensorDecorator(Sensor sensor) {
        if (sensor == null) {
            throw new IllegalArgumentException("Sensor may not be null");
        }
        this.wrapped = sensor;
    }

    @Override
    public String getId() {
        return wrapped.getId();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public SensorType getType() {
        return wrapped.getType();
    }

    @Override
    public double getCurrentReading() {
        return wrapped.getCurrentReading();
    }

    @Override
    public void updateReading(double value) throws SensorReadingException {
        wrapped.updateReading(value);
    }

    @Override
    public List<Double> getReadingHistory() {
       if (wrapped instanceof AbstractSensor abs) {
           return abs.getReadingHistory();
       }
       if (wrapped instanceof SensorDecorator dec) {
           return dec.getReadingHistory();
       }
       return List.of(wrapped.getCurrentReading());
    }

    @Override
    public boolean isActive() {
        return wrapped.isActive();
    }

    @Override
    public AlertLevel checkAlertLevel() {
        return wrapped.checkAlertLevel();
    }

    @Override
    public void setActive(boolean b) {
        wrapped.setActive(b);
    }
}
