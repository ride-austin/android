package com.rideaustin.engine;

/**
 * Created by rost on 8/11/16.
 */
public interface EngineState extends SwitchNext {
    enum Type {
        INACTIVE, OFFLINE, ONLINE, PENDING_ACCEPT, ACCEPTED, ARRIVED, STARTED, ENDED,
        // artificial state should never happen, please don't use it
        UNKNOWN
    }

    Type getType();
}
