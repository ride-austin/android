package com.rideaustin.schedulers;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.idling.CountingIdlingResource;

import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;

/**
 * https://github.com/ReactiveX/RxAndroid/issues/149#issuecomment-234381110
 */
public class IdlingScheduler extends Scheduler {

    private CountingIdlingResource countingIdlingResource;

    private Scheduler scheduler;

    public IdlingScheduler(Scheduler scheduler, String name) {
        this(scheduler, name, false);
    }

    public IdlingScheduler(Scheduler scheduler, String name, boolean debugCounting) {
        this.scheduler = scheduler;
        countingIdlingResource = new CountingIdlingResource(name, debugCounting);
        Espresso.registerIdlingResources(countingIdlingResource);
    }

    @Override
    public Worker createWorker() {
        return new IdlingWorker(scheduler.createWorker());
    }

    private class IdlingWorker extends Worker {

        private Worker worker;
        private boolean recursive;

        public IdlingWorker(Worker worker) {
            this.worker = worker;
        }

        @Override
        public Subscription schedule(Action0 action) {
            return recursive ?
                    worker.schedule(action) :
                    worker.schedule(decorateAction(action));
        }

        @Override
        public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
            return recursive ?
                    worker.schedule(action, delayTime, unit) :
                    worker.schedule(decorateAction(action), delayTime, unit);
        }

        @Override
        public Subscription schedulePeriodically(Action0 action, long initialDelay, long period, TimeUnit unit) {
            recursive = true;
            return worker.schedulePeriodically(decorateAction(action), initialDelay, period, unit);
        }

        @Override
        public void unsubscribe() {
            worker.unsubscribe();
        }

        @Override
        public boolean isUnsubscribed() {
            return worker.isUnsubscribed();
        }

        private Action0 decorateAction(Action0 action) {
            return () -> {
                countingIdlingResource.increment();
                try {
                    action.call();
                } finally {
                    countingIdlingResource.decrement();
                }
            };
        }
    }
}