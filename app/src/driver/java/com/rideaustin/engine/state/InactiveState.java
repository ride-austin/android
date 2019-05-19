package com.rideaustin.engine.state;

import com.rideaustin.engine.BaseEngineState;
import com.rideaustin.engine.DriverTrackingType;
import com.rideaustin.engine.SwitchNextData;
import com.rideaustin.schedulers.RxSchedulers;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import timber.log.Timber;

/**
 * Created by kshumelchyk on 8/16/16.
 */
public class InactiveState extends BaseEngineState {
    private static final long CHECK_DRIVER_IS_ACTIVE_INTERVAL_S = 10;

    public InactiveState() {
        super(DriverTrackingType.NONE);
    }

    @Override
    public Observable<Object> switchNext(SwitchNextData data) {
        // Inactive state have no switch option, so this code never executed
        return Observable.defer(() -> {
            switchState(getStateManager().createOfflinePoolingState());
            return null;
        });
    }

    @Override
    public Type getType() {
        return Type.INACTIVE;
    }

    @Override
    protected void onActivated() {
        super.onActivated();
        final Subscription subscription = getDataManager()
                .checkIsDriverActivePeriodically(CHECK_DRIVER_IS_ACTIVE_INTERVAL_S, TimeUnit.SECONDS)
                .retry()
                .doOnError(throwable -> Timber.d(throwable, "error during driver check"))
                .observeOn(RxSchedulers.main())
                .subscribe(isActive -> {
                    Timber.d("is driver active " + isActive);
                    if (isActive) {
                        switchState(getStateManager().createOfflinePoolingState());
                    }
                });

        subscribeUntilDeactivated(subscription);
    }
}
