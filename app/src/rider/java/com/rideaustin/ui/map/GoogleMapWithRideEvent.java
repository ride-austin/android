package com.rideaustin.ui.map;

import com.google.android.gms.maps.GoogleMap;
import com.rideaustin.events.RideStatusEvent;

/**
 * Created by hatak on 16.12.16.
 */

public class GoogleMapWithRideEvent {
    private final GoogleMap googleMap;
    private final RideStatusEvent rideStatusEvent;

    public GoogleMapWithRideEvent(final GoogleMap googleMap, final RideStatusEvent rideStatusEvent) {
        this.googleMap = googleMap;
        this.rideStatusEvent = rideStatusEvent;
    }

    public GoogleMap getGoogleMap() {
        return googleMap;
    }

    public RideStatusEvent getRideStatusEvent() {
        return rideStatusEvent;
    }
}
