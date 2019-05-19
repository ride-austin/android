package com.rideaustin.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.lang.ref.WeakReference;

/**
 * @author sdelaysam.
 */

public class AppForegroundMonitor implements Application.ActivityLifecycleCallbacks {

    final private WeakReference<Callback> callbackRef;
    private int numActive = 0;
    private boolean isRunning = false;

    public AppForegroundMonitor(Callback callback) {
        callbackRef = new WeakReference<>(callback);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        numActive++;
        if (!isRunning) {
            isRunning = true;
            if (callbackRef.get() != null) {
                callbackRef.get().onAppForeground();
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        numActive--;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (numActive == 0) {
            isRunning = false;
            if (callbackRef.get() != null) {
                callbackRef.get().onAppBackground();
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public interface Callback {
        void onAppForeground();
        void onAppBackground();
    }


}
