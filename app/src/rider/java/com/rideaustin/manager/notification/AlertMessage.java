package com.rideaustin.manager.notification;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sergey Petrov on 24/04/2017.
 */

public class AlertMessage {

    @SerializedName("alert")
    private String alert;

    public String getAlert() {
        return alert;
    }
}
