package com.rideaustin.utils;

import android.util.Pair;

import com.rideaustin.api.DataManager;
import com.rideaustin.api.config.AutoGoOffline;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.base.retrofit.RetrofitException;
import com.rideaustin.manager.AppVisibilityState;
import com.rideaustin.engine.EngineState;
import com.rideaustin.engine.StateManager;
import com.rideaustin.manager.AppNotificationManager;
import com.rideaustin.manager.ConfigurationManager;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * @author sdelaysam.
 */

public class AutoOfflineHelper {

    private final StateManager stateManager;
    private final AppNotificationManager notificationManager;
    private final ConfigurationManager configurationManager;
    private DataManager dataManager;
    private Subscription autoOfflineSubscription = Subscriptions.empty();
    private Subscription goOfflineSubscription = Subscriptions.empty();
    private Subscription stateChangedSubscription = Subscriptions.empty();
    private Subscription appVisibilitySubscription = Subscriptions.empty();
    private boolean inForeground = false;

    public AutoOfflineHelper(Observable<AppVisibilityState> visibility,
                             StateManager stateManager,
                             AppNotificationManager notificationManager,
                             ConfigurationManager configurationManager,
                             DataManager dataManager) {
        this.stateManager = stateManager;
        this.notificationManager = notificationManager;
        this.configurationManager = configurationManager;
        this.dataManager = dataManager;
        stateChangedSubscription = stateManager.getEngineStateObservable()
                .subscribe(state -> checkAutoOffline());
        appVisibilitySubscription = visibility
                .subscribe(state -> {
                    switch (state) {
                        case FOREGROUND:
                            onAppForeground();
                            break;
                        case BACKGROUND:
                            onAppBackground();
                            break;
                    }
                });
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void onAppBackground() {
        inForeground = false;
        checkAutoOffline();
    }

    public void onAppForeground() {
        inForeground = true;
        cancelAutoOffline();
    }

    public void onStop() {
        cancelAutoOffline();
        goOfflineSubscription.unsubscribe();
        stateChangedSubscription.unsubscribe();
        appVisibilitySubscription.unsubscribe();
    }

    private void checkAutoOffline() {
        if (inForeground || !isOnlineState()) {
            return;
        }
        if (!autoOfflineSubscription.isUnsubscribed()) {
            return;
        }
        autoOfflineSubscription = configurationManager.getLiveConfig()
                .map(GlobalConfig::getAutoGoOffline)
                .filter(autoGoOffline -> autoGoOffline != null)
                .flatMap(autoGoOffline -> {
                    if (!autoGoOffline.isEnabled()) {
                        // do nothing
                        return Observable.empty();
                    }
                    // warning timer
                    int period = autoGoOffline.getBackgroundWarningPeriod();
                    return Observable.zip(Observable.just(autoGoOffline),
                            Observable.timer(period, TimeUnit.SECONDS),
                            Pair::new);
                })
                .flatMap(pair -> {
                    doOnOfflineWarning(pair.first);
                    // offline timer
                    int period = pair.first.getBackgroundMaximumPeriod() -
                            pair.first.getBackgroundWarningPeriod();
                    // at least 1 second delay
                    period = Math.max(period, 1);
                    return Observable.zip(Observable.just(pair.first),
                            Observable.timer(period, TimeUnit.SECONDS),
                            Pair::new);

                })
                .subscribe(pair -> doOnAutoOffline(pair.first), Timber::e);
    }

    private void cancelAutoOffline() {
        autoOfflineSubscription.unsubscribe();
        notificationManager.cancelAutoOfflineNotification();
    }

    private void doOnOfflineWarning(AutoGoOffline autoGoOffline) {
        notificationManager.notifyAutoOfflineWarning(autoGoOffline);
    }

    private void doOnAutoOffline(AutoGoOffline autoGoOffline) {
        notificationManager.notifyAutoOffline(autoGoOffline);
        goOfflineSubscription.unsubscribe();
        goOfflineSubscription = dataManager.deactivateDriver()
                // Filter errors caused by network
                // 120 attempts 5 seconds each = 10 mins
                // after 10 mins of inactivity server will send driver offline anyway
                .retryWhen(new RetryWithDelay(5000, 120, this::causedByNetwork))
                .subscribe(aVoid -> switchOffline(), Timber::e);
    }

    private void switchOffline() {
        stateManager.switchState(stateManager.createOfflinePoolingState());
    }

    private boolean isOnlineState() {
        return stateManager.getCurrentEngineStateType().equals(EngineState.Type.ONLINE);
    }

    private boolean causedByNetwork(Throwable throwable) {
        return throwable instanceof RetrofitException
                && (((RetrofitException) throwable).causedByNetwork());
    }
}
