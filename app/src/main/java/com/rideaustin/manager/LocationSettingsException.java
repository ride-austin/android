package com.rideaustin.manager;

import com.google.android.gms.location.LocationSettingsResult;

/**
 * Created by Sergey Petrov on 06/04/2017.
 */

public class LocationSettingsException extends Exception {

    private final transient LocationSettingsResult result;

    public LocationSettingsException(LocationSettingsResult result) {
        this.result = result;
    }

    public LocationSettingsResult getResult() {
        return result;
    }
}
