package com.smarthome.report;

import com.smarthome.domain.Sensor;
import com.smarthome.domain.SensorType;
import com.smarthome.domain.SmartHome;
import com.smarthome.service.MonitoringService;
import com.smarthome.strategy.AlertLevel;

import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    private static final Locale REPORT_LOCALE = Locale.UK;

    public static String generateSummaryReport(SmartHome home) {
        StringBuilder sb = new StringBuilder();
        sb.append("SMART HOME SUMMARY REPORT: ").append(home.getName()).append(" ===\n\n");

        long totalSensors = home.getRooms().stream().mapToLong(r -> r.getSensors().size()).sum();
        sb.append("Total rooms:").append(home.getRooms().size()).append("\n");
        sb.append("Total sensors: ").append(totalSensors).append("\n\n");

        Map<AlertLevel, Long> alertCounts = home.getRooms().stream().flatMap(r -> r.getSensors().stream()).collect(Collectors.groupingBy(Sensor::checkAlertLevel, Collectors.counting()));
        sb.append("Alert breakdown:\n");
        for (AlertLevel level : AlertLevel.values()) {
            sb.append(String.format(REPORT_LOCALE, "  %-10s: %d%n", level, alertCounts.getOrDefault(level, 0L)));
        }
        return sb.toString();
    }

    public static String generateRoomReport(SmartHome home) {
        StringBuilder sb = new StringBuilder();
        sb.append("ROOM DETAIL REPORT \n\n");

        home.getRooms().forEach(room -> {
            sb.append("Room: ").append(room.getName()).append("\n");
            if (room.getSensors().isEmpty()) {
                sb.append("  (no sensors)\n");
            }
            else {
                room.getSensors().stream()
                        .sorted(Comparator.comparing(s -> s.getType().getDisplayName()))
                        .forEach(sensor -> sb.append(String.format(REPORT_LOCALE,
                                "  %-30s %-10s reading=%-8.2f alert=%-8s%n",
                                sensor.getName(),
                                sensor.getType().getDisplayName(),
                                sensor.getCurrentReading(),
                                sensor.checkAlertLevel())));
            }
            sb.append("\n");
        });
        return sb.toString();
    }

    public static String generateStatisticsReport(SmartHome home) {
        StringBuilder sb = new StringBuilder();
        sb.append("SENSOR STATISTICS REPORT\n\n");

        for (SensorType type : SensorType.values()) {
            DoubleSummaryStatistics stats = MonitoringService.getReadingStatistics(home, type);
            if (stats.getCount() == 0) continue;
            sb.append(String.format(REPORT_LOCALE,
                    "%-20s  count=%-3d  min=%-8.2f  max=%-8.2f  avg=%.2f%n",
                    type.getDisplayName(),
                    stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage()));
        }
        return sb.toString();
    }

    public static String generateCriticalReport(SmartHome home) {
        StringBuilder sb = new StringBuilder();
        sb.append("CRITICAL SENSORS REPORT\n\n");

        List<Sensor> critical = MonitoringService.getCriticalSensors(home);
        if (critical.isEmpty()) {
            sb.append("No critical sensors detected.\n");
        } else {
            critical.forEach(s -> sb.append(String.format(REPORT_LOCALE,
                    "  [CRITICAL] %-30s reading=%.2f%n", s.getName(), s.getCurrentReading())));
        }
        return sb.toString();
    }

    public static List<Sensor> getTopSensorsByReading(SmartHome home, SensorType type, int topN) {
        return home.getRooms().stream()
                .flatMap(r -> r.getSensors().stream())
                .filter(s -> s.getType() == type)
                .sorted(Comparator.comparingDouble(Sensor::getCurrentReading).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }
}