package com.rideaustin.api.config;

import android.support.annotation.VisibleForTesting;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Rides implements Serializable {

    @SerializedName("distantPickUpNotificationThreshold")
    private Integer distantPickUpNotificationThreshold;

    @SerializedName("rideSummaryDescription")
    private String rideSummaryDescription;

    @SerializedName("rideSummaryDescriptionFreeCreditCharged")
    private String rideSummaryDescriptionFreeCreditCharged;

    public final static long serialVersionUID = 7664719486419297361L;

    public Integer getDistantPickUpNotificationThreshold() {
        return distantPickUpNotificationThreshold;
    }

    @VisibleForTesting
    public void setDistantPickUpNotificationThreshold(Integer distantPickUpNotificationThreshold) {
        this.distantPickUpNotificationThreshold = distantPickUpNotificationThreshold;
    }

    public String getRideSummaryDescription() {
        return rideSummaryDescription;
    }

    @VisibleForTesting
    public void setRideSummaryDescription(String rideSummaryDescription) {
        this.rideSummaryDescription = rideSummaryDescription;
    }

    public String getRideSummaryDescriptionFreeCreditCharged() {
        return rideSummaryDescriptionFreeCreditCharged;
    }

    @VisibleForTesting
    public void setRideSummaryDescriptionFreeCreditCharged(String rideSummaryDescriptionFreeCreditCharged) {
        this.rideSummaryDescriptionFreeCreditCharged = rideSummaryDescriptionFreeCreditCharged;
    }
}