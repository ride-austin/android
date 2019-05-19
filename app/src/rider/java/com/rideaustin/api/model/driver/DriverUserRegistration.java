package com.rideaustin.api.model.driver;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.api.model.Address;

import java.io.Serializable;

/**
 * Created by rideclientandroid on 17.08.2016.
 */
public class DriverUserRegistration implements Serializable {

    private Address address;
    private String enabled;
    @SerializedName("lastname")
    private String lastName;
    private String dateOfBirth;
    @SerializedName("firstname")
    private String firstName;
    private String middleName;
    private String email;
    private String phoneNumber;

    public DriverUserRegistration(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return Boolean.parseBoolean(enabled);
    }

    public void setEnabled(boolean enabled) {
        this.enabled =String.valueOf(enabled);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
