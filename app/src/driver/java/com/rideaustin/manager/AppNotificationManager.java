package com.rideaustin.manager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.BaseApp;
import com.rideaustin.R;
import com.rideaustin.api.config.AutoGoOffline;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.Rider;
import com.rideaustin.api.model.User;
import com.rideaustin.engine.EngineState;
import com.rideaustin.manager.notification.InAppMessage;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.utils.UIUtils;
import com.rideaustin.utils.Constants;

import java.util.Collection;
import java.util.Collections;

import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;

/**
 * Created by kshumelchyk on 7/21/16.
 */
public class AppNotificationManager extends BaseAppNotificationManager {

    private static final int NOTIFICATION_ID_DESTINATION_CHANGED = 0;
    private static final int NOTIFICATION_ID_RIDE_REQUEST = 1;
    private static final int NOTIFICATION_ID_MOTION_DETECTED = 2;
    private static final int NOTIFICATION_ID_RIDE_CANCELLED = 3;
    private static final int NOTIFICATION_ID_RATE_RIDE = 4;
    private static final int NOTIFICATION_ID_AIRPORT_QUEUE = 5;
    private static final int NOTIFICATION_ID_RIDE_UPGRADE = 6;
    private static final int NOTIFICATION_ID_AUTO_OFFLINE = 7;
    private static final int NOTIFICATION_ID_FINISH_RIDE = 8;
    private static final int NOTIFICATION_ID_RIDE_REASSIGNED = 9;
    // please check showOnlyNotification and update NUMBER_OF_NOTIFICATION_IDS accordingly.
    private static final int NUMBER_OF_NOTIFICATION_IDS = 10;

    public AppNotificationManager(BaseApp app) {
        super(app);
    }

    public void notifyDestinationChanged() {
        String title = App.getAppName();
        String message = app.getString(R.string.notification_destination_changed);
        PendingIntent intent = getDrawerActivityIntent();
        Notification notification = createNotification(title, message, intent, false);
        showOnlyNotification(NOTIFICATION_ID_DESTINATION_CHANGED, notification);
    }

    public void notifyRideRequest(Rider rider, @Nullable String eta) {
        if (isInBackground()) {
            String riderName = getRiderName(rider);
            String title = App.getAppName();
            String message = eta == null
                    ? app.getString(R.string.notification_request, riderName)
                    : app.getString(R.string.notification_request_eta, riderName, eta);
            PendingIntent intent = getDrawerActivityIntent();
            Notification notification = createNotification(title, message, intent, false);
            showOnlyNotification(NOTIFICATION_ID_RIDE_REQUEST, notification);
        }
    }

    public void notifyMotionDetected(EngineState state) {
        if (isInBackground()) {
            String title = App.getAppName();
            String message = state.getType().equals(EngineState.Type.ARRIVED)
                    ? app.getString(R.string.motion_detected_start_dialog_title)
                    : app.getString(R.string.motion_detected_end_dialog_title);
            PendingIntent intent = getDrawerActivityIntent();
            Notification notification = createNotification(title, message, intent, false);
            showOnlyNotification(NOTIFICATION_ID_MOTION_DETECTED, notification);
        }
    }

    public boolean notifyRideCancelled(Ride ride) {
        String title = app.getString(R.string.ride_cancelled);
        String message;
        boolean needNotification;
        switch (RideStatus.valueOf(ride.getStatus())) {
            case RIDER_CANCELLED:
                message = getRiderCancelledMessage(ride);
                needNotification = true;
                break;
            case DRIVER_CANCELLED:
                message = getDriverCancelledMessage(ride);
                needNotification = false;
                break;
            case ADMIN_CANCELLED:
                message = app.getString(R.string.ride_cancelled_by_admin);
                needNotification = true;
                break;
            default:
                Exception e = new Exception(UNEXPECTED_STATE_KEY);
                Timber.e(e, "Unexpected ride status when showing cancel notification: " + ride);
                return false;
        }

        readMessages(ride.getId());

        // build message
        InAppMessage inAppMessage = new InAppMessage(title, message, ride);

        if (needNotification && isInBackground()) {
            // build and show notification
            inAppMessage.setNotificationId(NOTIFICATION_ID_RIDE_CANCELLED);
            Notification notification = createNotification(title, message, createPendingIntent(inAppMessage), true);
            showOnlyNotification(inAppMessage.getNotificationId(), notification);
        }

        // show message (it is probably getting consumed right after this)
        App.getInstance().getInAppMessageManager().show(inAppMessage);

        return needNotification;
    }

    public void notifyRideReassigned(Event event) {
        InAppMessage inAppMessage = new InAppMessage(event.getTitle(), event.getMessage());
        if (isInBackground()) {
            inAppMessage.setNotificationId(NOTIFICATION_ID_RIDE_REASSIGNED);
            Notification notification = createNotification(event.getTitle(), event.getMessage(), createPendingIntent(inAppMessage), true);
            showOnlyNotification(inAppMessage.getNotificationId(), notification);
        }
        App.getInstance().getInAppMessageManager().show(inAppMessage);
    }

    public void notifyRateRide() {
        String title = App.getAppName();
        String message = app.getString(R.string.notification_rate);
        PendingIntent intent = getDrawerActivityIntent();
        Notification notification = createNotification(title, message, intent, false);
        showOnlyNotification(NOTIFICATION_ID_RATE_RIDE, notification);
    }

    public void notifyRideUpgrade(@StringRes int message) {
        if (isInBackground()) {
            String title = App.getAppName();
            PendingIntent intent = getDrawerActivityIntent();
            Notification notification = createNotification(title, app.getString(message), intent, false);
            showOnlyNotification(NOTIFICATION_ID_RIDE_UPGRADE, notification);
        }
    }

    public void notifyAirportQueue(String message) {
        if (isInBackground()) {
            String title = App.getAppName();
            PendingIntent intent = getDrawerActivityIntent();
            Notification notification = createNotification(title, message, intent, true);
            showOnlyNotification(NOTIFICATION_ID_AIRPORT_QUEUE, notification);
        }
    }

    public void notifyAirportQueueUpdated(String queueName, Collection<Integer> positions) {
        if (isInBackground() && !TextUtils.isEmpty(queueName)) {
            String title = App.getAppName();
            if (positions != null && positions.size() > 0) {
                Integer minimalPositionValue = Collections.min(positions) + 1;
                if (minimalPositionValue <= Constants.MINIMAL_QUEUE_POSITION_TO_NOTIFY) {
                    final String message = app.getResources().getQuantityString(R.plurals.queue_zone_position, minimalPositionValue, minimalPositionValue);

                    PendingIntent intent = getDrawerActivityIntent();
                    Notification notification = createNotification(title, message, intent, false);
                    showOnlyNotification(NOTIFICATION_ID_AIRPORT_QUEUE, notification);
                }
            }
        }
    }

    public void notifyAutoOfflineWarning(AutoGoOffline autoGoOffline) {
        String title = App.getAppName();
        String message = autoGoOffline.getWarningMessage();
        PendingIntent intent = getDrawerActivityIntent();
        Notification notification = createNotification(title, message, intent, true);
        showOnlyNotification(NOTIFICATION_ID_AUTO_OFFLINE, notification);
    }

    public void notifyAutoOffline(AutoGoOffline autoGoOffline) {
        String title = App.getAppName();
        String message = autoGoOffline.getOfflineBackgroundMessage();
        PendingIntent intent = getDrawerActivityIntent();
        Notification notification = createNotification(title, message, intent, false);
        showOnlyNotification(NOTIFICATION_ID_AUTO_OFFLINE, notification);
    }

    public void cancelAutoOfflineNotification() {
        notificationManager.cancel(NOTIFICATION_ID_AUTO_OFFLINE);
    }

    private void showOnlyNotification(int id, Notification notification) {
        for (int i = NOTIFICATION_ID_DESTINATION_CHANGED; i < NUMBER_OF_NOTIFICATION_IDS; i++) {
            if (i != id) {
                notificationManager.cancel(i);
            }
        }
        notificationManager.notify(id, notification);
    }

    private PendingIntent getDrawerActivityIntent() {
        return PendingIntent.getActivity(app, 0, new Intent(app, NavigationDrawerActivity.class), PendingIntent.FLAG_ONE_SHOT);
    }

    private String getRiderName(Rider rider) {
        if (rider == null) {
            return app.getString(R.string.notification_rider);
        }

        String name = rider.getFirstname();

        if (TextUtils.isEmpty(name)) {
            name = rider.getFullName();
        }

        if (TextUtils.isEmpty(name)) {
            final User user = rider.getUser();
            if (user != null) {
                name = user.getFirstName();

                if (TextUtils.isEmpty(name)) {
                    name = user.getFullName();
                }
            }
        }

        if (TextUtils.isEmpty(name)) {
            name = app.getString(R.string.notification_rider);
        }

        return name;
    }

    private String getRiderCancelledMessage(Ride ride) {
        String riderName = getRiderName(ride.getRider());
        if (ride.getDriverPayment() > 0) {
            String payment = UIUtils.convertMoneyToString(ride.getDriverPayment());
            return app.getString(R.string.ride_cancelled_by_rider_with_fee, riderName, payment);
        } else {
            return app.getString(R.string.ride_cancelled_by_rider, riderName);
        }
    }

    private String getDriverCancelledMessage(Ride ride) {
        if (ride.getDriverPayment() > 0) {
            String payment = UIUtils.convertMoneyToString(ride.getDriverPayment());
            return app.getString(R.string.ride_cancelled_by_driver_with_fee, payment);
        } else {
            return app.getString(R.string.ride_cancelled_by_driver_without_fee);
        }
    }

    public void notifyDestinationReached() {
        String title = App.getAppName();
        PendingIntent intent = getDrawerActivityIntent();
        Notification notification = createNotification(title, App.getInstance().getString(R.string.finish_ride_notification_msg), intent, true);
        showOnlyNotification(NOTIFICATION_ID_FINISH_RIDE, notification);
    }
}
