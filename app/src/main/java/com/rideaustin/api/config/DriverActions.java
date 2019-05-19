package com.rideaustin.api.config;

/**
 * Created on 2/2/19.
 *
 * @author sdelaysam
 */
public class DriverActions {

    private int autoArriveDistanceToPickup;

    private int autoEndDistanceToDestination;

    private int allowArriveDistanceToPickup;

    private int remindToArriveDistanceFromPickup;

    public int getAutoArriveDistanceToPickup() {
        return autoArriveDistanceToPickup;
    }

    public int getAutoEndDistanceToDestination() {
        return autoEndDistanceToDestination;
    }

    public int getAllowArriveDistanceToPickup() {
        return allowArriveDistanceToPickup;
    }

    public int getRemindToArriveDistanceFromPickup() {
        return remindToArriveDistanceFromPickup;
    }
}
