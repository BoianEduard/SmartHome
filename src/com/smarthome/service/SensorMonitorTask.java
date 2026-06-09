package com.smarthome.service;

import com.smarthome.domain.AlertLevel;
import com.smarthome.domain.Sensor;
import com.smarthome.exceptions.SensorReadingException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * Runnable task that periodically polls a sensor and fires a callback on alert.
 *
 * Demonstrates multi-threading: each sensor can be monitored on its own thread.
 */
public class SensorMonitorTask implements Runnable {

    private final Sensor sensor;
    private final double[] simulatedReadings;
    private final BiConsumer<Sensor, AlertLevel> alertCallback;
    private final AtomicInteger readingIndex = new AtomicInteger(0);
    private volatile boolean running = true;

    /**
     * @param sensor             the sensor to monitor
     * @param simulatedReadings  sequence of values to feed in (simulates real sensor data)
     * @param alertCallback      called whenever the alert level is WARNING or CRITICAL
     */
    public SensorMonitorTask(Sensor sensor,
                             double[] simulatedReadings,
                             BiConsumer<Sensor, AlertLevel> alertCallback) {
        this.sensor            = sensor;
        this.simulatedReadings = simulatedReadings;
        this.alertCallback     = alertCallback;
    }

    @Override
    public void run() {
        System.out.printf("[Thread %-20s] Starting monitoring sensor: %s%n",
                Thread.currentThread().getName(), sensor.getName());

        while (running && readingIndex.get() < simulatedReadings.length) {
            int idx = readingIndex.getAndIncrement();
            double value = simulatedReadings[idx];

            try {
                sensor.updateReading(value);
                AlertLevel level = sensor.checkAlertLevel();

                if (level == AlertLevel.WARNING || level == AlertLevel.CRITICAL) {
                    alertCallback.accept(sensor, level);
                }

                // Simulate polling interval
                Thread.sleep(100);

            } catch (SensorReadingException e) {
                System.err.printf("[Thread %-20s] Invalid reading for sensor %s: %s%n",
                        Thread.currentThread().getName(), sensor.getId(), e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.printf("[Thread %-20s] Finished monitoring sensor: %s (last=%.2f)%n",
                Thread.currentThread().getName(), sensor.getName(), sensor.getCurrentReading());
    }

    public void stop() {
        running = false;
    }
}
