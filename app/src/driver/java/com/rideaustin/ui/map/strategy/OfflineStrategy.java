package com.rideaustin.ui.map.strategy;


import com.rideaustin.R;
import com.rideaustin.engine.state.OfflinePollingState;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.map.MapFragmentInterface;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.toast.RAToast;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static com.rideaustin.ui.map.MapFragmentInterface.MenuType.SUPPORT;

/**
 * Created by rost on 8/12/16.
 */
public class OfflineStrategy extends BaseUIStrategy<OfflinePollingState> {

    public OfflineStrategy(OfflinePollingState state) {
        super(state);
    }

    @Override
    protected void onAttach(MapFragmentInterface view) {
        super.onAttach(view);
        updateToolbar();
        view.showDriverOnMap(true);
        view.loadSurgeAreas();
        view.showMenu(SUPPORT);
        view.hideTopFragment();
        view.hideBottomFragment();

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

        subscribeUntilDetach(getState().backgroundErrors().subscribe(new DefaultErrorProcessor()));
        view.clearRideUpgradeDialog();
    }

    @Override
    public void updateToolbar() {
        getView().clearToolbarTitle();
        updateToolbarActionButton();
        if (hasPendingEvents()) {
            listenToPendingEventsSending();
        }
    }

    private void switchToOnlineState() {
        subscribeUntilDetach(getState().switchNextWithPendingEvents(true).subscribe(createLoadingSubscriber(true)));
    }

    private void sync() {
        subscribeUntilDetach(getState().switchNextWithPendingEvents(true).subscribe(createLoadingSubscriber(false, true)));
    }

    @Override
    protected void doOnPendingEventsSending(boolean sending) {
        getView().showToolbarButton(sending ? R.string.action_in_progress : R.string.action_go_online,
                sending ? MapFragmentInterface.ButtonStyle.WARN : MapFragmentInterface.ButtonStyle.FATAL,
                this::sync, sending);
    }

    private void updateToolbarActionButton() {
        if (hasPendingEvents()) {
            boolean isSending = pendingEventsSending();
            doOnPendingEventsSending(isSending);
        } else {
            getView().showToolbarButton(R.string.action_go_online,
                    MapFragmentInterface.ButtonStyle.FATAL,
                    this::switchToOnlineState, false);
        }
    }

    @Override
    public boolean onMenuItemSelected(int menuId) {
        getView().showGenericContactSupport();
        return true;
    }

    @Override
    public void onFlowAction(boolean confirmed) {
        switchToOnlineState();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        getView().hideNearestDrivers();
        getView().hideDriverOnMap();
    }

}
