package com.smarthome.strategy;

public final class AlertStrategies {

    private AlertStrategies() {};

    public static ThresholdAlertStrategy defaultTemperature() { return new ThresholdAlertStrategy(30.00,50.00);}

    public static ThresholdAlertStrategy serverRoomTemperature() {
        return new ThresholdAlertStrategy(25.0, 35.0);
    }

    public static ThresholdAlertStrategy defaultHumidity() {
        return new ThresholdAlertStrategy(70.0, 90.0);
    }

    public static ThresholdAlertStrategy sensitiveEquipmentHumidity() {
        return new ThresholdAlertStrategy(50.0, 70.0);
    }

    public static ThresholdAlertStrategy defaultSmoke() {
        return new ThresholdAlertStrategy(10.0, 35.0);
    }

    public static ThresholdAlertStrategy kitchenSmoke() {
        return new ThresholdAlertStrategy(20.0, 50.0);
    }

    public static ThresholdAlertStrategy custom(double warning, double critical) { return new ThresholdAlertStrategy(warning, critical);}
}
