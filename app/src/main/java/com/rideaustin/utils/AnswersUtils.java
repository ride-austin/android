package com.rideaustin.utils;

import android.support.annotation.Nullable;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.rideaustin.BuildConfig;
import com.rideaustin.api.model.Rider;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.base.retrofit.RetrofitException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import timber.log.Timber;

/**
 * Created by crossover on 04/04/2017.
 */

public class AnswersUtils {

    private static final String DEV_SUFFIX = getDevSuffix();

    private static final String RIDE_DECLINED = "Ride Declined" + DEV_SUFFIX;
    private static final String RIDE_ACCEPTED = "Ride Accepted" + DEV_SUFFIX;
    private static final String RIDE_CANCELLED = "Ride Cancelled" + DEV_SUFFIX;
    private static final String DNS_LOOKUP_FAILED = "DNS Lookup Failed" + DEV_SUFFIX;
    private static final String CONNECTION_ERROR = "Connection Error" + DEV_SUFFIX;

    private static final String RIDER_ID = "Rider ID";
    private static final String RIDER_EMAIL = "Rider Email";
    private static final String DRIVER_ID = "Driver ID";
    private static final String DRIVER_EMAIL = "Driver Email";
    private static final String MESSAGE = "Message";

    public static void logDeclineRide(@Nullable Driver driver) {
        CustomEvent event = new CustomEvent(RIDE_DECLINED);
        fillDriverDetails(event, driver);
        Answers.getInstance().logCustom(event);
    }

    public static void logAcceptRide(@Nullable Driver driver) {
        CustomEvent event = new CustomEvent(RIDE_ACCEPTED);
        fillDriverDetails(event, driver);
        Answers.getInstance().logCustom(event);
    }

    public static void logCancelledRide(@Nullable Driver driver) {
        CustomEvent event = new CustomEvent(RIDE_CANCELLED);
        fillDriverDetails(event, driver);
        Answers.getInstance().logCustom(event);
    }

    public static void logConnectionError(@Nullable Driver driver, RetrofitException exception) {
        CustomEvent event = createConnectionError(exception);
        fillDriverDetails(event, driver);
        Answers.getInstance().logCustom(event);
    }

    public static void logConnectionError(@Nullable Rider rider, RetrofitException exception) {
        CustomEvent event = createConnectionError(exception);
        fillRiderDetails(event, rider);
        Answers.getInstance().logCustom(event);
    }

    private static CustomEvent createConnectionError(RetrofitException exception) {
        String eventName = exception.getCause() instanceof UnknownHostException
                ? DNS_LOOKUP_FAILED
                : CONNECTION_ERROR;
        CustomEvent event = new CustomEvent(eventName);
        event.putCustomAttribute(MESSAGE, exception.getCause().getMessage());
        return event;
    }

    private static void fillRiderDetails(CustomEvent event, Rider rider) {
        if (rider != null) {
            event.putCustomAttribute(RIDER_ID, Long.toString(rider.getId()));
            event.putCustomAttribute(RIDER_EMAIL, rider.getEmail());
        }
    }

    private static void fillDriverDetails(CustomEvent event, Driver driver) {
        if (driver != null) {
            event.putCustomAttribute(DRIVER_ID, Long.toString(driver.getId()));
            event.putCustomAttribute(DRIVER_EMAIL, driver.getEmail());
        }
    }

    private static String getDevSuffix() {
        if (BuildConfig.FLAVOR.toLowerCase().contains(Constants.ENV_PROD)) {
            return "";
        } else {
            return " beta";
        }
    }
}
