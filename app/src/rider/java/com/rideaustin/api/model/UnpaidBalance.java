package com.rideaustin.api.model;

import android.support.annotation.VisibleForTesting;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.utils.DateHelper;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by Sergey Petrov on 18/08/2017.
 */

public class UnpaidBalance implements Serializable {

    @SerializedName("rideId")
    private long rideId;

    @SerializedName("amount")
    private String amount;

    @SerializedName("willChargeOn")
    private long willChargeOn;

    @SerializedName("bevoBucksUrl")
    private String bevoBucksUrl;

    public long getRideId() {
        return rideId;
    }

    public String getAmount() {
        return amount;
    }

    public long getWillChargeOn() {
        return willChargeOn;
    }

    public String getBevoBucksUrl() {
        return bevoBucksUrl;
    }

    @VisibleForTesting
    public void setAmount(String amount) {
        this.amount = amount;
    }

    @VisibleForTesting
    public void setWillChargeOn(long willChargeOn) {
        this.willChargeOn = willChargeOn;
    }

    @VisibleForTesting
    public void setBevoBucksUrl(String bevoBucksUrl) {
        this.bevoBucksUrl = bevoBucksUrl;
    }
}

