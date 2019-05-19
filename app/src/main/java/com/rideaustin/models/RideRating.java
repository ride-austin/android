package com.rideaustin.models;

import java.io.Serializable;

/**
 * Created on 30/01/2018
 *
 * @author sdelaysam
 */

public class RideRating implements Serializable {

    private long rideId;
    private double rate;

    public RideRating(long rideId, double rate) {
        this.rideId = rideId;
        this.rate = rate;
    }

    public long getRideId() {
        return rideId;
    }

    public void setRideId(long rideId) {
        this.rideId = rideId;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RideRating that = (RideRating) o;

        if (rideId != that.rideId) return false;
        return Double.compare(that.rate, rate) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (rideId ^ (rideId >>> 32));
        temp = Double.doubleToLongBits(rate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
