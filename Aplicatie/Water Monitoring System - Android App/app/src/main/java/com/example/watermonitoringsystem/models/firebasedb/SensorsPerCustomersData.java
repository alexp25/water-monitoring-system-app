package com.example.watermonitoringsystem.models.firebasedb;

/**
 * @author Ioan-Alexandru Chirita
 */
public class SensorsPerCustomersData {

    private int sensorId;
    private String customerCode;

    public SensorsPerCustomersData() {
    }

    public int getSensorId() {
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    @Override
    public String toString() {
        return "SensorsData{" +
                "sensorId=" + sensorId +
                ", customerCode='" + customerCode + '\'' +
                '}';
    }
}
