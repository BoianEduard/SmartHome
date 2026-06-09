package com.smarthome.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Room {
    private final String id;
    private final String name;
    private final List<Sensor> sensors = new ArrayList<>();

    public Room(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Room id may not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Room name may not be null or blank");
        }

        this.id = id;
        this.name = name;
    }

    public void addSensor(Sensor sensor) {
        if (sensor == null) {
            throw new IllegalArgumentException("Sensor may not be null");
        }
        sensors.add(sensor);
    }

    public List<Sensor> getSensors() {
        return Collections.unmodifiableList(sensors);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
