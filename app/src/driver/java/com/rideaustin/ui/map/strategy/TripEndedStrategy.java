package com.rideaustin.ui.map.strategy;

import com.rideaustin.api.model.Ride;
import com.rideaustin.engine.EngineState;
import com.rideaustin.engine.state.TripEndedState;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.map.MapFragmentInterface;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.toast.RAToast;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Created by rost on 8/15/16.
 */
public class TripEndedStrategy extends BaseRiderUIStrategy<TripEndedState> {

    public TripEndedStrategy(TripEndedState state) {
        super(state);
    }

    @Override
    protected void onAttach(MapFragmentInterface view) {
        super.onAttach(view);
        boolean zoomToDriver = getState().getPreviousType().orElse(EngineState.Type.UNKNOWN)
                != EngineState.Type.STARTED;
        view.showDriverOnMap(zoomToDriver);
        updateToolbar();
        view.hideMenu();
        view.hideTopFragment();
        view.hideBottomFragment();
        view.clearSurgeAreas();

        subscribeUntilDetach(getState().backgroundErrors().subscribe(new DefaultErrorProcessor()));

        subscribeUntilDetach(getState().getNearDriversPeriodically(Constants.TIMEOUT_RETRY_GET_NEAREST_DRIVERS_S, TimeUnit.SECONDS)
                .doOnError(new DefaultErrorProcessor())
                .observeOn(RxSchedulers.main())
                .subscribe(driverLocations -> {
                    getView().hideNoLocationAvailableDialog();
                    getView().showNearestDrivers(driverLocations);
                }, throwable -> {
                    RAToast.showShort("Error occurred: " + throwable.getLocalizedMessage());
                    Timber.e(throwable, throwable.getMessage());
                }));

        showRatingWidget(getState().getRide());
        view.clearRideUpgradeDialog();
    }

    private void showRatingWidget(final Ride ride) {
        getView().showRatingWidget(ride.getDriverPayment(),
                getState().getRate(),
                rate -> getState().setRate(rate),
                () -> getState()
                        .switchNextWithPendingEvents(false)
                        .subscribe(createLoadingSubscriber(true)));
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

    }

    @Override
    protected void onDetach() {
        super.onDetach();
        getView().clearToolbarTitle();
        getView().hideNearestDrivers();
        getView().hideDriverOnMap();
        getView().hideRatingWidget();
    }
}