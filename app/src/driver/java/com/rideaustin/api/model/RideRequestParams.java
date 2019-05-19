package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Sergey Petrov on 13/07/2017.
 */

public class RideRequestParams implements Serializable {

    @SerializedName("acceptanceExpiration")
    private long acceptanceExpiration;

    public long getAcceptanceExpiration() {
        return acceptanceExpiration;
    }

    public void setAcceptanceExpiration(long acceptanceExpiration) {
        this.acceptanceExpiration = acceptanceExpiration;
    }

    @Override
    public String toString() {
        return "RideRequestParams{" +
                "acceptanceExpiration=" + acceptanceExpiration +
                '}';
    }
}
