package com.rideaustin.api.config.v2_6_1;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConfigRideCancellationResponse {

    @SerializedName("rideCancellationFee")
    @Expose
    public Integer rideCancellationFee;
    @SerializedName("rideCancellationPeriod")
    @Expose
    public Integer rideCancellationPeriod;

    public Integer getRideCancellationFee() {
        return rideCancellationFee;
    }

    public void setRideCancellationFee(Integer rideCancellationFee) {
        this.rideCancellationFee = rideCancellationFee;
    }

    public Integer getRideCancellationPeriod() {
        return rideCancellationPeriod;
    }

    public void setRideCancellationPeriod(Integer rideCancellationPeriod) {
        this.rideCancellationPeriod = rideCancellationPeriod;
    }

    @Override
    public String toString() {
        return "ConfigRideCancellationResponse{" +
                "rideCancellationFee=" + rideCancellationFee +
                ", rideCancellationPeriod=" + rideCancellationPeriod +
                '}';
    }


}