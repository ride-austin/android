package com.rideaustin.ui.map.strategy;

import com.rideaustin.R;
import com.rideaustin.engine.SwitchNextData;
import com.rideaustin.engine.state.OnlineState;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.map.MapFragmentInterface;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.toast.RAToast;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import timber.log.Timber;

import static com.rideaustin.ui.map.MapFragmentInterface.MenuType.SUPPORT;

/**
 * Created by rost on 8/12/16.
 */
public class OnlineStrategy extends BaseUIStrategy<OnlineState> {

    public OnlineStrategy(OnlineState state) {
        super(state);
    }

    @Override
    protected void onAttach(MapFragmentInterface view) {
        super.onAttach(view);
        view.hideRide();
        updateToolbar();
        view.showMenu(SUPPORT);
        view.hideMotionDetectedDialog();
        view.loadSurgeAreas();
        view.showDriverOnMap(true);
        view.hideTopFragment();
        view.hideBottomFragment();

        final Subscription subscription = getState().getNearDriversPeriodically(Constants.TIMEOUT_RETRY_GET_NEAREST_DRIVERS_S, TimeUnit.SECONDS)
                .doOnError(new DefaultErrorProcessor())
                .observeOn(RxSchedulers.main())
                .subscribe(driverLocations -> {
                    getView().hideNoLocationAvailableDialog();
                    getView().showNearestDrivers(driverLocations);
                }, throwable -> {
                    RAToast.showShort("Error occurred: " + throwable.getLocalizedMessage());
                    Timber.e(throwable, throwable.getMessage());
                });

        final Subscription backgroundErrorsSubscription = getState().backgroundErrors()
                .subscribe(new DefaultErrorProcessor());

        subscribeUntilDetach(backgroundErrorsSubscription);
        subscribeUntilDetach(subscription);
        view.hidePickupMarker();
        view.clearRideUpgradeDialog();
    }

    @Override
    public void updateToolbar() {
        getView().clearToolbarTitle();
        getView().showToolbarButton(R.string.action_go_offline,
                MapFragmentInterface.ButtonStyle.REGULAR,
                this::switchToOfflineStateSubscription, false);
    }

    private void switchToOfflineStateSubscription() {
        subscribeUntilDetach(getState().switchNext(SwitchNextData.ok()).subscribe(createLoadingSubscriber(true)));
    }

    @Override
    public boolean onMenuItemSelected(int menuId) {
        getView().showGenericContactSupport();
        return true;
    }

    @Override
    public void onFlowAction(boolean confirmed) {
        switchToOfflineStateSubscription();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        getView().hideDriverOnMap();
    }
}
