package com.example.watermonitoringsystem.utils;

/**
 * @author Ioan-Alexandru Chirita
 */
public class Validator {

    public static boolean isEmailValid(String email) {
        String REGEX_EMAIL_VALID = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";
        return email.matches(REGEX_EMAIL_VALID);
    }

    public static boolean isPasswordValid(String password) {
        String REGEX_PASSWORD_VALID = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,})";
        return password.matches(REGEX_PASSWORD_VALID);
    }

    public static boolean isPhoneNumberValid(String phoneNumber) {
        String REGEX_PHONE_NUMBER_VALID = "^[0-9\\-+]{9,15}$";
        return phoneNumber.matches(REGEX_PHONE_NUMBER_VALID);
    }
}
