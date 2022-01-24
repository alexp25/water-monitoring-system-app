package com.example.watermonitoringsystem.models;

import android.util.Log;

import com.example.watermonitoringsystem.mqtt.MqttConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author Ioan-Alexandru Chirita
 */
public class MqttDataFormat {
    int nodeType;
    int sensorId;
    int command;
    List<Integer> channelValues;

    public static MqttDataFormat ofIdValue(int sensorId){
        MqttDataFormat result = new MqttDataFormat();
        result.sensorId = sensorId;
        return result;
    }

    public MqttDataFormat(){}

    public MqttDataFormat(String rawStringData) {
        buildSensorFormatData(rawStringData);
    }

    public int getNodeType() {
        return nodeType;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    public int getSensorId() {
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public List<Integer> getChannelValues() {
        return channelValues;
    }

    public void setChannelValues(List<Integer> channelValues) {
        this.channelValues = channelValues;
    }

    @Override
    public String toString() {
        return "SensorFormatData{" +
                "nodeType=" + nodeType +
                ", sensorId=" + sensorId +
                ", command=" + command +
                ", channelValues=" + channelValues +
                '}';
    }

    private void buildSensorFormatData(String rawStringData) {

        String[] tokens = rawStringData.replace(" ", "").split(",");
        // node_type
        nodeType = Integer.parseInt(tokens[MqttConstants.SENSOR_NODE_TYPE_POSITION]);
        // node_id = module_wifi_id
        sensorId = Integer.parseInt(tokens[MqttConstants.SENSOR_NODE_ID_POSITION]);
        // command
        command = Integer.parseInt(tokens[MqttConstants.SENSOR_CMD_POSITION]);

        int pos = MqttConstants.SENSOR_START_CHANNELS_POSITION;
        channelValues = new ArrayList<>();

        for (int i = pos; i < tokens.length; i++) {
            channelValues.add(Integer.parseInt(tokens[i]));
        }
        Log.i("buildSensorFormatData", "NoteType:" + nodeType + "; NodeId:" + sensorId + "; CMD:" + command + "; Channels:" + channelValues.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MqttDataFormat that = (MqttDataFormat) o;
        return sensorId == that.sensorId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensorId);
    }
}
