package com.rideaustin.events;

import android.support.annotation.Nullable;

import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;

import static com.rideaustin.api.model.RideStatus.FINISHED;
import static com.rideaustin.api.model.RideStatus.RIDER_CANCELLED;
import static com.rideaustin.api.model.RideStatus.valueOf;

/**
 * Created by kshumelchyk on 7/21/16.
 */
public class RideStatusEvent extends BaseEvent<RideStatus> {

    private String error;
    private Ride resp;

    public RideStatusEvent(RideStatus data, String error, Ride resp) {
        super(data);
        this.error = error;
        this.resp = resp;
    }

    public String getError() {
        return error;
    }

    public Ride getResp() {
        return resp;
    }

    public static RideStatusEvent from(@Nullable Ride ride) {
        if (ride == null) {
            return new RideStatusEvent(FINISHED, null, null);
        }
        return new RideStatusEvent(valueOf(ride.getStatus()), null, ride);
    }

    public static RideStatusEvent riderCancelled() {
        return new RideStatusEvent(RIDER_CANCELLED, null, null);
    }
}
