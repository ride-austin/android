package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by e.shloma on 24.07.16.
 */

public class ServicedZipCodes {

    @SerializedName("servicedZipCodes")
    private List<Integer> servicedZipCodes;

    public List<Integer> getServicedZipCodes() {
        return servicedZipCodes;
    }

    public void setServicedZipCodes(List<Integer> servicedZipCodes) {
        this.servicedZipCodes = servicedZipCodes;
    }

}
