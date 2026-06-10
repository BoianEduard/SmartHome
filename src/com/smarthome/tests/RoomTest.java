package com.smarthome.tests;

import com.smarthome.domain.HumiditySensor;
import com.smarthome.domain.Room;
import com.smarthome.domain.SmokeSensor;
import com.smarthome.domain.TemperatureSensor;
import org.junit.Test;

import static org.junit.Assert.*;

public class RoomTest {

    @Test
    public void testRoomCreation() {
        Room room = new Room("R1", "Living Room");
        assertEquals("R1", room.getId());
        assertEquals("Living Room", room.getName());
        assertTrue(room.getSensors().isEmpty());
    }

    @Test
    public void testAddSensor() {
        Room room = new Room("R1", "Living Room");
        room.addSensor(new TemperatureSensor("T1", "Temp", 22.0));
        assertEquals(1, room.getSensors().size());
    }

    @Test
    public void testAddMultipleSensors() {
        Room room = new Room("R1", "Kitchen");
        room.addSensor(new TemperatureSensor("T1", "Temp", 22.0));
        room.addSensor(new HumiditySensor("H1", "Humidity", 50.0));
        room.addSensor(new SmokeSensor("S1", "Smoke", 0.5));
        assertEquals(3, room.getSensors().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoomNullId() {
        new Room(null, "Living Room");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoomBlankName() {
        new Room("R1", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullSensor() {
        Room room = new Room("R1", "Living Room");
        room.addSensor(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSensorListIsUnmodifiable() {
        Room room = new Room("R1", "Living Room");
        room.getSensors().add(new TemperatureSensor("T1", "Temp", 22.0));
    }

    @Test
    public void testRoomToString() {
        Room room = new Room("R1", "Living Room");
        assertTrue(room.toString().contains("Living Room"));
    }
}
