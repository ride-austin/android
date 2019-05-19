package com.rideaustin.ui.map.views;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.api.model.driver.RequestedCarType;

import java.util.List;

/**
 * Created by Viktor Kifer
 * On 23-Dec-2016.
 */

public interface MapView extends RideEventsListener {

    void onSurgeAreasChanged();

    void onCarTypesUpdated(List<RequestedCarType> carTypes);

    void updateActiveRide();

    void drawDirectionOnMap(List<LatLng> direction);

    void onAddressAvailable(String address);

    void onAddressNotAvailable();

    void onTimeToPickupRequested();

    void onTimeToPickupResult(String timeToPickup);

    void onTimeToPickupCleared();

    void onNoDriversAvailable();

    void onNotAvailableAtLocation();

    boolean isInRideRequest();

    boolean isInForeground();

    void showRoundUpPopup();

    void onUpgradeNeeded();

    void onAddressIsFar(MaterialDialog.SingleButtonCallback singleButtonCallback);

    void manageOptionMenuButton();

    void onBottomOffsetChanged(int bottomOffset);
}