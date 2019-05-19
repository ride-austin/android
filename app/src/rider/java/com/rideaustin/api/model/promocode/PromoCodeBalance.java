package com.rideaustin.api.model.promocode;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PromoCodeBalance implements Serializable {

    @SerializedName("remainder")
    @Expose
    private double remainder;
    private final static long serialVersionUID = -4809413545769848278L;

    public double getRemainder() {
        return remainder;
    }

    public void setRemainder(double remainder) {
        this.remainder = remainder;
    }

}
