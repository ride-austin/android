package com.rideaustin.ui.map.strategy;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.api.config.DriverActions;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.engine.Direction;
import com.rideaustin.engine.EngineState;
import com.rideaustin.engine.state.AcceptedState;
import com.rideaustin.manager.location.RALocation;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.map.MapFragmentInterface;
import com.rideaustin.ui.map.PickupDestinationFragment;
import com.rideaustin.ui.map.RideActionsFragment;
import com.rideaustin.utils.location.DistanceUtil;

import rx.Observable;
import rx.functions.Func2;

import static com.rideaustin.ui.map.MapFragmentInterface.MenuType.CONTACT;

/**
 * Created by rost on 8/12/16.
 */
public class AcceptedStrategy extends BaseRiderUIStrategy<AcceptedState> implements PickupDestinationFragment.NavigationListener, RideActionsFragment.Listener {

    public AcceptedStrategy(AcceptedState state) {
        super(state);
    }

    private boolean arriveActionAllowed = true;

    private int arriveMinDistance = 500;

    @Override
    protected void onAttach(MapFragmentInterface view) {
        super.onAttach(view);
        view.showMenu(CONTACT);
        boolean zoomToDriver = getState().getPreviousType().orElse(EngineState.Type.UNKNOWN)
                != EngineState.Type.PENDING_ACCEPT;
        view.showDriverOnMap(zoomToDriver);

        view.clearSurgeAreas();
        view.hideNearestDrivers();
        updateToolbar();

        PickupDestinationFragment topFragment  = getView().getPickupDestinationFragment();
        topFragment.setNavigationListener(this);
        topFragment.showCancelButton(true);
        topFragment.showDestination(false);
        topFragment.showNextButton(false);
        topFragment.showComment(true);
        getView().showTopFragment(topFragment);

        RideActionsFragment bottomFragment = getView().getRiderActionsFragment();
        bottomFragment.setType(getState().getType());
        bottomFragment.setListener(this);
        getView().showBottomFragment(bottomFragment);

        getView().showPickupMarker(getState().getPickupLocation());


        Observable<Integer> minDistanceObservable = App.getConfigurationManager().getConfigurationUpdates()
                .map(GlobalConfig::getDriverActions)
                .map(DriverActions::getAllowArriveDistanceToPickup)
                .onErrorResumeNext(Observable.just(500))
                .distinctUntilChanged()
                .doOnNext(it -> arriveMinDistance = it);

        Observable<Integer> distanceObservable = getState().listenToDriverLocation()
                .filter(RALocation::isValid)
                .map(RALocation::getCoordinates)
                .map(driverPosition -> (int) DistanceUtil.distance(driverPosition, getState().getRide().getStartLocation()));

        subscribeUntilDetach(Observable.combineLatest(
                minDistanceObservable,
                distanceObservable,
                (minDistance, distance) -> distance <= minDistance)
                .observeOn(RxSchedulers.main())
                .subscribe(it -> arriveActionAllowed = it));

        subscribeUntilDetach(getState().getStartAddress()
                .doOnNext(startAddress -> getView().getPickupDestinationFragment().setStartAddresses(startAddress))
                .subscribe(createLoadingSubscriber(false, true)));

        subscribeUntilDetach(getState().getDirection()
                .doOnNext(this::doOnDirection)
                .subscribe(createLoadingSubscriber(false, true)));

        subscribeUntilDetach(App.getInstance().getStateManager()
                .getRiderLocationUpdates()
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(riderLocationUpdate -> getView().updateRiderLocation(riderLocationUpdate)));

        updateDirectionBasedOnDriverLocation();
    }

    @Override
    public void updateToolbar() {
        getView().clearToolbarTitle();
        getView().setToolbarTitle(getState().getRide().getRider().getFirstname());
        getView().setToolbarSubtitle(getState().getRide().getRequestedCarType().getTitle());
        getView().setAvatar(getState().getRide().getRider().getUser().getPhotoUrl());
    }

    @Override
    public boolean onMenuItemSelected(int menuId) {
        getView().showRideContactDialog(getState().getRide());
        return true;
    }

    @Override
    public void onCancelRideSelected(@Nullable String code, @Nullable String reason) {
        subscribeUntilDestroy(getState().cancel(code, reason)
                .subscribe(createLoadingSubscriber(true)));
    }

    @Override
    public void onFlowAction(boolean confirmed) {
        if (arriveActionAllowed) {
            subscribeUntilDetach(getState().switchNextWithPendingEvents(false).subscribe(createLoadingSubscriber(true)));
        } else {
            getView().showArriveNotAllowedDialog(arriveMinDistance);
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        getView().clearToolbarTitle();
        getView().hideDriverOnMap();
        getView().hideRide();
        getView().getPickupDestinationFragment().clear();
        getView().getRiderActionsFragment().clear();
        getView().hideConfirmCancelDialog();
        getView().hideRideContactDialog();
        getView().hideArriveNotAllowedDialog();
    }

    @Override
    public void onNavigateClicked() {
        navigateSubscribe();
    }

    @Override
    public void onCanceledClicked() {
        getView().showConfirmCancelDialog();
    }

    @Override
    public void onNextClicked() {
    }

    @Override
    protected void doOnDirection(Direction direction) {
        super.doOnDirection(direction);
        LatLng pickup = getState().getPickupLocation();
        if (pickup == null) {
            pickup = direction.getPolyline().get(0);
        }
        getView().showRide(direction.getPolyline(), pickup, null);
        getView().zoomToCurrentLocationWithGivenLocation(pickup.latitude, pickup.longitude);
    }
}
