package com.rideaustin.manager.location;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.TimeUtils;

public class RALocation {

    /**
     * State of the RALocation
     * ON - means valid location with coordinates
     * OFF - means location was switched off, no coordinates
     * INVALID - means location in unknown state, no coordinates
     */
    public enum State {
        ON, OFF, INVALID // only ON State has lat, lon and course information
    }

    private static final RALocation offLocation = new RALocation(State.OFF);
    private static final RALocation invalidLocation = new RALocation(State.INVALID);

    private final RALocationType owner;
    private final State state;
    private final Location location;
    private final LatLng coordinates;
    private final long timestamp;

    public RALocation(State state) {
        this(RALocationType.ME, state, new Location(""), 0);
    }

    public RALocation(RALocationType owner, State state, Location location, long timestamp) {
        this.state = state;
        this.location = location;
        this.owner = owner;
        this.coordinates = new LatLng(location.getLatitude(), location.getLongitude());
        this.timestamp = timestamp;
    }

    public static RALocation on(Location location) {
        return new RALocation(RALocationType.ME, State.ON, location, TimeUtils.currentTimeMillis());
    }

    public static RALocation off() {
        return offLocation;
    }

    public static RALocation invalid() {
        return invalidLocation;
    }

    public State getState() {
        return state;
    }

    public Location getLocation() {
        return location;
    }

    public LatLng getCoordinates(){
        return coordinates;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public RALocationType getOwner() {
        return owner;
    }

    public boolean isValid() {
        return state == State.ON
                && location != null
                && (int) (location.getLongitude()) != 0
                && (int) (location.getLatitude()) != 0
                && TimeUtils.currentTimeMillis() - getTimestamp() < Constants.LOCATION_IS_VALID_TIMEOUT_MS;
    }

    @Override
    public String toString() {
        return "RALocation{" +
                "owner=" + owner +
                ", state=" + state +
                ", location=" + location +
                '}';
    }
}
