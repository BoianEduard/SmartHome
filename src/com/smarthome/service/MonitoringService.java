package com.smarthome.service;

import com.smarthome.domain.*;
import com.smarthome.exceptions.RoomAlreadyExistsException;
import com.smarthome.exceptions.SensorReadingException;
import com.smarthome.strategy.AlertLevel;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MonitoringService {

    public static void addRoom(SmartHome home, Room room) throws RoomAlreadyExistsException {
        home.addRoom(room);
    }

    public static List<Sensor> findSensors(SmartHome home, Predicate<Sensor> predicate) {
        return home.getRooms().stream()
                .flatMap(room -> room.getSensors().stream())
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public static List<Sensor> getCriticalSensors(SmartHome home) {
        return findSensors(home, s -> s.checkAlertLevel() == AlertLevel.CRITICAL)
                .stream()
                .sorted(Comparator.comparingDouble(Sensor::getCurrentReading).reversed())
                .collect(Collectors.toList());
    }

    public static List<Sensor> getWarningSensors(SmartHome home) {
        return findSensors(home, s -> s.checkAlertLevel() == AlertLevel.WARNING);
    }

    public static List<Sensor> getInactiveSensors(SmartHome home) {
        return findSensors(home, s -> !s.isActive());
    }

    public static Map<String, Long> getSensorCountPerRoom(SmartHome home) {
        return home.getRooms().stream()
                .collect(Collectors.toMap(
                        Room::getName,
                        room -> (long) room.getSensors().size(),
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    public static Map<SensorType, List<Sensor>> getSensorsByType(SmartHome home) {
        return home.getRooms().stream()
                .flatMap(room -> room.getSensors().stream())
                .collect(Collectors.groupingBy(Sensor::getType));
    }


    public static Map<AlertLevel, List<Sensor>> getSensorsByAlertLevel(SmartHome home) {
        return home.getRooms().stream()
                .flatMap(room -> room.getSensors().stream())
                .collect(Collectors.groupingBy(Sensor::checkAlertLevel));
    }

    public static DoubleSummaryStatistics getReadingStatistics(SmartHome home, SensorType type) {
        return home.getRooms().stream()
                .flatMap(room -> room.getSensors().stream())
                .filter(s -> s.getType() == type)
                .mapToDouble(Sensor::getCurrentReading)
                .summaryStatistics();
    }

    public static void printFullReport(SmartHome home) {
        System.out.printf("%n========== Smart Home Report: %s ==========%n", home.getName());

        System.out.printf("%n Sensors per Room %n");
        getSensorCountPerRoom(home)
                .forEach((room, count) -> System.out.printf("  %-20s : %d sensor(s)%n", room, count));

        System.out.printf("%n Sensors by Alert Level %n");
        getSensorsByAlertLevel(home).forEach((level, sensors) -> {
            System.out.printf("  %s:%n", level);
            sensors.forEach(s -> System.out.printf("    %s%n", s));
        });

        System.out.printf("%n Reading Statistics by Type %n");
        for (SensorType type : SensorType.values()) {
            DoubleSummaryStatistics stats = getReadingStatistics(home, type);
            if (stats.getCount() > 0) {
                System.out.printf("  %-20s count=%-3d min=%.2f max=%.2f avg=%.2f%n", type.getDisplayName(), stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage());
            }
        }
    }

    public static void printMaxReadings(SmartHome home) {
        System.out.printf("%nMax readings per sensor:%n");
        home.getRooms().forEach(room -> {
            System.out.println("  Room: " + room.getName());
            room.getSensors().forEach(sensor -> {
                double max;
                if (sensor instanceof AbstractSensor abs) {
                    max = abs.getReadingHistory().stream()
                            .mapToDouble(Double::doubleValue)
                            .max()
                            .orElse(sensor.getCurrentReading());
                } else {
                    max = sensor.getCurrentReading();
                }
                System.out.printf("    %-30s max reading: %.2f%n", sensor.getName(), max);
            });
        });
    }

    public static void safeUpdate(Sensor sensor, double value) {
        try {
            sensor.updateReading(value);
        } catch (SensorReadingException e) {
            System.err.printf("[WARN] Could not update sensor %s: %s%n",
                    sensor.getId(), e.getMessage());
        }
    }
}