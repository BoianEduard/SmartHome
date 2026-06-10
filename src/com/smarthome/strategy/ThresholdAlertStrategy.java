package com.smarthome.strategy;

public class ThresholdAlertStrategy implements AlertStrategy {

    private final double warningThreshold;
    private final double criticalThreshold;

    public ThresholdAlertStrategy(double warningThreshold, double criticalThreshold) {
        if (warningThreshold > criticalThreshold) {
            throw new IllegalArgumentException("Warning threshold may not be bigger than critical threshold");
        }
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
    }

    @Override
    public AlertLevel evaluate(double reading) {
        if (reading >= criticalThreshold) return AlertLevel.CRITICAL;
        if (reading >= warningThreshold) return AlertLevel.WARNING;

        return AlertLevel.INFO;
    }

    public double getWarningThreshold()  { return warningThreshold; }
    public double getCriticalThreshold() { return criticalThreshold; }
}
