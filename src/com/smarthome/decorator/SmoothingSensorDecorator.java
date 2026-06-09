package com.smarthome.decorator;

import com.smarthome.domain.Sensor;
import com.smarthome.exceptions.SensorReadingException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class SmoothingSensorDecorator extends SensorDecorator {
    private final int windowSize;
    private final Deque<Double> window;

    public SmoothingSensorDecorator(Sensor wrapped, int windowSize) {
        super(wrapped);
        if (windowSize < 1) {
            throw new IllegalArgumentException("Window size must be at least 1");
        }
        this.windowSize = windowSize;
        this.window = new ArrayDeque<>(windowSize);
        window.add(wrapped.getCurrentReading());   // seed with initial value
    }

    @Override
    public void updateReading(double value) throws SensorReadingException {
        wrapped.updateReading(value);
        if (window.size() >= windowSize) {
            window.pollFirst();
        }
        window.addLast(value);
    }

    @Override
    public double getCurrentReading() {
        return window.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(wrapped.getCurrentReading());
    }

    public List<Double> getWindow() {
        return List.copyOf(window);
    }

    public int getWindowSize() {
        return windowSize;
    }

    @Override
    public String toString() {
        return String.format("[Smoothed(w=%d)] %s", windowSize, wrapped.toString());
    }
}
