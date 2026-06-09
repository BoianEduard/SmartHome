package com.smarthome.exceptions;

public class SensorReadingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SensorReadingException(String id, String message) {
        super(String.format("Sensor %s failed: %s", id, message));
    }
}
