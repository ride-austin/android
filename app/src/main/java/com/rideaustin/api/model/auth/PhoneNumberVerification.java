package com.rideaustin.api.model.auth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by hatak on 03.11.16.
 */

public class PhoneNumberVerification {

    @SerializedName("body")
    @Expose
    private PhoneNumberVerificationBody body;
    @SerializedName("statusCode")
    @Expose
    private String statusCode;

    /**
     * @return The body
     */
    public PhoneNumberVerificationBody getBody() {
        return body;
    }

    /**
     * @param body The body
     */
    public void setBody(PhoneNumberVerificationBody body) {
        this.body = body;
    }

    /**
     * @return The statusCode
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode The statusCode
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

}

