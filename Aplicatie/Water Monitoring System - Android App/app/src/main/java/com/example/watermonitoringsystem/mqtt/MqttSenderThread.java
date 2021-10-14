package com.example.watermonitoringsystem.mqtt;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;

/**
 * @author Ioan-Alexandru Chirita
 */
public class MqttSenderThread extends Thread implements MqttConstants {

    private static MqttAsyncClient mqttAsyncClient;
    public boolean isRunning = false;

    public MqttSenderThread() {
        try {
            String clientId = MqttClient.generateClientId();
            mqttAsyncClient = new MqttAsyncClient(BROKER_URI, clientId, new MemoryPersistence());
            mqttAsyncClient.setCallback(null);
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

    private void connect() throws MqttException {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(MqttConstants.USERNAME);
        mqttConnectOptions.setPassword(MqttConstants.PASSWORD.toCharArray());

        mqttAsyncClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.e("MQTT-CONNECTION", "Connection is OK!!!");
                DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                disconnectedBufferOptions.setBufferEnabled(true);
                disconnectedBufferOptions.setBufferSize(100);
                disconnectedBufferOptions.setPersistBuffer(false);
                disconnectedBufferOptions.setDeleteOldestMessages(false);
                mqttAsyncClient.setBufferOpts(disconnectedBufferOptions);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.e("MQTT-CONNECTION", "Connection is NOT OK!!!");
                exception.printStackTrace();
            }
        });
    }

    public static void publishToElectrovalveTopic(String electrovalvePayload) {
        try {
            byte[] encodedPayload = electrovalvePayload.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            mqttAsyncClient.publish(PUBLISH_TOPIC_ELECTROVALVE, message);
            Log.e("Electrovalve-SEND: ", electrovalvePayload);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // Data format for SEND-PUMP: "nodeType,moduleId,valueForPump". Value for pump between x and y
    public static void publishToPumpTopic(String pumpPayload) {
        try {
            byte[] encodedPayload = pumpPayload.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            mqttAsyncClient.publish(PUBLISH_TOPIC_PUMP, message);
            Log.e("Pump-SEND: ", pumpPayload);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
