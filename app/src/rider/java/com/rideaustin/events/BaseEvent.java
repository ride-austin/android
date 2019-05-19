package com.rideaustin.events;

/**
 * Created by kshumelchyk on 7/21/16.
 */
public class BaseEvent<T> {

    private T data;

    public BaseEvent(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
