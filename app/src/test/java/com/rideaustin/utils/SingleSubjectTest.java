package com.rideaustin.utils;

import org.junit.Test;

import rx.observers.TestSubscriber;

/**
 * Created by Sergey Petrov on 11/04/2017.
 */

public class SingleSubjectTest {

    @Test
    public void shouldEmitOnceToNewSubscription() {
        SingleSubject<Integer> subject = SingleSubject.create();
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        TestSubscriber<Integer> subscriber2 = new TestSubscriber<>();
        // subject has value
        subject.onNext(1);
        subject.subscribe(subscriber);
        // subscriber receives value and still subscribed
        subscriber.assertValue(1);
        subscriber.assertNoTerminalEvent();
        // next subscription gets no values
        subject.subscribe(subscriber2);
        subscriber2.assertNoValues();
        subscriber2.assertNoTerminalEvent();
    }

    @Test
    public void shouldEmitOnceToExistingSubscription() {
        SingleSubject<Integer> subject = SingleSubject.create();
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        TestSubscriber<Integer> subscriber2 = new TestSubscriber<>();
        // subject is empty
        subject.subscribe(subscriber);
        subject.onNext(1);
        // subscriber receives value and still subscribed
        subscriber.assertValue(1);
        subscriber.assertNoTerminalEvent();
        // next subscription gets no values
        subject.subscribe(subscriber2);
        subscriber2.assertNoValues();
        subscriber2.assertNoTerminalEvent();
    }

    @Test
    public void shouldEmitOnceToAllExistingSubscriptions() {
        SingleSubject<Integer> subject = SingleSubject.create();
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        TestSubscriber<Integer> subscriber2 = new TestSubscriber<>();
        TestSubscriber<Integer> subscriber3 = new TestSubscriber<>();
        // subject is empty
        subject.subscribe(subscriber);
        subject.subscribe(subscriber2);
        // all subscriber receives value and still subscribed
        subject.onNext(1);
        subscriber.assertValue(1);
        subscriber.assertNoTerminalEvent();
        subscriber2.assertValue(1);
        subscriber2.assertNoTerminalEvent();
        // next subscription gets no values
        subject.subscribe(subscriber3);
        subscriber3.assertNoValues();
        subscriber3.assertNoTerminalEvent();
    }

}
