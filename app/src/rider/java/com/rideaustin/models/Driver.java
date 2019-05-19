package com.rideaustin.models;

/**
 * This model is used in UI data binding. There is another class with same name which is used as
 * rest api data model.
 *
 * Created by supreethks on 05/09/16.
 */
public class Driver {

    private long driverId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String profilePicUrl;

    public long getDriverId() {
        return driverId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    @Override
    public String toString() {
        return "Driver{" +
                "driverId=" + driverId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                '}';
    }
}
