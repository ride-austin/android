package com.rideaustin.engine.state;


import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.Rider;
import com.rideaustin.engine.AcceptData;
import com.rideaustin.engine.BaseStateWithRide;
import com.rideaustin.engine.DriverTrackingType;
import com.rideaustin.engine.SwitchNextData;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.RetryWhenNoNetwork;
import com.rideaustin.utils.TimeUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * Created by rost on 8/12/16.
 */
public class PendingAcceptState extends BaseStateWithRide {
    private final AcceptData pendingAcceptData;
    private BehaviorSubject<Long> acceptCounter = BehaviorSubject.create();
    private BehaviorSubject<Boolean> isSending = BehaviorSubject.create(false);

    public Observable<Long> getAcceptCounter() {
        return acceptCounter;
    }

    public PendingAcceptState(AcceptData acceptData) {
        super(acceptData.getPendingRide(), DriverTrackingType.ONLINE);
        this.pendingAcceptData = acceptData;
    }

    public Observable<Object> switchNext(SwitchNextData data) {
        return getDataManager().acceptRide(pendingAcceptData.getPendingRide())
                .doOnSubscribe(() -> isSending.onNext(true))
                .observeOn(RxSchedulers.main())
                .doOnError(this::doOnSwitchError)
                .doOnCompleted(this::doOnSwitchComplete);
    }

    private void doOnSwitchError(Throwable throwable) {
        Timber.e(throwable);
        if (isStackedRide()) {
            getStateManager().switchToStateBasedOnCurrentRide();
        } else {
            switchState(getStateManager().restoreOnlineState());
        }
        // anyway, check correct state
        switchToCorrectState();
    }

    private void doOnSwitchComplete() {
        if (isStackedRide()) {
            getCurrentRide().setNextRide(getPendingRide());
            getStateManager().switchToStateBasedOnCurrentRide();
            // need to fetch current ride and sync state
            switchToCorrectState();
        } else {
            Ride ride = pendingAcceptData.getPendingRide();
            ride.setStatus(RideStatus.DRIVER_ASSIGNED.toString());
            switchState(getStateManager().createAcceptedState(ride));
        }
    }

    private void doOnCancel() {
        if (isStackedRide()) {
            getStateManager().switchToStateBasedOnCurrentRide();
        } else {
            switchState(getStateManager().restoreOnlineState());
        }
    }

    private void declineOnServer() {
        App.getDataManager()
                .getRidesService()
                .declineRide(getPendingRide().getId())
                .retryWhen(new RetryWhenNoNetwork(1000))
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(o -> {
                    Timber.d("::Ride declined::");
                    switchToCorrectState();
                }, throwable -> {
                    Timber.e(throwable, "::Ride decline failed::");
                    switchToCorrectState();
                });
    }

    public Observable<Object> cancel() {
        return Observable.fromCallable(() -> {
            doOnCancel();
            declineOnServer();
            return true;
        });
    }

    public Observable<Void> goOffline() {
        if (isStackedRide()) {
            return Observable.empty();
        }
        declineOnServer();
        return getDataManager().deactivateDriver()
                .doOnSubscribe(() -> isSending.onNext(true))
                .observeOn(RxSchedulers.main())
                .doOnCompleted(() -> switchState(getStateManager().createOfflinePoolingState()));
    }

    @Override
    public Type getType() {
        return Type.PENDING_ACCEPT;
    }

    public boolean isStackedRide() {
        return pendingAcceptData.isStacked();
    }

    public Ride getPendingRide() {
        return pendingAcceptData.getPendingRide();
    }

    public Ride getCurrentRide() {
        return pendingAcceptData.getCurrentRide();
    }

    public Observable<Boolean> getSending() {
        return isSending.asObservable().onBackpressureLatest();
    }

    public Rider getRider() {
        return pendingAcceptData.getPendingRide().getRider();
    }

    public Observable<String> getStartAddress() {
        if (pendingAcceptData == null || pendingAcceptData.getPendingRide() == null) {
            return Observable.just("");
        }
        Ride ride = pendingAcceptData.getPendingRide();
        if (ride.hasStartAddress()) {
            return Observable.just(ride.getStartAddressText());
        }
        //TODO: handle if we can't reverse geocode
        final LatLng latLng = new LatLng(ride.getStartLocationLat(), ride.getStartLocationLong());
        return getDataManager().loadAddress(latLng)
                .observeOn(RxSchedulers.serializer())
                .doOnNext(s -> pendingAcceptData.getPendingRide().setStartAddress(s))
                .observeOn(RxSchedulers.main());
    }


    @Override
    protected void onActivated() {
        super.onActivated();
        final long timeLeftS = calcTimeLeft();

        if (timeLeftS <= 0) {
            requestNotBindUI();
            doOnCancel();
            switchToCorrectState();
            return;
        }

        subscribeUntilDeactivated(Observable.interval(0, 1, TimeUnit.SECONDS)
                .takeWhile(counter -> counter < timeLeftS && !isSending.getValue())
                .observeOn(RxSchedulers.main())
                .map(counter -> Math.max(0, timeLeftS - counter))
                .doOnCompleted(() -> {
                    if (!isSending.getValue()) {
                        doOnCancel();
                        switchToCorrectState();
                    }
                })
                .doOnSubscribe(() -> App.getInstance().getSoundManager().playAcceptPendingTone())
                .doOnUnsubscribe(() -> App.getInstance().getSoundManager().stopAcceptPendingTone())
                .subscribe(acceptCounter));
    }

    private long calcTimeLeft() {
        if (pendingAcceptData != null) {
            return (pendingAcceptData.getAcceptExpiration() - TimeUtils.currentTimeMillis()) / 1000;
        }
        return -1;
    }
}
