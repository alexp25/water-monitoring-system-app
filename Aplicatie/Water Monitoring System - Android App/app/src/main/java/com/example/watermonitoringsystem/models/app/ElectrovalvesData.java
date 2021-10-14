package com.example.watermonitoringsystem.models.app;

import java.util.Objects;

/**
 * @author Ioan-Alexandru Chirita
 */
public class ElectrovalvesData {

    private int electrovalveId;
    private double latitude;
    private double longitude;
    private String state;

    public ElectrovalvesData() {
    }

    public int getElectrovalveId() {
        return electrovalveId;
    }

    public void setElectrovalveId(int electrovalveId) {
        this.electrovalveId = electrovalveId;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElectrovalvesData that = (ElectrovalvesData) o;
        return electrovalveId == that.electrovalveId &&
                Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                state.equals(that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(electrovalveId, latitude, longitude, state);
    }

    @Override
    public String toString() {
        return "ElectrovalvesData{" +
                "electrovalveId=" + electrovalveId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", open=" + state +
                '}';
    }
}
