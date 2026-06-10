package com.smarthome.tests;

import com.smarthome.domain.*;
import com.smarthome.exceptions.RoomAlreadyExistsException;
import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.report.ReportGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ReportGeneratorTest {

    private SmartHome home;

    @Before
    public void setUp() throws RoomAlreadyExistsException, SensorReadingException {
        home = new SmartHome("Report Home");

        Room r1 = new Room("R1", "Living Room");
        r1.addSensor(new TemperatureSensor("T1", "Temp", 22.0));
        r1.addSensor(new HumiditySensor("H1", "Humidity", 50.0));

        Room r2 = new Room("R2", "Kitchen");
        TemperatureSensor criticalTemp = new TemperatureSensor("T2", "Kitchen Temp", 20.0);
        criticalTemp.updateReading(60.0);
        r2.addSensor(criticalTemp);

        home.addRoom(r1);
        home.addRoom(r2);
    }

    @Test
    public void testSummaryReportContainsHomeName() {
        String report = ReportGenerator.generateSummaryReport(home);
        assertTrue(report.contains("Report Home"));
    }

    @Test
    public void testSummaryReportContainsTotals() {
        String report = ReportGenerator.generateSummaryReport(home);
        assertTrue(report.contains("Total rooms:2"));
        assertTrue(report.contains("Total sensors: 3"));
    }

    @Test
    public void testSummaryReportContainsCritical() {
        String report = ReportGenerator.generateSummaryReport(home);
        assertTrue(report.contains("CRITICAL"));
    }

    @Test
    public void testRoomReportContainsRoomNames() {
        String report = ReportGenerator.generateRoomReport(home);
        assertTrue(report.contains("Living Room"));
        assertTrue(report.contains("Kitchen"));
    }

    @Test
    public void testRoomReportContainsSensorData() {
        String report = ReportGenerator.generateRoomReport(home);
        assertTrue(report.contains("22.00") || report.contains("60.00"));
    }

    @Test
    public void testStatisticsReportContainsTemperature() {
        String report = ReportGenerator.generateStatisticsReport(home);
        assertTrue(report.contains("Temperature Sensor"));
    }

    @Test
    public void testCriticalReportListsCriticalSensors() {
        String report = ReportGenerator.generateCriticalReport(home);
        assertTrue(report.contains("Kitchen Temp"));
    }

    @Test
    public void testCriticalReportNoCritical() throws RoomAlreadyExistsException {
        SmartHome quietHome = new SmartHome("Quiet Home");
        Room r = new Room("Q1", "Safe Room");
        r.addSensor(new TemperatureSensor("T1", "Safe Temp", 20.0));
        quietHome.addRoom(r);

        String report = ReportGenerator.generateCriticalReport(quietHome);
        assertTrue(report.contains("No critical sensors"));
    }

    @Test
    public void testGetTopSensorsByReading() {
        List<Sensor> top = ReportGenerator.getTopSensorsByReading(home, SensorType.TEMPERATURE, 1);
        assertEquals(1, top.size());
        assertEquals("Kitchen Temp", top.get(0).getName());
    }

    @Test
    public void testGetTopSensorsByReadingLimitHigherThanCount() {
        List<Sensor> top = ReportGenerator.getTopSensorsByReading(home, SensorType.TEMPERATURE, 10);
        assertEquals(2, top.size());
    }
}
