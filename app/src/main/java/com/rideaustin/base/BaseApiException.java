package com.rideaustin.base;

/**
 * Created by kshumelchyk on 7/17/16.
 */
public class BaseApiException extends Exception {

    private int code;
    private String body;
    private boolean causedByNetwork;

    public BaseApiException(int code, String body, Throwable cause) {
        super(body, cause);
        this.code = code;
        this.body = body;
    }

    public BaseApiException(int code, String body, Throwable cause, boolean causedByNetwork) {
        super(body, cause);
        this.code = code;
        this.body = body;
        this.causedByNetwork = causedByNetwork;
    }


    public int getCode() {
        return code;
    }

    public String getBody() {
        return body;
    }

    public boolean causedByNetwork() {
        return causedByNetwork;
    }
}
