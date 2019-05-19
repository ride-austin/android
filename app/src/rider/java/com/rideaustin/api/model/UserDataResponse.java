package com.rideaustin.api.model;

/**
 * Created by kshumelchyk on 7/4/16.
 */
public class UserDataResponse {

    private String type;
    private long id;
    private User user;
    private boolean active;
    private float rating;
    private String uuid;

    public String getType() {
        return type;
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public boolean isActive() {
        return active;
    }

    public float getRating() {
        return rating;
    }

    public String getUuid() {
        return uuid;
    }
}
