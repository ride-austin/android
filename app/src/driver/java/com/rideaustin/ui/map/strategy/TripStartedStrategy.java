package com.rideaustin.ui.map.strategy;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.Ride;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.engine.Direction;
import com.rideaustin.engine.EngineState;
import com.rideaustin.engine.state.TripStartedState;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.map.MapFragmentInterface;
import com.rideaustin.ui.map.PickupDestinationFragment;
import com.rideaustin.ui.map.RideActionsFragment;

import java.util.List;

import java8.util.Optional;
import rx.Subscription;
import timber.log.Timber;

import static com.rideaustin.ui.map.MapFragmentInterface.MenuType.CONTACT;


/**
 * Created by rost on 8/12/16.
 */
public class TripStartedStrategy extends BaseRiderUIStrategy<TripStartedState> implements PickupDestinationFragment.NavigationListener, RideActionsFragment.Listener {

    public TripStartedStrategy(TripStartedState state) {
        super(state);
    }

    @Override
    protected void onAttach(MapFragmentInterface view) {
        super.onAttach(view);
        view.hideMenu();
        boolean zoomToDriver = getState().getPreviousType().orElse(EngineState.Type.UNKNOWN)
                != EngineState.Type.ARRIVED;
        view.showDriverOnMap(zoomToDriver);
        view.clearSurgeAreas();
        updateToolbar();

        boolean hasNextRide = getState().getRide().getNextRide() != null;
        updateMenu(hasNextRide);

        PickupDestinationFragment topFragment  = getView().getPickupDestinationFragment();
        topFragment.setNavigationListener(this);
        topFragment.showCancelButton(false);
        topFragment.showDestination(true);
        topFragment.showComment(false);
        topFragment.showNextButton(hasNextRide);
        getView().showTopFragment(topFragment);

        RideActionsFragment bottomFragment = getView().getRiderActionsFragment();
        bottomFragment.setType(getState().getType());
        bottomFragment.setListener(this);
        getView().showBottomFragment(bottomFragment);

        getView().showPickupMarker(getState().getPickupLocation());
        getView().showDestinationMarker(Optional.ofNullable(getState().getDropLocation()));
        getView().showNextRideMarker(Optional.ofNullable(getState().getRide())
                .map(Ride::getNextRide)
                .map(Ride::getStartLocation));

        loadStartAddress();

        loadEndAddress();

        loadDirection();

        updateDirectionBasedOnDriverLocation();

        subscribeUntilDetach(App.getInstance().getStateManager()
                .getDestinationUpdateObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnDestinationChanged, Timber::e));

        subscribeUntilDetach(App.getInstance().getStateManager()
                .getNextRide()
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnNextRide, Timber::e));
    }

    private void finishTrip() {
        subscribeUntilDetach(getState().switchNextWithPendingEvents(false).subscribe(createLoadingSubscriber(true)));
    }

    @Override
    public void updateToolbar() {
        getView().clearToolbarTitle();
        getView().setToolbarTitle(getState().getRide().getRider().getFirstname());
        getView().setToolbarSubtitle(getState().getRide().getRequestedCarType().getTitle());
        getView().setAvatar(getState().getRide().getRider().getUser().getPhotoUrl());
    }

    @Override
    public void onFlowAction(boolean confirmed) {
        if (confirmed) {
            finishTrip();
        } else {
            getView().showFinishTripConfirmationDialog(this::finishTrip);
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        getView().clearToolbarTitle();
        getView().hideDriverOnMap();
        getView().getPickupDestinationFragment().clear();
        getView().getRiderActionsFragment().clear();
        getView().hideTripConfirmationDialog();
        getView().hideRide();
        getView().showNextRideMarker(Optional.empty());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getView().hideNextRideDialog();
    }

    @Override
    public void onNavigateClicked() {
        LatLng location = getState().getDropLocation();
        if (location != null) {
            getView().navigateTo(location);
            return;
        }

        Subscription subscription = getState()
                .getFinishAddress()
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

    @Override
    public void onCanceledClicked() {
        throw new IllegalStateException("Can't cancel the ride after trip is started!");
    }

    @Override
    public void onNextClicked() {
        getView().showNextRideDialog();
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
        getView().showRide(direction.getPolyline(), pickup, destination);
        if (destination != null) {
            getView().zoomToCurrentLocationWithGivenLocation(destination.latitude, destination.longitude);
        }
    }

    private void doOnNextRide(Optional<Ride> ride) {
        boolean hasNextRide = ride.isPresent();
        getView().getPickupDestinationFragment().showNextButton(hasNextRide);
        getView().showNextRideMarker(ride
                .map(Ride::getStartLocation));
        updateMenu(hasNextRide);
    }

    private void doOnDestinationChanged(Event event) {
        loadEndAddress();
        loadDirection();
    }

    private void loadStartAddress() {
        subscribeUntilDetach(getState().getStartAddress()
                .doOnNext(startAddress -> getView().getPickupDestinationFragment().setStartAddresses(startAddress))
                .subscribe(createLoadingSubscriber(false, true)));
    }

    private void loadEndAddress() {
        subscribeUntilDetach(getState().getFinishAddress()
                .doOnNext(finishAddress -> getView().getPickupDestinationFragment().setFinishAddresses(finishAddress))
                .subscribe(createLoadingSubscriber(false, true)));
    }

    private void loadDirection() {
        subscribeUntilDetach(getState().getDirection()
                .doOnNext(this::doOnDirection)
                .subscribe(createLoadingSubscriber(false, true)));
    }

    private void updateMenu(boolean hasNextRide) {
        MapFragmentInterface.MenuType menuType = getView().getMenuType();
        if (hasNextRide) {
            if (menuType != null) {
                getView().hideMenu();
            }
        } else {
            if (menuType != CONTACT) {
                getView().showMenu(CONTACT);
            }
        }
    }
}
