package com.example.watermonitoringsystem.mqtt;

/**
 * Data format for RECV-SENSORS: "nodeType,moduleId,command,valueForChannel0,valueForChannel1, ..."
 * Data format for SEND-PUMP: "nodeType,moduleId,sendCmd,pumpId,valueForPump". Value for pump between 0 and 100
 * Data format for SEND-ELECTROVALVE: "nodeType,moduleId,sendCmd,electrovalveId,valueForElectrovalve"
 *
 * @author Ioan-Alexandru Chirita
 */
public interface MqttConstants {

    // MQTT connection credentials
    String BROKER_URI = "tcp://ec2-3-66-146-88.eu-central-1.compute.amazonaws.com";
    String USERNAME = "pi";
    String PASSWORD = "raspberry";

    // Topics
    String SUBSCRIPTION_TOPIC = "wsn/watergame/realtime";
    String PUBLISH_TOPIC_PUMP = "wsn/watergame/cmd";
    String PUBLISH_TOPIC_ELECTROVALVE = "wsn/watergame/cmd";

    // Node Type
    int SENSOR_NODE_TYPE = 1;
    int PUMP_NODE_TYPE = 2;
    int ELECTROVALVE_NODE_TYPE = 3;

    // Commands
    int SEND_COMMAND = 110;

    // Vector positions for recv, send data
    int SENSOR_NODE_TYPE_POSITION = 0;
    int SENSOR_NODE_ID_POSITION = 1;
    int SENSOR_CMD_POSITION = 2;
    int SENSOR_START_CHANNELS_POSITION = 3;

    int CMD = 1;
    int PUMP_ID = 1;

    int MODULE_PUMP = 200;
    int MODULE_ELECTRO = 300;
}
