package com.smarthome.tests;

import com.smarthome.domain.HumiditySensor;
import com.smarthome.domain.SensorType;
import com.smarthome.domain.SmokeSensor;
import com.smarthome.domain.TemperatureSensor;
import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.strategy.AlertLevel;
import org.junit.Test;

import static org.junit.Assert.*;

public class SensorTest {

    // ------------------------------------------------------------------ TemperatureSensor

    @Test
    public void testTemperatureSensorInitialState() {
        TemperatureSensor sensor = new TemperatureSensor("T1", "Living Room Temp", 22.0);
        assertEquals("T1", sensor.getId());
        assertEquals("Living Room Temp", sensor.getName());
        assertEquals(SensorType.TEMPERATURE, sensor.getType());
        assertEquals(22.0, sensor.getCurrentReading(), 0.001);
        assertTrue(sensor.isActive());
        assertEquals(AlertLevel.INFO, sensor.checkAlertLevel());
    }

    @Test
    public void testTemperatureSensorWarningLevel() throws SensorReadingException {
        TemperatureSensor sensor = new TemperatureSensor("T1", "Temp", 22.0);
        sensor.updateReading(35.0);
        assertEquals(AlertLevel.WARNING, sensor.checkAlertLevel());
    }

    @Test
    public void testTemperatureSensorCriticalLevel() throws SensorReadingException {
        TemperatureSensor sensor = new TemperatureSensor("T1", "Temp", 22.0);
        sensor.updateReading(55.0);
        assertEquals(AlertLevel.CRITICAL, sensor.checkAlertLevel());
    }

    @Test(expected = SensorReadingException.class)
    public void testTemperatureSensorInvalidReadingTooHigh() throws SensorReadingException {
        TemperatureSensor sensor = new TemperatureSensor("T1", "Temp", 22.0);
        sensor.updateReading(200.0);  // above 150
    }

    @Test(expected = SensorReadingException.class)
    public void testTemperatureSensorInvalidReadingTooLow() throws SensorReadingException {
        TemperatureSensor sensor = new TemperatureSensor("T1", "Temp", 22.0);
        sensor.updateReading(-100.0); // below -50
    }

    @Test(expected = SensorReadingException.class)
    public void testUpdateReadingOnInactiveSensor() throws SensorReadingException {
        TemperatureSensor sensor = new TemperatureSensor("T1", "Temp", 22.0);
        sensor.setActive(false);
        sensor.updateReading(25.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTemperatureSensorNullId() {
        new TemperatureSensor(null, "Temp", 22.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTemperatureSensorBlankName() {
        new TemperatureSensor("T1", "   ", 22.0);
    }

    // ------------------------------------------------------------------ HumiditySensor

    @Test
    public void testHumiditySensorInfoLevel() {
        HumiditySensor sensor = new HumiditySensor("H1", "Humidity", 50.0);
        assertEquals(AlertLevel.INFO, sensor.checkAlertLevel());
    }

    @Test
    public void testHumiditySensorWarning() throws SensorReadingException {
        HumiditySensor sensor = new HumiditySensor("H1", "Humidity", 50.0);
        sensor.updateReading(75.0);
        assertEquals(AlertLevel.WARNING, sensor.checkAlertLevel());
    }

    @Test
    public void testHumiditySensorCritical() throws SensorReadingException {
        HumiditySensor sensor = new HumiditySensor("H1", "Humidity", 50.0);
        sensor.updateReading(95.0);
        assertEquals(AlertLevel.CRITICAL, sensor.checkAlertLevel());
    }

    @Test(expected = SensorReadingException.class)
    public void testHumiditySensorInvalidNegative() throws SensorReadingException {
        HumiditySensor sensor = new HumiditySensor("H1", "Humidity", 50.0);
        sensor.updateReading(-5.0);
    }

    // ------------------------------------------------------------------ SmokeSensor

    @Test
    public void testSmokeSensorInfo() {
        SmokeSensor sensor = new SmokeSensor("S1", "Smoke", 2.0);
        assertEquals(AlertLevel.INFO, sensor.checkAlertLevel());
    }

    @Test
    public void testSmokeSensorWarning() throws SensorReadingException {
        SmokeSensor sensor = new SmokeSensor("S1", "Smoke", 2.0);
        sensor.updateReading(15.0);
        assertEquals(AlertLevel.WARNING, sensor.checkAlertLevel());
    }

    @Test
    public void testSmokeSensorCritical() throws SensorReadingException {
        SmokeSensor sensor = new SmokeSensor("S1", "Smoke", 2.0);
        sensor.updateReading(40.0);
        assertEquals(AlertLevel.CRITICAL, sensor.checkAlertLevel());
    }

    @Test(expected = SensorReadingException.class)
    public void testSmokeSensorNegativeReading() throws SensorReadingException {
        SmokeSensor sensor = new SmokeSensor("S1", "Smoke", 2.0);
        sensor.updateReading(-1.0);
    }

    // ------------------------------------------------------------------ Reading history

    @Test
    public void testReadingHistoryIsRecorded() throws SensorReadingException {
        TemperatureSensor sensor = new TemperatureSensor("T1", "Temp", 20.0);
        sensor.updateReading(25.0);
        sensor.updateReading(30.0);
        assertEquals(3, sensor.getReadingHistory().size());
        assertEquals(25, sensor.getReadingHistory().get(1), 0.001);
        assertEquals(30.0, sensor.getReadingHistory().get(2), 0.001);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testReadingHistoryIsUnmodifiable() {
        TemperatureSensor sensor = new TemperatureSensor("T1", "Temp", 20.0);
        sensor.getReadingHistory().add(99.0);
    }
}
