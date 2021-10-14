package com.example.watermonitoringsystem.mqtt;

/**
 * @author Ioan-Alexandru Chirita
 */
public interface SensorObserverInterface {
    void onIntegerChanged(String newValue);
}
