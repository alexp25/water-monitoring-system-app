package com.example.watermonitoringsystem.models.firebasedb;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Ioan-Alexandru Chirita
 */
public class SupplierData {

    private String fullName;
    private String email;
    private String password;
    private String profilePictureIdentifier;

    public SupplierData() {
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
        SupplierData supplier = (SupplierData) o;
        return Objects.equals(fullName, supplier.fullName) &&
                Objects.equals(email, supplier.email) &&
                Objects.equals(password, supplier.password) &&
                Objects.equals(profilePictureIdentifier, supplier.profilePictureIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, email, password, profilePictureIdentifier);
    }

    @NotNull
    @Override
    public String toString() {
        return "User{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", profilePictureIdentifier='" + profilePictureIdentifier + '\'' +
                '}';
    }
}
