package com.rideaustin.utils.location;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rideaustin.entities.GeoPosition;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import java8.util.Optional;
import timber.log.Timber;

/**
 * Created by Sergey Petrov on 10/08/2017.
 */

public class RideComments {

    /**
     * Lookup radius around test location to match location with comment
     */
    private static final double LOOKUP_RADIUS = 15;

    private static final String PREF_KEY = "key_comments_map";

    private static final Type TYPE = new TypeToken<Map<LatLng, String>>() { }.getType();

    private Map<LatLng, String> commentsMap = new HashMap<>();

    public void restore(SharedPreferences preferences) {
        commentsMap = deserializeMap(preferences.getString(PREF_KEY, ""));
    }

    public void save(SharedPreferences preferences) {
        if (!commentsMap.isEmpty()) {
            preferences.edit().putString(PREF_KEY, serializeMap()).apply();
        } else {
            preferences.edit().remove(PREF_KEY).apply();
        }
    }

    public void set(Optional<String> comment, GeoPosition address) {
        if (address != null && address.getLatLng() != null) {
            String commentStr = comment.orElse("");
            if (!TextUtils.isEmpty(commentStr)) {
                commentsMap.put(address.getLatLng(), commentStr);
            } else {
                commentsMap.remove(address.getLatLng());
            }
        }
    }

    public Optional<String> get(GeoPosition address) {
        if (address == null || address.getLatLng() == null || commentsMap.isEmpty()) {
            return Optional.empty();
        }
        // first try to match exact coordinates
        String comment = commentsMap.get(address.getLatLng());
        if (!TextUtils.isEmpty(comment)) {
            return Optional.of(comment);
        }
        // then try to find using approximation
        if (!address.hasIncludeRadius()) {
            address.setIncludeRadius(LOOKUP_RADIUS);
        }
        for (LatLng latLng : commentsMap.keySet()) {
            if (address.includesLocation(latLng)) {
                comment = commentsMap.get(latLng);
                return Optional.of(comment);
            }
        }
        return Optional.empty();
    }

    public void clear() {
        commentsMap.clear();
    }

    private String serializeMap() {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.toJson(commentsMap);
    }

    private Map<LatLng, String> deserializeMap(String string) {
        if (!TextUtils.isEmpty(string)) {
            try {
                Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                return gson.fromJson(string, TYPE);
            } catch (Exception e) {
                Timber.e(e, "cannot deSerialize for class: %s the string: %s", TYPE.getClass().getSimpleName(), string);
                throw e;
            }
        }
        return new HashMap<>();
    }
}
