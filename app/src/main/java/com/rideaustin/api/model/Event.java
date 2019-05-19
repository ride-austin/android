package com.rideaustin.api.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rideaustin.utils.SerializationHelper;

import java.io.Serializable;

import java8.util.Optional;
import timber.log.Timber;

/**
 * Created by kshumelchyk on 8/17/16.
 */
public class Event implements Serializable {

    @SerializedName("id")
    private long id;

    @SerializedName("eventType")
    private String eventType;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("parameters")
    private String parameters;

    @SerializedName("ride")
    private Ride ride;

    @SerializedName("nextRide")
    private Ride nextRide;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public RideStatus getEventType() {
        return RideStatus.valueOf(eventType);
    }

    public void setEventType(RideStatus eventType) {
        this.eventType = eventType.name();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }

    public Ride getNextRide() {
        return nextRide;
    }

    public void setNextRide(Ride nextRide) {
        this.nextRide = nextRide;
    }

    @Nullable
    public <T> T deserializeParams(Class<T> type) {
        if (!TextUtils.isEmpty(parameters)) {
            return SerializationHelper.deSerialize(parameters, type);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", eventType='" + eventType + '\'' +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", parameters='" + parameters + '\'' +
                ", ride=" + Optional.ofNullable(ride).map(Ride::getId).orElse(null) +
                ", nextRide=" + Optional.ofNullable(nextRide).map(Ride::getId).orElse(null) +
                '}';
    }
}
