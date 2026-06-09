package com.smarthome.strategy;

public interface AlertStrategy {

    AlertLevel evaluate(double reading);
}
