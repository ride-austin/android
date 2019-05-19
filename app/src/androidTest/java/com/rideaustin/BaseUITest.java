package com.rideaustin;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.rideaustin.api.CommonDataManager;
import com.rideaustin.api.model.Event;
import com.rideaustin.utils.ResourceUtils;
import com.rideaustin.utils.SerializationHelper;

import org.hamcrest.Matcher;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.fail;

/**
 * Created by crossover on 30/04/2017.
 */

public abstract class BaseUITest extends BaseTest implements MockDelegate {

    private MockInterceptor interceptor;

    @Override
    @CallSuper
    public void setUp() {
        super.setUp();
        registerMockInterceptor();
        // need to recreate endpoints
        // because they should use interceptors
        // and other resources we just set
        App.getInstance().refreshEndpoints();
    }

    @Override
    @CallSuper
    public void tearDown() {
        super.tearDown();
        setNetworkError(false);
    }

    protected void initMockResponseFactory(Class<? extends MockResponseFactory> factoryClass) {
        try {
            setMockResponseFactory(factoryClass.newInstance());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to instantiate MockResponseFactory with class: " + factoryClass);
        }
    }

    protected void setMockResponseFactory(MockResponseFactory mockResponseFactory) {
        interceptor.setMockResponseFactory(mockResponseFactory);
    }

    public void mockRequests(RequestType... requestTypes) {
        if (!interceptor.hasMockResponseFactory()) {
            throw new IllegalStateException("Please set mock response factory first");
        }
        interceptor.mockRequests(requestTypes);
    }

    public void mockRequest(RequestType requestType, Object responseBody) {
        if (!interceptor.hasMockResponseFactory()) {
            throw new IllegalStateException("Please set mock response factory first");
        }
        interceptor.mockRequest(RequestWrapper.wrap(requestType, responseBody));
    }

    public void mockRequest(RequestType requestType, String jsonString) {
        if (!interceptor.hasMockResponseFactory()) {
            throw new IllegalStateException("Please set mock response factory first");
        }
        interceptor.mockRequest(RequestWrapper.wrap(requestType, jsonString));
    }

    public void chainRequests(RequestType... requestTypes) {
        if (!interceptor.hasMockResponseFactory()) {
            throw new IllegalStateException("Please set mock response factory first");
        }
        interceptor.chainRequests(requestTypes);
    }

    public void chainRequests(RequestWrapper... wrappers) {
        if (!interceptor.hasMockResponseFactory()) {
            throw new IllegalStateException("Please set mock response factory first");
        }
        interceptor.chainRequests(wrappers);
    }

    public void delayRequest(RequestType requestType, long delay) {
        if (!interceptor.hasMockResponseFactory()) {
            throw new IllegalStateException("Please set mock response factory first");
        }
        interceptor.delayRequest(requestType, delay);
    }

    public void removeRequests(RequestType... requestTypes) {
        if (!interceptor.hasMockResponseFactory()) {
            throw new IllegalStateException("Please set mock response factory first");
        }
        interceptor.removeRequests(requestTypes);
    }

    @Override
    public void atomicOnRequests(Runnable runnable) {
        synchronized (interceptor) {
            runnable.run();
        }
    }

    public <T> T getResponse(String resourceName, Class<T> modelClass) {
        return SerializationHelper.deSerialize(ResourceUtils.getContent(getClass(), resourceName + ".json"), modelClass);
    }

    public <T> T getResponse(String resourceName, Type type) {
        return SerializationHelper.deSerialize(ResourceUtils.getContent(getClass(), resourceName + ".json"), type);
    }

    protected int getRequestCount(RequestType requestType) {
        RequestStats requestStats = interceptor.getStats(requestType);
        return requestStats != null ? requestStats.getCount() : 0;
    }

    protected boolean hasRequest(RequestType requestType) {
        return interceptor.getStats(requestType) != null;
    }

    protected boolean hasRequest(RequestType requestType, RequestMatcher requestMatcher) {
        RequestStats requestStats = interceptor.getStats(requestType);
        if (requestStats == null) {
            return false;
        }
        if (requestStats.getCount() > 1) {
            fail("Found multiple requests for type: " + requestType + ", count: " + requestStats.getCount());
        }
        RequestMatcher.Result result = requestMatcher.match(requestStats);
        return result.isSuccess();
    }

    protected boolean hasRequests(RequestType requestType, RequestMatcher requestMatcher) {
        RequestStats requestStats = interceptor.getStats(requestType);
        if (requestStats == null) {
            return false;
        }
        RequestMatcher.Result result = requestMatcher.match(requestStats);
        return result.isSuccess();
    }

    protected void verifyRequest(RequestType requestType, RequestMatcher requestMatcher) {
        RequestStats requestStats = interceptor.getStats(requestType);
        if (requestStats == null) {
            fail("No requests found for type: " + requestType);
        }
        if (requestStats.getCount() > 1) {
            fail("Found multiple requests for type: " + requestType + ", count: " + requestStats.getCount());
        }
        RequestMatcher.Result result = requestMatcher.match(requestStats);
        if (!result.isSuccess()) {
            fail("Request " + requestType + " failed: " + result.getMessage());
        }
    }

    protected void verifyRequests(RequestType requestType, RequestMatcher requestMatcher) {
        RequestStats requestStats = interceptor.getStats(requestType);
        if (requestStats == null) {
            fail("No requests found for type: " + requestType);
        }
        RequestMatcher.Result result = requestMatcher.match(requestStats);
        if (!result.isSuccess()) {
            fail("Request " + requestType + " failed: " + result.getMessage());
        }
    }

    protected void verifyRequestsCount(RequestType requestType, Matcher<Integer> matcher) {
        int count = getRequestCount(requestType);
        if (!matcher.matches(count)) {
            fail("expected count is: " + matcher + " actual is: " + count);
        }
    }

    protected void resetRequestStats(RequestType requestType) {
        interceptor.resetStats(requestType);
    }

    protected void setOnRequestAction(RequestType requestType, Runnable action) {
        interceptor.setOnRequestAction(requestType, action);
    }

    protected void removeOnRequestAction(RequestType requestType) {
        interceptor.removeOnRequestAction(requestType);
    }

    @Override
    public void setNetworkError(boolean hasError) {
        setNetworkError(hasError ? new UnknownHostException("Unable to reach api endpoint") : null);
    }

    protected void setNetworkError(@Nullable IOException error) {
        interceptor.setNetworkError(error);
    }

    private void registerMockInterceptor() {
        CommonDataManager.setMockInterceptor(interceptor = new MockInterceptor());
    }

    public List<Event> getEventsResponse(String resourceName) {
        Type type = new TypeToken<ArrayList<Event>>() {
        }.getType();
        return getResponse(resourceName, type);
    }

    public void mockEvent(RequestType eventType) {
        atomicOnRequests(() -> {
            removeRequests(eventType, RequestType.EVENTS_EMPTY_200_GET);
            chainRequests(eventType, RequestType.EVENTS_EMPTY_200_GET);
        });
    }

    public void mockEvent(RequestType eventType, String resourceName, ResponseModifier<Event> modifier) {
        atomicOnRequests(() -> {
            List<Event> events = getEventsResponse(resourceName);
            Event event = modifier.modifyResponse(events.get(0));
            events.set(0, event);
            removeRequests(eventType, RequestType.EVENTS_EMPTY_200_GET);
            chainRequests(RequestWrapper.wrap(eventType, events, 2000),
                    RequestWrapper.wrap(RequestType.EVENTS_EMPTY_200_GET, 2000));
        });
    }

    public void mockEvents(RequestType eventType, String resourceName, ResponseModifier<List<Event>> modifier) {
        atomicOnRequests(() -> {
            List<Event> events = getEventsResponse(resourceName);
            removeRequests(eventType, RequestType.EVENTS_EMPTY_200_GET);
            chainRequests(RequestWrapper.wrap(eventType, modifier.modifyResponse(events), 2000),
                    RequestWrapper.wrap(RequestType.EVENTS_EMPTY_200_GET, 2000));
        });

    }
}
