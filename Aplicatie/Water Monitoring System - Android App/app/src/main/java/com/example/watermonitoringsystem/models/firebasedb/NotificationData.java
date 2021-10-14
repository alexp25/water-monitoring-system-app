package com.example.watermonitoringsystem.models.firebasedb;

import java.util.Objects;

/**
 * @author Ioan-Alexandru Chirita
 */
public class NotificationData {
    private long notificationId;
    private String date;
    private String message;
    private String subject;
    private String customerCode;
    private String type;
    private boolean read;

    public NotificationData() {
    }

    public long getNotificationId() {
        return this.notificationId;
    }

    public void setNotificationId(long notificationId) {
        this.notificationId = notificationId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationData that = (NotificationData) o;
        return read == that.read &&
                Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(date, that.date) &&
                Objects.equals(message, that.message) &&
                Objects.equals(subject, that.subject) &&
                Objects.equals(customerCode, that.customerCode) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, date, message, subject, customerCode, type, read);
    }

    @Override
    public String toString() {
        return "NotificationData{" +
                "notificationId='" + notificationId + '\'' +
                ", date=" + date +
                ", message='" + message + '\'' +
                ", subject='" + subject + '\'' +
                ", customerCode='" + customerCode + '\'' +
                ", type='" + type + '\'' +
                ", read=" + read +
                '}';
    }
}
