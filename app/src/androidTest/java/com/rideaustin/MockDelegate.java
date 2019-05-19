package com.rideaustin;

import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.Ride;

import java.lang.reflect.Type;

/**
 * Created by Sergey Petrov on 22/05/2017.
 */
public interface MockDelegate {

    void mockLocation(double lat, double lng);

    void mockLocation(double lat, double lng, float accuracy);

    void mockRequests(RequestType... requestTypes);

    void mockRequest(RequestType requestType, Object responseBody);

    void mockRequest(RequestType requestType, String jsonString);

    void delayRequest(RequestType requestType, long delay);

    void removeRequests(RequestType... requestTypes);

    void chainRequests(RequestType... requestTypes);

    void chainRequests(RequestWrapper... wrappers);

    void atomicOnRequests(Runnable runnable);

    <T> T getResponse(String resourceName, Class<T> modelClass);

    <T> T getResponse(String resourceName, Type type);

    void mockEvent(RequestType eventType, String resourceName, ResponseModifier<Event> modifier);

    void setNetworkError(boolean error);
}