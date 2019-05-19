package com.rideaustin.engine;

import com.rideaustin.api.DataManager;
import com.rideaustin.api.model.Event;
import com.rideaustin.engine.state.InactiveState;
import com.rideaustin.engine.state.UnauthorizedState;
import com.rideaustin.utils.RetryWithDelay;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * Created on 23/11/2017
 *
 * @author sdelaysam
 */

public class LongPollingManager {

    private final StateManager stateManager;
    private final PendingEventsManager pendingEventsManager;
    private DataManager dataManager;

    private PublishSubject<Event> publishSubject = PublishSubject.create();
    private List<Event> unconsumedEvents = new ArrayList<>();
    private PublishSubject<Throwable> errorSubject = PublishSubject.create();

    private Subscription stateSubscription = Subscriptions.empty();
    private Subscription eventSubscription = Subscriptions.unsubscribed();

    public LongPollingManager(DataManager dataManager,
                              StateManager stateManager,
                              PendingEventsManager pendingEventsManager) {
        this.dataManager = dataManager;
        this.stateManager = stateManager;
        this.pendingEventsManager = pendingEventsManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void start() {
        stateSubscription.unsubscribe();
        stateSubscription = stateManager.getEngineStateObservable()
                .subscribe(state -> {
                    if (state instanceof InactiveState || state instanceof UnauthorizedState) {
                        stopListeningServerEvents();
                    } else {
                        startListeningServerEvents();
                    }
                });
    }

    public void stop() {
        stopListeningServerEvents();
        stateSubscription.unsubscribe();
    }

    public Observable<Event> getEvents() {
        return publishSubject
                .onBackpressureBuffer()
                .asObservable()
                .startWith(getUnconsumedEvents());
    }

    public Observable<Throwable> getErrors() {
        return errorSubject
                .serialize()
                .onBackpressureLatest()
                .asObservable();
    }

    private Observable<Event> getUnconsumedEvents() {
        synchronized (this) {
            if (unconsumedEvents.isEmpty()) {
                return Observable.empty();
            }
        }
        return Observable.from(unconsumedEvents).doOnCompleted(() -> unconsumedEvents.clear());
    }

    private void startListeningServerEvents() {
        if (!eventSubscription.isUnsubscribed() || !dataManager.isLoggedIn()) {
            return;
        }
        eventSubscription = checkPendingEvents()
                .concatWith(getDataEvents())
                .doOnError(errorSubject::onNext)
                .repeatWhen(observable -> observable)
                .retryWhen(new RetryWithDelay(5000, Integer.MAX_VALUE))
                .filter(event -> event != null)
                .subscribe(this::publishEvent, throwable -> {
                    Timber.e(throwable);
                    startListeningServerEvents();
                });
    }

    private void stopListeningServerEvents() {
        eventSubscription.unsubscribe();
    }

    private void publishEvent(Event event) {
        if (publishSubject.hasObservers()) {
            publishSubject.onNext(event);
        } else {
            synchronized (this) {
                unconsumedEvents.add(event);
            }
        }
    }

    private Observable<Event> checkPendingEvents() {
        return pendingEventsManager
                .tryToSend()
                .flatMap(response -> {
                    switch (response.getResult()) {
                        case SEND_FAILED:
                            return Observable.error(response.getException());
                        default :
                            // if pending event sent or empty
                            // take an attempt to sync before listening LP events
                            // Let StateManager decide, whether or not to sync with server
                            return stateManager.syncOnPendingEvents(response)
                                    .flatMap(changed -> Observable.empty());

                    }
                });
    }

    private Observable<Event> getDataEvents() {
        return Observable.defer(() -> dataManager.getEvents());
    }
}
