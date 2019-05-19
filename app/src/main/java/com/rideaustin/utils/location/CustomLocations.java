package com.rideaustin.utils.location;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.entities.GeoPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observable;

/**
 * Created by Viktor Kifer
 * On 31-Dec-2016.
 */

public class CustomLocations {

    /**
     * Custom location will include 30 meters radius around
     */
    private static final double INCLUDE_RADIUS = 30;

    private static final List<GeoPosition> positions = new ArrayList<>();
    private static final Map<String, GeoPosition> places = new HashMap<>();

    public static void put(@NonNull GeoPosition position) {
        // remove old position with same location/bounds
        // e.g. if some custom place like airport is defined again as "Work"
        // it will be replaced with new definition
        removePosition(position);

        // allow to detect near-by places as well
        // all geo positions covered by INCLUDE_RADIUS will be detected as custom place
        position.setIncludeRadius(INCLUDE_RADIUS);

        // add new position
        positions.add(position);
        if (!TextUtils.isEmpty(position.getPlaceId())) {
            places.put(position.getPlaceId(), position);
        }
    }

    public static Observable<GeoPosition> fromPlaceId(String placeId) {
        if (places.containsKey(placeId)) {
            return Observable.just(places.get(placeId));
        }
        return Observable.empty();
    }

    public static String getPlaceName(@NonNull GeoPosition position) {
        if (position.hasCustomName()) {
            return position.getCustomName();
        }
        GeoPosition p = findPosition(position);
        if (p.hasCustomName()) {
            position.setCustomName(p.getCustomName());
            return p.getCustomName();
        } else {
            position.setCustomName(null);
            return p.getAddressLine();
        }
    }

    public static void reset() {
        clear();
        put(new GeoPosition(
                "ChIJRf9KizuxRIYRaCfcyaj8pxw",
                "3600 Presidential Blvd",
                "Austin-Bergstrom International Airport, Presidential Boulevard, Austin, TX, United States",
                new LatLng(30.2021489, -97.666829),
                "78719"));
    }

    public static void clear() {
        positions.clear();
        places.clear();
    }

    private static GeoPosition findPosition(@NonNull GeoPosition position) {
        for (GeoPosition p : positions) {
            if (p.includesLocation(position.getLatLng())) {
                return p;
            }
        }
        return position;
    }

    private static void removePosition(@NonNull GeoPosition position) {
        Iterator<GeoPosition> iterator = positions.iterator();
        while (iterator.hasNext()) {
            GeoPosition p = iterator.next();
            if (p == position || p.includesLocation(position.getLatLng())) {
                iterator.remove();
            }
        }
    }
}
