package com.rideaustin.api.model.driver.earnings;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by vokol on 10.08.2016.
 */
public class DriverOnlineResponse implements Serializable {

    @SerializedName("seconds")
    private long seconds;

    public long getSeconds() {
        return seconds;
    }
}
