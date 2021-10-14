package com.example.watermonitoringsystem.utils;

import com.example.watermonitoringsystem.R;

/**
 * @author Ioan-Alexandru Chirita
 */
public enum NotificationType {

    SENSOR_REQUEST(R.drawable.dot_red, R.color.red),

    SERVICE_REQUEST(R.drawable.dot_orange, R.color.orange),

    COMPLAINT(R.drawable.dot_green, R.color.green);

    private final int iconValue;
    private final int colorCode;

    NotificationType(int iconValue, int colorCode) {
        this.iconValue = iconValue;
        this.colorCode = colorCode;
    }

    public int getIconValue() {
        return this.iconValue;
    }

    public int getColorCode() {
        return this.colorCode;
    }

    public static int getColorCodeByNotificationType(String type) {
        if (type.equals(SENSOR_REQUEST.name())) {
            return SENSOR_REQUEST.colorCode;
        } else if (type.equals(SERVICE_REQUEST.name())) {
            return SERVICE_REQUEST.colorCode;
        } else {
            return COMPLAINT.colorCode;
        }
    }

    public static int getIconValueByNotificationType(String type) {
        if (type.equals(SENSOR_REQUEST.name())) {
            return SENSOR_REQUEST.iconValue;
        } else if (type.equals(SERVICE_REQUEST.name())) {
            return SERVICE_REQUEST.iconValue;
        } else {
            return COMPLAINT.iconValue;
        }
    }
}

