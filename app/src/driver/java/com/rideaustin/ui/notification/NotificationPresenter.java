package com.rideaustin.ui.notification;

import com.rideaustin.api.model.Rider;
import com.rideaustin.engine.EngineState;
import com.rideaustin.engine.StateManager;
import com.rideaustin.engine.state.PendingAcceptState;
import com.rideaustin.manager.AppNotificationManager;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by rost on 8/27/16.
 */
public class NotificationPresenter {

    private final AppNotificationManager notificationManager;
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    public NotificationPresenter(AppNotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void showNotifications(StateManager stateManager) {
        subscriptions.add(stateManager.getUIStateObservable().subscribe(state -> {
            EngineState.Type stateType = state.getType();
            Timber.d("::showNotifications:: State: %s", stateType);
            switch (stateType) {
                case PENDING_ACCEPT:
                    final Rider rider = ((PendingAcceptState) state).getRider();
                    notificationManager.notifyRideRequest(rider, null);
                    break;
                case ENDED:
                    notificationManager.notifyRateRide();
                    break;
            }
        }));

        subscriptions.add(stateManager.getDestinationUpdateObservable()
                .subscribe(event -> {
                    EngineState.Type type = stateManager.getCurrentEngineStateType();
                    switch (type) {
                        case ARRIVED:
                        case STARTED:
                            notificationManager.notifyDestinationChanged();
                            break;
                    }
                }));

        subscriptions.add(stateManager.getCancelledRideObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(ride -> {
                    if (notificationManager.notifyRideCancelled(ride)) {
                        // notification shown, request app to foreground
                        NavigationDrawerActivity.getStartIntentForRideRequestLaunch();
                    }
                }));
    }

    public void onStop() {
        subscriptions.unsubscribe();
    }
}
