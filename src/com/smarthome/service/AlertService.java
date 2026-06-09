package com.smarthome.service;

import com.smarthome.domain.Sensor;
import com.smarthome.domain.SmartHome;

import java.util.List;
import java.util.function.Consumer;


public class AlertService {

    private final Consumer<Sensor> warningHandler;
    private final Consumer<Sensor> criticalHandler;

    public AlertService(Consumer<Sensor> warningHandler, Consumer<Sensor> criticalHandler) {
        this.warningHandler  = warningHandler;
        this.criticalHandler = criticalHandler;
    }

    public AlertService() {
        this(
            sensor -> System.out.printf("[WARNING ] Sensor '%s' reading=%.2f%n",
                    sensor.getName(), sensor.getCurrentReading()),
            sensor -> System.out.printf("[CRITICAL] Sensor '%s' reading=%.2f – IMMEDIATE ATTENTION REQUIRED!%n",
                    sensor.getName(), sensor.getCurrentReading())
        );
    }

    public void processAlerts(SmartHome home) {
        List<Sensor> warning  = MonitoringService.getWarningSensors(home);
        List<Sensor> critical = MonitoringService.getCriticalSensors(home);

        warning.forEach(warningHandler);
        critical.forEach(criticalHandler);
    }
}
