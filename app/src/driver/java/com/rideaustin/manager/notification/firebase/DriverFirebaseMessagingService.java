package com.rideaustin.manager.notification.firebase;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rideaustin.App;

import java.util.Map;

import timber.log.Timber;

/**
 * Created by hatak on 1/17/17.
 */

public class DriverFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TEXT_MESSAGE_KEY = "default";
    private static final String EVENT_KEY = "eventKey";
    private static final String BODY_KEY = "body";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        final Map<String, String> remoteMessageData = remoteMessage.getData();

        if (remoteMessageData.containsKey(TEXT_MESSAGE_KEY)) {
            final String message = remoteMessageData.get(TEXT_MESSAGE_KEY);
            App.getNotificationManager().showMessage(message, false);
        } else if (remoteMessageData.containsKey(EVENT_KEY)) {
            final String eventValue = remoteMessageData.get(EVENT_KEY);
            final String messageBody = remoteMessageData.get(BODY_KEY);
            Timber.d("::: received notification with event: "+eventValue + " and body: "+messageBody);
        }
    }
}
