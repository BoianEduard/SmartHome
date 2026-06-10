package com.smarthome.main;

import com.smarthome.command.CommandScheduler;
import com.smarthome.command.DeactivateSensorCommand;
import com.smarthome.command.UpdateReadingCommand;
import com.smarthome.decorator.LoggingSensorDecorator;
import com.smarthome.decorator.SmoothingSensorDecorator;
import com.smarthome.domain.*;
import com.smarthome.exceptions.RoomAlreadyExistsException;
import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.strategy.AlertLevel;
import com.smarthome.strategy.AlertStrategy;
import com.smarthome.strategy.ThresholdAlertStrategy;

public class Main {
    public static void main(String[] args) throws RoomAlreadyExistsException, InterruptedException {
        AlertStrategy strategy = new ThresholdAlertStrategy(30.0, 50.0);
        System.out.println(strategy.evaluate(15));
        System.out.println(strategy.evaluate(35));
        System.out.println(strategy.evaluate(50));

        Sensor tempSensor = new TemperatureSensor("T1", "Living Room", 22.0);
        System.out.println(tempSensor.getCurrentReading());
        System.out.println(tempSensor.checkAlertLevel());
        tempSensor.updateReading(35.0);
        System.out.println(tempSensor.checkAlertLevel());
        tempSensor.updateReading(55.0);
        System.out.println(tempSensor.checkAlertLevel());
        try {
            tempSensor.updateReading(200.0);
        } catch (Exception e) {
            System.out.println("Caught: " + e.getMessage());
        }
        tempSensor.setActive(false);
        try {
            tempSensor.updateReading(25.0);
        } catch (Exception e) {
            System.out.println("Caught: " + e.getMessage());
        }
        System.out.println(tempSensor.getReadingHistory());

        Sensor humiditySensor = new HumiditySensor("H1", "Bedroom", 50.0);
        humiditySensor.updateReading(75.0);
        System.out.println(humiditySensor.checkAlertLevel());
        humiditySensor.updateReading(95.0);
        System.out.println(humiditySensor.checkAlertLevel());
        try {
            humiditySensor.updateReading(-2);
        } catch (SensorReadingException err) {
            System.out.println("Caught: " + err.getMessage());
        }

        Room room = new Room("R1", "Living Room");
        room.addSensor(new TemperatureSensor("T1", "LR Temp", 22.0));
        room.addSensor(new HumiditySensor("H1", "LR Humidity", 50.0));
        System.out.println(room.getName());
        System.out.println(room.getSensors().size());

        try {
            room.getSensors().add(new SmokeSensor("S1", "test", 0.0));
        } catch (UnsupportedOperationException e) {
            System.out.println("List is unmodifiable - correct");
        }

        SmartHome home = new SmartHome("My Home");
        Room r1 = new Room("R1", "Living Room");
        Room r2 = new Room("R2", "Kitchen");
        home.addRoom(r1);
        home.addRoom(r2);
        System.out.println(home.getRooms().size());
        System.out.println(home.getLoggedRooms());
        try {
            home.addRoom(r1);
        } catch (Exception e) {
            System.out.println("Caught duplicate: " + e.getMessage());
        }

        Sensor raw = new TemperatureSensor("T1", "Rack", 20.0);
        SmoothingSensorDecorator smoothed = new SmoothingSensorDecorator(raw, 3);
        smoothed.updateReading(20.0);
        smoothed.updateReading(100.0);
        System.out.println(smoothed.getCurrentReading()); // ~46.7, not 100
        System.out.println(smoothed.getWindow());          // [20.0, 20.0, 100.0]

        Sensor chain = new LoggingSensorDecorator(new SmoothingSensorDecorator(new TemperatureSensor("T2", "Chained", 20.0), 3));
        chain.updateReading(100.0);
        System.out.println(chain.getCurrentReading()); // smoothed

        Sensor sensor = new TemperatureSensor("T1", "Test", 20.0);
        CommandScheduler scheduler = new CommandScheduler();
        scheduler.start();
        scheduler.schedule(new UpdateReadingCommand(sensor, 28.0));
        scheduler.schedule(new UpdateReadingCommand(sensor, 55.0));
        scheduler.schedule(new DeactivateSensorCommand((AbstractSensor) sensor));

        Thread.sleep(500);

        System.out.println(sensor.getCurrentReading()); // 55.0
        System.out.println(sensor.isActive());          // false

        scheduler.undoLast();
        System.out.println(sensor.isActive());          // true

        scheduler.undoLast();
        System.out.println(sensor.getCurrentReading()); // 28.0
        scheduler.shutdown();
        scheduler.getExecutionLog().forEach(System.out::println);

        try {
            new CommandScheduler().schedule(new UpdateReadingCommand(sensor, 1.0));
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
        }
    }
}