package com.rideaustin.api.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by hatak on 14.03.2017.
 */

public class RiderLiveLocation implements Serializable {

    private static final long serialVersionUID = 1555264585339161203L;

    @SerializedName("enabled")
    @Expose
    private boolean enabled;

    @SerializedName("requiredAccuracy")
    @Expose
    private Integer requiredAccuracy;

    @SerializedName("expirationTime")
    @Expose
    private Integer expirationTime = 120;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getRequiredAccuracy() {
        return requiredAccuracy;
    }

    public void setRequiredAccuracy(Integer requiredAccuracy) {
        this.requiredAccuracy = requiredAccuracy;
    }

    public Integer getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Integer expirationTime) {
        this.expirationTime = expirationTime;
    }
}
