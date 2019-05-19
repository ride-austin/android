package com.rideaustin.api.config;

import android.support.annotation.VisibleForTesting;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Online implements Serializable {

    @SerializedName("shouldShowPopup")
    private boolean shouldShowPopup;

    public boolean getShouldShowPopup() {
        return shouldShowPopup;
    }

    @VisibleForTesting
    public void setShouldShowPopup(boolean shouldShowPopup) {
        this.shouldShowPopup = shouldShowPopup;
    }

}