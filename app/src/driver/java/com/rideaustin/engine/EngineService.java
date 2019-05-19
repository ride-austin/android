package com.rideaustin.engine;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Pair;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.manager.AppVisibilityState;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.notification.NotificationPresenter;

import java8.util.Optional;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * Created by rost on 8/12/16.
 */
public class EngineService extends Service implements StateManager.ErrorListener {
    private StateManager stateManager;
    private LongPollingManager longPollingManager;

    private static final int SERVICE_ID = 23456;
    private static final String START = "command_start";
    private static final String SWITCH_NOT_STICKY = "command_not_sticky";
    private int startMode = START_STICKY;

    private BehaviorSubject<ServiceState> serviceState = BehaviorSubject.create(ServiceState.OFFLINE);
    private NotificationPresenter notificationPresenter;

    @Override
    public void onCreate() {
        super.onCreate();
        longPollingManager = App.getInstance().getLongPollingManager();
        longPollingManager.start();

        stateManager = App.getInstance().getStateManager();
        stateManager.setErrorListener(this);
        stateManager.start();

        // check service state on each engine state
        Observable.combineLatest(stateManager.getEngineStateObservable(), App.getInstance().getVisibilityObservable(), Pair::new)
                .map(pair -> getCurrentServiceState(pair.first.getType(), pair.second))
                .observeOn(RxSchedulers.main())
                .subscribe(serviceState::onNext);

        // check "marked as offline" service state
        Observable.combineLatest(serviceState, App.getInstance().getVisibilityObservable(), Pair::new)
                .filter(pair -> pair.first == ServiceState.MARKED_AS_OFFLINE && pair.second == AppVisibilityState.FOREGROUND)
                .observeOn(RxSchedulers.main())
                .subscribe(pair -> serviceState.onNext(ServiceState.OFFLINE), Timber::e);

        // listen to service state
        serviceState.scan(ServiceState.OFFLINE, (previousState, currentState) -> {
            switch(currentState) {
                case ONLINE:
                    switchToOnline();
                    break;
                case OFFLINE:
                    switchToOffline();
                    break;
                case MARKED_AS_OFFLINE:
                    switchToMarkedAsOffline(previousState);
                    break;
            }
            return currentState;
        }).subscribe();


        notificationPresenter = new NotificationPresenter(App.getNotificationManager());
        notificationPresenter.showNotifications(stateManager);
    }

    private void switchToOffline() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        } else if (startMode == START_STICKY) {
            switchToNotSticky(this);
        }
    }

    private void switchToOnline() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(R.string.foreground_service_online);
        } else if (startMode == START_NOT_STICKY) {
            start(this);
        }
    }

    private void switchToMarkedAsOffline(ServiceState previousState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (previousState == ServiceState.ONLINE) {
                startForeground(R.string.foreground_service_marked_as_offline);
            }
        } else if (startMode == START_STICKY) {
            switchToNotSticky(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stateManager.stop();
        longPollingManager.stop();
        notificationPresenter.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Optional.ofNullable(intent)
                .map(Intent::getAction)
                .ifPresentOrElse(action -> startMode = action.equals(SWITCH_NOT_STICKY) ? START_NOT_STICKY : START_STICKY,
                        () -> startMode = START_NOT_STICKY);
        return startMode;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onFatalError(Throwable ex) {
        Timber.e(ex, "unexpected error from the state manager! Finishing");
        shutdown(this);
    }

    public static void shutdown(Context context) {
        context.stopService(new Intent(context, EngineService.class));
    }

    private static void switchToNotSticky(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            context.startService(new Intent(context, EngineService.class).setAction(EngineService.SWITCH_NOT_STICKY));
        }
    }

    public static void start(Context context) {
        context.startService(new Intent(context, EngineService.class).setAction(EngineService.START));
    }

    private void startForeground(@StringRes int messageRes) {
        String message = getString(messageRes);
        startForeground(SERVICE_ID, App.getNotificationManager()
                .createForegroundServiceNotification(message));
    }

    private ServiceState getCurrentServiceState(BaseEngineState.Type type, AppVisibilityState visibilityState) {
        switch (type) {
            case INACTIVE:
            case UNKNOWN:
            case OFFLINE:
                return visibilityState == AppVisibilityState.FOREGROUND
                        ? ServiceState.OFFLINE
                        : ServiceState.MARKED_AS_OFFLINE;
        }
        return ServiceState.ONLINE;

    }

    private enum ServiceState {
        OFFLINE,
        MARKED_AS_OFFLINE,
        ONLINE
    }
}
