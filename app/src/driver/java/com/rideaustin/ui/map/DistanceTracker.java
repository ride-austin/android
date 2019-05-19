package com.rideaustin.ui.map;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.utils.location.DistanceUtil;

import timber.log.Timber;

/**
 * Created by Viktor Kifer
 * On 27-Dec-2016.
 */

public class DistanceTracker {

    private static final double ARRIVAL_DISTANCE_IN_METERS = 10;
    private LatLng destinationLocation;
    private boolean driverArrived = false;

    @Nullable
    private DestinationReachedListener listener;

    public void reset() {
        driverArrived = false;
        destinationLocation = null;
    }

    public void setDestinationLocation(@Nullable LatLng destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public void setDestinationReachedListener(DestinationReachedListener listener) {
        this.listener = listener;
    }

    public void updateDriverLocation(LatLng driverLocation) {
        if (destinationLocation == null) {
            Timber.d("Destination is not set");
            return;
        }

        if (listener == null) {
            Timber.d("Listener is not set");
            return;
        }

        if (driverArrived) {
            Timber.d("Notification was shown");
            return;
        }

        double distance = DistanceUtil.distance(destinationLocation, driverLocation);
        Timber.v("Distance between (%s %s): %f", destinationLocation, driverLocation, distance);
        if (distance < ARRIVAL_DISTANCE_IN_METERS) {
            listener.onDestinationReached();
            driverArrived = true;
        }
    }

    public interface DestinationReachedListener {
        void onDestinationReached();
    }
}
