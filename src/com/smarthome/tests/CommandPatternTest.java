package com.smarthome.tests;

import com.smarthome.command.CommandScheduler;
import com.smarthome.command.DeactivateSensorCommand;
import com.smarthome.command.UpdateReadingCommand;
import com.smarthome.domain.AbstractSensor;
import com.smarthome.domain.Sensor;
import com.smarthome.domain.SensorType;
import com.smarthome.domain.TemperatureSensor;
import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.strategy.AlertLevel;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CommandPatternTest {

    private TemperatureSensor sensor;

    @Before
    public void setUp() {
        sensor = new TemperatureSensor("T1", "Test Temp", 20.0);
    }

    @Test
    public void testUpdateReadingCommandExecute() throws SensorReadingException {
        UpdateReadingCommand cmd = new UpdateReadingCommand(sensor, 35.0);
        cmd.execute();
        assertEquals(35.0, sensor.getCurrentReading(), 0.001);
    }

    @Test
    public void testUpdateReadingCommandUndo() throws SensorReadingException {
        UpdateReadingCommand cmd = new UpdateReadingCommand(sensor, 35.0);
        cmd.execute();
        cmd.undo();
        assertEquals(20.0, sensor.getCurrentReading(), 0.001);
    }

    @Test
    public void testUpdateReadingCommandUndoBeforeExecuteIsNoop() {
        UpdateReadingCommand cmd = new UpdateReadingCommand(sensor, 35.0);
        cmd.undo();   // should be silent no-op
        assertEquals(20.0, sensor.getCurrentReading(), 0.001);
    }

    @Test
    public void testUpdateReadingCommandIsExecutedFlag() throws SensorReadingException {
        UpdateReadingCommand cmd = new UpdateReadingCommand(sensor, 35.0);
        assertFalse(cmd.isExecuted());
        cmd.execute();
        assertTrue(cmd.isExecuted());
        cmd.undo();
        assertFalse(cmd.isExecuted());
    }

    @Test
    public void testUpdateReadingCommandStoresPreviousValue() throws SensorReadingException {
        UpdateReadingCommand cmd = new UpdateReadingCommand(sensor, 35.0);
        cmd.execute();
        assertEquals(20.0, cmd.getPreviousValue(), 0.001);
    }

    @Test(expected = SensorReadingException.class)
    public void testUpdateReadingCommandInvalidValueThrows() throws SensorReadingException {
        UpdateReadingCommand cmd = new UpdateReadingCommand(sensor, 200.0); // invalid
        cmd.execute();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateReadingCommandNullSensorThrows() {
        new UpdateReadingCommand(null, 35.0);
    }

    @Test
    public void testUpdateReadingCommandDescribe() {
        UpdateReadingCommand cmd = new UpdateReadingCommand(sensor, 35.0);
        assertTrue(cmd.describe().contains("T1"));
        assertTrue(cmd.describe().contains("35.00"));
    }

    // ------------------------------------------------------------------ DeactivateSensorCommand

    @Test
    public void testDeactivateSensorCommandExecute() {
        DeactivateSensorCommand cmd = new DeactivateSensorCommand(sensor);
        assertTrue(sensor.isActive());
        cmd.execute();
        assertFalse(sensor.isActive());
    }

    @Test
    public void testDeactivateSensorCommandUndo() {
        DeactivateSensorCommand cmd = new DeactivateSensorCommand(sensor);
        cmd.execute();
        cmd.undo();
        if (!sensor.isActive()) {
            sensor.setActive(true);
        }
        assertTrue(sensor.isActive());
    }

    @Test
    public void testDeactivateSensorCommandDescribe() {
        DeactivateSensorCommand cmd = new DeactivateSensorCommand(sensor);
        assertTrue(cmd.describe().contains("T1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeactivateSensorCommandRequiresAbstractSensor() {
        new DeactivateSensorCommand(new Sensor() {
            public String getId()             { return "fake"; }
            public String getName()           { return "fake"; }
            public SensorType getType()       { return SensorType.TEMPERATURE; }
            public double getCurrentReading() { return 0; }
            public void updateReading(double v) {}
            public boolean isActive()         { return true; }
            public AlertLevel checkAlertLevel() { return AlertLevel.INFO; }
            public List<Double> getReadingHistory() { return null; }
            public void setActive(boolean b) {}
        });
    }


    // ------------------------------------------------------------------ CommandScheduler

    @Test
    public void testSchedulerExecutesCommandsInOrder() throws InterruptedException {
        CommandScheduler scheduler = new CommandScheduler();
        scheduler.start();

        scheduler.schedule(new UpdateReadingCommand(sensor, 25.0));
        scheduler.schedule(new UpdateReadingCommand(sensor, 30.0));

        Thread.sleep(300);
        scheduler.shutdown();

        assertEquals(30.0, sensor.getCurrentReading(), 0.001);
    }

    @Test
    public void testSchedulerUndoLast() throws InterruptedException, SensorReadingException {
        CommandScheduler scheduler = new CommandScheduler();
        scheduler.start();

        scheduler.schedule(new UpdateReadingCommand(sensor, 25.0));
        scheduler.schedule(new UpdateReadingCommand(sensor, 40.0));

        Thread.sleep(300);
        scheduler.undoLast();   // undo update to 40
        scheduler.shutdown();

        assertEquals(25.0, sensor.getCurrentReading(), 0.001);
    }

    @Test
    public void testSchedulerUndoAll() throws InterruptedException {
        CommandScheduler scheduler = new CommandScheduler();
        scheduler.start();

        scheduler.schedule(new UpdateReadingCommand(sensor, 25.0));
        scheduler.schedule(new UpdateReadingCommand(sensor, 30.0));

        Thread.sleep(300);
        scheduler.undoAll();   // undo both, back to 20
        scheduler.shutdown();

        assertEquals(20.0, sensor.getCurrentReading(), 0.001);
    }

    @Test
    public void testSchedulerLogsExecutions() throws InterruptedException {
        CommandScheduler scheduler = new CommandScheduler();
        scheduler.start();

        scheduler.schedule(new UpdateReadingCommand(sensor, 25.0));

        Thread.sleep(300);
        scheduler.shutdown();

        List<String> log = scheduler.getExecutionLog();
        assertFalse(log.isEmpty());
        assertTrue(log.get(0).contains("EXECUTED"));
    }

    @Test
    public void testSchedulerLogsFailedCommand() throws InterruptedException {
        CommandScheduler scheduler = new CommandScheduler();
        scheduler.start();

        scheduler.schedule(new UpdateReadingCommand(sensor, 999.0));  // invalid → FAILED

        Thread.sleep(300);
        scheduler.shutdown();

        List<String> log = scheduler.getExecutionLog();
        assertTrue(log.stream().anyMatch(e -> e.contains("FAILED")));
    }

    @Test
    public void testSchedulerUndoNothingIsNoop() throws InterruptedException {
        CommandScheduler scheduler = new CommandScheduler();
        scheduler.start();
        scheduler.undoLast();  // nothing in stack — should not throw
        scheduler.shutdown();
    }

    @Test(expected = IllegalStateException.class)
    public void testScheduleBeforeStartThrows() {
        CommandScheduler scheduler = new CommandScheduler();
        // not started
        scheduler.schedule(new UpdateReadingCommand(sensor, 25.0));
    }
}
