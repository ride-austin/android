package com.rideaustin.utils;


import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by hatak on 26.06.2017.
 */

public class RetryWithDelay implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private final long retryDelayMillis;
    private final int retryAttempts;
    private final Func1<Throwable, Boolean> errorFilter;
    private int retryCount = 0;

    public RetryWithDelay(final long retryDelayMillis) {
        this(retryDelayMillis, 0, null);
    }

    public RetryWithDelay(final long retryDelayMillis, final int retryAttempts) {
        this(retryDelayMillis, retryAttempts, null);
    }

    public RetryWithDelay(final long retryDelayMillis, final int retryAttempts, Func1<Throwable, Boolean> errorFilter) {
        this.retryDelayMillis = retryDelayMillis;
        this.retryAttempts = retryAttempts > 0 ? retryAttempts : Integer.MAX_VALUE;
        this.errorFilter = errorFilter != null ? errorFilter : throwable -> true;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> errors) {
        return errors.flatMap(throwable -> {
            if (errorFilter.call(throwable) && ++retryCount <= retryAttempts) {
                return Observable.timer(retryDelayMillis, TimeUnit.MILLISECONDS);
            }
            return Observable.error(throwable);
        });
    }
}
