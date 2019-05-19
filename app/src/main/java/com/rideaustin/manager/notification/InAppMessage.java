package com.rideaustin.manager.notification;

import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.utils.TimeUtils;

import java.sql.Time;

import timber.log.Timber;

/**
 * Created by hatak on 2/9/17.
 */

public class InAppMessage {

    /** start value should be unique across sessions **/
    private static volatile int messageId = (int) (TimeUtils.currentTimeMillis() % Integer.MAX_VALUE);

    private final int id;
    private String title;
    private String message;
    private boolean consumed;
    private int notificationId;
    private Ride ride;
    private long receivedAt;

    public InAppMessage() {
        this.id = messageId++;
        this.receivedAt = TimeUtils.currentTimeMillis();
        // empty constructor for custom InAppMessage implementation
    }

    public InAppMessage(String message) {
        this.id = messageId++;
        this.title = null;
        this.message = message;
        this.notificationId = id;
        this.receivedAt = TimeUtils.currentTimeMillis();
    }

    public InAppMessage(String title, String message) {
        this.id = messageId++;
        this.title = title;
        this.message = message;
        this.notificationId = id;
        this.receivedAt = TimeUtils.currentTimeMillis();
    }

    public InAppMessage(String title, String message, Ride ride) {
        this.id = messageId++;
        this.title = title;
        this.message = message;
        this.ride = ride;
        this.notificationId = id;
        this.receivedAt = TimeUtils.currentTimeMillis();
    }

    public int getId() {
        return id;
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

    public void consume() {
        consumed = true;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public long getRideId() {
        return ride != null ? ride.getId() : 0;
    }

    public RideStatus getRideStatus() {
        if (ride != null) {
            try {
                return RideStatus.valueOf(ride.getStatus());
            } catch (Exception e) {
                Timber.e(e);
                return RideStatus.UNKNOWN;
            }
        }
        return null;
    }

    public Ride getRide() {
        return ride;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public long getReceivedAt() {
        return receivedAt;
    }
}
