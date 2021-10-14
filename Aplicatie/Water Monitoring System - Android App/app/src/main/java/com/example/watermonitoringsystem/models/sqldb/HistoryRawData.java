package com.example.watermonitoringsystem.models.sqldb;

import java.util.List;

/**
 * @author Ioan-Alexandru Chirita
 */
public class HistoryRawData {
    private boolean status;
    private String message;
    private List<HistoryData> data;

    public HistoryRawData() {
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

    public List<HistoryData> getData() {
        return data;
    }

    public void setData(List<HistoryData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "HistoryRawData{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
