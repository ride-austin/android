package com.rideaustin.utils;

import android.content.Context;

import com.instacart.library.truetime.TrueTimeRx;
import com.rideaustin.schedulers.RxSchedulers;

import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.Subscription;
import timber.log.Timber;

/**
 * Created by crossover on 30/01/2017.
 */

public class TimeUtils {

    public static void init(Context context) {
        if (!TrueTimeRx.isInitialized()) {
            TrueTimeRx.build()
                    .withSharedPreferences(context)
                    .initializeRx("time.apple.com")
                    .subscribeOn(RxSchedulers.network())
                    .subscribe(date -> {
                        Timber.i("TrueTimeRx fetched");
                    }, throwable -> {
                        Timber.w(throwable, "TrueTimeRx cannot be initialised");
                    });
        }
    }

    public static long currentTimeMillis() {
        if (TrueTimeRx.isInitialized()) {
            return TrueTimeRx.now().getTime();
        } else {
            return System.currentTimeMillis();
        }
    }

}