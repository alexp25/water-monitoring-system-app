package com.example.watermonitoringsystem.models.sqldb;

/**
 * @author Ioan-Alexandru Chirita
 */
public class CoordinateDataReturn {
    private boolean status;
    private String message;
    private boolean data;

    public CoordinateDataReturn() {
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

    public boolean isData() {
        return data;
    }

    public void setData(boolean data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CoordinateDataReturn{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
