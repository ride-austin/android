package com.rideaustin.utils;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.ui.map.history.PlaceHistory;

import java.util.ArrayList;
import java.util.List;

import static com.rideaustin.utils.Constants.RECENTLY_VISITED_PLACES;

/**
 * Created by hatak on 30.06.2017.
 */

public class RecentPlacesUtils {

    public static final String PLACE_NAME_1 = "Austin Center";
    public static final String PLACE_NAME_2 = "Austin Airport";

    public static void mockRecentPlaces(int userId) {
        PlaceHistory placeHistory1 = new PlaceHistory("0001", PLACE_NAME_1, "Austin Center full name", 100L, new LatLng(30.277679, -97.741058), "78705");
        PlaceHistory placeHistory2 = new PlaceHistory("0001", PLACE_NAME_2, "Austin Airport full name", 200L, new LatLng(30.202435, -97.666405), "78719");
        List<PlaceHistory> recentPlaces = new ArrayList<>();
        recentPlaces.add(placeHistory1);
        recentPlaces.add(placeHistory2);
        App.getPrefs().getUserSpecificPreferences(userId).edit().putString(RECENTLY_VISITED_PLACES, SerializationHelper.serialize(recentPlaces)).apply();
    }

    public static void mockRecentPlacesFullList(int userId) {
        PlaceHistory placeHistory1 = new PlaceHistory("0001", "name 1", "full name 1", 100L, new LatLng(30.277679, -97.741058), "78705");
        PlaceHistory placeHistory2 = new PlaceHistory("0002", "name 2", "full name 2", 200L, new LatLng(30.202435, -97.666405), "78719");
        PlaceHistory placeHistory3 = new PlaceHistory("0003", "name 3", "full name 3", 300L, new LatLng(30.221435, -97.666405), "78719");
        PlaceHistory placeHistory4 = new PlaceHistory("0004", "name 4", "full name 4", 400L, new LatLng(30.244435, -97.666405), "78719");
        PlaceHistory placeHistory5 = new PlaceHistory("0005", "name 5", "full name 5", 500L, new LatLng(30.268435, -97.666405), "78719");
        PlaceHistory placeHistory6 = new PlaceHistory("0006", "name 6", "full name 6", 600L, new LatLng(30.277679, -97.741058), "78705");
        PlaceHistory placeHistory7 = new PlaceHistory("0007", "name 7", "full name 7", 700L, new LatLng(30.202435, -97.666405), "78719");
        PlaceHistory placeHistory8 = new PlaceHistory("0008", "name 8", "full name 8", 800L, new LatLng(30.221435, -97.666405), "78719");
        PlaceHistory placeHistory9 = new PlaceHistory("0009", "name 9", "full name 9", 900L, new LatLng(30.244435, -97.666405), "78719");
        PlaceHistory placeHistory10 = new PlaceHistory("0010", "name 10", "full name 10", 10000L, new LatLng(30.268435, -97.666405), "78719");

        List<PlaceHistory> recentPlaces = new ArrayList<>();
        recentPlaces.add(placeHistory1);
        recentPlaces.add(placeHistory2);
        recentPlaces.add(placeHistory3);
        recentPlaces.add(placeHistory4);
        recentPlaces.add(placeHistory5);
        recentPlaces.add(placeHistory6);
        recentPlaces.add(placeHistory7);
        recentPlaces.add(placeHistory8);
        recentPlaces.add(placeHistory9);
        recentPlaces.add(placeHistory10);
        App.getPrefs().getUserSpecificPreferences(userId).edit().putString(RECENTLY_VISITED_PLACES, SerializationHelper.serialize(recentPlaces)).apply();
    }

}
