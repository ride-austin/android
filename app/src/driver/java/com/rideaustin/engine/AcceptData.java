package com.rideaustin.engine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rideaustin.api.model.Ride;

/**
 * Created by rost on 8/18/16.
 */
public class AcceptData {

    private long acceptExpiration;
    private Ride pendingRide;
    private Ride currentRide;

    public AcceptData(long acceptExpiration, @NonNull Ride pendingRide, @Nullable Ride currentRide) {
        this.acceptExpiration = acceptExpiration;
        this.pendingRide = pendingRide;
        this.currentRide = currentRide;
    }

    public long getAcceptExpiration() {
        return acceptExpiration;
    }

    public Ride getPendingRide() {
        return pendingRide;
    }

    public Ride getCurrentRide() {
        return currentRide;
    }

    public boolean isStacked() {
        return currentRide != null;
    }
}
