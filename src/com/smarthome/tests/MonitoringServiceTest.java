package com.smarthome.tests;

import com.smarthome.domain.*;
import com.smarthome.exceptions.RoomAlreadyExistsException;
import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.service.MonitoringService;
import com.smarthome.strategy.AlertLevel;
import org.junit.Before;
import org.junit.Test;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MonitoringServiceTest {

    private SmartHome home;

    @Before
    public void setUp() throws RoomAlreadyExistsException, SensorReadingException {
        home = new SmartHome("Test Home");

        Room living = new Room("R1", "Living Room");
        living.addSensor(new TemperatureSensor("T1", "LR Temp", 22.0));   // INFO
        living.addSensor(new HumiditySensor("H1", "LR Humidity", 45.0));  // INFO
        living.addSensor(new SmokeSensor("S1", "LR Smoke", 2.0));         // INFO

        Room kitchen = new Room("R2", "Kitchen");
        TemperatureSensor kitchenTemp = new TemperatureSensor("T2", "Kitchen Temp", 24.0);
        kitchenTemp.updateReading(55.0); // CRITICAL
        kitchen.addSensor(kitchenTemp);

        SmokeSensor kitchenSmoke = new SmokeSensor("S2", "Kitchen Smoke", 0.5);
        kitchenSmoke.updateReading(15.0); // WARNING
        kitchen.addSensor(kitchenSmoke);

        MonitoringService.addRoom(home, living);
        MonitoringService.addRoom(home, kitchen);
    }

    @Test
    public void testFindSensorsActive() {
        List<Sensor> active = MonitoringService.findSensors(home, Sensor::isActive);
        assertEquals(5, active.size());
    }

    @Test
    public void testFindSensorsInactive() {
        home.getRooms().get(0).getSensors().get(0);
        // deactivate first sensor
        AbstractSensor first = (AbstractSensor) home.getRooms().get(0).getSensors().get(0);
        first.setActive(false);
        List<Sensor> inactive = MonitoringService.getInactiveSensors(home);
        assertEquals(1, inactive.size());
    }

    @Test
    public void testGetCriticalSensors() {
        List<Sensor> critical = MonitoringService.getCriticalSensors(home);
        assertEquals(1, critical.size());
        assertEquals("Kitchen Temp", critical.get(0).getName());
    }

    @Test
    public void testGetWarningSensors() {
        List<Sensor> warning = MonitoringService.getWarningSensors(home);
        assertEquals(1, warning.size());
        assertEquals("Kitchen Smoke", warning.get(0).getName());
    }

    @Test
    public void testGetSensorCountPerRoom() {
        Map<String, Long> counts = MonitoringService.getSensorCountPerRoom(home);
        assertEquals(2, counts.size());
        assertEquals(Long.valueOf(3), counts.get("Living Room"));
        assertEquals(Long.valueOf(2), counts.get("Kitchen"));
    }

    @Test
    public void testGetSensorsByType() {
        Map<SensorType, List<Sensor>> byType = MonitoringService.getSensorsByType(home);
        assertTrue(byType.containsKey(SensorType.TEMPERATURE));
        assertEquals(2, byType.get(SensorType.TEMPERATURE).size());
    }

    @Test
    public void testGetSensorsByAlertLevel() {
        Map<AlertLevel, List<Sensor>> byLevel = MonitoringService.getSensorsByAlertLevel(home);
        assertTrue(byLevel.containsKey(AlertLevel.CRITICAL));
        assertTrue(byLevel.containsKey(AlertLevel.WARNING));
        assertEquals(1, byLevel.get(AlertLevel.CRITICAL).size());
        assertEquals(1, byLevel.get(AlertLevel.WARNING).size());
    }

    @Test
    public void testGetReadingStatisticsTemperature() {
        DoubleSummaryStatistics stats =
                MonitoringService.getReadingStatistics(home, SensorType.TEMPERATURE);
        assertEquals(2, stats.getCount());
        assertEquals(22.0, stats.getMin(), 0.001);
        assertEquals(55.0, stats.getMax(), 0.001);
    }

    @Test
    public void testGetReadingStatisticsNoSensorsOfType() {
        // No door sensors added
        DoubleSummaryStatistics stats =
                MonitoringService.getReadingStatistics(home, SensorType.DOOR);
        assertEquals(0, stats.getCount());
    }

    @Test
    public void testSafeUpdateDoesNotThrow() {
        Sensor sensor = home.getRooms().get(0).getSensors().get(1); // HumiditySensor
        // This is a valid value, so no error
        MonitoringService.safeUpdate(sensor, 60.0);
        assertEquals(60.0, sensor.getCurrentReading(), 0.001);
    }

    @Test
    public void testSafeUpdateInvalidValueDoesNotThrow() {
        Sensor sensor = home.getRooms().getFirst().getSensors().get(1);
        MonitoringService.safeUpdate(sensor, -3);
        assertEquals(45.0, sensor.getCurrentReading(), 0.001);
    }
}
