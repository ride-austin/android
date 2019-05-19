package com.rideaustin.utils.file.logging;

import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

/**
 * Created by vokol on 27.10.2016.
 */

public class Logger implements HttpLoggingInterceptor.Logger {

    String tag;

    public Logger(String tag) {
        this.tag = tag;
    }

    @Override
    public void log(String message) {
        Timber.tag(tag).d(message);
    }
}
