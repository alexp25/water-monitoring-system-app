package com.example.watermonitoringsystem.models.app;

import java.util.Objects;

/**
 * @author Ioan-Alexandru Chirita
 */
public class SensorData {

    private int sensorId;
    private String customerCode;
    private double latitude;
    private double longitude;
    private boolean hasDataChannels;

    public SensorData() {
        this.customerCode = "-";
    }

    public int getSensorId() {
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean hasDataChannels() {
        return hasDataChannels;
    }

    public void setHasDataChannels(boolean hasDataChannels) {
        this.hasDataChannels = hasDataChannels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorData that = (SensorData) o;
        return sensorId == that.sensorId &&
                Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                Objects.equals(customerCode, that.customerCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensorId, customerCode, latitude, longitude);
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "sensorId=" + sensorId +
                ", customerCode='" + customerCode + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
