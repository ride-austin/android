package com.rideaustin.engine.state;

import com.rideaustin.App;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.engine.BaseStateWithRide;
import com.rideaustin.engine.DriverTrackingType;
import com.rideaustin.engine.PendingEventsManager;
import com.rideaustin.engine.SwitchNextData;
import com.rideaustin.models.RideRating;
import com.rideaustin.schedulers.RxSchedulers;

import rx.Observable;

/**
 * Created by rost on 8/15/16.
 */
public class TripEndedState extends BaseStateWithRide {

    private float rate = 0L;

    public TripEndedState(Ride ride) {
        super(ride, DriverTrackingType.ONLINE);
    }

    @Override
    public Type getType() {
        return Type.ENDED;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public float getRate() {
        return rate;
    }

    @Override
    public Observable<?> switchNext(SwitchNextData data) {
        return getDataManager().getRidesService()
                .rateRide(getRide().getId(), rate, AvatarType.DRIVER.name())
                .subscribeOn(RxSchedulers.network())
                .doOnError(throwable -> {
                    if (PendingEventsManager.shouldSavePendingEvent(throwable)) {
                        App.getPrefs().addPendingRideRating(new RideRating(getRide().getId(), rate));
                        App.getInstance().getStateManager().postPendingRideRating();
                    }
                    App.getPrefs().removeUnratedRide(getRide().getId());
                    getStateManager().getNextRideIfAny()
                            // switch to next ride by default only if previous state was STARTED
                            // otherwise, we got here after checking unrated ride (from OFFLINE/ONLINE)
                            // In this situation, its better to switch to OFFLINE and sync.
                            .filter(r -> getPreviousType().orElse(Type.UNKNOWN) == Type.STARTED)
                            .ifPresentOrElse(ride -> {
                                switchState(getStateManager().createAcceptedState(ride));
                            }, () -> {
                                switchState(getStateManager().createOfflinePoolingState());
                            });
                    switchToCorrectState();
                })
                .doOnNext(o -> {
                    App.getPrefs().removeUnratedRide(getRide().getId());
                    switchState(getStateManager().createOnlineState());
                    switchToCorrectState();
                });
    }
}
