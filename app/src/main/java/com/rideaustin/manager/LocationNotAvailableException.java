package com.rideaustin.manager;

/**
 * Created by rost on 8/17/16.
 */
public class LocationNotAvailableException extends Exception {
    public enum Reason {
        TIMEOUT, LOCATION_IS_OFF
    }

    private final Reason reason;

    public LocationNotAvailableException(Reason reason) {
        this.reason = reason;
    }
}
