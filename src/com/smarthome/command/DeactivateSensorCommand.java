package com.smarthome.command;

import com.smarthome.domain.AbstractSensor;
import com.smarthome.exceptions.SensorReadingException;

public class DeactivateSensorCommand implements SensorCommand {
    private final AbstractSensor sensor;
    private boolean previousState;
    private boolean executed = false;

    public DeactivateSensorCommand(AbstractSensor sensor) {
        if (sensor == null) {
            throw new IllegalArgumentException("Sensor may not be null");
        }
        this.sensor = sensor;
    }

    @Override
    public void execute() throws SensorReadingException {
        previousState = sensor.isActive();
        sensor.setActive(false);
        executed = true;
        System.out.printf("[CMD] Deactivated sensor '%s'%n", sensor.getName());
    }

    @Override
    public void undo() {
        if (!executed) {
            sensor.setActive(previousState);
            executed = false;
        }
        System.out.printf("[CMD UNDO] Restored sensor '%s' to active=%b%n", sensor.getName(), previousState);
    }

    @Override
    public String describe() {
        return "DeactivateSensor[sensor=" + sensor.getId() + "]";
    }
}
