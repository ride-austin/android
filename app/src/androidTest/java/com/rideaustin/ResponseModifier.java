package com.rideaustin;

/**
 * Created by Sergey Petrov on 14/06/2017.
 */
public interface ResponseModifier<T> {
    T modifyResponse(T response);
}
