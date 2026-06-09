package com.smarthome.domain;

public enum SensorType {
    TEMPERATURE("Temperature Sensor"),
    HUMIDITY("Humidity Sensor"),
    MOTION("Motion Sensor"),
    SMOKE("Smoke Detector"),
    DOOR("Door/Window Sensor");

    private final String displayName;

    SensorType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
