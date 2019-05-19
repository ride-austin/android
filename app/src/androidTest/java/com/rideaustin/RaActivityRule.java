package com.rideaustin;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;

import com.rideaustin.utils.DeviceTestUtils;

/**
 * Fixes <a href="https://issuetracker.google.com/issues/37082857">bug in Espresso</a>
 * Some of the UI test logic can't rely on @{@link org.junit.After}
 * as {@link Activity} is destroyed later and may execute code after test teared down.
 *
 * Created by Sergey Petrov on 30/05/2017.
 */

public class RaActivityRule<T extends Activity> extends ActivityTestRule<T> {

    @Nullable
    public Runnable beforeLaunched;

    public RaActivityRule(Class<T> activityClass) {
        super(activityClass);
    }

    public RaActivityRule(Class<T> activityClass, boolean initialTouchMode) {
        super(activityClass, initialTouchMode);
    }

    public RaActivityRule(Class<T> activityClass, boolean initialTouchMode, boolean launchActivity) {
        super(activityClass, initialTouchMode, launchActivity);
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();
        DeviceTestUtils.clearAppState();
        if (beforeLaunched != null) {
            beforeLaunched.run();
            beforeLaunched = null;
        }
    }

    @Override
    protected void afterActivityFinished() {
        super.afterActivityFinished();
        DeviceTestUtils.clearAppState();
        UITestRunner.clearOnDestroy = true;
    }
}
