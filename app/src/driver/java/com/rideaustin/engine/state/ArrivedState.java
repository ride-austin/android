package com.rideaustin.engine.state;

import android.support.annotation.Nullable;

import com.rideaustin.App;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.events.PendingEventType;
import com.rideaustin.engine.BaseStateWithRide;
import com.rideaustin.engine.DriverTrackingType;
import com.rideaustin.engine.PendingEventsManager;
import com.rideaustin.engine.SwitchNextData;
import com.rideaustin.schedulers.RxSchedulers;

import rx.Observable;

import static com.rideaustin.utils.Constants.DIRECTION_KEY;

/**
 * Created by rost on 8/15/16.
 */
public class ArrivedState extends BaseStateWithRide {

    public ArrivedState(Ride ride) {
        super(ride, DriverTrackingType.ON_TRIP);
    }

    @Override
    public Observable<Object> switchNext(SwitchNextData data) {
        return getDataManager().startRide(getRide())
                .observeOn(RxSchedulers.main())
                .onErrorResumeNext(throwable -> {
                    if (PendingEventsManager.shouldSavePendingEvent(throwable)) {
                        App.getInstance().getPendingEventsManager().record(PendingEventType.START_RIDE, getRide().getId());
                        return Observable.just(null);
                    }
                    switchToCorrectState();
                    return Observable.error(throwable);
                })
                .doOnCompleted(() -> {
                    Ride ride = getRide();
                    ride.setStatus(RideStatus.ACTIVE.toString());
                    switchState(getStateManager().createTripStartedState(ride));
                });
    }

    @Override
    public Type getType() {
        return Type.ARRIVED;
    }

    public Observable<?> cancel(@Nullable String code, @Nullable String reason) {
        return getDataManager().declineRide(getRide(), code, reason)
                .observeOn(RxSchedulers.main())
                .doOnCompleted(() -> switchState(getStateManager().restoreOnlineState()));
    }

    @Override
    protected void onDeactivated() {
        super.onDeactivated();
        getSerializer().remove(DIRECTION_KEY);
    }
}
