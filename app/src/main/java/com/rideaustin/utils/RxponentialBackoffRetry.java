package com.rideaustin.utils;

import android.support.v4.util.Pair;

import com.rideaustin.schedulers.RxSchedulers;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by supreethks on 08/11/16.
 */

public class RxponentialBackoffRetry {

    private static final int MAX_RETRIES = 3;
    final int maxRetries;
    private Func1<Observable<? extends Throwable>, Observable<?>> notificationHandler = new Func1<Observable<? extends Throwable>, Observable<?>>() {
        @Override
        public Observable<?> call(final Observable<? extends Throwable> observable) {
            return observable.zipWith(Observable.range(1, maxRetries), new Func2<Throwable, Integer, Pair<Throwable, Integer>>() {
                @Override
                public Pair<Throwable, Integer> call(Throwable throwable, Integer count) {
                    return new Pair<>(throwable, count);
                }
            }).flatMap((Func1<Pair<Throwable, Integer>, Observable<?>>) pair -> {
                Integer retryCount = pair.second;
                if (retryCount == maxRetries) {
                    return Observable.error(pair.first);
                }
                return Observable.timer((long) Math.pow(4, retryCount), TimeUnit.SECONDS, RxSchedulers.computation());
            });
        }
    };

    public RxponentialBackoffRetry() {
        maxRetries = MAX_RETRIES;
    }

    public RxponentialBackoffRetry(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Func1<Observable<? extends Throwable>, Observable<?>> getNotificationHandler() {
        return notificationHandler;
    }
}
