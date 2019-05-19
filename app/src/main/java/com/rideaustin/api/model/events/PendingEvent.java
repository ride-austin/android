package com.rideaustin.api.model.events;

import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.utils.TimeUtils;

import java.io.Serializable;

/**
 * Created by hatak on 23.12.16.
 */

public class PendingEvent implements Serializable {

    @PendingEventType
    @SerializedName("eventType")
    private String pendingEventType;

    @SerializedName("eventTimestamp")
    private long eventTimestamp;

    @SerializedName("avatarType")
    private String avatarType;

    @SerializedName("rideId")
    private long rideId;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("speed")
    private Float speed;

    @SerializedName("heading")
    private Float heading;

    @SerializedName("course")
    private Float course;

    @SerializedName("endLocationLat")
    private Double endLocationLat;

    @SerializedName("endLocationLong")
    private Double endLocationLong;

    @PendingEventType
    public String getPendingEventType() {
        return pendingEventType;
    }

    public void setPendingEventType(final String pendingEventType) {
        this.pendingEventType = pendingEventType;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(final long eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public String getAvatarType() {
        return avatarType;
    }

    public void setAvatarType(final String avatarType) {
        this.avatarType = avatarType;
    }

    public long getRideId() {
        return rideId;
    }

    public void setRideId(final long rideId) {
        this.rideId = rideId;
    }

    @Nullable
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(final Double latitude) {
        this.latitude = latitude;
    }

    @Nullable
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(final Double longitude) {
        this.longitude = longitude;
    }

    @Nullable
    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(final Float speed) {
        this.speed = speed;
    }

    @Nullable
    public Float getHeading() {
        return heading;
    }

    public void setHeading(final Float heading) {
        this.heading = heading;
    }

    @Nullable
    public Float getCourse() {
        return course;
    }

    public void setCourse(final Float course) {
        this.course = course;
    }

    @Nullable
    public Double getEndLocationLat() {
        return endLocationLat;
    }

    public void setEndLocationLat(Double endLocationLat) {
        this.endLocationLat = endLocationLat;
    }

    @Nullable
    public Double getEndLocationLong() {
        return endLocationLong;
    }

    public void setEndLocationLong(Double endLocationLong) {
        this.endLocationLong = endLocationLong;
    }

    public static PendingEvent create(final @PendingEventType String eventType, final long rideId) {
        PendingEvent pendingEvent = new PendingEvent();
        pendingEvent.pendingEventType = eventType;
        pendingEvent.rideId = rideId;
        pendingEvent.eventTimestamp = TimeUtils.currentTimeMillis();
        pendingEvent.avatarType = AvatarType.DRIVER.name();
        return pendingEvent;
    }

    public static PendingEvent createWithLocation(final @PendingEventType String eventType, final long rideId, Location location) {
        PendingEvent pendingEvent = new PendingEvent();
        pendingEvent.setPendingEventType(eventType);
        pendingEvent.setRideId(rideId);
        pendingEvent.setEventTimestamp(TimeUtils.currentTimeMillis());
        pendingEvent.setAvatarType(AvatarType.DRIVER.name());
        pendingEvent.setCourse(location.getBearing());
        pendingEvent.setLatitude(location.getLatitude());
        pendingEvent.setLongitude(location.getLongitude());
        pendingEvent.setSpeed(location.getSpeed());
        return pendingEvent;
    }

    public static PendingEvent createWithEndLocation(final @PendingEventType String eventType, final long rideId, LatLng location) {
        PendingEvent pendingEvent = new PendingEvent();
        pendingEvent.setPendingEventType(eventType);
        pendingEvent.setRideId(rideId);
        pendingEvent.setEventTimestamp(TimeUtils.currentTimeMillis());
        pendingEvent.setAvatarType(AvatarType.DRIVER.name());
        pendingEvent.setEndLocationLat(location.latitude);
        pendingEvent.setEndLocationLong(location.longitude);
        return pendingEvent;
    }
}
