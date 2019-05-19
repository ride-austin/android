package com.rideaustin.manager;

/**
 * Created by rost on 8/15/16.
 */
public class MissingLocationPermissionException extends Exception {
    public MissingLocationPermissionException() {
    }

    public MissingLocationPermissionException(String detailMessage) {
        super(detailMessage);
    }
}
