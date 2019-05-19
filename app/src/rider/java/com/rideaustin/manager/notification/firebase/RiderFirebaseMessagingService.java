package com.rideaustin.manager.notification.firebase;

import android.support.annotation.VisibleForTesting;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rideaustin.App;
import com.rideaustin.utils.Constants;

import java.util.Map;

import timber.log.Timber;

/**
 * Created by hatak on 30.11.16.
 */

public class RiderFirebaseMessagingService extends FirebaseMessagingService {

    private static RiderFirebaseMessagingService instance;
    private static final String TEXT_MESSAGE_KEY = "default";
    private static final String RIDE_UPGRADE_KEY = "RIDE_UPGRADE";
    private static final String RIDE_DRIVER_ASSIGNED_KEY = "DRIVER_ASSIGNED";
    private static final String RIDE_DRIVER_REACHED_KEY = "DRIVER_REACHED";
    private static final String RIDE_RATE_REMINDER_KEY = "RATE_REMINDER";
    private static final String RIDE_NO_AVAILABLE_DRIVER_KEY = "NO_AVAILABLE_DRIVER";
    private static final String RIDE_DRIVER_CANCELLED_KEY = "DRIVER_CANCELLED";
    private static final String RIDE_ADMIN_CANCELLED_KEY = "ADMIN_CANCELLED";
    private static final String PAYMENT_STATUS_CHANGED = "PAYMENT_STATUS_CHANGED";
    private static final String EVENT_KEY = "eventKey";
    private static final String BODY_KEY = "body";
    private static final String ALERT_KEY = "alert";


    public RiderFirebaseMessagingService() {
        instance = this;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        final Map<String, String> remoteMessageData = remoteMessage.getData();

        if (remoteMessageData.containsKey(TEXT_MESSAGE_KEY)) {
            final String message = remoteMessageData.get(TEXT_MESSAGE_KEY);
            App.getNotificationManager().showMessage(message, false);
        } else if (remoteMessageData.containsKey(EVENT_KEY)) {
            final String key = remoteMessageData.get(EVENT_KEY);
            final String body = remoteMessageData.get(BODY_KEY);
            if (isSplitFare(key)) {
                //noinspection WrongConstant
                App.getNotificationManager().notifySplitFare(key, body);
                App.getDataManager().postSplitFareChanged();
            } else if (isRateRide(key)) {
                App.getNotificationManager().notifyRateRide(body);
            } else if (isRideUpgrade(key)) {
                App.getNotificationManager().notifyRideUpgrade(key, body);
            } else if (isPaymentStatusChanged(key)) {
                App.getDataManager().requestUnpaid();
            } else {
                Timber.e("Notification not supported " + key);
            }
        }
    }

    private boolean isSplitFare(final String key) {
        switch (key) {
            case Constants.SplitFare.REQUESTED:
            case Constants.SplitFare.ACCEPTED:
            case Constants.SplitFare.DECLINED:
                return true;
            default:
                return false;
        }
    }

    private boolean isRateRide(final String key) {
        return key.equals(RIDE_RATE_REMINDER_KEY);
    }

    private boolean isRideUpgrade(final String key) {
        return key.equals(RIDE_UPGRADE_KEY);
    }

    private boolean isPaymentStatusChanged(final String key) {
        return key.equals(PAYMENT_STATUS_CHANGED);
    }

    @VisibleForTesting
    public static void injectPushNotification(final RemoteMessage remoteMessage) {
        if (instance == null) {
            instance = new RiderFirebaseMessagingService();
        }
        instance.onMessageReceived(remoteMessage);
    }
}
