package com.rideaustin.engine.state;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.events.PendingEventType;
import com.rideaustin.engine.BaseStateWithRide;
import com.rideaustin.engine.DriverTrackingType;
import com.rideaustin.engine.PendingEventsManager;
import com.rideaustin.engine.SwitchNextData;
import com.rideaustin.manager.LocationNotAvailableException;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.map.DistanceTracker;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.manager.LocationNotAvailableException.Reason.LOCATION_IS_OFF;
import static com.rideaustin.utils.Constants.DIRECTION_KEY;


/**
 * Created by rost on 8/12/16.
 */
public class TripStartedState extends BaseStateWithRide {

    private LatLng endPoint;
    private Subscription destinationChangedSubscription = Subscriptions.empty();
    private Subscription reachedDestinationSubscription = Subscriptions.empty();

    public TripStartedState(Ride ride) {
        super(ride, DriverTrackingType.ON_TRIP);
    }

    @Override
    public Type getType() {
        return Type.STARTED;
    }

    @Override
    public Observable<?> switchNext(SwitchNextData data) {
        return getDriverLocationManager()
                .getLastLocation(true, true)
                // execute below code on network thread
                .observeOn(RxSchedulers.network())
                .switchMap(driverLocation -> {
                    endPoint = driverLocation.getCoordinates();
                    if (data.isOk()) {
                        // try to end ride if pending events sent or empty
                        return getDataManager().getRidesService().endRide(getRide().getId(), endPoint.latitude, endPoint.longitude);
                    } else {
                        // propagate error because pending events failed
                        return Observable.error(data.getException());
                    }
                })
                .onErrorResumeNext(throwable -> {
                    if (endPoint == null) {
                        // ask user to turn location on
                        return Observable.error(new LocationNotAvailableException(LOCATION_IS_OFF));
                    } else if (PendingEventsManager.shouldSavePendingEvent(throwable)) {
                        // save pending event and send driver offline
                        // pending events would be force sent on driver's next attempt to go online
                        App.getInstance().getPendingEventsManager().record(PendingEventType.END_RIDE, getRide().getId(), endPoint);
                        return Observable.just(getRide());
                    } else {
                        switchToCorrectState();
                        return Observable.error(throwable);
                    }
                })
                // execute below code on main
                .observeOn(RxSchedulers.main())
                .doOnNext(ride -> {
                    if (RideStatus.COMPLETED.name().equals(ride.getStatus())) {
                        switchState(getStateManager().createTripEndedState(ride));
                    } else {
                        getStateManager().getNextRideIfAny()
                                .ifPresentOrElse(nextRide -> {
                                    switchState(getStateManager().createAcceptedState(nextRide));
                                    switchToCorrectState();
                                }, () -> {
                                    switchState(getStateManager().createOfflinePoolingState());
                                });
                    }
                    getSerializer().remove(DIRECTION_KEY);
                });
    }

    @Override
    protected void onActivated() {
        super.onActivated();
        subscribeUntilDeactivated(App.getInstance()
                .getVisibilityObservable()
                .subscribe(state -> {
                    switch (state) {
                        case BACKGROUND:
                            monitorCurrentLocationAgainstDestination();
                            listenForDestinationChanges();
                            break;
                        case FOREGROUND:
                            destinationChangedSubscription.unsubscribe();
                            reachedDestinationSubscription.unsubscribe();
                            break;
                    }
                }));
    }

    @Override
    protected void onDeactivated() {
        super.onDeactivated();
    }

    private void listenForDestinationChanges() {
        destinationChangedSubscription.unsubscribe();
        destinationChangedSubscription = App.getInstance().getStateManager()
                .getDestinationUpdateObservable()
                .observeOn(RxSchedulers.computation())
                .subscribe(event -> monitorCurrentLocationAgainstDestination());
        subscribeUntilDeactivated(destinationChangedSubscription);
    }

    private void monitorCurrentLocationAgainstDestination() {
        reachedDestinationSubscription.unsubscribe();
        DistanceTracker distanceTracker = new DistanceTracker();
        distanceTracker.setDestinationLocation(getNavigationLocation());
        distanceTracker.setDestinationReachedListener(() -> {
            App.getNotificationManager().notifyDestinationReached();
            reachedDestinationSubscription.unsubscribe();
        });
        reachedDestinationSubscription = App.getLocationManager().getLocationUpdates()
                .observeOn(RxSchedulers.computation())
                .subscribe(raLocation -> {
                    distanceTracker.updateDriverLocation(raLocation.getCoordinates());
                }, throwable -> {
                    Timber.e(throwable, "error wile monitoring if reached destination");
                });
        subscribeUntilDeactivated(reachedDestinationSubscription);
    }
}
