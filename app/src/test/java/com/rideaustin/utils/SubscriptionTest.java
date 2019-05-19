package com.rideaustin.utils;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import rx.Observable;
import rx.Scheduler;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Sergey Petrov on 18/05/2017.
 */

public class SubscriptionTest {

    private static final String MAIN_THREAD = "main-thread";
    private static final String BACKGROUND_THREAD = "background-thread";

    @Test
    public void shouldSubscribeAndObserveOnCorrectThreads() throws InterruptedException {
        Scheduler main = createExecutor(MAIN_THREAD);
        Scheduler background = createExecutor(BACKGROUND_THREAD);
        TestSubscriber<Integer> subscriber = TestSubscriber.create();

        CountDownLatch latch = new CountDownLatch(1);
        testObservable(BACKGROUND_THREAD)
                .subscribeOn(background)
                .observeOn(main)
                .doAfterTerminate(latch::countDown)
                .subscribe(subscriber);
        latch.await();

        checkThread(MAIN_THREAD, subscriber.getLastSeenThread());
        subscriber.assertNoErrors();
    }

    @Test
    public void shouldExecuteFlatMapOnSubscriptionThread() throws InterruptedException {
        Scheduler main = createExecutor(MAIN_THREAD);
        Scheduler background = createExecutor(BACKGROUND_THREAD);
        TestSubscriber<Integer> subscriber = TestSubscriber.create();

        CountDownLatch latch = new CountDownLatch(1);
        testObservable(BACKGROUND_THREAD)
                .flatMap(integer -> testObservable(BACKGROUND_THREAD))
                .subscribeOn(background)
                .observeOn(main)
                .doAfterTerminate(latch::countDown)
                .subscribe(subscriber);
        latch.await();

        checkThread(MAIN_THREAD, subscriber.getLastSeenThread());
        subscriber.assertNoErrors();
    }

    @Test
    public void shouldSwitchExecutionThreadAccordingToObserveOn() throws InterruptedException {
        Scheduler main = createExecutor(MAIN_THREAD);
        Scheduler background = createExecutor(BACKGROUND_THREAD);
        TestSubscriber<Integer> subscriber = TestSubscriber.create();

        CountDownLatch latch = new CountDownLatch(1);
        testObservable(MAIN_THREAD)
                .observeOn(background)
                .flatMap(integer -> testObservable(BACKGROUND_THREAD))
                .observeOn(main)
                .flatMap(integer -> testObservable(MAIN_THREAD))
                .observeOn(background)
                .subscribeOn(main)
                .doAfterTerminate(latch::countDown)
                .subscribe(subscriber);

        latch.await();

        checkThread(BACKGROUND_THREAD, subscriber.getLastSeenThread());
        subscriber.assertNoErrors();
    }

    private Observable<Integer> testObservable(String expectedThreadName) {
        return Observable.defer(() -> {
            checkCurrentThread(expectedThreadName);
            return Observable.just(1);
        });
    }

    private void checkCurrentThread(String expectedThreadName) {
        assertEquals(expectedThreadName, Thread.currentThread().getName());
    }

    private void checkThread(String expectedThreadName, Thread thread) {
        assertEquals(expectedThreadName, thread.getName());
    }


    private Scheduler createExecutor(String name) {
        return Schedulers.from(Executors.newSingleThreadExecutor(new TestThreadFactory(name)));
    }

    private static class TestThreadFactory implements ThreadFactory {

        private final String name;

        TestThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(name) {
                @Override
                public void run() {
                    setPriority(Thread.MAX_PRIORITY);
                    r.run();
                }
            };
        }
    }


}
