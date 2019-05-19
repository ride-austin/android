package com.rideaustin;

import java.util.Arrays;

import okhttp3.Request;

/**
 * Created by Sergey Petrov on 04/05/2017.
 */

public class SimpleMockResponse implements MockResponse {

    private String requestMethod;
    private String requestPath;
    private int responseCode;
    private long delay;
    private String responseBody;
    private RequestWrapper[] chain;

    public SimpleMockResponse() {

    }

    public SimpleMockResponse(String requestMethod, String requestPath, int responseCode, String responseBody) {
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
        this.responseCode = responseCode;
        this.responseBody = responseBody;
    }

    @Override
    public SimpleMockResponse delayedBy(long delay) {
        this.delay = delay;
        return this;
    }

    @Override
    public long getDelay() {
        return delay;
    }

    @Override
    public int getHttpCode() {
        return responseCode;
    }

    @Override
    public String getBody() {
        return responseBody;
    }

    @Override
    public void setBody(String body) {
        responseBody = body;
    }

    @Override
    public boolean matches(Request request) {
        return request.method().equals(requestMethod)
                && request.url().encodedPath().endsWith(requestPath);
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public RequestWrapper[] getChain() {
        return chain;
    }

    @Override
    public void setChain(RequestWrapper[] chain) {
        this.chain = chain;
    }

    @Override
    public String toString() {
        return "SimpleMockResponse{" +
                "requestMethod='" + requestMethod + '\'' +
                ", requestPath='" + requestPath + '\'' +
                ", responseCode=" + responseCode +
                ", delay=" + delay +
                ", responseBody='" + responseBody + '\'' +
                ", chain=" + Arrays.toString(chain) +
                '}';
    }
}
