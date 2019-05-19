package com.rideaustin.manager;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.rideaustin.App;
import com.rideaustin.events.RideStatusEvent;
import com.rideaustin.ui.map.history.PlaceHistory;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.SerializationHelper;
import com.rideaustin.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java8.util.Optional;
import timber.log.Timber;

import static com.rideaustin.utils.Constants.RECENTLY_VISITED_PLACES;


/**
 * Created by vokol on 01.08.2016.
 */
public class MapPreferencesManager {

    private MapPreferencesManager() {
    }

    public static void saveCurrentRideState(Optional<RideStatusEvent> currentStatus) {
        currentStatus.ifPresent(rideStatusEvent -> App.getPrefs().setRideStatus(rideStatusEvent.getData().name()));
    }

    public static void clearRideStatus() {
        App.getPrefs().setRideStatus("");
    }

    public static void clearSavedMarkersCoordinates() {
        App.getPrefs().clearValue(Constants.PICKUP_LATITUDE_LOCATION_KEY);
        App.getPrefs().clearValue(Constants.PICKUP_LONGITUDE_LOCATION_KEY);
        App.getPrefs().clearValue(Constants.DESTINATION_LATITUDE_LOCATION_KEY);
        App.getPrefs().clearValue(Constants.DESTINATION_LONGITUDE_LOCATION_KEY);
        App.getPrefs().setDestinationAddressString("");
        App.getPrefs().setPickupAddressString("");
        App.getPrefs().clearValue(Constants.MARKER_STORED_TIME_KEY);
    }

    public static void saveMarkerCoordinates(@Nullable Marker pickupMarker, @Nullable Marker destinationMarker) {
        if (pickupMarker != null) {
            LatLng pickup = pickupMarker.getPosition();

            App.getPrefs().putDouble(Constants.PICKUP_LATITUDE_LOCATION_KEY, pickup.latitude);
            App.getPrefs().putDouble(Constants.PICKUP_LONGITUDE_LOCATION_KEY, pickup.longitude);

            double destLat = 0;
            double destLng = 0;

            if (destinationMarker != null) {
                LatLng destination = destinationMarker.getPosition();
                destLat = destination.latitude;
                destLng = destination.longitude;
            }

            App.getPrefs().putDouble(Constants.DESTINATION_LATITUDE_LOCATION_KEY, destLat);
            App.getPrefs().putDouble(Constants.DESTINATION_LONGITUDE_LOCATION_KEY, destLng);
            App.getPrefs().putLong(Constants.MARKER_STORED_TIME_KEY, TimeUtils.currentTimeMillis());

        }
    }

    public static List<PlaceHistory> getRecentlyVisitedPlaces() {
        List<PlaceHistory> list = null;
        String settStr = App.getPrefs().getUserSpecificPreferences().getString(RECENTLY_VISITED_PLACES, "");
        if (!TextUtils.isEmpty(settStr)) {
            PlaceHistory[] arr = SerializationHelper.deSerialize(settStr, PlaceHistory[].class);
            if (arr != null) {
                list = new ArrayList<>(Arrays.asList(arr));
            }
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public static void setRecentlyVisitedPlaces(List<PlaceHistory> recentlyVisitedPlaces) {
        if (recentlyVisitedPlaces == null || recentlyVisitedPlaces.isEmpty()) {
            App.getPrefs().getUserSpecificPreferences().edit()
                    .remove(RECENTLY_VISITED_PLACES)
                    .apply();
        } else {
            App.getPrefs().getUserSpecificPreferences().edit()
                    .putString(RECENTLY_VISITED_PLACES, SerializationHelper.serialize(recentlyVisitedPlaces))
                    .apply();
        }
    }
}
