package com.smarthome.command;

import com.smarthome.domain.Sensor;
import com.smarthome.exceptions.SensorReadingException;

public class UpdateReadingCommand implements SensorCommand {
    private final Sensor sensor;
    private final double newValue;
    private double previousValue;
    private boolean executed = false;

    public UpdateReadingCommand(Sensor sensor, double newValue) {
        if (sensor == null) {
            throw new IllegalArgumentException("Sensor cannot be null;");
        }
        this.sensor = sensor;
        this.newValue = newValue;
    }

    @Override
    public void execute() throws SensorReadingException {
        previousValue = sensor.getCurrentReading();
        sensor.updateReading(newValue);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) return;
        sensor.updateReading(previousValue);
        executed = false;
    }

    public String describe() {
        return String.format(java.util.Locale.UK, "UpdateReading[sensor=%s, value=%.2f]", sensor.getId(), newValue);
    }

    public boolean isExecuted() { return executed; }
    public double getPreviousValue() { return previousValue; }
}
