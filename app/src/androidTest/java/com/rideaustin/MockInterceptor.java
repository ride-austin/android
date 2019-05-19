package com.rideaustin;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.rideaustin.utils.SerializationHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

/**
 * Created by crossover on 03/05/2017.
 */

public class MockInterceptor implements Interceptor {

    private MockResponseFactory mockResponseFactory;

    private Map<RequestType, MockResponse> mockResponses = new HashMap<>();

    private Map<RequestType, RequestStats> stats = new HashMap<>();

    private Map<RequestType, Runnable> onRequestActions = new ConcurrentHashMap<>();

    private IOException networkError;

    public void setMockResponseFactory(MockResponseFactory mockResponseFactory) {
        this.mockResponseFactory = mockResponseFactory;
        synchronized (this) {
            mockResponses.clear();
        }
    }

    public boolean hasMockResponseFactory() {
        return mockResponseFactory != null;
    }

    public synchronized void mockRequests(RequestType... requestTypes) {
        for (RequestType requestType : requestTypes) {
            mockResponses.put(requestType, mockResponseFactory.create(requestType));
        }
    }

    public synchronized void mockRequest(RequestWrapper wrapper) {
        mockResponses.put(wrapper.getRequestType(), createResponse(wrapper));
    }

    public synchronized void mockRequest(RequestType requestType, String body) {
        MockResponse response = mockResponseFactory.create(requestType);
        response.setBody(body);
        mockResponses.put(requestType, response);
    }

    public synchronized void chainRequests(RequestType... requestTypes) {
        RequestWrapper[] wrappers = new RequestWrapper[requestTypes.length];
        for (int i = 0; i < requestTypes.length; i++) {
            wrappers[i] = RequestWrapper.wrap(requestTypes[i]);
        }
        updateRequestsChain(0, wrappers);
    }

    public synchronized void chainRequests(RequestWrapper... wrappers) {
        updateRequestsChain(0, wrappers);
    }

    public synchronized void delayRequest(RequestType requestType, long delay) {
        MockResponse response = mockResponses.containsKey(requestType)
                ? mockResponses.get(requestType)
                : mockResponseFactory.create(requestType);
        mockResponses.put(requestType, response.delayedBy(delay));
    }

    public synchronized void removeRequests(RequestType... requestTypes) {
        for (RequestType requestType : requestTypes) {
            mockResponses.remove(requestType);
        }
    }

    public void setNetworkError(@Nullable IOException networkError) {
        this.networkError = networkError;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (networkError != null) {
            throw networkError;
        }
        RequestType mockRequest = null;
        MockResponse mockResponse = null;

        synchronized (this) {
            if (mockResponses.isEmpty()) {
                // No mocks, use original response
                return chain.proceed(chain.request());
            }

            // find mock response for request type
            List<String> multipleMocksLog = new ArrayList<>();
            for (RequestType requestType : mockResponses.keySet()) {
                MockResponse response = mockResponses.get(requestType);
                if (response.matches(chain.request())) {
                    multipleMocksLog.add(requestType.name() + " ---> " + chain.request().method() + " " + chain.request().url());
                    if (mockResponse == null) {
                        mockRequest = requestType;
                        mockResponse = response;
                    } else {
                        throw new RuntimeException("multiple mocks found for: " + multipleMocksLog.toString());
                    }
                }
            }
            if (mockRequest != null) {
                // request detected, save request for verifying
                Timber.d("### intercepting: %s - %s", chain.request().method(), chain.request().url());
                if (stats.containsKey(mockRequest)) {
                    stats.get(mockRequest).add(chain.request());
                } else {
                    stats.put(mockRequest, new RequestStats(chain.request()));
                }
                Runnable action = onRequestActions.get(mockRequest);
                if (action != null) {
                    // action should be called out of http request call stack
                    new Handler(Looper.getMainLooper()).post(action);
                }
                updateRequestsChain(1, mockResponse.getChain());
            }
        }
        if (mockResponse != null) {
            if (mockResponse.getDelay() > 0L) {
                try {
                    Thread.sleep(mockResponse.getDelay());
                } catch (InterruptedException e) {
                    Timber.e(e, "Sleep interrupted");
                }
            }

            Timber.d("### intercepted response: '%s' ", mockResponse.getBody());
            return new Response.Builder()
                    .protocol(Protocol.HTTP_1_0)
                    .request(chain.request())
                    .code(mockResponse.getHttpCode())
                    .message(getMessage(mockResponse.getHttpCode()))
                    .body(ResponseBody.create(MediaType.parse("application/json"), mockResponse.getBody().getBytes()))
                    .build();
        }
        throw new IllegalStateException("Unable to mock request: " + chain.request());
    }

    private synchronized void updateRequestsChain(int startIndex, RequestWrapper[] chain) {
        if (chain == null || chain.length <= startIndex) {
            // NOTE: last request in chain will stay mocked
            return;
        }
        // remove unwanted mocks
        for (int i = 0; i < startIndex; i++) {
            removeRequests(chain[i].getRequestType());
        }
        // mock request at start index
        RequestWrapper start = chain[startIndex];
        MockResponse response = createResponse(start);

        if (chain.length > startIndex + 1) {
            // save rest of requests as chain
            response.setChain(Arrays.copyOfRange(chain, startIndex, chain.length));
        }
        mockResponses.put(start.getRequestType(), response);
    }

    private MockResponse createResponse(RequestWrapper wrapper) {
        MockResponse response = mockResponseFactory.create(wrapper.getRequestType());
        if (wrapper.getResponseBody() != null) {
            if (wrapper.getResponseBody() instanceof String) {
                response.setBody(wrapper.getResponseBody().toString());
            } else {
                response.setBody(SerializationHelper.serialize(wrapper.getResponseBody()));
            }
        }
        if (wrapper.getDelay() != null) {
            response.delayedBy(wrapper.getDelay());
        }
        return response;
    }

    /**
     * check {@code okhttp3.mockwebserverMockResponse#setResponseCode}
     */
    static String getMessage(int code) {
        String reason = "Mock Response";
        if (code >= 100 && code < 200) {
            reason = "Informational";
        } else if (code >= 200 && code < 300) {
            reason = "OK";
        } else if (code >= 300 && code < 400) {
            reason = "Redirection";
        } else if (code >= 400 && code < 500) {
            reason = "Client Error";
        } else if (code >= 500 && code < 600) {
            reason = "Server Error";
        }
        return "HTTP/1.1 " + code + " " + reason;
    }

    public void resetStats(RequestType requestType) {
        stats.remove(requestType);
    }

    @Nullable
    public RequestStats getStats(RequestType requestType) {
        return stats.get(requestType);
    }

    public void setOnRequestAction(RequestType requestType, Runnable action) {
        onRequestActions.put(requestType, action);
    }

    public void removeOnRequestAction(RequestType requestType) {
        onRequestActions.remove(requestType);
    }

}
