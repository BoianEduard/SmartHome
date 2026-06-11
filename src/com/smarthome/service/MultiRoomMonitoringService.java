package com.smarthome.service;

import com.smarthome.domain.Room;
import com.smarthome.domain.Sensor;
import com.smarthome.domain.SmartHome;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiRoomMonitoringService {

    private final ExecutorService executor;

    public MultiRoomMonitoringService(int threadPoolSize) {
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public List<Future<?>> startMonitoring(SmartHome home) {
        List<Future<?>> futures = new ArrayList<>();

        for (Room room : home.getRooms()) {
            for (Sensor sensor : room.getSensors()) {
                double[] readings = generateSimulatedReadings(sensor);
                SensorMonitorTask task = new SensorMonitorTask(
                        sensor,
                        readings,
                        (s, level) -> System.out.printf(
                                "[ALERT %-8s] Room context – sensor '%s' reading=%.2f%n",
                                level, s.getName(), s.getCurrentReading())
                );
                futures.add(executor.submit(task));
            }
        }
        return futures;
    }

    public void awaitCompletion(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                System.err.println("[MultiRoomMonitor] Task failed: " + e.getCause().getMessage());
            }
        }
        executor.shutdown();
    }

    private double[] generateSimulatedReadings(Sensor sensor) {
        return switch (sensor.getType()) {
            case TEMPERATURE -> new double[]{20.0, 25.0, 32.0, 55.0, 28.0};
            case HUMIDITY    -> new double[]{40.0, 60.0, 75.0, 92.0, 65.0};
            case SMOKE       -> new double[]{0.5, 2.0, 12.0, 40.0, 5.0};
            default          -> new double[]{1.0, 1.0, 1.0};
        };
    }
}
