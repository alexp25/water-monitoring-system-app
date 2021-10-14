import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Random;

public class Main {

    public static boolean START = false;
    private static MqttClient mqttClient;

    private static String SUBSCRIPTION_TOPIC = "wsn/watergame/realtime";
    private static String CLIENT_ID = "JavaSample";
    private static String BROKER_URI = "tcp://ec2-18-159-69-145.eu-central-1.compute.amazonaws.com";
    private static String USERNAME = "pi";
    private static String PASSWORD = "raspberry";

    private static MemoryPersistence persistence = new MemoryPersistence();

    public static void main(String[] args) {
        startMqtt();
        ThreadSender threadSend = new ThreadSender(mqttClient);
        START = true;
        threadSend.start();
    }

    private static void startMqtt() {

        try {
            mqttClient = new MqttClient(BROKER_URI, CLIENT_ID, persistence);

            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setUserName(USERNAME);
            mqttConnectOptions.setPassword(PASSWORD.toCharArray());
            System.out.println("Connecting to broker: " + BROKER_URI);
            mqttClient.connect(mqttConnectOptions);
            System.out.println("Connected");
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

    public static void close() throws MqttException {
        mqttClient.disconnect();
        System.out.println("Disconnected");
        System.exit(0);
    }

    static void publishToSensorsTopic() throws MqttException {
        try {
            int[] values = new int[11];
            StringBuilder payload = new StringBuilder();
            payload.append("1,100,1,");
            for (int i = 0; i < values.length; i++) {
                values[i] = new Random().nextInt(400);
                payload.append(values[i]).append(",");
            }
            payload.append("\n");

            System.out.println("Publishing message: " + payload.toString());
            MqttMessage message = new MqttMessage(payload.toString().getBytes());
            mqttClient.publish(SUBSCRIPTION_TOPIC, message);
            System.out.println("Message published");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
