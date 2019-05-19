package com.rideaustin;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Sergey Petrov on 29/08/2017.
 */

public class RequestWrapper {

    @NonNull
    private RequestType requestType;

    @Nullable
    private Object responseBody;

    @Nullable
    private Long delay;

    public RequestWrapper(@NonNull RequestType requestType) {
        this.requestType = requestType;
    }

    public static RequestWrapper wrap(RequestType requestType) {
        return new RequestWrapper(requestType);
    }

    public static RequestWrapper wrap(RequestType requestType, long delay) {
        RequestWrapper wrapper = new RequestWrapper(requestType);
        wrapper.setDelay(delay);
        return wrapper;
    }

    public static RequestWrapper wrap(RequestType requestType, Object responseBody) {
        RequestWrapper wrapper = new RequestWrapper(requestType);
        wrapper.setResponseBody(responseBody);
        return wrapper;
    }

    public static RequestWrapper wrap(RequestType requestType, Object responseBody, long delay) {
        RequestWrapper wrapper = new RequestWrapper(requestType);
        wrapper.setResponseBody(responseBody);
        wrapper.setDelay(delay);
        return wrapper;
    }

    @NonNull
    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(@NonNull RequestType requestType) {
        this.requestType = requestType;
    }

    @Nullable
    public Object getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(@Nullable Object responseBody) {
        this.responseBody = responseBody;
    }

    @Nullable
    public Long getDelay() {
        return delay;
    }

    public void setDelay(@Nullable Long delay) {
        this.delay = delay;
    }

    @Override
    public String toString() {
        return "RequestWrapper{" +
                "requestType=" + requestType +
                ", responseBody=" + responseBody +
                ", delay=" + delay +
                '}';
    }
}
