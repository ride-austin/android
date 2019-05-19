package com.rideaustin.manager.notification;

import com.rideaustin.manager.AppNotificationManager;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

/**
 * Created by Sergey Petrov on 02/03/2017.
 */

public class InAppMessageManager {

    private AppNotificationManager notificationManager;
    private ReplaySubject<InAppMessage> messages = ReplaySubject.create();
    private PublishSubject<InAppMessage> consumedMessage = PublishSubject.create();
    private List<Integer> ids = new ArrayList<>();

    public InAppMessageManager(AppNotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void show(InAppMessage message) {
        if (!hasMessage(message)) {
            messages.onNext(message);
            ids.add(message.getId());
        }
    }

    public void read(InAppMessage message) {
        message.consume();
        notificationManager.cancelNotification(message.getNotificationId());
        consumedMessage.onNext(message);
    }

    public void read(Func1<? super InAppMessage, Boolean> predicate) {
        int size = messages.size();
        if (size > 0) {
            InAppMessage[] arr = new InAppMessage[size];
            arr = messages.getValues(arr);
            for (InAppMessage message : arr) {
                if (!message.isConsumed() && predicate.call(message)) {
                    read(message);
                }
            }
        }
    }

    public void readAll() {
        read(inAppMessage -> true);
    }

    public Observable<InAppMessage> getNewMessages() {
        return messages
                .asObservable()
                .filter(message -> !message.isConsumed())
                .serialize()
                .onBackpressureBuffer();
    }

    public Observable<InAppMessage> getConsumedMessage() {
        return consumedMessage
                .asObservable()
                .serialize()
                .onBackpressureBuffer();
    }

    private boolean hasMessage(InAppMessage message) {
        return ids.contains(message.getId());
    }

}
