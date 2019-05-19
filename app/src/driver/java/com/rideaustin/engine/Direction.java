package com.rideaustin.engine;

import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.List;

/**
 * Created by ysych on 8/19/16.
 */
public class Direction {
    private final List<LatLng> polyline;

    public Direction(List<LatLng> polyline) {
        this.polyline = polyline;
    }

    public List<LatLng> getPolyline() {
        return Collections.unmodifiableList(polyline);
    }
}
