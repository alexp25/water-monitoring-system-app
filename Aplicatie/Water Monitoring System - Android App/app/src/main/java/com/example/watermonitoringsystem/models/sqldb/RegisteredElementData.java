package com.example.watermonitoringsystem.models.sqldb;

/**
 * @author Ioan-Alexandru Chirita
 */
public class RegisteredElementData {

    private int id;
    private int sensorId;
    private int sensorTypeCode;
    private int logRate;
    private int topicCode;
    private String timestamp;
    private Double lat;
    private Double lng;
    private Topic topic;

    public RegisteredElementData() {
        super();
    }

    public RegisteredElementData(int id, int sensorId, int sensorTypeCode, int logRate, int topicCode, String timestamp, Double lat, Double lng, Topic topic) {
        this.id = id;
        this.sensorId = sensorId;
        this.sensorTypeCode = sensorTypeCode;
        this.logRate = logRate;
        this.topicCode = topicCode;
        this.timestamp = timestamp;
        this.lat = lat;
        this.lng = lng;
        this.topic = topic;
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

    public int getSensorTypeCode() {
        return sensorTypeCode;
    }

    public void setSensorTypeCode(int sensorTypeCode) {
        this.sensorTypeCode = sensorTypeCode;
    }

    public int getLogRate() {
        return logRate;
    }

    public void setLogRate(int logRate) {
        this.logRate = logRate;
    }

    public int getTopicCode() {
        return topicCode;
    }

    public void setTopicCode(int topicCode) {
        this.topicCode = topicCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return "SensorDataRegistered{" +
                "id=" + id +
                ", sensorId=" + sensorId +
                ", sensorTypeCode=" + sensorTypeCode +
                ", logRate=" + logRate +
                ", topicCode=" + topicCode +
                ", timestamp='" + timestamp + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", topic=" + topic +
                '}';
    }
}
