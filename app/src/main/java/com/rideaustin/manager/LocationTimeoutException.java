package com.rideaustin.manager;

/**
 * Created by Sergey Petrov on 25/04/2017.
 */

public class LocationTimeoutException extends Exception {

    public LocationTimeoutException(Throwable throwable) {
        super(throwable);
    }
}
