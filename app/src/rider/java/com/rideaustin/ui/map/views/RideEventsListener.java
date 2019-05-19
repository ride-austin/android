package com.rideaustin.ui.map.views;

import com.rideaustin.events.DriverRatedEvent;
import com.rideaustin.events.RideStatusEvent;

/**
 * Created by Viktor Kifer
 * On 23-Dec-2016.
 */

public interface RideEventsListener {
    void onRideRequested();

    void onRideNoAvailableDriver();

    void onRideAdminCancelled(final RideStatusEvent event);

    void onRideDriverCancelled(final RideStatusEvent event);

    void onRideRiderCancelled(final RideStatusEvent event);

    void onRideDriverAssigned(final RideStatusEvent event);

    void onRideDriverReached(final RideStatusEvent event);

    void onRideActive(final RideStatusEvent event);

    void onRideCompleted(final RideStatusEvent event);

    void onRideFinished();

    void onRideRequestError(final RideStatusEvent event);

    void onRideRated(final DriverRatedEvent event);
}
