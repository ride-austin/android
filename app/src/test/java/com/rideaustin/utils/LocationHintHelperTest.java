package com.rideaustin.utils;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.api.config.LocationHint;
import com.rideaustin.api.model.Coordinates;
import com.rideaustin.api.model.LocationHintCoord;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * Created by Sergey Petrov on 31/08/2017.
 */

@RunWith(RobolectricTestRunner.class)
public class LocationHintHelperTest {

    private LatLng location = new LatLng(30.285218, -97.730635);
    private LatLng locationNear = new LatLng(30.287448, -97.733263);
    private LatLng locationFar = new LatLng(30.265157, -97.727003);

    @Test
    public void nullOnNullInput() {
        LocationHintHelper helper = new LocationHintHelper();
        helper.setHints(null, null);
        assertNull(helper.findNearestLocationCoord(location, LocationHintHelper.AreaType.PICKUP, true));
    }

    @Test
    public void nullOnEmptyInput() {
        LocationHintHelper helper = new LocationHintHelper();
        List<LocationHint> hints = new ArrayList<>();
        hints.add(createCorrentPickupHint());
        helper.setHints(hints, null);
        assertNull(helper.findNearestLocationCoord(location, LocationHintHelper.AreaType.PICKUP, true));
    }

    @Test
    public void nullOnIncorrectArea() {
        LocationHintHelper helper = new LocationHintHelper();
        List<LocationHint> hints = new ArrayList<>();
        hints.add(createIncorrentPickupHint(
                new LocationHintCoord("1", new Coordinates(locationNear.latitude, locationNear.longitude)),
                new LocationHintCoord("2", new Coordinates(locationFar.latitude, locationFar.longitude))));
        helper.setHints(hints, null);
        assertNull(helper.findNearestLocationCoord(location, LocationHintHelper.AreaType.PICKUP, true));
    }

    @Test
    public void shouldUseNearestLocationFromCorrectArea() {
        LocationHintHelper helper = new LocationHintHelper();
        List<LocationHint> hints = new ArrayList<>();
        hints.add(createCorrentPickupHint(
                new LocationHintCoord("1", new Coordinates(locationNear.latitude, locationNear.longitude)),
                new LocationHintCoord("2", new Coordinates(locationFar.latitude, locationFar.longitude))));
        helper.setHints(hints, null);
        LocationHintCoord coord = helper.findNearestLocationCoord(location, LocationHintHelper.AreaType.PICKUP, true);
        assertNotNull(coord);
        assertEquals(locationNear, coord.getDriverCoord().getLatLng());
    }

    @Test
    public void shouldUseLocationFromMultipleAreas() {
        LocationHintHelper helper = new LocationHintHelper();
        List<LocationHint> hints = new ArrayList<>();
        hints.add(createCorrentPickupHint(
                new LocationHintCoord("1", new Coordinates(locationFar.latitude, locationFar.longitude))));
        hints.add(createIncorrentPickupHint(
                new LocationHintCoord("2", new Coordinates(locationNear.latitude, locationNear.longitude))));
        helper.setHints(hints, null);
        LocationHintCoord coord = helper.findNearestLocationCoord(location, LocationHintHelper.AreaType.PICKUP, true);
        assertNotNull(coord);
        assertEquals(locationFar, coord.getDriverCoord().getLatLng());
    }

    private LocationHint createCorrentPickupHint(LocationHintCoord... designatedPickups) {
        LocationHint hint = new LocationHint();
        List<Coordinates> correctArea = new ArrayList<>();
        correctArea.add(new Coordinates(30.293218, -97.746318));
        correctArea.add(new Coordinates(30.293218, -97.722521));
        correctArea.add(new Coordinates(30.277347, -97.722521));
        correctArea.add(new Coordinates(30.277347, -97.746318));
        hint.setAreaPolygon(correctArea);
        hint.setDesignatedPickups(Arrays.asList(designatedPickups));
        return hint;
    }

    private LocationHint createIncorrentPickupHint(LocationHintCoord... designatedPickups) {
        LocationHint hint = new LocationHint();
        List<Coordinates> area = new ArrayList<>();
        area.add(new Coordinates(31.293218, -97.746318));
        area.add(new Coordinates(31.293218, -97.722521));
        area.add(new Coordinates(31.277347, -97.722521));
        area.add(new Coordinates(31.277347, -97.746318));
        hint.setAreaPolygon(area);
        hint.setDesignatedPickups(Arrays.asList(designatedPickups));
        return hint;
    }

}
