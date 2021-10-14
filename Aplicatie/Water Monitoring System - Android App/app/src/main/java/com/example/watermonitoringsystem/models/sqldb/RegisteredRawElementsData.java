package com.example.watermonitoringsystem.models.sqldb;

import java.util.List;

/**
 * @author Ioan-Alexandru Chirita
 */
public class RegisteredRawElementsData {

    private boolean status;
    private String message;
    private List<RegisteredElementData> data;

    public RegisteredRawElementsData() {
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

    public List<RegisteredElementData> getData() {
        return data;
    }

    public void setData(List<RegisteredElementData> data) {
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
