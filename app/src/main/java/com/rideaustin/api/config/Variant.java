package com.rideaustin.api.config;

import android.support.annotation.VisibleForTesting;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Variant implements Serializable {

    private static final long serialVersionUID = -5154493143284962128L;

    @SerializedName("carCategory")
    private String carCategory;

    @SerializedName("validUpgrades")
    private List<String> validUpgrades;

    public String getCarCategory() {
        return carCategory;
    }

    public List<String> getValidUpgrades() {
        return validUpgrades;
    }

    @VisibleForTesting
    public void setValidUpgrades(List<String> validUpgrades) {
        this.validUpgrades = validUpgrades;
    }
}