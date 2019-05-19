package com.rideaustin;

import okhttp3.Request;

/**
 * Created by Sergey Petrov on 05/05/2017.
 */

public interface MockResponse {
    long getDelay();

    MockResponse delayedBy(long delay);

    int getHttpCode();

    String getBody();

    void setBody(String body);

    boolean matches(Request request);

    void setChain(RequestWrapper[] chain);

    RequestWrapper[] getChain();
}
