package com.rideaustin.utils;

import android.support.annotation.Nullable;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.Subscriptions;

/**
 * Subject, based on {@link BehaviorSubject}. Acts like a trigger.
 * Holds a value until first subscription and discards it right after consuming.
 * If subject has several subscriptions, {@link SingleSubject#onNext(T)} would trigger them all.
 * If subject has no subscriptions, {@link SingleSubject#onNext(T)} would be passed to first subscription.
 * If subject has a subscription, any {@link SingleSubject#onNext(Object)} would be passed to it.
 * 
 * Created by Sergey Petrov on 25/03/2017.
 */

public final class SingleSubject<T> implements Observer<T> {

    private static final Object EMPTY = new Object();

    private final BehaviorSubject<T> subject;
    private final Observable<T> observable;

    public static <T> SingleSubject<T> create() {
        return new SingleSubject<>((T)EMPTY);
    }

    public static <T> SingleSubject<T> create(T defaultValue) {
        return new SingleSubject<>(defaultValue);
    }

    private SingleSubject(@Nullable T defaultValue) {
        if (defaultValue != null) {
            subject = BehaviorSubject.create(defaultValue);
        } else {
            subject = BehaviorSubject.create();
        }
        observable = createObservable();
    }

    @Override
    public void onNext(T v) {
        subject.onNext(v);
    }

    @Override
    public void onCompleted() {
        subject.onCompleted();
    }

    @Override
    public void onError(Throwable e) {
        subject.onError(e);
    }

    public final Observable<T> subscribeOn(Scheduler scheduler) {
        observable.subscribeOn(scheduler);
        return observable;
    }

    public final Observable<T> observeOn(Scheduler scheduler) {
        observable.observeOn(scheduler);
        return observable;
    }

    public final Subscription subscribe(final Action1<? super T> onNext) {
        return observable.subscribe(onNext);
    }

    public final Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError) {
        return observable.subscribe(onNext, onError);
    }

    public final Subscription subscribe(final Observer<? super T> observer) {
        return observable.subscribe(observer);
    }

    public final Observable<T> asObservable() {
        return observable;
    }

    private Observable<T> createObservable() {
        return subject.asObservable()
                .filter(t -> t != EMPTY)
                .doOnNext(t -> subject.onNext((T)EMPTY));
    }
}