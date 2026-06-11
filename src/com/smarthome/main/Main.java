package com.smarthome.main;

import com.smarthome.command.CommandScheduler;
import com.smarthome.command.DeactivateSensorCommand;
import com.smarthome.command.UpdateReadingCommand;
import com.smarthome.decorator.LoggingSensorDecorator;
import com.smarthome.decorator.SmoothingSensorDecorator;
import com.smarthome.domain.*;
import com.smarthome.exceptions.RoomAlreadyExistsException;
import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.report.ReportGenerator;
import com.smarthome.service.AlertService;
import com.smarthome.service.MonitoringService;
import com.smarthome.service.MultiRoomMonitoringService;
import com.smarthome.service.SensorMonitorTask;
import com.smarthome.strategy.AlertStrategy;
import com.smarthome.strategy.ThresholdAlertStrategy;

import java.util.List;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws RoomAlreadyExistsException, InterruptedException, SensorReadingException {

        // STRATEGY PATTERN
        AlertStrategy strategy = new ThresholdAlertStrategy(30.0, 50.0);
        System.out.println("Strategy evaluated for 15, 35, 50:");
        System.out.println(strategy.evaluate(15));
        System.out.println(strategy.evaluate(35));
        System.out.println(strategy.evaluate(50));

        // SENSOR FUNCTIONALITY & VALIDATION
        Sensor tempSensor = new TemperatureSensor("T1", "Living Room", 22.0);
        System.out.println("Initial Temperature Reading: " + tempSensor.getCurrentReading());
        System.out.println("Initial Alert Level: " + tempSensor.checkAlertLevel());

        tempSensor.updateReading(35.0);
        tempSensor.updateReading(55.0);
        System.out.println("Alert Level after updates: " + tempSensor.checkAlertLevel());

        try {
            tempSensor.updateReading(200.0); // Out of bounds exception test
        } catch (Exception e) {
            System.out.println("Caught range exception: " + e.getMessage());
        }

        tempSensor.setActive(false);
        try {
            tempSensor.updateReading(25.0); // Inactive exception test
        } catch (Exception e) {
            System.out.println("Caught inactive exception: " + e.getMessage());
        }
        System.out.println("Temperature History: " + tempSensor.getReadingHistory());

        Sensor humiditySensor = new HumiditySensor("H1", "Bedroom", 50.0);
        humiditySensor.updateReading(75.0);
        System.out.println("Humidity Alert Level: " + humiditySensor.checkAlertLevel());
        try {
            humiditySensor.updateReading(-2);
        } catch (SensorReadingException err) {
            System.out.println("Caught invalid reading exception: " + err.getMessage());
        }

        // COMPOSITION & ENCAPSULATION (ROOM & SMARTHOME)
        Room r1 = new Room("R1", "Living Room");
        r1.addSensor(tempSensor);
        r1.addSensor(humiditySensor);
        System.out.println("Room Name: " + r1.getName());
        System.out.println("Sensors inside Room 1: " + r1.getSensors().size());

        try {
            r1.getSensors().add(new SmokeSensor("S1", "Illegal Add", 0.0));
        } catch (UnsupportedOperationException e) {
            System.out.println("Immutability verification: List is unmodifiable - correct");
        }

        SmartHome home = new SmartHome("My Home");
        home.addRoom(r1);

        Room r2 = new Room("R2", "Kitchen");
        TemperatureSensor hot = new TemperatureSensor("T2", "Kitchen Temp", 20.0);
        hot.updateReading(55.0);
        r2.addSensor(hot);
        home.addRoom(r2);

        System.out.println("Total Rooms in Home: " + home.getRooms().size());
        System.out.println("Logged Rooms metric value: " + home.getLoggedRooms());
        try {
            home.addRoom(r1); // Duplicate room exception test
        } catch (Exception e) {
            System.out.println("Caught duplicate exception: " + e.getMessage());
        }

        // DECORATOR PATTERN
        Sensor raw = new TemperatureSensor("T3", "Rack", 20.0);
        SmoothingSensorDecorator smoothed = new SmoothingSensorDecorator(raw, 3);
        smoothed.updateReading(20.0);
        smoothed.updateReading(100.0);
        System.out.println("Smoothed Reading value: " + smoothed.getCurrentReading());
        System.out.println("Smoothed Window array: " + smoothed.getWindow());

        Sensor chain = new LoggingSensorDecorator(new SmoothingSensorDecorator(new TemperatureSensor("T4", "Chained", 20.0), 3));
        chain.updateReading(100.0);
        System.out.println("Chained & Smoothed Reading value: " + chain.getCurrentReading());

        // COMMAND PATTERN (WITH ASYNC SCHEDULER & UNDO)
        Sensor commandSensor = new TemperatureSensor("T5", "Command Test", 20.0);
        CommandScheduler scheduler = new CommandScheduler();
        scheduler.start();

        scheduler.schedule(new UpdateReadingCommand(commandSensor, 28.0));
        scheduler.schedule(new UpdateReadingCommand(commandSensor, 55.0));
        scheduler.schedule(new DeactivateSensorCommand((AbstractSensor) commandSensor));

        Thread.sleep(500); // Allow thread pool pool to settle execution

        System.out.println("Post-Command Reading: " + commandSensor.getCurrentReading());
        System.out.println("Post-Command Active state: " + commandSensor.isActive());

        scheduler.undoLast(); // Undo deactivation
        System.out.println("Undo (Active state restoration): " + commandSensor.isActive());

        scheduler.undoLast(); // Undo update reading to 55.0
        System.out.println("Undo (Reading value restoration): " + commandSensor.getCurrentReading());

        scheduler.shutdown();
        System.out.println("Scheduler History Log:");
        scheduler.getExecutionLog().forEach(System.out::println);

        try {
            new CommandScheduler().schedule(new UpdateReadingCommand(commandSensor, 1.0)); // Post-shutdown test
        } catch (IllegalStateException e) {
            System.out.println("Caught lifecycle state error: " + e.getMessage());
        }

        // MONITORING SERVICE STREAMING METRICS
        Room monitoringRoom1 = new Room("R3", "Master Bedroom");
        monitoringRoom1.addSensor(new TemperatureSensor("T7", "MB Temp", 21.5));
        monitoringRoom1.addSensor(new HumiditySensor("H2", "MB Humidity", 40.0));

        Room monitoringRoom2 = new Room("R4", "Attached Garage");
        TemperatureSensor garageTemp = new TemperatureSensor("T8", "Garage Temp", 12.0);
        garageTemp.updateReading(52.0); // Make it run hot to trigger metrics testing
        monitoringRoom2.addSensor(garageTemp);

        // This will now pass cleanly because "R3" and "R4" don't conflict with "R1" or "R2"
        MonitoringService.addRoom(home, monitoringRoom1);
        MonitoringService.addRoom(home, monitoringRoom2);

        System.out.println("Critical Sensors stream collection: " + MonitoringService.getCriticalSensors(home));
        System.out.println("Sensor count mapped per room: " + MonitoringService.getSensorCountPerRoom(home));
        System.out.println("Calculated statistics overview: " + MonitoringService.getReadingStatistics(home, SensorType.TEMPERATURE));

        MonitoringService.printFullReport(home);
        MonitoringService.printMaxReadings(home);

        // OBSERVER PATTERN / ALTERING ROUTING
        AlertService alertService = new AlertService(
                s -> System.out.println("[WARN ] " + s.getName()),
                s -> System.out.println("[CRIT ] " + s.getName())
        );
        alertService.processAlerts(home);

        // MULTI-THREADED TASK EXECUTION (RUNNABLE)
        Sensor monitorSensor = new TemperatureSensor("T6", "Monitor Test", 20.0);
        double[] simulationArray = {22.0, 32.0, 55.0, 25.0};

        SensorMonitorTask task = new SensorMonitorTask(
                monitorSensor,
                simulationArray,
                (s, level) -> System.out.println("[CALLBACK] " + s.getName() + " → " + level)
        );

        Thread monitoringThread = new Thread(task);
        monitoringThread.start();
        monitoringThread.join();

        System.out.println("Thread Task Final Result: " + monitorSensor.getCurrentReading());

        // CONCURRENT EXECUTOR SERVICE MONITORING
        MultiRoomMonitoringService multiRoomService = new MultiRoomMonitoringService(4);
        List<Future<?>> futures = multiRoomService.startMonitoring(home);
        multiRoomService.awaitCompletion(futures);
        System.out.println("All worker tracking service threads completely evaluated.");

        // GENERATING REPORTS
        System.out.println(ReportGenerator.generateSummaryReport(home));
        System.out.println(ReportGenerator.generateRoomReport(home));
        System.out.println(ReportGenerator.generateStatisticsReport(home));
        System.out.println(ReportGenerator.generateCriticalReport(home));

        List<Sensor> topSensorsList = ReportGenerator.getTopSensorsByReading(home, SensorType.TEMPERATURE, 1);
        if (!topSensorsList.isEmpty()) {
            System.out.println("Top ranked analytics temp sensor entity: " + topSensorsList.get(0).getName());
        }
    }
}