package com.rideaustin.engine;

import android.location.Location;
import android.support.annotation.VisibleForTesting;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.api.model.events.PendingEvent;
import com.rideaustin.api.model.events.PendingEventType;
import com.rideaustin.api.model.events.PendingEvents;
import com.rideaustin.base.retrofit.RetrofitException;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.DeviceInfoUtil;
import com.rideaustin.utils.NetworkHelper;
import com.rideaustin.utils.RetryWithDelay;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java8.util.Optional;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

import static com.rideaustin.engine.PendingEventsResponse.Result.NO_EVENTS;
import static com.rideaustin.engine.PendingEventsResponse.Result.SEND_FAILED;
import static com.rideaustin.engine.PendingEventsResponse.Result.SEND_SUCCEEDED;
import static com.rideaustin.utils.CommonConstants.NETWORK_TIMEOUT_IN_SECONDS;

/**
 * Created by hatak on 28.04.2017.
 */

public class PendingEventsManager {

    private static final String RIDE_IS_ALREADY_ENDED_OR_CANCELED = "Ride is already ended or canceled.";
    private static final String RIDE_NOT_FOUND = "Ride not found";

    private PendingEvents events;
    private BehaviorSubject<Boolean> sending = BehaviorSubject.create(false);
    private volatile boolean hasEvents;

    public PendingEventsManager() {
        loadFromStorage();
    }

    public Observable<PendingEventsResponse> tryToSend() {
        return Observable.fromCallable(this::send)
                .subscribeOn(DeviceInfoUtil.isEspresso() ? Schedulers.io() : RxSchedulers.network())
                .doOnNext(this::doOnResponse)
                .doOnSubscribe(() -> sending.onNext(hasUnsent()))
                .doOnUnsubscribe(() -> sending.onNext(false));
    }

    public Observable<PendingEventsResponse> retryToSend() {
        return tryToSend()
                .flatMap(response -> {
                    if (response.getResult() == SEND_FAILED && canRetry(response.getException())) {
                        return Observable.error(response.getException());
                    }
                    return Observable.just(response);
                })
                .retryWhen(new RetryWithDelay(1000));
    }

    private synchronized PendingEventsResponse send() {
        final boolean hasPendingEvents = hasUnsent();
        Timber.d("::: pending events present: " + hasPendingEvents);
        if (hasPendingEvents) {
            try {
                App.getDataManager().getEventsService()
                        .sendPendingEvents(events)
                        .take(NETWORK_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                        .toBlocking()
                        .first();

                List<PendingEvent> eventsList = events.getList();
                PendingEventsResponse pendingEventsResponse = new PendingEventsResponse(SEND_SUCCEEDED, null, Optional.of(eventsList));
                removeExisting();
                Timber.d("::: pending events sent");
                return pendingEventsResponse;
            } catch (Exception e) {
                Timber.d("::: pending events send failed: " + e.getLocalizedMessage());
                String message = e.getMessage();
                Timber.e(e, "Pending events failed against the message '%s'", message);
                // I know how it look like but there is no other solution since there is
                // possibility to distinguish server errors
                // it's a fail safe for case when ride is ended but there are pending
                // event's not sent due to previous errors - ended ride can't be updated
                if (message != null && (message.contains(RIDE_IS_ALREADY_ENDED_OR_CANCELED) || message.contains(RIDE_NOT_FOUND))) {
                    Timber.d("::: pending events deleting due to fact ride is already completed");
                    removeExisting();
                }
                // Let stream decide what to do with this error:
                // a) prevent driver from going online and show error toast
                // b) proceed with state change, but discard any subsequent calls to API (ex. END_RIDE)
                // c) proceed with state change, ignoring it
                // d) just ignoring it
                return new PendingEventsResponse(SEND_FAILED, e, Optional.empty());
            }
        } else {
            return new PendingEventsResponse(NO_EVENTS, null, Optional.empty());
        }
    }

    public boolean hasUnsent() {
        return hasEvents ;
    }

    public boolean isSending() {
        return sending.getValue();
    }

    public Observable<Boolean> isSendingObservable() {
        return sending.serialize().onBackpressureLatest().asObservable();
    }

    private void removeExisting() {
        events = new PendingEvents();
        saveOnStorage(events);
    }

    private boolean canRetry(Throwable throwable) {
        return NetworkHelper.causedByNetwork(throwable) || throwable instanceof TimeoutException;
    }

    /**
     * +     * For event type {@link PendingEventType#END_RIDE} Location must exist
     * +
     */
    public synchronized void record(@PendingEventType String eventType, final long rideId, LatLng... location) {
        Timber.d("::: pending event recorded ::: " + eventType);
        if (eventType.equals(PendingEventType.DRIVER_REACHED) ||
                eventType.equals(PendingEventType.START_RIDE)) {
            events.add(PendingEvent.create(eventType, rideId));
        } else if (eventType.equals(PendingEventType.END_RIDE)) {
            events.add(PendingEvent.createWithEndLocation(eventType, rideId, location[0]));
        }
        saveOnStorage(events);
    }

    public synchronized void record(@PendingEventType String eventType, Location location) {
        Timber.d("::: pending event recorded ::: " + eventType);
        if (eventType.equals(PendingEventType.UPDATE_LOCATION)) {
            // record location updates only when driver started trip
            if (App.getInstance().getStateManager().getCurrentEngineStateType() == EngineState.Type.STARTED) {
                events.add(PendingEvent.createWithLocation(eventType, App.getPrefs().getRideId(), location));
                saveOnStorage(events);
            }
        }
    }

    @VisibleForTesting
    public synchronized void loadFromStorage() {
        events = App.getPrefs().loadPendingEvents();
        hasEvents = !events.isEmpty();
    }

    @VisibleForTesting
    public synchronized PendingEvents getEvents() {
        return events;
    }

    private void saveOnStorage(final PendingEvents pendingEvents) {
        App.getPrefs().savePendingEvents(pendingEvents);
        hasEvents = !events.isEmpty();
    }

    private void doOnResponse(PendingEventsResponse response) {
        if (response.getResult() == SEND_SUCCEEDED) {
            boolean hasEndRide = false;
            for (PendingEvent event : response.getSendEvents()) {
                if (PendingEventType.END_RIDE.equals(event.getPendingEventType())) {
                    hasEndRide = true;
                    App.getPrefs().addUnratedRide(event.getRideId());
                }
            }
            if (hasEndRide) {
                App.getInstance().getStateManager().postRideUnrated();
            }
        }
    }

    public static boolean shouldSavePendingEvent(Throwable throwable) {
        if (throwable instanceof RetrofitException) {
            RetrofitException retrofitException = (RetrofitException) throwable;
            final int statusCode = retrofitException.getStatusCode();
            if (statusCode == 400) {
                // something wrong with current request (most probably its illegal)
                return false;
            }
        }
        // otherwise, save pending event on any error
        return true;
    }

}
