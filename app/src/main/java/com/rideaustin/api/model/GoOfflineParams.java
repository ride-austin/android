package com.rideaustin.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import timber.log.Timber;

/**
 * Created by Sergey Petrov on 15/06/2017.
 */

public class GoOfflineParams implements Serializable {

    @SerializedName("source")
    private String source;

    @SerializedName("message")
    private String message;

    public Type getType() {
        try {
            return Type.valueOf(source);
        } catch (Exception e) {
            Timber.e(e);
            return Type.UNKNOWN;
        }
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }

    public enum Type {
        TERMS_NOT_ACCEPTED,
        MISSED_RIDES,
        DRIVER_INACTIVE,
        CAR_TYPES_DEACTIVATE,
        UNKNOWN
    }
}
