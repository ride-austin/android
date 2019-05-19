package com.rideaustin;

import java.util.Arrays;

import javax.annotation.RegEx;

import okhttp3.Request;

/**
 * Created by Sergey Petrov on 05/05/2017.
 */

public class PatternMockResponse implements MockResponse {

    private String requestMethod;
    private String requestPathRegex;
    private int responseCode;
    private long delay;
    private String responseBody;
    private RequestWrapper[] chain;

    public PatternMockResponse() {

    }

    public PatternMockResponse(String requestMethod, @RegEx String requestPathRegex, int responseCode, String responseBody) {
        this.requestMethod = requestMethod;
        this.requestPathRegex = requestPathRegex;
        this.responseCode = responseCode;
        this.responseBody = responseBody;
    }

    @Override
    public PatternMockResponse delayedBy(long delay) {
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
        boolean methodMatched = request.method().equals(requestMethod);
        boolean urlMatched = request.url().toString().matches(requestPathRegex);
        //Timber.d("### matching: " + request.url() + " vs: " + requestPathRegex + " method[" + methodMatched + "] url[" + urlMatched + "]");
        return methodMatched && urlMatched;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestPathRegex() {
        return requestPathRegex;
    }

    public void setRequestPathRegex(String requestPathRegex) {
        this.requestPathRegex = requestPathRegex;
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
        return "PatternMockResponse{" +
                "requestMethod='" + requestMethod + '\'' +
                ", requestPathRegex='" + requestPathRegex + '\'' +
                ", responseCode=" + responseCode +
                ", delay=" + delay +
                ", responseBody='" + responseBody + '\'' +
                ", chain=" + Arrays.toString(chain) +
                '}';
    }
}
