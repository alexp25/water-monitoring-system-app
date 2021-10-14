package com.example.watermonitoringsystem.models.sqldb;

/**
 * @author Ioan-Alexandru Chirita
 */
public class CoordinatesData {

    private int sensorId;
    private double lat;
    private double lng;

    public CoordinatesData() {
    }

    public int getSensorId() {
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "CoordinatesData{" +
                "sensorId=" + sensorId +
                ", lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
