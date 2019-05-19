package com.rideaustin.engine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.GoOfflineParams;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.RiderLocationUpdate;
import com.rideaustin.api.model.UpgradeRequest;
import com.rideaustin.api.model.UpgradeRequestStatus;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.DriverStatus;
import com.rideaustin.api.model.surgearea.SurgeArea;
import com.rideaustin.api.model.surgearea.SurgeAreas;
import com.rideaustin.engine.state.AcceptedState;
import com.rideaustin.engine.state.ArrivedState;
import com.rideaustin.engine.state.InactiveState;
import com.rideaustin.engine.state.OfflinePollingState;
import com.rideaustin.engine.state.OnlineState;
import com.rideaustin.engine.state.PendingAcceptState;
import com.rideaustin.engine.state.TripEndedState;
import com.rideaustin.engine.state.TripStartedState;
import com.rideaustin.engine.state.UnauthorizedState;
import com.rideaustin.manager.ConnectionStatus;
import com.rideaustin.models.RideRating;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.AnswersUtils;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.PermissionUtils;
import com.rideaustin.utils.RetryWhenNoNetwork;
import com.rideaustin.utils.SingleSubject;

import java.util.Collections;
import java.util.List;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.api.model.UpgradeRequestStatus.REQUESTED;
import static com.rideaustin.api.model.UpgradeRequestStatus.SEND;
import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;

/**
 * Created by rost on 8/11/16.
 */
public class StateManager {

    private final BehaviorSubject<BaseEngineState> currentEngineState = BehaviorSubject.create();
    private final BehaviorSubject<BaseEngineState> uiCurrentState = BehaviorSubject.create();
    private final SingleSubject<GoOfflineParams> goOfflineEvent = SingleSubject.create();
    private final PublishSubject<Ride> cancelledRide = PublishSubject.create();
    private final PublishSubject<Event> destinationUpdateEvent = PublishSubject.create();
    private final PublishSubject<RiderLocationUpdate> riderLocation = PublishSubject.create();
    private final PublishSubject<List<SurgeArea>> surgeUpdateEvent = PublishSubject.create();
    private final BehaviorSubject<Optional<String>> riderCommentsSubject = BehaviorSubject.create(Optional.empty());
    private final BehaviorSubject<Optional<Ride>> currentRideSubject = BehaviorSubject.create(Optional.empty());
    private final BehaviorSubject<Optional<Ride>> nextRideSubject = BehaviorSubject.create(Optional.empty());

    private CompositeSubscription subscriptions;
    private CompositeSubscription motionSubscriptions;
    private Subscription syncWithServerSubscription = Subscriptions.unsubscribed();
    private Subscription unratedRideSubscription = Subscriptions.unsubscribed();
    private Subscription pendingRideRatingsSubscription = Subscriptions.unsubscribed();
    private ErrorListener listener;

    private boolean started = false;
    private volatile boolean needSyncWithServer = false;

    public void postCurrentRide(@Nullable Ride ride) {
        if (ride == null || !RideStatus.REQUESTED.toString().equals(ride.getStatus())) {
            Optional<Ride> optional = Optional.ofNullable(ride);
            currentRideSubject.onNext(optional);
            postNextRide(optional.map(Ride::getNextRide).orElse(null));
        }
    }

    public void postNextRide(@Nullable Ride ride) {
        nextRideSubject.onNext(Optional.ofNullable(ride));
    }

    public Observable<Optional<Ride>> getCurrentRide() {
        return currentRideSubject
                .serialize()
                .onBackpressureLatest();
    }

    public Observable<Optional<Ride>> getNextRide() {
        return nextRideSubject
                .serialize()
                .onBackpressureLatest();
    }

    public Observable<Boolean> cancelNextRide(@Nullable String code, @Nullable String reason) {
        Optional<Ride> ride = nextRideSubject.getValue();
        if (ride.isPresent()) {
            return App.getDataManager()
                    .declineRide(ride.get(), code, reason)
                    .doOnNext(aBoolean -> resetNextRide());
        }
        return Observable.empty();
    }

    public Optional<Ride> getNextRideIfAny() {
        return nextRideSubject.getValue();
    }

    public void processRiderCommentsUpdate(Ride ride) {
        Optional<String> oldComment = riderCommentsSubject.getValue();
        Optional<String> newComment = Optional.ofNullable(ride.getComment());
        if (!oldComment.equals(newComment)) {
            riderCommentsSubject.onNext(newComment);
        }
    }

    public Observable<Optional<String>> getRiderComments() {
        return riderCommentsSubject
                .filter(o -> shouldShowComment())
                .onBackpressureLatest()
                .asObservable();
    }

    public void clearRideComment() {
        if (riderCommentsSubject.getValue().isPresent()) {
            riderCommentsSubject.onNext(Optional.empty());
        }
    }

    public Observable<RiderLocationUpdate> getRiderLocationUpdates() {
        return riderLocation.asObservable()
                .onBackpressureLatest();
    }

    public void logout() {
        Timber.d("::logout::");
        switchState(createUnauthorizedState());
        EngineService.shutdown(App.getInstance());
    }

    public void postSurgeUpdate(Event event) {
        if (event.getEventType() == RideStatus.SURGE_AREA_UPDATES) {
            SurgeAreas surgeAreas = event.deserializeParams(SurgeAreas.class);
            if (surgeAreas != null) {
                surgeUpdateEvent.onNext(surgeAreas.getSurgeAreas());
            }
        } else if (event.getEventType() == RideStatus.SURGE_AREA_UPDATE) {
            SurgeArea surgeArea = event.deserializeParams(SurgeArea.class);
            if (surgeArea != null) {
                surgeUpdateEvent.onNext(Collections.singletonList(surgeArea));
            }
        }
    }

    public Observable<List<SurgeArea>> getSurgeUpdateObservable() {
        return surgeUpdateEvent.asObservable()
                .onBackpressureBuffer();
    }

    public void postOfflineEvent(@NonNull Event event) {
        GoOfflineParams params = event.deserializeParams(GoOfflineParams.class);
        if (params != null) {
            goOfflineEvent.onNext(params);
        }
    }

    public Observable<GoOfflineParams> getGoOfflineObservable() {
        return goOfflineEvent.asObservable()
                .onBackpressureBuffer()
                .filter(params -> params != null);
    }

    public void postDestinationUpdateEvent(Event event) {
        destinationUpdateEvent.onNext(event);
    }

    public Observable<Event> getDestinationUpdateObservable() {
        return destinationUpdateEvent.asObservable()
                .onBackpressureLatest();
    }

    public void postCancelledRide(Ride ride) {
        cancelledRide.onNext(ride);
    }

    public Observable<Ride> getCancelledRideObservable() {
        return cancelledRide.asObservable()
                .onBackpressureLatest()
                .distinctUntilChanged((r1, r2) ->
                        r1.getId().equals(r2.getId()) && r1.getStatus().equals(r2.getStatus()));
    }

    public void postRideUnrated() {
        checkUnratedRides();
    }

    public void postPendingRideRating() {
        if (pendingRideRatingsSubscription.isUnsubscribed()) {
            List<RideRating> ratings = App.getPrefs().getPendingRideRatings();
            if (!ratings.isEmpty()) {
                pendingRideRatingsSubscription = Observable.from(ratings)
                        .concatMap(rating -> App.getDataManager().getRidesService()
                                .rateRide(rating.getRideId(), rating.getRate(), AvatarType.DRIVER.name())
                                .retryWhen(new RetryWhenNoNetwork(5000))
                                .map(o -> rating)
                                .onErrorReturn(throwable -> rating))
                        .doOnNext(rating -> App.getPrefs().removePendingRideRating(rating))
                        .doOnCompleted(this::postPendingRideRating) // do it again to check new pending
                        .subscribe(rating -> {}, Timber::e);
            }
        }
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * @return true when upgrade request status is REQUESTED,
     * false if it is any state before and after.
     */
    public boolean isRideUpgradeStatusInRequested() {
        boolean upgradeRequestedByRide = getCurrentRideIfAny()
                .map(Ride::getUpgradeRequest)
                .map(UpgradeRequest::getStatus)
                .map(UpgradeRequestStatus::valueOf)
                .filter(upgradeRequestStatus -> upgradeRequestStatus == REQUESTED)
                .isPresent();
        if (upgradeRequestedByRide) {
            return true;
        }
        return getCurrentEngineStateIfAny()
                .filter(engineState -> engineState instanceof BaseStateWithRide)
                .filter(engineState -> {
                    UpgradeRequestStatus status = ((BaseStateWithRide) engineState).getUpgradeStatus().getStatus();
                    return status == REQUESTED || status == SEND;
                })
                .isPresent();
    }

    public void addMotionSubscription(Subscription subscription) {
        motionSubscriptions.add(subscription);
    }

    public void clearMotionSubscriptions() {
        motionSubscriptions.clear();
    }


    public void riderLocationUpdated(RiderLocationUpdate riderLocationUpdate) {
        riderLocation.onNext(riderLocationUpdate);
    }

    public interface ErrorListener {
        void onFatalError(Throwable ex);
    }

    public Observable<BaseEngineState> getUIStateObservable() {
        return uiCurrentState.asObservable()
                .filter(state -> state != null)
                .serialize()
                .onBackpressureBuffer();
    }

    public Observable<BaseEngineState> getEngineStateObservable() {
        return currentEngineState.asObservable()
                .filter(state -> state != null)
                .serialize()
                .onBackpressureBuffer();
    }

    public EngineState.Type getCurrentEngineStateType() {
        return getCurrentEngineStateIfAny().map(EngineState::getType).orElse(EngineState.Type.UNKNOWN);
    }

    public Optional<BaseEngineState> getCurrentEngineStateIfAny() {
        return Optional.ofNullable(currentEngineState.getValue());
    }

    public Optional<Ride> getCurrentRideIfAny() {
        return currentRideSubject.getValue();
    }

    public void setErrorListener(ErrorListener listener) {
        this.listener = listener;
    }

    public void start() {
        started = true;
        subscriptions = new CompositeSubscription();
        motionSubscriptions = new CompositeSubscription();
        subscriptions.add(getEngineStateObservable()
                .observeOn(RxSchedulers.computation())
                .scan(null, (Func2<BaseEngineState, BaseEngineState, BaseEngineState>) (previousState, nextState) -> {
                    if (previousState != null) {
                        previousState.deactivate();
                    }
                    nextState.activate(StateManager.this);
                    return nextState;
                })
                .filter(baseEngineState -> baseEngineState != null && !baseEngineState.isNotBindUI())
                .doOnNext(this::doOnEngineState)
                .observeOn(RxSchedulers.main())
                .subscribe(uiCurrentState::onNext));

        subscriptions.add(initialState()
                .subscribe(initialState -> {
                    Timber.d("restored state to: %s", initialState.getType());
                    currentEngineState.onNext(initialState);
                }, throwable -> {
                    Timber.e(throwable, "Unexpected exception");
                    if (listener != null) {
                        listener.onFatalError(throwable);
                    }
                }));

        subscriptions.add(getCurrentRide()
                .map(r -> r.map(Ride::getId).orElse(0L))
                .distinctUntilChanged()
                .subscribe(rideId -> App.getPrefs().setRideId(rideId)));

        subscriptions.add(App.getDataManager().getAppInfoObservable()
                .take(1)
                .subscribeOn(RxSchedulers.network())
                .subscribe(response -> {
                    if (AppInfoUtil.isMandatoryRequired(response) && AppInfoUtil.canShowUpdate()) {
                        EngineService.shutdown(App.getInstance());
                    }
                }, Timber::e));
        subscriptions.add(App.getInstance().getConnectionStatusManager()
                .getStatusObservable()
                .subscribe(status -> {
                    if (status == ConnectionStatus.DISCONNECTED && hasActiveRide()) {
                        // next time anyone will attempt to sync
                        // (most probably it will be LongPollingManager)
                        // this flag will force sync with server
                        // see syncOnPendingEvents()
                        needSyncWithServer = true;
                    }
                }));
    }

    public boolean hasActiveRide() {
        switch (getCurrentEngineStateType()) {
            case STARTED:
            case ARRIVED:
            case ACCEPTED:
            case ENDED:
                return true;
        }
        return false;
    }

    public void stop() {
        Timber.d("stop");
        started = false;
        subscriptions.unsubscribe();
        motionSubscriptions.unsubscribe();
        syncWithServerSubscription.unsubscribe();
        unratedRideSubscription.unsubscribe();
        pendingRideRatingsSubscription.unsubscribe();
        resetRides();
        clearState();
    }

    private void clearState() {
        currentEngineState.onNext(null);
        uiCurrentState.onNext(null);
        goOfflineEvent.onNext(null);
        clearRideComment();
    }

    public void switchState(BaseEngineState engineState) {
        Timber.d("switchingState: %s", engineState);
        currentEngineState.onNext(engineState);
    }

    public void switchToCorrectState() {
        if (syncWithServerSubscription.isUnsubscribed()) {
            // keep only one active sync (it may perform retries under the hood)
            syncWithServerSubscription = App.getInstance().getPendingEventsManager().retryToSend()
                    .flatMap(response -> syncOnPendingEvents(response, true))
                    .subscribe(changed -> {}, Timber::e);
        }
    }

    public void switchToStateBasedOnCurrentRide() {
        BaseEngineState state = getCurrentRideIfAny()
                .map(this::getStateFromRide)
                .orElse(createOnlineState());
        if (getCurrentEngineStateType() != state.getType()) {
            switchState(state);
        }
    }

    private void doOnEngineState(BaseEngineState engineState) {
        boolean hasRide = engineState instanceof BaseStateWithRide;
        if (hasRide && shouldShowComment()) {
            BaseStateWithRide state = (BaseStateWithRide) engineState;
            if (state.getRide() != null) {
                processRiderCommentsUpdate(state.getRide());
            }
        }
        if (hasRide) {
            BaseStateWithRide state = (BaseStateWithRide) engineState;
            postCurrentRide(state.getRide());
        } else {
            resetRides();
        }
        checkUnratedRides();
    }

    private boolean shouldShowComment() {
        switch (getCurrentEngineStateType()) {
            case ACCEPTED:
            case ARRIVED:
                return true;
        }
        return false;
    }

    private Observable<BaseEngineState> initialState() {
        if (!App.getDataManager().isAuthorised()) {
            return Observable.just(createUnauthorizedState());
        }

        return Observable.zip(fetchCurrentDriver(), fetchStateFromServer(), Pair::new)
                .observeOn(RxSchedulers.network())
                .map(pair -> getInitialState(pair.first, pair.second))
                .flatMap(baseEngineState -> {
                    if (baseEngineState.getType() == EngineState.Type.OFFLINE) {
                        return App.getDataManager().deactivateDriver()
                                .onErrorReturn(throwable -> {
                                    Timber.e(throwable, "Unable to go offline on initial state");
                                    return null;
                                })
                                .map(aVoid -> baseEngineState);
                    } else {
                        return Observable.just(baseEngineState);
                    }
                })
                .doOnNext(this::doOnInitialState)
                .observeOn(RxSchedulers.main());
    }

    private Observable<Driver> fetchCurrentDriver() {
        return App.getDataManager()
                .getOrLoadCurrentDriver()
                .retryWhen(new RetryWhenNoNetwork(5000))
                .onErrorReturn(throwable -> null);
    }

    private Observable<ActiveDriver> fetchActiveDriver() {
        return App.getDataManager().loadActiveDriver()
                .retryWhen(new RetryWhenNoNetwork(5000))
                .onErrorReturn(throwable -> null)
                .subscribeOn(RxSchedulers.network());
    }

    private Observable<BaseEngineState> fetchStateFromServer() {
        return fetchActiveDriver().map(this::getStateFromActiveDriver);
    }

    private BaseEngineState getInitialState(Driver driver, BaseEngineState serverState) {
        boolean locationPermissionGranted = PermissionUtils.isLocationPermissionGranted(App.getInstance());
        boolean driverIsActive = driver != null && driver.isActive();
        if (driverIsActive) {
            if (serverState.getType() == EngineState.Type.ONLINE && !locationPermissionGranted) {
                return createOfflinePoolingState();
            }
            return serverState;
        }
        return createInactiveState();
    }

    private void doOnInitialState(BaseEngineState state) {
        switch (state.getType()) {
            case OFFLINE:
            case ONLINE:
                subscriptions.add(App.getInstance()
                        .getPendingEventsManager()
                        .retryToSend()
                        .flatMap(this::syncOnPendingEvents)
                        .subscribe(changed -> {}, Timber::e));
        }
    }

    public Observable<Boolean> syncOnPendingEvents(PendingEventsResponse response) {
        return syncOnPendingEvents(response, false);
    }

    private Observable<Boolean> syncOnPendingEvents(PendingEventsResponse response,
                                                   boolean forceServerState) {

        boolean needServerState;
        if (forceServerState || needSyncWithServer) {
            // fetch if pending events sent or empty
            needServerState = response.isSuccessful();
        } else {
            // fetch only if pending events sent
            needServerState = response.getResult() == PendingEventsResponse.Result.SEND_SUCCEEDED;
        }
        if (needServerState) {
            needSyncWithServer = false;
            return fetchStateFromServer().map(state -> {
                if (shouldSwitchStateOnSync(getCurrentEngineStateType(), state.getType())) {
                    switchState(state);
                    return true;
                }
                return false;
            });
        } else if (response.getResult() == PendingEventsResponse.Result.SEND_FAILED) {
            return Observable.fromCallable(() -> {
                if (getCurrentEngineStateType() == EngineState.Type.ONLINE) {
                    // current state is ONLINE but pending events failed
                    // switch to OFFLINE state and ask driver to sync
                    switchState(createOfflinePoolingState());
                    return true;
                }
                return false;
            });
        } else {
            return Observable.just(false);
        }
    }

    private boolean shouldSwitchStateOnSync(EngineState.Type local, EngineState.Type remote) {
        if (local != remote) {
            if (local == EngineState.Type.ENDED) {
                // switch only if there is a ride
                return remote == EngineState.Type.PENDING_ACCEPT
                        || remote == EngineState.Type.ACCEPTED
                        || remote == EngineState.Type.ARRIVED
                        || remote == EngineState.Type.STARTED;
            }
            return true;
        }
        return false;
    }

    private void resetNextRide() {
        currentRideSubject.getValue().ifPresentOrElse(ride -> {
            ride.setNextRide(null);
            postCurrentRide(ride);
        }, () -> {
            postNextRide(null);
        });
    }

    private void resetRides() {
        postCurrentRide(null);
    }

    private void checkUnratedRides() {
        if (canShowUnratedRide() && unratedRideSubscription.isUnsubscribed()) {
            List<Long> unratedRides= App.getPrefs().getUnratedRides();
            if (!unratedRides.isEmpty()) {
                long rideId = unratedRides.get(0);
                unratedRideSubscription = App.getDataManager()
                        .loadRideStatus(rideId)
                        .retryWhen(new RetryWhenNoNetwork(5000))
                        .observeOn(RxSchedulers.main())
                        .subscribe(ride -> {
                            if (canShowUnratedRide()
                                    && ride != null
                                    && RideStatus.valueOf(ride.getStatus()) == RideStatus.COMPLETED) {
                                switchState(createTripEndedState(ride));
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                            if (!PendingEventsManager.shouldSavePendingEvent(throwable)) {
                                // something wrong with unrated ride, skip it and check next
                                App.getPrefs().removeUnratedRide(rideId);
                                checkUnratedRides();
                            }
                        });
            }
        }
    }

    private boolean canShowUnratedRide() {
        switch (getCurrentEngineStateType()) {
            case OFFLINE:
            case ONLINE:
                return true;
            default:
                return false;
        }
    }

    public InactiveState createInactiveState() {
        return new InactiveState();
    }

    public OnlineState restoreOnlineState() {
        return new OnlineState();
    }

    public OnlineState createOnlineState() {
        return new OnlineState();
    }

    public OfflinePollingState createOfflinePoolingState() {
        return new OfflinePollingState();
    }

    public UnauthorizedState createUnauthorizedState() {
        return new UnauthorizedState();
    }

    public PendingAcceptState createPendingAcceptState(AcceptData data) {
        return new PendingAcceptState(data);
    }

    public AcceptedState createAcceptedState(Ride ride) {
        return new AcceptedState(ride);
    }

    public ArrivedState createArrivedState(Ride ride) {
        return new ArrivedState(ride);
    }

    public TripStartedState createTripStartedState(Ride ride) {
        return new TripStartedState(ride);
    }

    public TripEndedState createTripEndedState(Ride ride) {
        return new TripEndedState(ride);
    }

    /**
     * Try to obtain current engine state using {@code ride} parameter.
     *
     * @param ride server ride used to detect state
     * @return engine state
     */
    private BaseEngineState getStateFromRide(@Nullable Ride ride) {
        if (ride != null) {
            switch (RideStatus.valueOf(ride.getStatus())) {
                case DRIVER_ASSIGNED:
                    return createAcceptedState(ride);
                case DRIVER_REACHED:
                    return createArrivedState(ride);
                case ACTIVE:
                    return createTripStartedState(ride);
                case COMPLETED:
                    return createTripEndedState(ride);
            }
        }
        Exception e = new Exception(UNEXPECTED_STATE_KEY);
        Timber.e(e, "Driver is riding but ride is " + ride);
        return createOnlineState();
    }

    /**
     * Try to obtain current engine state using {@code activeDriver} parameter.
     *
     * @param activeDriver server active driver used to detect state
     * @return engine state
     */
    private BaseEngineState getStateFromActiveDriver(@Nullable ActiveDriver activeDriver) {
        if (activeDriver != null && !TextUtils.isEmpty(activeDriver.getStatus())) {
            switch (DriverStatus.valueOf(activeDriver.getStatus())) {
                case AVAILABLE:
                case AWAY: // iOS treat this as online
                    return createOnlineState();
                case REQUESTED:
                    return getCurrentEngineStateIfAny().orElse(createOnlineState());
                case RIDING:
                    if (activeDriver.getRide() != null) {
                        postCurrentRide(activeDriver.getRide());
                        return getStateFromRide(activeDriver.getRide());
                    } else {
                        Exception e = new Exception(UNEXPECTED_STATE_KEY);
                        Timber.e(e, "Driver is riding but no ride attached: activeDriver=" + activeDriver);
                        return createOnlineState();
                    }
            }
        }
        return createOfflinePoolingState();
    }
}
