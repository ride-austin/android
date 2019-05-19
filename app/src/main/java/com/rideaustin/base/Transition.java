package com.rideaustin.base;

/**
 * Created by Sergey Petrov on 19/07/2017.
 */

public enum Transition {
    NONE,
    FORWARD,
    BACKWARD,
    FORWARD_SIMPLE,
    BACKWARD_SIMPLE,
    UP;

    Transition toSimple() {
        switch (this) {
            case FORWARD: return FORWARD_SIMPLE;
            case BACKWARD: return BACKWARD_SIMPLE;
        }
        return this;
    }
}
