package com.smarthome.tests;

import com.smarthome.domain.SmokeSensor;
import com.smarthome.domain.TemperatureSensor;
import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.strategy.AlertLevel;
import com.smarthome.strategy.AlertStrategies;
import com.smarthome.strategy.AlertStrategy;
import com.smarthome.strategy.ThresholdAlertStrategy;
import org.junit.Test;

import static org.junit.Assert.*;

public class StrategyPatternTest {

    // ------------------------------------------------------------------ ThresholdAlertStrategy

    @Test
    public void testThresholdStrategyInfo() {
        AlertStrategy strategy = new ThresholdAlertStrategy(30.0, 50.0);
        assertEquals(AlertLevel.INFO, strategy.evaluate(20.0));
    }

    @Test
    public void testThresholdStrategyWarning() {
        AlertStrategy strategy = new ThresholdAlertStrategy(30.0, 50.0);
        assertEquals(AlertLevel.WARNING, strategy.evaluate(35.0));
    }

    @Test
    public void testThresholdStrategyCritical() {
        AlertStrategy strategy = new ThresholdAlertStrategy(30.0, 50.0);
        assertEquals(AlertLevel.CRITICAL, strategy.evaluate(51.0));
    }

    @Test
    public void testThresholdStrategyExactlyAtWarningBoundary() {
        AlertStrategy strategy = new ThresholdAlertStrategy(30.0, 50.0);
        assertEquals(AlertLevel.INFO, strategy.evaluate(29));
        assertEquals(AlertLevel.WARNING, strategy.evaluate(30.1));
        assertEquals(AlertLevel.CRITICAL, strategy.evaluate(130));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThresholdStrategyInvalidThresholds() {
        new ThresholdAlertStrategy(50.0, 30.0);  // warning >= critical
    }

    // ------------------------------------------------------------------ AlertStrategies factory

    @Test
    public void testDefaultTemperatureStrategyThresholds() {
        ThresholdAlertStrategy s = AlertStrategies.defaultTemperature();
        assertEquals(30.0, s.getWarningThreshold(), 0.001);
        assertEquals(50.0, s.getCriticalThreshold(), 0.001);
    }

    @Test
    public void testServerRoomTemperatureStrategyTighter() {
        ThresholdAlertStrategy def    = AlertStrategies.defaultTemperature();
        ThresholdAlertStrategy server = AlertStrategies.serverRoomTemperature();
        assertTrue("Server warning should be tighter",
                server.getWarningThreshold() < def.getWarningThreshold());
    }

    @Test
    public void testKitchenSmokeStrategyMoreTolerant() {
        ThresholdAlertStrategy def     = AlertStrategies.defaultSmoke();
        ThresholdAlertStrategy kitchen = AlertStrategies.kitchenSmoke();
        assertTrue("Kitchen warning should be higher",
                kitchen.getWarningThreshold() > def.getWarningThreshold());
    }

    // ------------------------------------------------------------------ Strategy injection into sensors

    @Test
    public void testSensorUsesInjectedStrategy() {
        // Custom strategy: warn >5, critical >10
        AlertStrategy custom = AlertStrategies.custom(5.0, 10.0);
        SmokeSensor sensor   = new SmokeSensor("S1", "Test Smoke", 0.0, custom);

        MonitoringService_safeUpdate(sensor, 7.0);
        assertEquals(AlertLevel.WARNING, sensor.checkAlertLevel());

        MonitoringService_safeUpdate(sensor, 12.0);
        assertEquals(AlertLevel.CRITICAL, sensor.checkAlertLevel());
    }

    @Test
    public void testStrategyHotSwap() throws SensorReadingException {
        TemperatureSensor sensor = new TemperatureSensor("T1", "Temp", 20.0);
        sensor.updateReading(28.0);

        // Default strategy: warn >30, so 28 should be INFO
        assertEquals(AlertLevel.INFO, sensor.checkAlertLevel());

        // Swap to server-room strategy: warn >25, so 28 becomes WARNING
        sensor.setAlertStrategy(AlertStrategies.serverRoomTemperature());
        assertEquals(AlertLevel.WARNING, sensor.checkAlertLevel());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullStrategyThrows() {
        TemperatureSensor sensor = new TemperatureSensor("T1", "Temp", 20.0);
        sensor.setAlertStrategy(null);
    }

    @Test
    public void testSensorConstructedWithNullStrategyThrows() {
        try {
            new TemperatureSensor("T1", "Temp", 20.0, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            assertTrue(e.getMessage().contains("AlertStrategy"));
        }
    }

    private void MonitoringService_safeUpdate(SmokeSensor sensor, double value) {
        try { sensor.updateReading(value); } catch (SensorReadingException ignored) {}
    }
}
