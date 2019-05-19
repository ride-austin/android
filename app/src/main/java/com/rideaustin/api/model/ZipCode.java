package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by yshloma on 01.08.2016.
 */
public class ZipCode {

    @SerializedName("zipCode")
    private int zipCode;

    @SerializedName("coordinates")
    private List<Coordinates> coordinates;

    public int getZipCode() {
        return zipCode;
    }

    public void setZipCode(int zipCode) {
        this.zipCode = zipCode;
    }

    public List<Coordinates> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinates> coordinates) {
        this.coordinates = coordinates;
    }
}
