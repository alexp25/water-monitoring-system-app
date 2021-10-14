package com.example.watermonitoringsystem.models.firebasedb;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Ioan-Alexandru Chirita
 */
public class CustomerData {

    private String fullName;
    private String email;
    private String password;
    private String customerCode;
    private String countryPhoneNumberPrefix;
    private String phoneNumber;
    private String birthDayDate;
    private String profilePictureIdentifier;

    public CustomerData() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCountryPhoneNumberPrefix() {
        return countryPhoneNumberPrefix;
    }

    public void setCountryPhoneNumberPrefix(String countryPhoneNumberPrefix) {
        this.countryPhoneNumberPrefix = countryPhoneNumberPrefix;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBirthDayDate() {
        return birthDayDate;
    }

    public void setBirthDayDate(String birthDayDate) {
        this.birthDayDate = birthDayDate;
    }

    public String getProfilePictureIdentifier() {
        return profilePictureIdentifier;
    }

    public void setProfilePictureIdentifier(String profilePictureIdentifier) {
        this.profilePictureIdentifier = profilePictureIdentifier;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerData customerData = (CustomerData) o;
        return Objects.equals(fullName, customerData.fullName) &&
                Objects.equals(email, customerData.email) &&
                Objects.equals(password, customerData.password) &&
                Objects.equals(customerCode, customerData.customerCode) &&
                Objects.equals(countryPhoneNumberPrefix, customerData.countryPhoneNumberPrefix) &&
                Objects.equals(phoneNumber, customerData.phoneNumber) &&
                Objects.equals(birthDayDate, customerData.birthDayDate) &&
                Objects.equals(profilePictureIdentifier, customerData.profilePictureIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, email, password, customerCode, countryPhoneNumberPrefix, phoneNumber, birthDayDate, profilePictureIdentifier);
    }

    @NotNull
    @Override
    public String toString() {
        return "User{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", customerCode='" + customerCode + '\'' +
                ", countryPhoneNumberPrefix='" + countryPhoneNumberPrefix + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", birthDayDate='" + birthDayDate + '\'' +
                ", profilePictureIdentifier='" + profilePictureIdentifier + '\'' +
                '}';
    }
}
