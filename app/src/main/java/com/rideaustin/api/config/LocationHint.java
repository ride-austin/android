package com.rideaustin.api.config;

import android.support.annotation.VisibleForTesting;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.PolyUtil;
import com.rideaustin.api.model.Coordinates;
import com.rideaustin.api.model.LocationHintCoord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergey Petrov on 30/08/2017.
 */

public class LocationHint implements Serializable {

    @SerializedName("name")
    private String name;

    @SerializedName("areaPolygon")
    private List<Coordinates> areaPolygon;

    @SerializedName("designatedPickups")
    private List<LocationHintCoord> designatedPickups;

    public String getName() {
        return name;
    }

    private transient List<LatLng> boundaries;

    public List<LatLng> getBoundaries() {
        if (boundaries == null && areaPolygon != null) {
            boundaries = new ArrayList<>(areaPolygon.size());
            for (Coordinates coord : areaPolygon) {
                boundaries.add(coord.getLatLng());
            }
        }
        return boundaries;
    }

    public boolean contains(LatLng location) {
        return getBoundaries() != null && PolyUtil.containsLocation(location, getBoundaries(), true);
    }

    public List<LocationHintCoord> getCoords() {
        return designatedPickups;
    }

    @VisibleForTesting
    public void setAreaPolygon(List<Coordinates> areaPolygon) {
        this.areaPolygon = areaPolygon;
    }

    @VisibleForTesting
    public void setDesignatedPickups(List<LocationHintCoord> designatedPickups) {
        this.designatedPickups = designatedPickups;
    }
}
