package com.example.watermonitoringsystem.mqtt;

import android.util.Log;

import com.example.watermonitoringsystem.activities.common.SensorsModuleInfoActivity;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Calendar;

/**
 * @author Ioan-Alexandru Chirita
 */
public class MqttReceiverThread extends Thread implements MqttConstants {

    private MqttAsyncClient mqttAsyncClient;
    private final int THRESHOLD = 2000;
    private long lastTimeOfRecvSensorCmd;
    private long lastTimeOfRecvElectroCmd;
    private long lastTimeOfRecvPumpCmd;
    public boolean isRunning = false;

    public MqttReceiverThread() {
        lastTimeOfRecvSensorCmd = Calendar.getInstance().getTimeInMillis();
        lastTimeOfRecvElectroCmd = Calendar.getInstance().getTimeInMillis();
        lastTimeOfRecvPumpCmd = Calendar.getInstance().getTimeInMillis();
        try {
            String clientId = MqttClient.generateClientId();
            mqttAsyncClient = new MqttAsyncClient(BROKER_URI, clientId, new MemoryPersistence());
            SensorsModuleInfoActivity.sensorsDataFromMQTT = new SensorDataListener();
            setCallback();
            connect();
            isRunning = true;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() throws MqttException {
        if (isRunning) {
            mqttAsyncClient.disconnect();
        }
    }

    public void setCallback() {
        mqttAsyncClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
            }

            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {

                if (topic.equals(MqttConstants.SUBSCRIPTION_TOPIC)) {
                    String[] dataRecv = mqttMessage.toString().replace(" ", "").split(",");
                    int nodeType = Integer.parseInt(dataRecv[0]);
                    switch (nodeType) {
                        case SENSOR_NODE_TYPE:
                            long currentTimeOfRecvSensorCmd = Calendar.getInstance().getTimeInMillis();
                            if (currentTimeOfRecvSensorCmd - lastTimeOfRecvSensorCmd > THRESHOLD) {
                                SensorsModuleInfoActivity.sensorsDataFromMQTT.set(mqttMessage.toString());
                                Log.d("MQTT_RECV", "SENSORS: " + mqttMessage.toString());
                                lastTimeOfRecvSensorCmd = currentTimeOfRecvSensorCmd;
                            }
                            break;
                        case ELECTROVALVE_NODE_TYPE:
                            long currentTimeOfRecvElectrovalveCmd = Calendar.getInstance().getTimeInMillis();
                            if (currentTimeOfRecvElectrovalveCmd - lastTimeOfRecvElectroCmd >= THRESHOLD) {
                                Log.d("MQTT_RECV", "ELECTROVALVE: " + mqttMessage.toString());
                                lastTimeOfRecvElectroCmd = currentTimeOfRecvElectrovalveCmd;
                            }
                            break;
                        case PUMP_NODE_TYPE:
                            long currentTimeOfRecvPumpCmd = Calendar.getInstance().getTimeInMillis();
                            if (currentTimeOfRecvPumpCmd - lastTimeOfRecvPumpCmd >= THRESHOLD) {
                                Log.d("MQTT_RECV", "PUMP: " + mqttMessage.toString());
                                lastTimeOfRecvPumpCmd = currentTimeOfRecvPumpCmd;
                            }
                            break;
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAsyncClient.setCallback(callback);
    }

    private void connect() throws MqttException {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(MqttConstants.USERNAME);
        mqttConnectOptions.setPassword(MqttConstants.PASSWORD.toCharArray());

        mqttAsyncClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.e("MQTT_CONNECTION", "Connection is OK!!!");
                DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                disconnectedBufferOptions.setBufferEnabled(true);
                disconnectedBufferOptions.setBufferSize(100);
                disconnectedBufferOptions.setPersistBuffer(false);
                disconnectedBufferOptions.setDeleteOldestMessages(false);
                mqttAsyncClient.setBufferOpts(disconnectedBufferOptions);
                try {
                    mqttAsyncClient.subscribe(SUBSCRIPTION_TOPIC, 0);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.e("MQTT_CONNECTION", "Connection is NOT OK!!!");
                exception.printStackTrace();
            }
        });
    }
}
