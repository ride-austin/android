package com.rideaustin.manager.location;

import com.rideaustin.manager.LocationTimeoutException;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * {@link RALocationManager} should be rewritten to be testable (like 99% of RA code)
 * So its just helper class to simulate {@link RALocationManager}'s logic
 *
 * Created by Sergey Petrov on 06/04/2017.
 */

public class RALocationManagerTest {

    private static long LOCATION_TIMEOUT_MS = 100;

    @Test
    public void shouldReturnCachedLocation() {
        BehaviorSubject<Integer> subject = BehaviorSubject.create(1);
        Observable<Integer> request = getRequestWithSuccess(2);
        BehaviorSubject<Integer> last = BehaviorSubject.create();
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        // test that value would be returned from cache
        getLastLocation(subject, request, true, false).doOnNext(last::onNext).subscribe(subscriber);
        subscriber.assertValue(1);
        subscriber.assertUnsubscribed();
        // test that location request was not even executed
        assertEquals(1, subject.getValue().intValue());
        // test that last location is saved
        assertEquals(1, last.getValue().intValue());
    }

    @Test
    public void shouldReturnLiveLocation() {
        BehaviorSubject<Integer> subject = BehaviorSubject.create(1);
        Observable<Integer> request = getRequestWithSuccess(2);
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        // test that live value would be returned
        getLastLocation(subject, request, false, false).subscribe(subscriber);
        subscriber.assertValue(2);
        subscriber.assertUnsubscribed();
    }

    @Test
    public void shouldReturnError() {
        Throwable error = new Throwable("No permissions");
        BehaviorSubject<Integer> subject = BehaviorSubject.create(1);
        Observable<Integer> request = getRequestWithError(error);
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        // test that live value would return error
        getLastLocation(subject, request, false, false).subscribe(subscriber);
        subscriber.assertNoValues();
        subscriber.assertError(error);
        subscriber.assertUnsubscribed();
    }

    @Test
    public void shouldReturnTimeout() {
        BehaviorSubject<Integer> subject = BehaviorSubject.create(1);
        Observable<Integer> request = getRequestWithTimeout(2 * LOCATION_TIMEOUT_MS, 2);
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        // test that live request with timeout would fail
        getLastLocation(subject, request, false, true).subscribe(subscriber);
        subscriber.assertError(LocationTimeoutException.class);
    }

    /**
     * This code should be identical to {@link RALocationManager#getLocation(boolean, boolean)}
     */
    private Observable<Integer> getLastLocation(Observable<Integer> subject, Observable<Integer> request, boolean fromCache, boolean withTimeout) {
        Observable<Integer> observable;
        if (fromCache) {
            observable = subject.switchIfEmpty(request);
        } else {
            observable = request;
        }
        observable = observable.filter(integer -> integer != null).take(1);
        if (withTimeout) {
            observable = observable.timeout(LOCATION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .onErrorResumeNext(throwable -> Observable.error(new LocationTimeoutException(throwable)));;
        }
        return observable;
    }

    private Observable<Integer> getRequestWithSuccess(int value) {
        return Observable.<Integer>create(subscriber -> subscriber.onNext(value));
    }

    private Observable<Integer> getRequestWithError(Throwable error) {
        return Observable.<Integer>create(subscriber -> subscriber.onError(error));
    }

    private Observable<Integer> getRequestWithTimeout(long timeout, int value) {
        return Observable.<Integer>create(subscriber -> {
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(value);
                        }
                    }
                });
    }

}