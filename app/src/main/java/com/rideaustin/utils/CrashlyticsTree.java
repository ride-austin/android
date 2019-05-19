package com.rideaustin.utils;

import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.rideaustin.App;
import com.rideaustin.api.model.User;

import java.util.Locale;

import timber.log.Timber;

/**
 * Created by v.garshyn on 18.08.16.
 */
public class CrashlyticsTree extends Timber.Tree {

    private static final String CRASHLYTICS_TAG_WARN = "WARNING";
    private static final String CRASHLYTICS_TAG_INFO = "INFO";
    private static final String CRASHLYTICS_TAG_ERROR = "ERROR";

    @Override
    public void e(String message, Object... args) {
        log(Log.ERROR, CRASHLYTICS_TAG_ERROR, getFormattedMessage(message, args), null);
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        log(Log.ERROR, CRASHLYTICS_TAG_ERROR, getFormattedMessage(message, args), t);
    }

    @Override
    public void w(String message, Object... args) {
        log(Log.WARN, CRASHLYTICS_TAG_WARN, getFormattedMessage(message, args), null);
    }

    @Override
    public void w(Throwable t, String message, Object... args) {
        log(Log.WARN, CRASHLYTICS_TAG_WARN, getFormattedMessage(message, args), t);
    }

    @Override
    public void i(String message, Object... args) {
        log(Log.INFO, CRASHLYTICS_TAG_INFO, getFormattedMessage(message, args), null);
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        log(Log.INFO, CRASHLYTICS_TAG_INFO, getFormattedMessage(message, args), t);
    }

    private String getFormattedMessage(String message, Object[] args) {
        try {
            // sometimes it causing exception
            return String.format(Locale.US, message, args);
        } catch (Exception e) {
            // fallback to raw message
            return message;
        }
    }

    protected void log(int priority, @Nullable String tag, @Nullable String message, @Nullable Throwable t) {
        try {
            if (App.getDataManager() != null && App.getDataManager().isLoggedIn()) {
                User user = App.getDataManager().getCurrentUser();
                // to make it easier to debug things, we add user specific information to the log
                Crashlytics.setUserIdentifier("ID: " + user.getId() + " TOKEN: " + App.getDataManager().getXToken());
                Crashlytics.setUserEmail(user.getEmail());
                Crashlytics.setUserName(user.getFullName());
            } else {
                Crashlytics.setUserIdentifier(null);
                Crashlytics.setUserEmail(null);
                Crashlytics.setUserName(null);
            }
            // FROM CRASHLYTICS DOCUMENTATION:
            // To make sure that sending crash reports has the smallest impact
            // on your user’s devices, Crashlytics logs have a maximum size of 64 KB.
            // When a log exceeds 64 KB, the earliest logged values will
            // be dropped in order to maintain this threshold.
            Crashlytics.log(priority, tag, message);
            // we log exceptions as well
            if (t != null) {
                Crashlytics.logException(t);
            }
        } catch (Exception e) {
            Log.e("Crashlytics", "Unable to log", e);
        }

    }
}
