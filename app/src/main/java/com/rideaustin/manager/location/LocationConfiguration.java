package com.rideaustin.manager.location;

/**
 * Created by hatak on 16.03.2017.
 */

public class LocationConfiguration {
    private final long timeIntervalMillis;
    private final float smallestDisplacementMeters;

    public LocationConfiguration(long timeIntervalMillis, float smallestDisplacementMeters) {
        this.timeIntervalMillis = timeIntervalMillis;
        this.smallestDisplacementMeters = smallestDisplacementMeters;
    }

    public long getTimeIntervalMillis() {
        return timeIntervalMillis;
    }

    public float getSmallestDisplacementMeters() {
        return smallestDisplacementMeters;
    }
}
