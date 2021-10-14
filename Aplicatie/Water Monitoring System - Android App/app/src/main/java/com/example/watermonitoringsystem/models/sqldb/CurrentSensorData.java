package com.example.watermonitoringsystem.models.sqldb;

import java.util.List;

/**
 * @author Ioan-Alexandru Chirita
 */
public class CurrentSensorData {

    private boolean status;
    private String message;
    private List<String> data;

    public CurrentSensorData() {
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SensorRegistered{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
