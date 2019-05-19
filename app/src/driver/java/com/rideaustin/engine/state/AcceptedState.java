package com.rideaustin.engine.state;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.events.PendingEventType;
import com.rideaustin.engine.BaseStateWithRide;
import com.rideaustin.engine.Direction;
import com.rideaustin.engine.DriverTrackingType;
import com.rideaustin.engine.PendingEventsManager;
import com.rideaustin.engine.SwitchNextData;
import com.rideaustin.manager.location.RALocation;
import com.rideaustin.schedulers.RxSchedulers;

import rx.Observable;
import timber.log.Timber;

import static com.rideaustin.utils.Constants.DIRECTION_KEY;


/**
 * Created by rost on 8/12/16.
 */
public class AcceptedState extends BaseStateWithRide {

    public AcceptedState(Ride ride) {
        super(ride, DriverTrackingType.ON_TRIP);
    }

    @Override
    public Observable<Object> switchNext(SwitchNextData data) {
        return getDataManager().reachedRide(getRide())
                .observeOn(RxSchedulers.main())
                .onErrorResumeNext(throwable -> {
                    if (PendingEventsManager.shouldSavePendingEvent(throwable)) {
                        App.getInstance().getPendingEventsManager().record(PendingEventType.DRIVER_REACHED, getRide().getId());
                        return Observable.just(null);
                    }
                    switchToCorrectState();
                    return Observable.error(throwable);
                })
                .doOnCompleted(() -> {
                    Ride ride = getRide();
                    ride.setStatus(RideStatus.DRIVER_REACHED.toString());
                    switchState(getStateManager().createArrivedState(ride));
                });
    }

    @Override
    public Observable<Direction> getDirection() {
        return getSerializer().readRx(DIRECTION_KEY, Direction.class)
                .subscribeOn(RxSchedulers.computation())
                .switchIfEmpty(loadDirectionToRider())
                .observeOn(RxSchedulers.main());
    }

    public Observable<RALocation> listenToDriverLocation() {
        return getDriverLocationManager().getLocationUpdates();
    }

    private Observable<Direction> loadDirectionToRider() {
        Timber.d("::loadDirectionToRider:: begin");

        return getDriverLocationManager().getLastLocation(true, true)
                .switchMap(raLocation -> {
                    LatLng endPoint = new LatLng(getRide().getStartLocationLat(), getRide().getStartLocationLong());
                    Timber.d("::loadDirectionToRider:: %s %s", raLocation.getLocation(), endPoint);
                    final LatLng startPoint = raLocation.getCoordinates();
                    return getDataManager().loadDirection(startPoint, endPoint);
                });

    }

    @Override
    public Type getType() {
        return Type.ACCEPTED;
    }

    @Override
    protected void onDeactivated() {
        super.onDeactivated();
        getSerializer().remove(DIRECTION_KEY);
    }

    public Observable<?> cancel(@Nullable String code, @Nullable String reason) {
        return getDataManager().declineRide(getRide(), code, reason)
                .observeOn(RxSchedulers.main())
                .doOnCompleted(() -> switchState(getStateManager().restoreOnlineState()));
    }
}
