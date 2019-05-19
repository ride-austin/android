package com.rideaustin.ui.map;

import com.rideaustin.api.model.RideStatus;
import com.rideaustin.events.RideStatusEvent;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Sergey Petrov on 09/03/2017.
 */

public class MapViewModelTest {

    /**
     * Test backpressure in {@link MapViewModel}
     * Should be rewritten to use {@link MapViewModel}'s code
     */
    @Test
    public void shouldUseLatestOnBackPressure() throws InterruptedException {

        PublishSubject<RideStatusEvent> rideEventsSubject = PublishSubject.create();
        TestSubscriber<RideStatusEvent> testSubscriber = TestSubscriber.create();

        // create thread lock
        CountDownLatch latch = new CountDownLatch(1);

        // subscribe to potentially intensive flow
        rideEventsSubject
                // get last from flow on backpressure
                // remove to break the test
                .onBackpressureLatest()
                // use computation for observation
                // (don't know why, immediate doesn't fire MissingBackpressureException)
                .observeOn(Schedulers.computation())
                // release thread await on complete
                .doOnCompleted(latch::countDown)
                .subscribe(rideStatusEvent -> {
                    try {
                        // simulate hard work
                        Thread.sleep(10L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // save consumes event
                    testSubscriber.onNext(rideStatusEvent);
                }, throwable -> {
                    // save error and release thread await
                    testSubscriber.onError(throwable);
                    latch.countDown();
                });


        RideStatus[] values = RideStatus.values();


        // simulate intensive flow
        int numEvents = 1000;
        for (int i = 0; i < numEvents; i++) {
            int random = ThreadLocalRandom.current().nextInt(0, values.length);
            rideEventsSubject.onNext(new RideStatusEvent(values[random], null, null));
        }

        RideStatusEvent last = new RideStatusEvent(RideStatus.ACTIVE, null, null);
        rideEventsSubject.onNext(last);

        // fire complete to release thread await
        rideEventsSubject.onCompleted();

        // await
        latch.await();

        // no backpressure error (or any other)
        testSubscriber.assertNoErrors();

        int actualCount = testSubscriber.getOnNextEvents().size();
        // actual events is less than expected (its normal for onBackpressureLatest())
        assertTrue(actualCount < numEvents + 1);
        // latest event should be the latest
        assertEquals(last, testSubscriber.getOnNextEvents().get(actualCount - 1));
    }
}
