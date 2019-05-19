package com.rideaustin.api.config;

import android.support.annotation.VisibleForTesting;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Sergey Petrov on 28/07/2017.
 */

public class LocationUpdateIntervals implements Serializable {

    @SerializedName("whenOnTrip")
    private long whenOnTrip;

    @SerializedName("whenOnlineAndNotMoving")
    private long whenOnlineAndNotMoving;

    @SerializedName("movementSpeed")
    private long movementSpeed;

    @SerializedName("whenOnlineAndMoving")
    private long whenOnlineAndMoving;

    public long getWhenOnTrip() {
        return whenOnTrip;
    }

    public long getWhenOnlineAndNotMoving() {
        return whenOnlineAndNotMoving;
    }

    public long getMovementSpeed() {
        return movementSpeed;
    }

    public long getWhenOnlineAndMoving() {
        return whenOnlineAndMoving;
    }

    @VisibleForTesting
    public void setWhenOnTrip(long whenOnTrip) {
        this.whenOnTrip = whenOnTrip;
    }

    @VisibleForTesting
    public void setWhenOnlineAndNotMoving(long whenOnlineAndNotMoving) {
        this.whenOnlineAndNotMoving = whenOnlineAndNotMoving;
    }

    @VisibleForTesting
    public void setMovementSpeed(long movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    @VisibleForTesting
    public void setWhenOnlineAndMoving(long whenOnlineAndMoving) {
        this.whenOnlineAndMoving = whenOnlineAndMoving;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationUpdateIntervals that = (LocationUpdateIntervals) o;

        if (whenOnTrip != that.whenOnTrip) return false;
        if (whenOnlineAndNotMoving != that.whenOnlineAndNotMoving) return false;
        if (movementSpeed != that.movementSpeed) return false;
        return whenOnlineAndMoving == that.whenOnlineAndMoving;
    }

    @Override
    public int hashCode() {
        int result = (int) (whenOnTrip ^ (whenOnTrip >>> 32));
        result = 31 * result + (int) (whenOnlineAndNotMoving ^ (whenOnlineAndNotMoving >>> 32));
        result = 31 * result + (int) (movementSpeed ^ (movementSpeed >>> 32));
        result = 31 * result + (int) (whenOnlineAndMoving ^ (whenOnlineAndMoving >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LocationUpdateIntervals{" +
                "whenOnTrip=" + whenOnTrip +
                ", whenOnlineAndNotMoving=" + whenOnlineAndNotMoving +
                ", movementSpeed=" + movementSpeed +
                ", whenOnlineAndMoving=" + whenOnlineAndMoving +
                '}';
    }
}
