package com.smarthome.decorator;

import com.smarthome.domain.Sensor;
import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.strategy.AlertLevel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggingSensorDecorator extends SensorDecorator {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private int updateCount = 0;

    public LoggingSensorDecorator(Sensor sensor) {
        super(sensor);
    }

    @Override
    public void updateReading(double value) throws SensorReadingException {
        wrapped.updateReading(value);
        updateCount++;
        AlertLevel level = wrapped.checkAlertLevel();
        System.out.printf("[%s] [SENSOR LOG] %-25s | new = %7.2f | alert = %s%n", LocalDateTime.now().format(dtf), wrapped.getName(), value, level);
    }

    public int getUpdateCount() {
        return updateCount;
    }

    @Override
    public String toString() {
        return "[Logged] " + wrapped.toString();
    }
}
