package com.rideaustin.schedulers;

import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Created by Viktor Kifer
 * On 25-Dec-2016.
 * <p>
 * Performs replacement of default schedulers with immediate scheduler.
 * This class should be used in tests to make async code synchronous
 */

public class TestRxSchedulers {

    public static void initSchedulersForTests() {
        RxSchedulers.factory = new RxSchedulers.SchedulersFactory() {
            @Override
            public Scheduler createMainScheduler() {
                return Schedulers.immediate();
            }

            @Override
            public Scheduler createComputationScheduler() {
                return Schedulers.immediate();
            }

            @Override
            public Scheduler createSerializerScheduler() {
                return Schedulers.immediate();
            }

            @Override
            public Scheduler createNetworkScheduler() {
                return Schedulers.immediate();
            }

            @Override
            public Scheduler createEventPollingScheduler() {
                return Schedulers.immediate();
            }
        };
    }
}
