
package com.example.watermonitoringsystem.api;

import com.example.watermonitoringsystem.models.sqldb.CoordinateDataReturn;
import com.example.watermonitoringsystem.models.sqldb.CoordinatesData;
import com.example.watermonitoringsystem.models.sqldb.CurrentSensorData;
import com.example.watermonitoringsystem.models.sqldb.HistoryRawData;
import com.example.watermonitoringsystem.models.sqldb.RegisteredRawElementsData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Class for Retrofit API Interface
 *
 * @author Ioan-Alexandru
 */
public interface ApiInterface {

    String SERVER_URI = "http://ec2-3-66-146-88.eu-central-1.compute.amazonaws.com:8081";

    @GET("/sensors/manager/get-registered-sensors")
    Call<RegisteredRawElementsData> getRegisteredSensors(@Query("type") int type);

    @GET("/sensors/data/get-current-data-snapshot")
    Call<CurrentSensorData> getSensorChannelsDataBySensorId(@Query("sensorId") int sensorId);

    @GET("/sensors/data/get-stored-data")
    Call<HistoryRawData> getChannelDataHistoryBySensorIdAndChannelId(@Query("sensorId") int sensorId,
                                                                     @Query("chan") int channelId,
                                                                     @Query("limit") int limit,
                                                                     @Query("mode") int mode);

    @POST("/sensors/manager/set-coords")
    Call<CoordinateDataReturn> addCoordinatesToSensor(@Body CoordinatesData coordinatesData);
}

