package com.rideaustin;

import android.app.Activity;
import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;
import android.support.test.runner.lifecycle.ActivityLifecycleCallback;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;

import com.rideaustin.schedulers.UITestRxSchedulers;
import com.rideaustin.utils.DeviceTestUtils;

import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;

/**
 * Created by Sergey Petrov on 10/05/2017.
 */

public class UITestRunner extends AndroidJUnitRunner {

    /**
     * This flag signals that app state should be cleared on next {@link Activity#onDestroy()}
     * Please see {@link RaActivityRule#afterActivityFinished()}, which triggers this flag.
     * Also consider, that {@link RaActivityRule#afterActivityFinished()} can't be used to clear app state directly,
     * because it just notifies that {@link Activity#finish()} is called, but all lifecycle callbacks are executed later.
     */
    static boolean clearOnDestroy = false;

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        UITestRxSchedulers.initSchedulersForTests();
        ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(lifecycleCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(lifecycleCallback);
    }

    private ActivityLifecycleCallback lifecycleCallback = (activity, stage) -> {
        switch (stage) {
            case PRE_ON_CREATE:
                // force tested activity to turn/keep screen on
                activity.getWindow().addFlags(FLAG_DISMISS_KEYGUARD | FLAG_TURN_SCREEN_ON | FLAG_KEEP_SCREEN_ON);
                break;
            case DESTROYED:
                if (clearOnDestroy) {
                    DeviceTestUtils.clearAppState();
                    clearOnDestroy = false;
                }
                break;
        }
    };

}
