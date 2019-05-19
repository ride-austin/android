package com.rideaustin.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Live ETA
 */
public class ShareTokenResponse {

    @SerializedName("token")
    @Expose
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "ShareTokenResponse{" +
                "token='" + token + '\'' +
                '}';
    }
}


