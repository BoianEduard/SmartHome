package com.smarthome.domain;

import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.strategy.AlertLevel;

import java.util.List;

public interface Sensor {

    String getId();

    String getName();

    SensorType getType();

    double getCurrentReading();

    void updateReading(double value) throws SensorReadingException;

    List<Double> getReadingHistory();

    boolean isActive();

    AlertLevel checkAlertLevel();

    void setActive(boolean b);
}
