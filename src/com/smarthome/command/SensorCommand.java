package com.smarthome.command;

import com.smarthome.exceptions.SensorReadingException;

public interface SensorCommand {
    void execute() throws SensorReadingException;

    void undo();

    String describe();
}
