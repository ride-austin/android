package com.rideaustin.ui.map;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RiderLocationUpdate;
import com.rideaustin.api.model.UpgradeRequestStatus;
import com.rideaustin.engine.EngineState;
import com.rideaustin.manager.location.RALocation;

import java.util.List;

import java8.util.Optional;

/**
 * Created by rost on 8/12/16.
 */
public interface MapFragmentInterface {

    void showConfirmCancelDialog();

    void hideConfirmCancelDialog();

    void showMotionDetectedDialog(EngineState state);

    void hideMotionDetectedDialog();

    void setAvatar(String photoUrl);

    void updateFabMenu();

    void showRideUpgradeAcceptedDialog();

    void showRideUpgradeFailedDialog(UpgradeRequestStatus status);

    void clearRideUpgradeDialog();

    void showArriveNotAllowedDialog(int meters);

    void hideArriveNotAllowedDialog();

    void showStartTripConfirmationDialog(Runnable onConfirmed);

    void showFinishTripConfirmationDialog(Runnable onConfirmed);

    void hideTripConfirmationDialog();

    interface OnButtonClickListener {
        void onClicked();
    }

    interface OnRateChangedListener {
        void onChanged(float rate);
    }

    enum ButtonStyle {
        FATAL, WARN, REGULAR
    }

    enum MenuType {
        SUPPORT, CONTACT, DECLINE
    }

    void showInactiveWarning();

    void hideInactiveWarning();

    void showTopFragment(Fragment fragment);

    void hideTopFragment();

    void showBottomFragment(Fragment fragment);

    void hideBottomFragment();

    void showRide(List<LatLng> direction, @NonNull LatLng pickup, @Nullable LatLng destination);

    void hideRide();

    void updateRiderLocation(final RiderLocationUpdate riderLocationUpdate);

    void loadSurgeAreas();

    void clearSurgeAreas();

    void showLoading();

    void hideLoading();

    void showRatingWidget(double driverPayment, float startingRating, OnRateChangedListener onRateChanged, OnButtonClickListener onClicked);

    void hideRatingWidget();

    void showDriverOnMap(boolean zoomToDriver);

    void hideDriverOnMap();

    void showNearestDrivers(List<RALocation> driverLocations);

    void hideNearestDrivers();

    void showNoLocationAvailableDialog();

    void hideNoLocationAvailableDialog();

    void showMissingLocationPermissionMessage();

    void showMenu(@NonNull MenuType type);

    void hideMenu();

    @Nullable
    MenuType getMenuType();

    void showRideContactDialog(Ride ride);

    void hideRideContactDialog();

    void navigateTo(String place);

    void navigateTo(LatLng place);

    void setToolbarTitle(String title);

    void setToolbarSubtitle(String subtitle);

    void clearToolbarTitle();

    void showAppNameTitle();

    void showToolbarButton(@StringRes int stringId, ButtonStyle style, OnButtonClickListener onClicked, boolean progress);

    void showPickupMarker(LatLng latLng);

    void showDestinationMarker(Optional<LatLng> latLng);

    void showNextRideMarker(Optional<LatLng> latLng);

    void hidePickupMarker();

    void zoomToCurrentLocationWithGivenLocation(double lat, double lng);

    void showTermsDialog(String message);

    void showGenericContactSupport();

    void showNextRideDialog();

    void hideNextRideDialog();

    RideActionsFragment getRiderActionsFragment();

    PickupDestinationFragment getPickupDestinationFragment();

    PendingAcceptFragment getPendingAcceptFragment();

    interface MapPaddingListener {
        void onTopPaddingUpdated(int value);
        void onBottomPaddingUpdated(int value);
    }
}
