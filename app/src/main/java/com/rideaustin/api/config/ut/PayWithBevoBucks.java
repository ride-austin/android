
package com.rideaustin.api.config.ut;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PayWithBevoBucks implements Serializable {

    @SerializedName("enabled")
    private Boolean enabled;

    @SerializedName("largeIconUrl")
    private String largeIconUrl;

    @SerializedName("ridePaymentDelay")
    private Integer ridePaymentDelay;

    @SerializedName("description")
    private String description;

    @SerializedName("availableForSplitfare")
    private boolean availableForSplitFare;

    @SerializedName("splitfareMessage")
    private String splitFareMessage;

    public Boolean getEnabled() {
        return enabled;
    }

    public String getLargeIconUrl() {
        return largeIconUrl;
    }

    public Integer getRidePaymentDelay() {
        return ridePaymentDelay;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAvailableForSplitFare() {
        return availableForSplitFare;
    }

    public String getSplitFareMessage() {
        return splitFareMessage;
    }
}
