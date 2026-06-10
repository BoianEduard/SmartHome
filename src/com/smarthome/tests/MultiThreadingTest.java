package com.smarthome.tests;

import com.smarthome.domain.HumiditySensor;
import com.smarthome.domain.Room;
import com.smarthome.domain.SmartHome;
import com.smarthome.domain.TemperatureSensor;
import com.smarthome.exceptions.RoomAlreadyExistsException;
import com.smarthome.service.MultiRoomMonitoringService;
import com.smarthome.service.SensorMonitorTask;
import com.smarthome.strategy.AlertLevel;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class MultiThreadingTest {

    private SmartHome home;

    @Before
    public void setUp() throws RoomAlreadyExistsException {
        home = new SmartHome("Threading Home");
        Room room = new Room("R1", "Room");
        room.addSensor(new TemperatureSensor("T1", "Temp", 20.0));
        room.addSensor(new HumiditySensor("H1", "Humidity", 40.0));
        home.addRoom(room);
    }

    @Test
    public void testMultiRoomMonitoringCompletesSuccessfully() throws InterruptedException {
        MultiRoomMonitoringService monitor = new MultiRoomMonitoringService(2);
        List<Future<?>> futures = monitor.startMonitoring(home);
        monitor.awaitCompletion(futures);
        // If we get here without timeout/exception, multi-threading completed fine
        assertTrue(futures.size() >= 2);
    }

    @Test
    public void testSensorMonitorTaskUpdatesReading() throws InterruptedException {
        TemperatureSensor sensor = new TemperatureSensor("T99", "TaskTemp", 20.0);
        double[] readings = {25.0, 30.0, 35.0};
        List<String> alerts = new ArrayList<>();

        SensorMonitorTask task = new SensorMonitorTask(
                sensor,
                readings,
                (s, level) -> alerts.add(level.name())
        );

        Thread t = new Thread(task);
        t.start();
        t.join(3000); // wait up to 3 s

        assertEquals(35.0, sensor.getCurrentReading(), 0.001);
        assertTrue(alerts.contains("WARNING")); // 35.0 > 30 threshold
    }

    @Test
    public void testSensorMonitorTaskProducesCriticalAlert() throws InterruptedException {
        TemperatureSensor sensor = new TemperatureSensor("T99", "HotSensor", 20.0);
        double[] readings = {55.0}; // immediately CRITICAL
        List<AlertLevel> captured = new ArrayList<>();

        SensorMonitorTask task = new SensorMonitorTask(
                sensor,
                readings,
                (s, level) -> captured.add(level)
        );

        Thread t = new Thread(task);
        t.start();
        t.join(2000);

        assertFalse(captured.isEmpty());
        assertEquals(AlertLevel.CRITICAL, captured.get(0));
    }

    @Test
    public void testMultiRoomMonitoringWithEmptyHome() {
        SmartHome emptyHome = new SmartHome("Empty Home");
        MultiRoomMonitoringService monitor = new MultiRoomMonitoringService(2);
        List<Future<?>> futures = monitor.startMonitoring(emptyHome);
        monitor.awaitCompletion(futures);
        assertTrue(futures.isEmpty());
    }
}
