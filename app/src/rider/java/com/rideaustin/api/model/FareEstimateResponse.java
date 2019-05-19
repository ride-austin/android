package com.rideaustin.api.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.api.model.campaigns.Campaign;

import java.io.Serializable;

/**
 * Created by vokol on 04.07.2016.
 */
public class FareEstimateResponse implements Serializable {
    @SerializedName("totalFare")
    private double totalFare;
    @SerializedName("duration")
    private int duration;
    @Nullable
    private Campaign campaignInfo;

    public double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(double totalFare) {
        this.totalFare = totalFare;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Nullable
    public Campaign getCampaignInfo() {
        return campaignInfo;
    }

    public void setCampaignInfo(@Nullable Campaign campaignInfo) {
        this.campaignInfo = campaignInfo;
    }
}
