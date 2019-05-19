package com.rideaustin.engine;

import android.support.annotation.Nullable;

/**
 * Data object for {@link SwitchNext#switchNext(SwitchNextData)} command.
 * May be used to pass data to {@link BaseEngineState}
 * It may seem wrong that {@link BaseEngineState}  make use of it (not {@link com.rideaustin.ui.map.strategy.UIStrategy})
 * But it's currently designed that way.
 *
 * Created by Sergey Petrov on 31/03/2017.
 */

public class SwitchNextData {

    private enum Status {
        OK,
        ERROR
    }

    private final Status status;

    @Nullable
    private final Exception exception;

    private SwitchNextData(Status status, @Nullable Exception exception) {
        this.status = status;
        this.exception = exception;
    }

    public boolean isOk() {
        return status == Status.OK;
    }

    public Exception getException() {
        return exception;
    }

    public static SwitchNextData ok() {
        return new SwitchNextData(Status.OK, null);
    }

    public static SwitchNextData error(Exception e) {
        return new SwitchNextData(Status.ERROR, e);
    }

}
