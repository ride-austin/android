package com.rideaustin.utils;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

//taken from: https://github.com/mcharmas/Android-ReactiveLocation/blob/master/android-reactive-location/src/main/java/pl/charmas/android/reactivelocation/observables/PendingResultObservable.java

public class PendingResultObservable<T extends Result> implements Observable.OnSubscribe<T> {
    private final PendingResult<T> result;
    private boolean complete = false;

    public PendingResultObservable(PendingResult<T> result) {
        this.result = result;
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        result.setResultCallback(t -> {
            subscriber.onNext(t);
            complete = true;
            subscriber.onCompleted();
        });
        subscriber.add(Subscriptions.create(() -> {
            if (!complete) {
                result.cancel();
            }
        }));
    }
}