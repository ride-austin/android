package com.rideaustin.schedulers;

import com.rideaustin.utils.DeviceInfoUtil;

import rx.Scheduler;
import rx.plugins.RxJavaHooks;
import rx.schedulers.Schedulers;

/**
 * Created by Sergey Petrov on 15/05/2017.
 */

public class UITestRxSchedulers {

    public static void initSchedulersForTests() {

        if (DeviceInfoUtil.isEmulator()) {
            // computation on emulator sucks
            // use new thread for each task
            RxJavaHooks.setOnComputationScheduler(scheduler -> Schedulers.newThread());
        }
        if (RxSchedulers.factory == null) {
            RxSchedulers.factory = new RxSchedulers.DefaultSchedulersFactory() {
                @Override
                public Scheduler createMainScheduler() {
                    return new IdlingScheduler(super.createMainScheduler(), "Main");
                }

                @Override
                public Scheduler createComputationScheduler() {
                    return new IdlingScheduler(Schedulers.io(), "Computation");
                }

                @Override
                public Scheduler createSerializerScheduler() {
                    return new IdlingScheduler(super.createSerializerScheduler(), "Serializer");
                }

                @Override
                public Scheduler createNetworkScheduler() {
                    // Use io() scheduler for both network() and computation()
                    // to prevent thread locks due to pool bounds
                    // NOTE: locks only experienced during UI tests so far
                    return new IdlingScheduler(Schedulers.io(), "Network");
                }
            };
        }
    }
}
