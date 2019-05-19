package com.rideaustin.utils.location;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by rost on 8/15/16.
 */
public final class DistanceUtil {
    private DistanceUtil() {
    }

    public static float distance(@NonNull LatLng start, @NonNull LatLng finish) {
        final float[] result = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, finish.latitude, finish.longitude, result);
        return result[0];
    }


}
