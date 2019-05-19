package com.rideaustin.manager;

import android.content.Context;

import com.github.pwittchen.reactivenetwork.library.Connectivity;
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;
import com.github.pwittchen.reactivenetwork.library.internet.observing.error.DefaultErrorHandler;
import com.github.pwittchen.reactivenetwork.library.internet.observing.strategy.SocketInternetObservingStrategy;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.gradle.BuildConfigProxy;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.Subscriptions;

/**
 * Listens to network connection and server reachability.
 * Planned to be used to detect connection status changed and connection recovery.
 * Based on {@link ReactiveNetwork}.
 *
 * Created by Sergey Petrov on 26/06/2017.
 */

public class ConnectionStatusManager {

    private static final int CONNECTIVITY_INTERVAL_MS = 5000;
    private static final int CONNECTIVITY_TIMEOUT_MS = 5000;

    private final Context context;
    private final BehaviorSubject<ConnectionStatus> statusSubject = BehaviorSubject.create();

    private Subscription generalSubscription = Subscriptions.empty();
    private Subscription serverSubscription = Subscriptions.empty();

    public ConnectionStatusManager(Context context) {
        this.context = context;
        start();
    }

    public void start() {
        stop();
        generalSubscription = ReactiveNetwork.observeNetworkConnectivity(context)
                .observeOn(RxSchedulers.main())
                .doOnNext(this::doOnGeneralConnectivity)
                .subscribe();
    }

    public void stop() {
        generalSubscription.unsubscribe();
        serverSubscription.unsubscribe();
    }

    public Observable<ConnectionStatus> getStatusObservable() {
        return statusSubject.onBackpressureLatest()
                .distinctUntilChanged()
                .asObservable();
    }

    public Optional<ConnectionStatus> getStatus() {
        return Optional.ofNullable(statusSubject.getValue());
    }

    private void doOnGeneralConnectivity(Connectivity connectivity) {
        switch (connectivity.getState()) {
            case DISCONNECTED:
            case DISCONNECTING:
            case SUSPENDED:
                // no network connection
                statusSubject.onNext(ConnectionStatus.DISCONNECTED);
                // stop polling server connectivity
                stopListeningServerConnectivity();
                break;
            case CONNECTING:
                // network attempts to connect
                statusSubject.onNext(ConnectionStatus.CONNECTING);
                // stop polling server connectivity
                stopListeningServerConnectivity();
                break;
            case CONNECTED:
                // network obtained connection
                statusSubject.onNext(ConnectionStatus.CONNECTING);
                // start polling server connectivity
                startListeningServerConnectivity();
                break;
        }
    }

    private void startListeningServerConnectivity() {
        serverSubscription = ReactiveNetwork.observeInternetConnectivity(
                new SocketInternetObservingStrategy(), // checking socket connection to host
                0, // initial timeout (ms)
                CONNECTIVITY_INTERVAL_MS,
                BuildConfigProxy.getHost(),
                BuildConfigProxy.getPort(),
                CONNECTIVITY_TIMEOUT_MS,
                new DefaultErrorHandler()) // just log
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnServerConnected);
        }

    private void stopListeningServerConnectivity() {
        serverSubscription.unsubscribe();
    }

    private void doOnServerConnected(boolean connected) {
        if (connected) {
            // server connected
            statusSubject.onNext(ConnectionStatus.CONNECTED);
        } else if (statusSubject.getValue() == ConnectionStatus.CONNECTED) {
            // server disconnected but network did not signal disconnection
            statusSubject.onNext(ConnectionStatus.CONNECTING);
        } // else rely on general subscription
    }

}
