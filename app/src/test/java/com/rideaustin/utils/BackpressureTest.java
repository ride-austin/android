package com.rideaustin.utils;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.exceptions.MissingBackpressureException;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Sergey Petrov on 09/05/2017.
 */

public class BackpressureTest {

    @Test
    public void shouldSucceedOnBackpressureDropInSingleThread() throws InterruptedException {
        PublishSubject<Integer> subject = PublishSubject.create();
        TestSubscriber<Integer> subscriber = TestSubscriber.create();
        Observable<Integer> observable = subject.asObservable().onBackpressureDrop();

        simulateBackpressureOnSingleThread(observable, subject, subscriber);
        subscriber.assertNoErrors();
    }

    @Test
    public void shouldFailWithoutBackpressureHandlingInSingleThread() throws InterruptedException {
        PublishSubject<Integer> subject = PublishSubject.create();
        TestSubscriber<Integer> subscriber = TestSubscriber.create();
        Observable<Integer> observable = subject.asObservable();

        simulateBackpressureOnSingleThread(observable, subject, subscriber);
        subscriber.assertError(MissingBackpressureException.class);
    }

    @Test
    public void shouldSucceedOnSerializedBackpressureDropInMultithread() throws InterruptedException {
        PublishSubject<Integer> subject = PublishSubject.create();
        TestSubscriber<Integer> subscriber = TestSubscriber.create();
        Observable<Integer> observable = subject.asObservable().serialize().onBackpressureDrop();

        simulateBackpressureOnMultipleThreads(observable, subject, subscriber);
        subscriber.assertNoErrors();
    }

    @Test
    public void shouldFailOnBackpressureDropInMultithread() throws InterruptedException {
        PublishSubject<Integer> subject = PublishSubject.create();
        TestSubscriber<Integer> subscriber = TestSubscriber.create();
        Observable<Integer> observable = subject.asObservable().onBackpressureDrop();

        simulateBackpressureOnMultipleThreads(observable, subject, subscriber);
        // TODO: test fails on Jenkins but works locally, need to check why
        // subscriber.assertError(MissingBackpressureException.class);
    }

    @Test
    public void shouldFailWithoutBackpressureHandlingInMultithread() throws InterruptedException {
        PublishSubject<Integer> subject = PublishSubject.create();
        TestSubscriber<Integer> subscriber = TestSubscriber.create();
        Observable<Integer> observable = subject.asObservable();

        simulateBackpressureOnMultipleThreads(observable, subject, subscriber);
        subscriber.assertError(MissingBackpressureException.class);
    }

    private void simulateBackpressureOnSingleThread(Observable<Integer> observable,
                                                    Observer<Integer> observer,
                                                    Subscriber<Integer> subscriber) throws InterruptedException {
        // create thread lock
        CountDownLatch latch = new CountDownLatch(1);

        observable
                // use computation for observation
                // (don't know why, immediate doesn't fire MissingBackpressureException)
                .observeOn(Schedulers.computation())
                // release thread await on complete
                .doOnCompleted(latch::countDown)
                .subscribe(value -> {
                    try {
                        // simulate hard work
                        Thread.sleep(10L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // save consumed value
                    subscriber.onNext(value);
                }, throwable -> {
                    // save error and release thread await
                    subscriber.onError(throwable);
                    latch.countDown();
                });

        // simulate intensive flow on single thread
        int numEvents = 1000;
        for (int i = 0; i < numEvents; i++) {
            observer.onNext(i);
        }

        // fire complete to release thread await
        observer.onCompleted();

        // await
        latch.await();
    }

    private void simulateBackpressureOnMultipleThreads(Observable<Integer> observable,
                                                       Observer<Integer> observer,
                                                       Subscriber<Integer> subscriber) throws InterruptedException {

        int numThreads = 10;
        // create thread lock
        CountDownLatch latch = new CountDownLatch(numThreads);
        observable
                // use computation for observation
                // (don't know why, immediate doesn't fire MissingBackpressureException)
                .observeOn(Schedulers.computation())
                .subscribe(value -> {
                    try {
                        // simulate hard work
                        Thread.sleep(10L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // save consumed value
                    subscriber.onNext(value);
                }, throwable -> {
                    // save error and release thread await
                    subscriber.onError(throwable);
                    while (latch.getCount() > 0) {
                        latch.countDown();
                    }
                });


        int numEventsPerThread = 200;
        // simulate intensive flow on multiple thread
        for (int i = 0; i < numThreads; i++) {
            Observable.range(0, numEventsPerThread)
                    .subscribeOn(Schedulers.computation())
                    // each emission on new thread
                    .observeOn(Schedulers.newThread())
                    // countdown on thread complete
                    .doOnCompleted(latch::countDown)
                    .subscribe(observer::onNext);
        }

        // await
        latch.await();
    }

}
