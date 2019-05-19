package com.rideaustin.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.rideaustin.utils.Constants;

import java.util.Arrays;
import java.util.List;

/**
 * Caches direction calculated by Google.
 * Will return the same direction if:
 * <ul>
 * <li>start and end locations not changed</li>
 * <li>start location changed but belongs to direction and end location not changed</li>
 * </ul>
 * Created by Sergey Petrov on 26/05/2017.
 */
public class CachedDirection {

    @NonNull
    private LatLng start;

    @NonNull
    private LatLng end;

    @Nullable
    private List<LatLng> direction;

    private LatLng[] waypoints;

    public CachedDirection(@NonNull LatLng start, @NonNull LatLng end, @Nullable List<LatLng> direction, LatLng... waypoints) {
        this.start = start;
        this.end = end;
        this.direction = direction;
        this.waypoints = waypoints;
    }

    public void setDirection(@Nullable List<LatLng> direction, LatLng... waypoints) {
        this.direction = direction;
        this.waypoints = waypoints;
    }

    @Nullable
    public List<LatLng> getDirection() {
        return direction;
    }

    public boolean isDirectionRequested(@NonNull LatLng start, @NonNull LatLng end, LatLng... waypoints) {
        return this.start.equals(start) && this.end.equals(end) && waypointsEqual(waypoints);
    }

    public boolean isDirectionCached(@NonNull LatLng current, @NonNull LatLng end, LatLng... waypoints) {
        if (direction == null) {
            return false;
        }
        if (isDirectionRequested(current, end, waypoints)) {
            // current location equals to start, end locations equal
            // assume direction is the same
            return true;
        }
        if (this.end.equals(end) && waypointsEqual(waypoints)) {
            // RA-9853: end locations equal, check if current location belongs to direction
            return PolyUtil.isLocationOnPath(current, direction, true, Constants.ROUTE_TOLERANCE);
        }
        return false;
    }

    private boolean waypointsEqual(LatLng[] waypoints) {
        return Arrays.deepEquals(this.waypoints, waypoints);
    }

}
