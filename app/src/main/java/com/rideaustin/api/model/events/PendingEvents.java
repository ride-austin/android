package com.rideaustin.api.model.events;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hatak on 23.12.16.
 * Package of events not sent to server due to connectivity problems
 */

public class PendingEvents implements Serializable {

    @SerializedName("events")
    private List<PendingEvent> pendingEvents = new ArrayList<>();

    public List<PendingEvent> getList() {
        return pendingEvents;
    }

    public void add(final PendingEvent pendingEvent) {
        pendingEvents.add(pendingEvent);
    }

    public boolean isEmpty() {
        return pendingEvents.isEmpty();
    }
}
