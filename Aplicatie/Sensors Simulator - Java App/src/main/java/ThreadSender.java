
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Calendar;

public class ThreadSender extends Thread {

    private MqttClient mqttClient;
    private int STEP = 1000;
    private long COUNTER;


    ThreadSender(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
        COUNTER = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public void run() {
        while (Main.START) {
            long millis = Calendar.getInstance().getTimeInMillis();
            if (millis - COUNTER > STEP) {
                COUNTER = millis;
                try {
                    Main.publishToSensorsTopic();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
