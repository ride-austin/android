package com.rideaustin.engine;

import android.support.annotation.Nullable;

import com.rideaustin.api.model.events.PendingEvent;
import com.rideaustin.api.model.events.PendingEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java8.util.Optional;

/**
 * Synthesized response for {@link com.rideaustin.api.service.EventsService#sendPendingEvents(PendingEvents)}
 * Contains {@link Result} of request and {@link Exception} if request failed.
 * <p>
 * Created by Sergey Petrov on 31/03/2017.
 */

public class PendingEventsResponse {

    public enum Result {
        SEND_SUCCEEDED, SEND_FAILED, NO_EVENTS
    }

    private final Result result;
    private final List<PendingEvent> sendEvents;


    @Nullable
    private final Exception exception;

    public boolean isSuccessful() {
        return result != Result.SEND_FAILED;
    }

    public Result getResult() {
        return result;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }

    public PendingEventsResponse(Result result, @Nullable Exception exception, Optional<List<PendingEvent>> sendEvents) {
        this.result = result;
        this.exception = exception;
        this.sendEvents = new ArrayList<>(sendEvents.orElse(Collections.EMPTY_LIST));
    }

    public SwitchNextData getSwitchData() {
        if (isSuccessful()) {
            return SwitchNextData.ok();
        } else {
            return SwitchNextData.error(exception);
        }
    }

    public List<PendingEvent> getSendEvents() {
        return sendEvents;
    }
}
