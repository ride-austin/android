package com.rideaustin.ui.base;

import com.rideaustin.base.BaseApiException;

/**
 * Created by kshumelchyk on 7/17/16.
 */
public class NoTokenException extends Throwable {
    public NoTokenException() {
        super("No Token Available");
    }
}
