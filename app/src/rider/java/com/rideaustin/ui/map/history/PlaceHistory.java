package com.rideaustin.ui.map.history;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.rideaustin.entities.GeoPosition;

import java.io.Serializable;

/**
 * Created by rideclientandroid on 10.10.2016.
 */

public class PlaceHistory implements Serializable {

    @SerializedName("timestamp")
    private long timeStamp;
    @SerializedName("geoposition")
    private final GeoPosition geoPosition;

    public PlaceHistory(String placeId, String primaryText, String fullText, long timeMillis, LatLng latLng, String zipCode) {
        this.geoPosition = new GeoPosition(placeId, primaryText, fullText, latLng, zipCode);
        this.timeStamp = timeMillis;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public GeoPosition getGeoPosition() {
        return geoPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlaceHistory that = (PlaceHistory) o;

        return geoPosition.getFullAddress() != null ? geoPosition.getFullAddress().equals(that.geoPosition.getFullAddress()) : that.geoPosition.getFullAddress() == null;
    }

    @Override
    public int hashCode() {
        return geoPosition.getFullAddress() != null ? geoPosition.getFullAddress().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PlaceHistory{" +
                "timeStamp=" + timeStamp +
                ", geoPosition=" + geoPosition +
                '}';
    }
}
