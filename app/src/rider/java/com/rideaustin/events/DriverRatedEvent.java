package com.rideaustin.events;

/**
 * Created by vokol on 27.07.2016.
 */
public class DriverRatedEvent extends BaseEvent<Boolean> {
    private final boolean driverRated;

    public DriverRatedEvent(boolean driverRated) {
        super(driverRated);
        this.driverRated = driverRated;
    }

    public boolean isDriverRated() {
        return driverRated;
    }
}
