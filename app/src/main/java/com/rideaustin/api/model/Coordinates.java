package com.rideaustin.api.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by yshloma on 01.08.2016.
 */
public class Coordinates implements Serializable {

    @SerializedName("lat")
    private double lat;

    @SerializedName("lng")
    private double lng;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }

    public Coordinates() {
        // empty constructor for serializer
    }

    public Coordinates(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }
}
