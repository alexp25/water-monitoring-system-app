package com.example.watermonitoringsystem.api;

import android.util.Log;

import com.example.watermonitoringsystem.models.sqldb.CoordinateDataReturn;
import com.example.watermonitoringsystem.models.sqldb.CoordinatesData;
import com.example.watermonitoringsystem.models.sqldb.CurrentSensorData;
import com.example.watermonitoringsystem.models.sqldb.HistoryRawData;
import com.example.watermonitoringsystem.models.sqldb.RegisteredRawElementsData;
import com.example.watermonitoringsystem.models.sqldb.AllSensorsRealTimeDataResponse;

import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class for Retrofit API Manager
 *
 * @author Ioan-Alexandru
 */
public class ApiManager {

    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(ApiInterface.SERVER_URI)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static final ApiInterface apiClient = retrofit.create(ApiInterface.class);

    public static void getRegisteredSensors(int type, Callback<RegisteredRawElementsData> callback) {
        apiClient.getRegisteredSensors(type).enqueue(callback);
    }

    public static void getSensorChannelsDataBySensorId(int sensorId, Callback<CurrentSensorData> callback) {
        apiClient.getSensorChannelsDataBySensorId(sensorId).enqueue(callback);
    }

    public static void getChannelDataHistoryBySensorIdAndChannelId(int sensorId, int channelId, int limit, int mode, Callback<HistoryRawData> callback) {
        Log.e("test", "Limit: " + limit + "mode: "+ mode);
        apiClient.getChannelDataHistoryBySensorIdAndChannelId(sensorId, channelId, limit, mode).enqueue(callback);
    }

    public static void addNewCoordinatesToSensor(CoordinatesData coordinatesData, Callback<CoordinateDataReturn> callback) {
        apiClient.addCoordinatesToSensor(coordinatesData).enqueue(callback);
    }

    public static void getAllSensorsRealTimeDataChannels(Callback<AllSensorsRealTimeDataResponse> callback){
        apiClient.getAllSensorsRealTimeDataChannels().enqueue(callback);
    }
}
