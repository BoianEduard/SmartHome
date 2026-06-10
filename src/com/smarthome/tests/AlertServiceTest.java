package com.smarthome.tests;

import com.smarthome.domain.HumiditySensor;
import com.smarthome.domain.Room;
import com.smarthome.domain.SmartHome;
import com.smarthome.domain.TemperatureSensor;
import com.smarthome.exceptions.RoomAlreadyExistsException;
import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.service.AlertService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AlertServiceTest {

    private SmartHome home;

    @Before
    public void setUp() throws RoomAlreadyExistsException, SensorReadingException {
        home = new SmartHome("Alert Home");

        Room room = new Room("R1", "Test Room");

        TemperatureSensor warn = new TemperatureSensor("T1", "Warm Sensor", 22.0);
        warn.updateReading(35.0); // WARNING

        TemperatureSensor crit = new TemperatureSensor("T2", "Hot Sensor", 22.0);
        crit.updateReading(55.0); // CRITICAL

        room.addSensor(warn);
        room.addSensor(crit);
        room.addSensor(new HumiditySensor("H1", "Safe Humidity", 40.0)); // INFO

        home.addRoom(room);
    }

    @Test
    public void testWarningHandlerCalledForWarningSensor() {
        List<String> calledFor = new ArrayList<>();
        AlertService service = new AlertService(
                s -> calledFor.add("warn:" + s.getName()),
                s -> calledFor.add("crit:" + s.getName())
        );

        service.processAlerts(home);

        assertTrue(calledFor.stream().anyMatch(s -> s.startsWith("warn:")));
    }

    @Test
    public void testCriticalHandlerCalledForCriticalSensor() {
        List<String> calledFor = new ArrayList<>();
        AlertService service = new AlertService(
                s -> calledFor.add("warn:" + s.getName()),
                s -> calledFor.add("crit:" + s.getName())
        );

        service.processAlerts(home);

        assertTrue(calledFor.stream().anyMatch(s -> s.startsWith("crit:")));
    }

    @Test
    public void testInfoSensorDoesNotTriggerHandlers() {
        List<String> calledFor = new ArrayList<>();
        AlertService service = new AlertService(
                s -> calledFor.add(s.getName()),
                s -> calledFor.add(s.getName())
        );

        service.processAlerts(home);

        assertFalse(calledFor.contains("Safe Humidity"));
    }

    @Test
    public void testDefaultConstructorDoesNotThrow() throws RoomAlreadyExistsException {
        SmartHome h = new SmartHome("Default Home");
        Room r = new Room("R1", "Room");
        r.addSensor(new TemperatureSensor("T1", "T", 22.0));
        h.addRoom(r);

        AlertService service = new AlertService(); // uses default console handlers
        service.processAlerts(h); // should not throw
    }
}
