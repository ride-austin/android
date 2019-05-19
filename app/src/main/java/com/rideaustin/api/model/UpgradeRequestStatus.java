package com.rideaustin.api.model;

/**
 * Created by hatak on 08.06.2017.
 */

public enum UpgradeRequestStatus {
    REQUESTED("REQUESTED"), ACCEPTED("ACCEPTED"), DECLINED("DECLINED"), EXPIRED("EXPIRED"), CANCELLED("CANCELLED"),
    /**
     * This is for internal usage only, server is not involved
     */
    NONE("NONE"),
    /**
     * This is for internal usage only, server is not involved
     */
    SEND("SEND");

    private String status;

    UpgradeRequestStatus(String status) {
        this.status = status;
    }
}
