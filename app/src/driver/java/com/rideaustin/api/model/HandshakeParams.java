package com.rideaustin.api.model;

import java.io.Serializable;

/**
 * Created on 02/04/2018
 *
 * @author sdelaysam
 */

public class HandshakeParams implements Serializable {

    private long rideId;

    private long handshakeExpiration;

    public long getRideId() {
        return rideId;
    }

    public void setRideId(long rideId) {
        this.rideId = rideId;
    }

    public long getHandshakeExpiration() {
        return handshakeExpiration;
    }

    public void setHandshakeExpiration(long handshakeExpiration) {
        this.handshakeExpiration = handshakeExpiration;
    }

    @Override
    public String toString() {
        return "HandshakeParams{" +
                "rideId=" + rideId +
                ", handshakeExpiration=" + handshakeExpiration +
                '}';
    }
}
