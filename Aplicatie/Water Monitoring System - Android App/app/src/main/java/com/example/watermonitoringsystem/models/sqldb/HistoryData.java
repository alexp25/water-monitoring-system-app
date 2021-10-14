package com.example.watermonitoringsystem.models.sqldb;

/**
 * @author Ioan-Alexandru Chirita
 */
public class HistoryData {

    private int id;
    private int sensorId;
    private int chan;
    private int value;
    private String timestamp;

    public HistoryData() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSensorId() {
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    public int getChan() {
        return chan;
    }

    public void setChan(int chan) {
        this.chan = chan;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "HistoryData{" +
                "id=" + id +
                ", sensorId=" + sensorId +
                ", chan=" + chan +
                ", value=" + value +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
