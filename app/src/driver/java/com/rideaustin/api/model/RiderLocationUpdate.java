package com.rideaustin.api.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.rideaustin.utils.SerializationHelper;

/**
 * Created by hatak on 13.03.2017.
 */

public class RiderLocationUpdate {

    @SerializedName("lat")
    private Double lat;
    @SerializedName("lng")
    private Double lng;
    @SerializedName("timeRecorded")
    private Long timeRecorded;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Long getTimeRecorded() {
        return timeRecorded;
    }

    public void setTimeRecorded(Long timeRecorded) {
        this.timeRecorded = timeRecorded;
    }

    @Override
    public String toString() {
        return "RiderLocationUpdate{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", timeRecorded=" + timeRecorded +
                '}';
    }

    @Nullable
    public static RiderLocationUpdate fromJson(final String json) {
        if (!TextUtils.isEmpty(json)) {
            return SerializationHelper.deSerialize(json, RiderLocationUpdate.class);
        }
        return null;
    }
}
