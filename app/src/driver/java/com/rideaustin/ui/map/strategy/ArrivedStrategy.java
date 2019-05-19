package com.rideaustin.ui.map.strategy;


import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.api.model.Event;
import com.rideaustin.engine.Direction;
import com.rideaustin.engine.EngineState;
import com.rideaustin.engine.state.ArrivedState;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.map.MapFragmentInterface;
import com.rideaustin.ui.map.PickupDestinationFragment;
import com.rideaustin.ui.map.RideActionsFragment;

import java.util.List;

import java8.util.Optional;

import static com.rideaustin.ui.map.MapFragmentInterface.MenuType.CONTACT;

/**
 * Created by rost on 8/12/16.
 */
public class ArrivedStrategy extends BaseRiderUIStrategy<ArrivedState> implements PickupDestinationFragment.NavigationListener, RideActionsFragment.Listener {


    public ArrivedStrategy(ArrivedState state) {
        super(state);
    }

    @Override
    protected void onAttach(final MapFragmentInterface view) {
        super.onAttach(view);
        view.showMenu(CONTACT);
        boolean zoomToDriver = getState().getPreviousType().orElse(EngineState.Type.UNKNOWN)
                != EngineState.Type.ACCEPTED;
        view.showDriverOnMap(zoomToDriver);
        view.clearSurgeAreas();
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
//        getView().showDestinationMarker(Optional.ofNullable(getState().getDropLocation()));

        loadStartAddress();

//        loadEndAddress();

        loadDirection();

        startMotionDetection();

        subscribeUntilDetach(App.getInstance().getStateManager()
                .getRiderLocationUpdates()
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(riderLocationUpdate -> {
                    getView().updateRiderLocation(riderLocationUpdate);
                }));

//        subscribeUntilDetach(App.getInstance().getStateManager()
//                .getDestinationUpdateObservable()
//                .observeOn(RxSchedulers.main())
//                .subscribe(this::doOnDestinationChanged));
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
        if (confirmed) {
            startTrip();
        } else {
            getView().showStartTripConfirmationDialog(this::startTrip);
        }
    }

    private void startTrip() {
        subscribeUntilDetach(getState().switchNextWithPendingEvents(false).subscribe(createLoadingSubscriber(true)));
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        getView().clearToolbarTitle();
        getView().hideDriverOnMap();
        getView().hideRide();
        getView().getPickupDestinationFragment().clear();
        getView().getRiderActionsFragment().clear();
        getView().hideMotionDetectedDialog();
        getView().hideConfirmCancelDialog();
        getView().hideTripConfirmationDialog();
        getView().hideRideContactDialog();
    }

    @Override
    public void onNavigateClicked() {
        getView().showStartTripConfirmationDialog(() -> {
            subscribeUntilDetach(getState().switchNextWithPendingEvents(false)
                    .subscribe(createLoadingSubscriber(this::navigateSubscribe, true, true)));
        });
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
        List<LatLng> polyline = direction.getPolyline();
        LatLng pickup = getState().getPickupLocation();
        if (pickup == null) {
            pickup = polyline.get(0);
        }
        LatLng destination = getState().getDropLocation();
        if (destination == null && polyline.size() > 1) {
            destination = polyline.get(polyline.size() - 1);
        }
        getView().showPickupMarker(pickup);
        getView().showDestinationMarker(Optional.ofNullable(destination));
        getView().zoomToCurrentLocationWithGivenLocation(pickup.latitude, pickup.longitude);
    }

//    private void doOnDestinationChanged(Event event) {
//        loadEndAddress();
//        loadDirection();
//    }

    private void loadStartAddress() {
        subscribeUntilDetach(getState().getStartAddress()
                .doOnNext(startAddress -> getView().getPickupDestinationFragment().setStartAddresses(startAddress))
                .subscribe(createLoadingSubscriber(false, true)));
    }

//    private void loadEndAddress() {
//        subscribeUntilDetach(getState().getFinishAddress()
//                .doOnNext(finishAddress -> getView().getPickupDestinationFragment().setFinishAddresses(finishAddress))
//                .subscribe(createLoadingSubscriber(false, true)));
//    }

    private void loadDirection() {
        subscribeUntilDetach(getState().getDirection()
                .doOnNext(this::doOnDirection)
                .subscribe(createLoadingSubscriber(false, true)));
    }
}
