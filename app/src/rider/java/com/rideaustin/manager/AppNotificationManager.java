package com.rideaustin.manager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.BaseApp;
import com.rideaustin.R;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.rideupgrade.RideUpgradeMessage;
import com.rideaustin.manager.notification.AlertMessage;
import com.rideaustin.manager.notification.InAppMessage;
import com.rideaustin.manager.notification.SplitFareMessage;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.SerializationHelper;

import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;

/**
 * Created by kshumelchyk on 7/21/16.
 */
public class AppNotificationManager extends BaseAppNotificationManager {

    private static final int NOTIFICATION_ID_RIDE_STATUS = 0;
    private static final int NOTIFICATION_ID_RIDE_UPGRADE = 1;
    private static final int NOTIFICATION_ID_SPLIT_FARE = 2;
    private static final int NOTIFICATION_ID_SPLIT_FARE_ACCEPTED_DECLINED = 3;

    public static final long[] DEFAULT_VIBRATION_PATTERN = new long[]{1000, 1000, 1000, 1000, 1000};

    public AppNotificationManager(BaseApp app) {
        super(app);
    }

    public void notifyDriverAssigned() {
        notificationManager.cancel(NOTIFICATION_ID_RIDE_STATUS);
        if (isInBackground()) {
            PendingIntent intent = PendingIntent.getActivity(app, 0, new Intent(app, NavigationDrawerActivity.class), PendingIntent.FLAG_ONE_SHOT);
            String title = App.getAppName();
            String message = app.getString(R.string.driver_assigned_notification_msg);
            Notification notification = createNotification(title, message, intent, false);
            notificationManager.notify(NOTIFICATION_ID_RIDE_STATUS, notification);
        }
    }

    public void notifyDriverReached(Ride ride) {
        notificationManager.cancel(NOTIFICATION_ID_RIDE_STATUS);
        readMessages(ride.getId());
        showMessage(app.getString(R.string.driver_reached_notification_msg), false, ride);
    }

    public void notifyDriverEta(final String eta) {
        notificationManager.cancel(NOTIFICATION_ID_RIDE_STATUS);
        if (isInBackground()) {
            PendingIntent intent = PendingIntent.getActivity(app, 0, new Intent(app, NavigationDrawerActivity.class), PendingIntent.FLAG_ONE_SHOT);
            String title = App.getAppName();
            String message = app.getString(R.string.driver_assigned_eta_notification_msg, eta);
            Notification notification = createNotification(title, message, intent, false);
            // do not cancel previous notification here, its received quite often
            // so need to avoid playing sound each several seconds.
            notificationManager.notify(NOTIFICATION_ID_RIDE_STATUS, notification);
        }
    }

    public void notifyRedispatchFailed() {
        notificationManager.cancel(NOTIFICATION_ID_RIDE_STATUS);
        notificationManager.cancel(NOTIFICATION_ID_RIDE_UPGRADE);
        showMessage(app.getString(R.string.redispatch_failed_notification_msg), false);
    }

    public void notifyDirectConnectFailed() {
        showMessage(app.getString(R.string.direct_connect_no_driver), false);
    }

    public void notifyNoAvailableDrivers() {
        String notificationTitle = App.getAppName();
        String messageTitle = app.getString(R.string.title_no_driver_available);
        String messageText = app.getString(R.string.message_no_driver_available);
        notificationManager.cancel(NOTIFICATION_ID_RIDE_UPGRADE);
        notificationManager.cancel(NOTIFICATION_ID_RIDE_STATUS);
        showMessage(notificationTitle, messageText, messageTitle, messageText, null, false, null);
    }

    public void notifyAdminCancelled(Ride ride) {
        double payment = getDriverPayment(ride);
        String message = payment > 0
                ? app.getString(R.string.message_ride_cancelled_by_admin_with_fee, payment)
                : app.getString(R.string.message_ride_cancelled_by_admin);
        notificationManager.cancel(NOTIFICATION_ID_RIDE_UPGRADE);
        notificationManager.cancel(NOTIFICATION_ID_RIDE_STATUS);
        if (ride != null) {
            readMessages(ride.getId());
        }
        showMessage(message, false, ride);
    }

    public void notifyDriverCancelled(Ride ride) {
        String title = App.getAppName();
        String notificationMessage = app.getString(R.string.driver_cancelled_notification_msg);
        double payment = getDriverPayment(ride);
        String message = payment > 0
                ? app.getString(R.string.message_ride_cancelled_by_driver_with_fee, payment)
                : app.getString(R.string.message_ride_cancelled_by_driver);
        notificationManager.cancel(NOTIFICATION_ID_RIDE_UPGRADE);
        notificationManager.cancel(NOTIFICATION_ID_RIDE_STATUS);
        if (ride != null) {
            readMessages(ride.getId());
        }
        showMessage(title, message, title, notificationMessage, DEFAULT_VIBRATION_PATTERN, false, ride);
    }

    public void notifyRiderCancelled(@Nullable Ride ride) {
        // don't show notification, rider is aware of that we had cancelled
        double payment = getDriverPayment(ride);
        String message = payment > 0
                ? app.getString(R.string.message_ride_cancelled_by_user_with_fee, payment)
                : app.getString(R.string.message_ride_cancelled_by_user);
        notificationManager.cancel(NOTIFICATION_ID_RIDE_UPGRADE);
        notificationManager.cancel(NOTIFICATION_ID_RIDE_STATUS);
        if (ride != null) {
            readMessages(ride.getId());
        }
        showMessage(message, false, ride);
    }

    public void notifyRateRide(final String body) {
        //noinspection ConstantConditions
        AlertMessage message = SerializationHelper.deSerialize(body, AlertMessage.class);
        if (message != null) {
            PendingIntent intent = PendingIntent.getActivity(app, 0, new Intent(app, NavigationDrawerActivity.class), PendingIntent.FLAG_ONE_SHOT);
            String title = App.getAppName();
            Notification notification = createNotification(title, message.getAlert(), intent, false, DEFAULT_VIBRATION_PATTERN);
            notificationManager.cancel(NOTIFICATION_ID_RIDE_UPGRADE);
            notificationManager.cancel(NOTIFICATION_ID_RIDE_STATUS);
            notificationManager.notify(NOTIFICATION_ID_RIDE_STATUS, notification);
        }
    }

    public void notifySplitFare(final @Constants.SplitFare String key, @Nullable final String body) {
        if (isEmptyBody(key, body)) {
            return;
        }

        // deserialize message from GCM payload
        //noinspection ConstantConditions
        SplitFareMessage splitFareMessage = SerializationHelper.deSerialize(body, SplitFareMessage.class);
        if (splitFareMessage == null) {
            return;
        }
        splitFareMessage.setType(key);

        // gather data for notification
        int notificationId;
        String title;
        String message;
        switch (key) {
            case Constants.SplitFare.REQUESTED:
                notificationId = NOTIFICATION_ID_SPLIT_FARE;
                title = app.getString(R.string.fare_split_notification_title);
                message = app.getString(R.string.fare_split_notification_message, splitFareMessage.getSourceUser());
                break;
            case Constants.SplitFare.ACCEPTED:
                notificationId = NOTIFICATION_ID_SPLIT_FARE_ACCEPTED_DECLINED;
                title = app.getString(R.string.fare_split_notification_accept_title);
                message = app.getString(R.string.fare_split_notification_accept_message, splitFareMessage.getTargetUser());
                break;
            case Constants.SplitFare.DECLINED:
                notificationId = NOTIFICATION_ID_SPLIT_FARE_ACCEPTED_DECLINED;
                title = app.getString(R.string.fare_split_notification_declined_title);
                message = app.getString(R.string.fare_split_notification_declined_message, splitFareMessage.getTargetUser());
                break;
            default:
                Exception e = new Exception(UNEXPECTED_STATE_KEY);
                Timber.e(e, "Unknown split fare key: " + key);
                return;
        }

        // build message
        splitFareMessage.setNotificationId(notificationId);

        if (isInBackground()) {
            // build launcher intent with message
            Intent launcherIntent = new Intent(app, NavigationDrawerActivity.class);
            launcherIntent.putExtra(Constants.EXTRA_KEY_SPLIT_FARE_MESSAGE, SerializationHelper.serialize(splitFareMessage));
            launcherIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent = PendingIntent.getActivity(app, 0, launcherIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // build and show notification
            // NOTE: don't cancel previous notification, use PendingIntent.FLAG_UPDATE_CURRENT instead
            Notification notification = createNotification(title, message, intent, true, DEFAULT_VIBRATION_PATTERN);
            notificationManager.notify(splitFareMessage.getNotificationId(), notification);
        }
        // show message (it is probably getting consumed right after this)
        App.getInstance().getInAppMessageManager().show(splitFareMessage);
    }

    private boolean isEmptyBody(final String key, @Nullable final String body) {
        if (TextUtils.isEmpty(body)) {
            Exception e = new Exception(UNEXPECTED_STATE_KEY);
            Timber.e(e, "Notification " + key + " can not have empty body");
            return true;
        }
        return false;
    }

    public void notifyRideUpgrade(String key, String body) {
        if (isEmptyBody(key, body)) {
            return;
        }

        if (isInBackground()) {
            RideUpgradeMessage message = SerializationHelper.deSerialize(body, RideUpgradeMessage.class);
            String messageContent = App.getInstance().getString(R.string.ride_upgrade_msg, message.getSource().toUpperCase(), message.getTarget().toUpperCase());

            // build launcher intent with message
            Intent launcherIntent = new Intent(app, NavigationDrawerActivity.class);
            launcherIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent = PendingIntent.getActivity(app, 0, launcherIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // build and show notification
            // NOTE: don't cancel previous notification, use PendingIntent.FLAG_UPDATE_CURRENT instead
            String title = App.getAppName();
            Notification notification = createNotification(title, messageContent, intent, true);
            cancelCarUpgradeNotification();
            notificationManager.notify(NOTIFICATION_ID_RIDE_UPGRADE, notification);
        }
    }

    public void cancelCarUpgradeNotification() {
        notificationManager.cancel(NOTIFICATION_ID_RIDE_UPGRADE);
    }

    private double getDriverPayment(Ride ride) {
        return ride != null ? ride.getDriverPayment() : 0;
    }
}
