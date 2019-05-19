package com.rideaustin.ui.map.strategy;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.Ride;
import com.rideaustin.engine.SwitchNextData;
import com.rideaustin.engine.state.PendingAcceptState;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.map.MapFragmentInterface;
import com.rideaustin.ui.map.PendingAcceptFragment;
import com.rideaustin.utils.AnswersUtils;

import timber.log.Timber;

import static com.rideaustin.ui.map.MapFragmentInterface.MenuType.DECLINE;

/**
 * Created by rost on 8/12/16.
 */
public class PendingAcceptStrategy extends BaseUIStrategy<PendingAcceptState> {

    public PendingAcceptStrategy(PendingAcceptState state) {
        super(state);
    }

    @Override
    protected void onAttach(MapFragmentInterface view) {
        super.onAttach(view);
        view.showMenu(DECLINE);
        updateToolbar();
        view.hideTopFragment();
        view.clearSurgeAreas();

        Ride pendingRide = getState().getPendingRide();
        PendingAcceptFragment fragment = view.getPendingAcceptFragment();
        fragment.setRide(pendingRide);
        fragment.setAcceptCounter(getState().getAcceptCounter());
        fragment.setAcceptButtonClickedListener(() -> {
            reset();
            getState().switchNext(SwitchNextData.ok()).subscribe(createLoadingSubscriber(true));
        });
        view.showBottomFragment(fragment);

        showPickupMarker(pendingRide.getStartLocationLat(), pendingRide.getStartLocationLong());
        subscribeUntilDetach(getState().getStartAddress()
                .subscribe(s -> getView().getPendingAcceptFragment().setStartAddress(s), throwable -> Timber.e(throwable, "Unable to fetch start address")));
        subscribeUntilDetach(getState().getSending()
                .observeOn(RxSchedulers.main())
                .subscribe(accepting -> {
                    if (accepting) {
                        getView().hideBottomFragment();
                    }
                }, Timber::e));
    }

    @Override
    public void updateToolbar() {
        if (getState().isStackedRide()) {
            getView().clearToolbarTitle();
            getView().showAppNameTitle();
        } else {
            getView().clearToolbarTitle();
            getView().showToolbarButton(R.string.action_go_offline,
                    MapFragmentInterface.ButtonStyle.REGULAR,
                    this::switchToOfflineStateSubscription, false);
        }
    }

    private void showPickupMarker(double lat, double lng) {
        Timber.d("::showPickupMarker:: Location (%f,%f)", lat, lng);
        getView().showPickupMarker(new LatLng(lat, lng));
        getView().showDriverOnMap(false);
        getView().zoomToCurrentLocationWithGivenLocation(lat, lng);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        reset();
        getView().hideDriverOnMap();
        getView().hideBottomFragment();
    }

    private void reset() {
        getView().hidePickupMarker();
    }

    @Override
    public boolean onMenuItemSelected(int menuId) {
        AnswersUtils.logDeclineRide(App.getDataManager().getCurrentDriver());
        decline();
        return true;
    }

    @Override
    public void onFlowAction(boolean confirmed) {
        reset();
        getState().switchNext(SwitchNextData.ok()).subscribe(createLoadingSubscriber(true));
    }

    private void switchToOfflineStateSubscription() {
        reset();
        subscribeUntilDetach(getState().goOffline().subscribe(createLoadingSubscriber(true)));
    }

    private void decline() {
        reset();
        subscribeUntilDestroy(getState().cancel().subscribe(createLoadingSubscriber(true)));
    }

}
