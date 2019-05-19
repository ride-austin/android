package com.rideaustin.utils;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.manager.MapPreferencesManager;
import com.rideaustin.ui.map.history.PlaceHistory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java8.util.Optional;

import static com.rideaustin.utils.Constants.MAX_ELEMENTS_TO_SAVE_IN_HISTORY;

/**
 * @author sdelaysam.
 */

public class RecentPlacesHelper {

    public static List<PlaceHistory> getPlaces() {
        return sortHistories(MapPreferencesManager.getRecentlyVisitedPlaces());
    }

    public static void saveAddress(GeoPosition address) {
        if (isFavoritePlace(address.getLatLng())) {
            // don't save favorite place to history
            return;
        }

        List<PlaceHistory> placeHistories = MapPreferencesManager.getRecentlyVisitedPlaces();
        PlaceHistory placeHistory = new PlaceHistory(
                Optional.ofNullable(address.getPlaceId()).orElse(""),
                address.getAddressLine(),
                address.getFullAddress(),
                TimeUtils.currentTimeMillis(),
                address.getLatLng(),
                address.getZipCode());

        // remove existing if any to update time
        placeHistories.remove(placeHistory);
        placeHistories.add(placeHistory);

        List<PlaceHistory> sortedList = sortHistories(placeHistories);
        if (sortedList.size() > MAX_ELEMENTS_TO_SAVE_IN_HISTORY) {
            for (int i = MAX_ELEMENTS_TO_SAVE_IN_HISTORY - 1; i < sortedList.size(); i++) {
                PlaceHistory removed = sortedList.get(sortedList.size() - 1);
                placeHistories.remove(removed);
                sortedList.remove(removed);
            }
        }

        MapPreferencesManager.setRecentlyVisitedPlaces(placeHistories);
    }

    @NonNull
    private static List<PlaceHistory> sortHistories(List<PlaceHistory> historyViewModelSet) {
        List<PlaceHistory> sortedList = new ArrayList<>(historyViewModelSet);
        Collections.sort(sortedList, (lhs, rhs) -> {
            Long currentDate = rhs.getTimeStamp();
            return currentDate.compareTo(lhs.getTimeStamp());
        });
        return sortedList;
    }

    private static boolean isFavoritePlace(LatLng latLng) {
        return App.getPrefs().getHome().filter(p -> p.includesLocation(latLng)).isPresent()
                || App.getPrefs().getWork().filter(p -> p.includesLocation(latLng)).isPresent();
    }

}
