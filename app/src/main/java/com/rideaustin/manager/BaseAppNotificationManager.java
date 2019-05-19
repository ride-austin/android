package com.rideaustin.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.rideaustin.App;
import com.rideaustin.BaseApp;
import com.rideaustin.R;
import com.rideaustin.api.model.Ride;
import com.rideaustin.manager.notification.InAppMessage;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.SerializationHelper;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Sergey Petrov on 03/03/2017.
 */

public abstract class BaseAppNotificationManager {

    private static final String SERVICE_CHANNEL_ID = "service_channel";
    private static final String NOTIFICATION_CHANNEL_ID = "service_channel";

    protected final BaseApp app;
    protected final NotificationManagerCompat notificationManager;
    private final Uri notificationSoundURI;
    private NotificationChannel notificationChannel;

    public BaseAppNotificationManager(BaseApp app) {
        this.app = app;
        this.notificationManager = NotificationManagerCompat.from(app);
        this.notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    /**
     * Same as {@link AppNotificationManager#showMessage(String, String, boolean)}
     * but {@link App#getAppName()} is used as a title
     * @param message full text message to show in notification/dialog
     * @param forceNotification show notification even if app is in foreground
     */
    public void showMessage(String message, boolean forceNotification) {
        showMessage(App.getAppName(), message, forceNotification);
    }

    /**
     * Same as {@link AppNotificationManager#showMessage(String, String, boolean)}
     * but {@link App#getAppName()} is used as a title
     * @param message full text message to show in notification/dialog
     * @param forceNotification show notification even if app is in foreground
     * @param ride ride
     */
    public void showMessage(String message, boolean forceNotification, @Nullable Ride ride) {
        showMessage(App.getAppName(), message, forceNotification, ride);
    }

    /**
     * Show {@link InAppMessage} and notification if app is in background (or if {@code forceNotification} is TRUE
     * When user dismisses/cancels the dialog message, notification would be also cancelled.
     * When user taps on notification, the app would be launched/restored and dialog would be shown (if not currently showing).
     * @param title title of the notification
     * @param message full text message to show in notification/dialog
     * @param forceNotification show notification even if app is in foreground
     */
    public void showMessage(String title, String message, boolean forceNotification) {
        showMessage(title, message, title, message, null, forceNotification, null);
    }

    /**
     * Show {@link InAppMessage} and notification if app is in background (or if {@code forceNotification} is TRUE
     * When user dismisses/cancels the dialog message, notification would be also cancelled.
     * When user taps on notification, the app would be launched/restored and dialog would be shown (if not currently showing).
     * @param title title of the notification
     * @param message full text message to show in notification/dialog
     * @param forceNotification show notification even if app is in foreground
     * @param ride ride
     */
    public void showMessage(String title, String message, boolean forceNotification, @Nullable Ride ride) {
        showMessage(title, message, title, message, null, forceNotification, ride);
    }

    public void showMessage(String messageTitle,
                            String messageText,
                            String notificationTitle,
                            String notificationText,
                            long[] vibrate,
                            boolean forceNotification,
                            @Nullable Ride ride) {

        InAppMessage inAppMessage = new InAppMessage(messageTitle, messageText, ride);
        if (forceNotification || isInBackground()) {
            Notification notification = createNotification(notificationTitle, notificationText,
                    createPendingIntent(inAppMessage), true, vibrate);
            notificationManager.notify(inAppMessage.getNotificationId(), notification);
        }
        App.getInstance().getInAppMessageManager().show(inAppMessage);
    }

    public void readMessages(long rideId) {
        App.getInstance().getInAppMessageManager()
                .read(inAppMessage -> inAppMessage.getRideId() == rideId);
    }

    /**
     * Cancel notification from status bar.
     * This doesn't dismiss associated {@link android.app.Dialog}
     * @param id - notification id, see {@link InAppMessage#getNotificationId()}
     */
    public void cancelNotification(int id) {
        notificationManager.cancel(id);
    }

    /**
     * Show notification about foreground service being started
     * @param id - notification id, same as id used to start foreground service
     * @param message - message explaining the purpose of service
     */
    public void notifyForegroundService(int id, String message) {
        notificationManager.notify(id, createForegroundServiceNotification(message));
    }

    public Notification createForegroundServiceNotification(String message) {
        Intent openApp = new Intent(app, NavigationDrawerActivity.class);
        if (notificationChannel == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = App.getAppName();
            notificationChannel = new NotificationChannel(SERVICE_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) app.getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(notificationChannel);
            }
        }
        return new NotificationCompat.Builder(app, SERVICE_CHANNEL_ID)
                .setContentTitle(App.getAppName())
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(app, 0, openApp, 0))
                .setSmallIcon(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? R.drawable.ic_notification_icon : R.mipmap.ic_launcher)
                .setOnlyAlertOnce(true)
                .build();
    }

    protected Notification createNotification(String title, String message, PendingIntent intent, boolean multiline) {
        return createNotification(title, message, intent, multiline, null);
    }

    protected Notification createNotification(String title, String message, PendingIntent intent, boolean multiline, long[] vibrate) {
        return new NotificationCompat.Builder(App.getInstance(), NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(intent)
                .setSmallIcon(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? R.drawable.ic_notification_icon : R.mipmap.ic_launcher)
                .setOnlyAlertOnce(true)
                .setSound(notificationSoundURI)
                .setVibrate(vibrate)
                .setStyle(multiline ? new NotificationCompat.BigTextStyle().bigText(message) : null)
                .build();
    }

    protected PendingIntent createPendingIntent(InAppMessage message) {
        // build launcher intent with message
        Intent launcherIntent = new Intent(app, NavigationDrawerActivity.class);
        launcherIntent.putExtra(CommonConstants.EXTRA_KEY_NOTIFICATION_MESSAGE, SerializationHelper.serialize(message));
        launcherIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // use PendingIntent.FLAG_ONE_SHOT with unique request code to force each notification launch/restore the app
        // without request code only the first tapped notification launches the app
        return PendingIntent.getActivity(app, message.getId(), launcherIntent, PendingIntent.FLAG_ONE_SHOT);
    }

    protected boolean isInBackground() {
        return app.getVisibilityState() == AppVisibilityState.BACKGROUND;
    }

}
