package com.smarthome.tests;

import com.smarthome.decorator.LoggingSensorDecorator;
import com.smarthome.decorator.SmoothingSensorDecorator;
import com.smarthome.domain.Sensor;
import com.smarthome.domain.SensorType;
import com.smarthome.domain.TemperatureSensor;
import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.strategy.AlertLevel;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DecoratorPatternTest {

    private TemperatureSensor raw;

    @Before
    public void setUp() {
        raw = new TemperatureSensor("T1", "Test Sensor", 20.0);
    }

    // ------------------------------------------------------------------ SensorDecorator (base)

    @Test
    public void testDecoratorForwardsId() {
        LoggingSensorDecorator dec = new LoggingSensorDecorator(raw);
        assertEquals("T1", dec.getId());
    }

    @Test
    public void testDecoratorForwardsName() {
        LoggingSensorDecorator dec = new LoggingSensorDecorator(raw);
        assertEquals("Test Sensor", dec.getName());
    }

    @Test
    public void testDecoratorForwardsType() {
        LoggingSensorDecorator dec = new LoggingSensorDecorator(raw);
        assertEquals(SensorType.TEMPERATURE, dec.getType());
    }

    @Test
    public void testDecoratorForwardsIsActive() {
        LoggingSensorDecorator dec = new LoggingSensorDecorator(raw);
        assertTrue(dec.isActive());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecoratorRejectsNullWrapped() {
        new LoggingSensorDecorator(null);
    }

    // ------------------------------------------------------------------ LoggingSensorDecorator

    @Test
    public void testLoggingDecoratorTracksUpdateCount() throws SensorReadingException {
        LoggingSensorDecorator dec = new LoggingSensorDecorator(raw);
        assertEquals(0, dec.getUpdateCount());
        dec.updateReading(25.0);
        dec.updateReading(30.0);
        assertEquals(2, dec.getUpdateCount());
    }

    @Test
    public void testLoggingDecoratorDelegatesReading() throws SensorReadingException {
        LoggingSensorDecorator dec = new LoggingSensorDecorator(raw);
        dec.updateReading(35.0);
        assertEquals(35.0, dec.getCurrentReading(), 0.001);
    }

    @Test
    public void testLoggingDecoratorDelegatesAlertLevel() throws SensorReadingException {
        LoggingSensorDecorator dec = new LoggingSensorDecorator(raw);
        dec.updateReading(55.0);
        assertEquals(AlertLevel.CRITICAL, dec.checkAlertLevel());
    }

    @Test(expected = SensorReadingException.class)
    public void testLoggingDecoratorPropagatesException() throws SensorReadingException {
        LoggingSensorDecorator dec = new LoggingSensorDecorator(raw);
        dec.updateReading(200.0);   // invalid for temperature sensor
    }

    // ------------------------------------------------------------------ SmoothingSensorDecorator

    @Test
    public void testSmoothingDecoratorInitialReading() {
        SmoothingSensorDecorator dec = new SmoothingSensorDecorator(raw, 3);
        assertEquals(20.0, dec.getCurrentReading(), 0.001);  // window = [20]
    }

    @Test
    public void testSmoothingDecoratorAveragesWindow() throws SensorReadingException {
        SmoothingSensorDecorator dec = new SmoothingSensorDecorator(raw, 3);
        dec.updateReading(30.0);   // window = [20, 30]
        dec.updateReading(40.0);   // window = [20, 30, 40]
        assertEquals((20.0 + 30.0 + 40.0) / 3.0, dec.getCurrentReading(), 0.001);
    }

    @Test
    public void testSmoothingDecoratorEvictsOldest() throws SensorReadingException {
        SmoothingSensorDecorator dec = new SmoothingSensorDecorator(raw, 3);
        dec.updateReading(30.0);
        dec.updateReading(40.0);
        dec.updateReading(100.0);   // evicts 20 → window = [30, 40, 100]
        assertEquals((30.0 + 40.0 + 100.0) / 3.0, dec.getCurrentReading(), 0.001);
    }

    @Test
    public void testSmoothingDecoratorDampensSpike() throws SensorReadingException {
        SmoothingSensorDecorator dec = new SmoothingSensorDecorator(raw, 3);
        dec.updateReading(20.0);
        dec.updateReading(100.0);   // spike
        // smoothed = (20+20+100)/3 ≈ 46.7, not 100
        assertTrue(dec.getCurrentReading() < 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSmoothingDecoratorRejectsWindowSizeZero() {
        new SmoothingSensorDecorator(raw, 0);
    }

    @Test
    public void testSmoothingDecoratorExposesWindow() throws SensorReadingException {
        SmoothingSensorDecorator dec = new SmoothingSensorDecorator(raw, 2);
        dec.updateReading(25.0);
        List<Double> window = dec.getWindow();
        assertEquals(2, window.size());
        assertTrue(window.contains(25.0));
    }

    // ------------------------------------------------------------------ Decorator chaining

    @Test
    public void testDecoratorChain_LoggedSmoothed() throws SensorReadingException {
        // Smoothing inside, logging outside
        Sensor smoothed = new SmoothingSensorDecorator(raw, 3);
        LoggingSensorDecorator logged = new LoggingSensorDecorator(smoothed);

        logged.updateReading(30.0);
        logged.updateReading(100.0);   // spike

        // LoggingDecorator should have counted 2 updates
        assertEquals(2, logged.getUpdateCount());
        // getCurrentReading routes through smoothing: (20+30+100)/3 ≈ 50
        assertTrue(logged.getCurrentReading() < 100.0);
    }

    @Test
    public void testDecoratorStillImplementsSensorInterface() {
        Sensor dec = new LoggingSensorDecorator(new SmoothingSensorDecorator(raw, 2));
        // Confirm it is usable anywhere a Sensor is expected
        assertNotNull(dec.getId());
        assertNotNull(dec.getType());
    }
}
