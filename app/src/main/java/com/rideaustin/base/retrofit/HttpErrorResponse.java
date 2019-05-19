package com.rideaustin.base.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HttpErrorResponse {

    @SerializedName("errorCode")
    @Expose
    private String errorCode;
    @SerializedName("errorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("message")
    @Expose
    private String message;

    /**
     * @return The errorCode
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode The errorCode
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @return The errorMessage
     */
    public String getErrorMessage() {
        return errorMessage == null ? message : errorMessage;
    }

    /**
     * @param errorMessage The errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}