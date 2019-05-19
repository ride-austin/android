package com.rideaustin.api.model;

/**
 * Created by vokol on 07.07.2016.
 */
public enum AvatarType {
    RIDER("RIDER"),
    DRIVER("DRIVER"),
    ADMIN("ADMIN");

    public final String avatarType;

    AvatarType(String type){
        this.avatarType = type;
    }
}
