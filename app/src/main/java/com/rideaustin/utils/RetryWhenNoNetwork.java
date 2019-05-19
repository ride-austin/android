package com.rideaustin.utils;

import com.rideaustin.schedulers.RxSchedulers;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Sergey Petrov on 29/06/2017.
 */

public class RetryWhenNoNetwork implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private final int retryDelayMillis;

    public RetryWhenNoNetwork(final int retryDelayMillis) {
        this.retryDelayMillis = retryDelayMillis;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> errors) {
        return errors.flatMap((Func1<Throwable, Observable<?>>) throwable -> {
            if (NetworkHelper.causedByNetwork(throwable)) {
                return Observable.timer(retryDelayMillis, TimeUnit.MILLISECONDS, RxSchedulers.computation());
            }
            return Observable.error(throwable);
        });
    }
}
