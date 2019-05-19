package com.rideaustin.api.config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Sergey Petrov on 12/09/2017.
 */

public class Alert implements Serializable {

    @SerializedName("enabled")
    private boolean enabled;

    @SerializedName("message")
    private String message;

    @SerializedName("actionTitle")
    private String actionTitle;

    @SerializedName("cancelTitle")
    private String cancelTitle;

    public boolean isEnabled() {
        return enabled;
    }

    public String getMessage() {
        return message;
    }

    public String getActionTitle() {
        return actionTitle;
    }

    public String getCancelTitle() {
        return cancelTitle;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "enabled=" + enabled +
                ", message='" + message + '\'' +
                ", actionTitle='" + actionTitle + '\'' +
                ", cancelTitle='" + cancelTitle + '\'' +
                '}';
    }
}
