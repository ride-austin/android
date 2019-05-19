package com.rideaustin.api.model;

import android.location.Location;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.api.model.driver.Driver;

/**
 * Created by yshloma on 07.07.2016.
 */
public class DriverLocation {

    @SerializedName("id")
    private int id;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("course")
    private float course;

    private Driver driver;

    @SerializedName("drivingTimeToRider")
    private Integer drivingTimeToRider;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getCourse() {
        return course;
    }

    public void setCourse(float course) {
        this.course = course;
    }

    public Driver getDriver() {
        return driver;
    }

    public Integer getDrivingTimeToRider() {
        return drivingTimeToRider;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public void setDrivingTimeToRider(Integer drivingTimeToRider) {
        this.drivingTimeToRider = drivingTimeToRider;
    }

    public Location getLocation() {
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setBearing(course);
        return location;
    }
}
