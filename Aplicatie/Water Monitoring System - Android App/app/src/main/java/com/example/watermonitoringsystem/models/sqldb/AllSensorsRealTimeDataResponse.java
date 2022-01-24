package com.example.watermonitoringsystem.models.sqldb;

import com.example.watermonitoringsystem.models.MqttDataFormat;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mihai Draghici
 */
public class AllSensorsRealTimeDataResponse {
    @Expose
    private boolean status;
    @Expose
    private String message;
    @Expose
    @SerializedName("data")
    private List<String> rawData;

    private List<MqttDataFormat> parsedData;

    public List<MqttDataFormat> getParsedData(){
        if(parsedData == null){
            parsedData = new ArrayList<>();
            rawData.forEach(x -> parsedData.add(new MqttDataFormat(x)));
        }
        return parsedData;
    }

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getRawData() {
        return rawData;
    }

    @Override
    public String toString() {
        return "AllSensorsRealTimeDataResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", rawData=" + rawData +
                '}';
    }
}
