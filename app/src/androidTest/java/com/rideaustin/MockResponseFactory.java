package com.rideaustin;

import android.text.TextUtils;

import com.rideaustin.utils.ResourceUtils;

import javax.annotation.RegEx;

/**
 * Created by Sergey Petrov on 04/05/2017.
 */

public abstract class MockResponseFactory {

    public abstract MockResponse create(RequestType requestType);

    protected MockResponse get200(String requestPath, String resourceName) {
        return new SimpleMockResponse("GET", requestPath, 200, getResponseBody(resourceName));
    }

    protected MockResponse get200Regex(@RegEx String requestPathRegex, String resourceName) {
        return new PatternMockResponse("GET", requestPathRegex, 200, getResponseBody(resourceName));
    }

    protected MockResponse get200Empty(String requestPath) {
        return new SimpleMockResponse("GET", requestPath, 200, "");
    }

    protected MockResponse get200EmptyRegex(@RegEx String requestPathRegex) {
        return new PatternMockResponse("GET", requestPathRegex, 200, "");
    }

    protected MockResponse post200(String requestPath, String resourceName) {
        return new SimpleMockResponse("POST", requestPath, 200, getResponseBody(resourceName));
    }

    protected MockResponse post200Regex(@RegEx String requestPathRegex, String resourceName) {
        return new PatternMockResponse("POST", requestPathRegex, 200, getResponseBody(resourceName));
    }

    protected MockResponse post200Empty(String requestPath) {
        return new SimpleMockResponse("POST", requestPath, 200, "");
    }

    protected MockResponse post200EmptyRegex(@RegEx String requestPathRegex) {
        return new PatternMockResponse("POST", requestPathRegex, 200, "");
    }

    protected MockResponse put200(String requestPath, String resourceName) {
        return new SimpleMockResponse("PUT", requestPath, 200, getResponseBody(resourceName));
    }

    protected MockResponse put200Regex(@RegEx String requestPathRegex, String resourceName) {
        return new PatternMockResponse("PUT", requestPathRegex, 200, getResponseBody(resourceName));
    }

    protected MockResponse put200EmptyRegex(@RegEx String requestPathRegex) {
        return new PatternMockResponse("PUT", requestPathRegex, 200, "");
    }

    protected MockResponse getWithStringResponse(String requestPath, int httpCode, String response) {
        return new SimpleMockResponse("GET", requestPath, httpCode, response);
    }

    protected MockResponse getWithStringResponseRegex(String requestPath, int httpCode, String response) {
        return new PatternMockResponse("GET", requestPath, httpCode, response);
    }

    protected MockResponse postWithStringResponse(String requestPath, int httpCode, String response) {
        return new SimpleMockResponse("POST", requestPath, httpCode, response);
    }

    protected MockResponse postWithStringResponseRegex(String requestPath, int httpCode, String response) {
        return new PatternMockResponse("POST", requestPath, httpCode, response);
    }

    protected MockResponse postEmpty(String requestPath, int httpCode) {
        return new SimpleMockResponse("POST", requestPath, httpCode, "");
    }

    protected static String getResponseBody(String resourceName) {
        return ResourceUtils.getContent(MockResponseFactory.class, resourceName + ".json");
    }

    public static class Builder {
        private String url;
        private String regexpUrl;
        private MockResponseFactory.Method method;
        private String fileResource = "";
        private String stringResource = "";
        private long delay = 0l;
        private int responseCode;

        public Builder(int responseCode, MockResponseFactory.Method method) {
            this.responseCode = responseCode;
            this.method = method;
        }

        public void withDelay(long delay) {
            this.delay = delay;
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withRegexpUrl(String regexpUrl) {
            this.regexpUrl = regexpUrl;
            return this;
        }

        public Builder withMethod(MockResponseFactory.Method method) {
            this.method = method;
            return this;
        }

        public Builder withFileResponse(String fileResource) {
            this.fileResource = fileResource;
            return this;
        }

        public Builder withStringResponse(String stringResource) {
            this.stringResource = stringResource;
            return this;
        }

        public Builder withEmptyStringResponse() {
            this.stringResource = "";
            return this;
        }


        public MockResponse build() {
            if (TextUtils.isEmpty(url)) {
                PatternMockResponse mockResponse = new PatternMockResponse();
                mockResponse.setRequestMethod(method.getValue());
                mockResponse.setResponseCode(responseCode);
                mockResponse.setRequestPathRegex(regexpUrl);
                if (TextUtils.isEmpty(fileResource)) {
                    mockResponse.setResponseBody(stringResource);
                } else {
                    mockResponse.setResponseBody(getResponseBody(fileResource));
                }
                return mockResponse;
            } else {
                SimpleMockResponse mockResponse = new SimpleMockResponse();
                mockResponse.setRequestMethod(method.getValue());
                mockResponse.setResponseCode(responseCode);
                mockResponse.setRequestPath(url);
                if (TextUtils.isEmpty(fileResource)) {
                    mockResponse.setResponseBody(stringResource);
                } else {
                    mockResponse.setResponseBody(getResponseBody(fileResource));
                }
                return mockResponse;
            }
        }
    }

    public enum Method {
        GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE");

        private String value;

        Method(String method) {
            this.value = method;
        }

        public String getValue() {
            return value;
        }
    }
}
