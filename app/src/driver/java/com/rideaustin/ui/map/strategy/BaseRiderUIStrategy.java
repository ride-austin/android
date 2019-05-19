package com.rideaustin.ui.map.strategy;

import android.content.DialogInterface;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.RideSpecificUpgradeStatus;
import com.rideaustin.api.model.UpgradeRequestStatus;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.engine.BaseStateWithRide;
import com.rideaustin.engine.Direction;
import com.rideaustin.engine.EngineState;
import com.rideaustin.engine.SwitchNextData;
import com.rideaustin.manager.location.RALocation;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.map.MapFragmentInterface;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.location.DistanceUtil;

import rx.Observable;
import rx.Subscription;
import timber.log.Timber;

/**
 * Created by ridedriverandroid on 23.09.2016.
 */

abstract class BaseRiderUIStrategy<T extends BaseStateWithRide> extends BaseUIStrategy<T> {

    @Nullable
    protected Direction direction;

    BaseRiderUIStrategy(T state) {
        super(state);
    }

    @Override
    protected void onAttach(MapFragmentInterface view) {
        super.onAttach(view);
        subscribeUntilDetach(getState()
                .getRideUpgradeObservable()
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(status -> {
                    getView().updateFabMenu();
                    switch (status.getStatus()) {
                        case REQUESTED:
                            break;
                        case ACCEPTED:
                            updateToolbar();
                            if (!status.equals(App.getPrefs().getLastShownUpgradeDialog())) {
                                getView().showRideUpgradeAcceptedDialog();
                                App.getPrefs().setLastShownUpgradeDialog(status);
                            }
                            break;
                        case EXPIRED:
                        case DECLINED:
                        case CANCELLED:
                            if (needToShowUpgradeFailed(status)) {
                                // RA-12189: proceed only if local status is positive,
                                // otherwise driver already notified that upgrade failed
                                App.getPrefs().setLastShownUpgradeDialog(status);
                                if (status.getStatus() != UpgradeRequestStatus.CANCELLED) {
                                    // no need to show upgrade dialog when driver cancelled
                                    // he will see cancel success dialog
                                    getView().showRideUpgradeFailedDialog(status.getStatus());
                                }
                            }
                            break;
                    }
                }));
    }

    private boolean needToShowUpgradeFailed(RideSpecificUpgradeStatus status) {
        // assume that new status is EXPIRED || DECLINED || CANCELLED
        return App.getPrefs().getLastShownUpgradeDialog().getRideId() != status.getRideId()
                || !App.getPrefs().getLastShownUpgradeDialog().isDenied();
    }

    void navigateSubscribe() {
        LatLng location = getState().getNavigationLocation();
        if (location != null) {
            getView().navigateTo(location);
            return;
        }

        Subscription subscription = getState()
                .getFinishAddressForNavigation()
                .subscribe(new ApiSubscriber<String>() {
                               @Override
                               public void onNext(String s) {
                                   super.onNext(s);
                                   getView().navigateTo(s);
                               }
                           }
                );
        subscribeUntilDetach(subscription);
    }

    protected void startMotionDetection() {
        // subscribe to driver movement after arrive
        App.getInstance().getStateManager().addMotionSubscription(App.getLocationManager()
                .getLastLocation(true, false).take(1)
                .flatMap(this::getDriverMovementWhileArrived)
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(driverLocation -> {
                    showMotionDetectedMessage(getState());
                }, throwable -> {
                    Timber.e(throwable, throwable.getMessage());
                }));

        // subscribe to change state
        App.getInstance().getStateManager().addMotionSubscription(App.getInstance().getStateManager()
                .getEngineStateObservable()
                .map(EngineState::getType)
                .filter(type -> type != EngineState.Type.ACCEPTED && type != EngineState.Type.ARRIVED)
                .subscribe(type -> {
                    App.getInstance().getStateManager().clearMotionSubscriptions();
                }));
    }

    private void showMotionDetectedMessage(T state) {
        App.getInstance().getStateManager().clearMotionSubscriptions();
        App.getNotificationManager().notifyMotionDetected(state);
        getView().showMotionDetectedDialog(state);
    }

    private Observable<RALocation> getDriverMovementWhileArrived(RALocation arrivedLocation) {
        return App.getLocationManager()
                .getLocationUpdates()
                .filter(RALocation::isValid)
                .filter(driverLocation -> {
                    boolean isArrived = App.getInstance().getStateManager().getCurrentEngineStateType() == EngineState.Type.ARRIVED;
                    final float distance = DistanceUtil.distance(arrivedLocation.getCoordinates(), driverLocation.getCoordinates());
                    return distance > Constants.MOVEMENT_DETECTION_THRESHOLD_M && isArrived;
                });
    }

    /**
     * Updates direction if driver changed the route.
     * Checks if driver location belongs to direction. If no, will update.
     * Direction should be valid and contain at least two points.
     * New direction will start from current driver's location.
     * End position will not be affected
     */
    protected void updateDirectionBasedOnDriverLocation() {
        subscribeUntilDetach(App.getLocationManager()
                .getLocationUpdates()
                .filter(RALocation::isValid)
                .subscribe(this::doOnDriverLocation, Timber::e));
    }

    @CallSuper
    protected void doOnDirection(Direction direction) {
        this.direction = direction;
    }

    protected void doOnDriverLocation(RALocation location) {
        if (location.getCoordinates() != null  // location data is valid
                && direction != null  // direction is not null
                && direction.getPolyline().size() > 1) { // direction has at least two points

            boolean locationOnPath = PolyUtil.isLocationOnPath(location.getCoordinates(), direction.getPolyline(), true, Constants.ROUTE_TOLERANCE);
            if (!locationOnPath) {
                LatLng endPoint = direction.getPolyline().get(direction.getPolyline().size() - 1);
                updateDirection(location.getCoordinates(), endPoint);
            }
        }
    }

    private void updateDirection(LatLng startPoint, LatLng endPoint) {
        subscribeUntilDetach(getState().loadDirection(startPoint, endPoint)
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnDirection, throwable -> Timber.e(throwable, "Can't update drivers direction")));
    }
}
