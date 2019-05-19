package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author sdelaysam.
 */

public class DriverStatsConfig implements Serializable {

    @SerializedName("enabled")
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "DriverStatsConfig{" +
                "enabled=" + enabled +
                '}';
    }
}
