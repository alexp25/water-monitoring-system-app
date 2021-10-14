package com.example.watermonitoringsystem.utils;

/**
 * @author Ioan-Alexandru Chirita
 */
public interface Constants {

    String keyUserType = "user_type";
    String keyFullName = "full_name";
    String keyEmail = "email";
    String keyCustomerCode = "client_code";
    String keyProfilePicture = "profile_picture";

    String SUPPLIER = "1";
    String CUSTOMER = "2";

    String OPEN = "OPEN";
    String CLOSE = "CLOSE";

    String WATER_PUMP_STATE = "state";
    String WATER_PUMP_VALUE = "value";

    String DOUBLE_GPS_COORDINATE_FORMAT = "%.4f";
    String NOTIFICATION_DATE_FORMAT = "dd-MMM-yyyy, HH:mm";
    String TIMESTAMPS_GRAPH_DATE_FORMAT = "dd/MMM/yyy\nHH:mm:ss";
    String TIMESTAMPS_DATES_FROM_MYSQL_DB = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    String MQTT_DATA_SEPARATOR = ",";
    int MIN_PASSWORD_LENGTH = 6;

    int LAST_COUNT_MODE = 1;
    int LAST_TIME_SECONDS_MODE = 2;
    int LAST_TIME_HOURS_MODE = 3;
    int TIME_FRAME_MODE = 4;
}
