package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by crossover on 06/06/2017.
 */

public class RideUpgrade implements Serializable {

    private static final long serialVersionUID = -4033917449235994913L;

    @SerializedName("variants")
    private List<Variant> variants;

    public List<Variant> getVariants() {
        return variants;
    }
}
