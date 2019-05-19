package com.rideaustin.api.model.driver.earnings;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class End implements Serializable {
    @SerializedName("address")
    private String address;
    @SerializedName("zipCode")
    private String zipCode;

    public String getAddress() {
        return address;
    }

    public String getZipCode() {
        return zipCode;
    }
}
