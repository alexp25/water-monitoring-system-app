package com.example.watermonitoringsystem.mqtt;

/**
 * Class used for sharing in real time data between MqttReceiverThread and activities where the channels' data should be updated (SensorsModuleInfoActivity)
 *
 * @author Ioan-Alexandru Chirita
 */
public class SensorDataListener {

    private SensorObserverInterface listener;

    private String value;

    public void setSensorObserverInterface(SensorObserverInterface listener) {
        this.listener = listener;
    }

    public String get() {
        return value;
    }

    public void set(String value) {
        this.value = value;

        if (listener != null) {
            listener.onIntegerChanged(value);
        }
    }
}
