package com.example.watermonitoringsystem.authentication;

public enum SharedPrefsKeys {
    KEY_USER_TYPE("user_type"),
    KEY_FULLNAME("full_name"),
    KEY_EMAIL("email"),
    KEY_CUSTOMER_CODE("client_code"),
    KEY_PROFILE_PICTURE("profile_picture"),
    KEY_PASSWORD("password");

    private String keyValue;

    private SharedPrefsKeys(String aKeyValue) {
        keyValue = aKeyValue;
    }

    public String getKeyValue() {
        return keyValue;
    }

}
