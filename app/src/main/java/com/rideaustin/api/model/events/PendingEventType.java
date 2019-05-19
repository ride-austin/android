package com.rideaustin.api.model.events;

import android.support.annotation.StringDef;

/**
 * Created by hatak on 23.12.16.
 */

@StringDef({
        PendingEventType.DRIVER_REACHED,
        PendingEventType.START_RIDE,
        PendingEventType.UPDATE_LOCATION,
        PendingEventType.END_RIDE,
        PendingEventType.NO_TYPE})
public @interface PendingEventType {
    String DRIVER_REACHED = "DRIVER_REACHED";
    String START_RIDE = "START_RIDE";
    String UPDATE_LOCATION = "UPDATE_LOCATION";
    String END_RIDE = "END_RIDE";
    String NO_TYPE = "NO_TYPE";
}


