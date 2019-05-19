package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by rideclientandroid on 17.08.2016.
 */
public class Address implements Serializable {
    @SerializedName("address")
    String address;

    @SerializedName("zipCode")
    String zipCode;

    @SerializedName("latitude")
    Double latitude;

    @SerializedName("longitude")
    Double longitude;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Address{" +
                "address='" + address + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
