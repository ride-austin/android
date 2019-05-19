package com.rideaustin.ui.common;

import java8.util.Optional;

/**
 * Created by hatak on 23.10.2017.
 */

public class ProgressEvent {

    public enum Type {
        SHOW_PROGRESS,
        SHOW_PROGRESS_WITH_MESSAGE,
        SHOW_LOADING_WHEEL,
        HIDE_PROGRESS
    }

    private final Type type;
    private final Optional<String> message;

    private ProgressEvent(Type type, Optional<String> message) {
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public Optional<String> getMessage() {
        return message;
    }

    public static ProgressEvent showProgress() {
        return new ProgressEvent(Type.SHOW_PROGRESS, Optional.empty());
    }

    public static ProgressEvent showProgress(String message) {
        return new ProgressEvent(Type.SHOW_PROGRESS, Optional.ofNullable(message));
    }

    public static ProgressEvent showLoadingWheel() {
        return new ProgressEvent(Type.SHOW_LOADING_WHEEL, Optional.empty());
    }

    public static ProgressEvent hideProgress() {
        return new ProgressEvent(Type.HIDE_PROGRESS, Optional.empty());
    }
}
