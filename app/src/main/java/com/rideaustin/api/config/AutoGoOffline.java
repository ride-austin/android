package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author sdelaysam.
 */

public class AutoGoOffline implements Serializable {

    @SerializedName("enabled")
    private boolean enabled;

    @SerializedName("warningMessage")
    private String warningMessage;

    @SerializedName("offlineBackgroundMessage")
    private String offlineBackgroundMessage;

    @SerializedName("backgroundWarningPeriod")
    private int backgroundWarningPeriod;

    @SerializedName("backgroundMaximumPeriod")
    private int backgroundMaximumPeriod;

    public boolean isEnabled() {
        return enabled;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public String getOfflineBackgroundMessage() {
        return offlineBackgroundMessage;
    }

    public int getBackgroundWarningPeriod() {
        return backgroundWarningPeriod;
    }

    public int getBackgroundMaximumPeriod() {
        return backgroundMaximumPeriod;
    }
}
