package com.rideaustin.utils;

import android.graphics.Rect;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.rideaustin.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static com.rideaustin.BaseUITest.IDLE_TIMEOUT_MS;
import static com.rideaustin.BaseUITest.getString;
import static com.rideaustin.utils.DeviceTestUtils.search;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by crossover on 16/05/2017.
 */

public class MapTestUtil {

    public static void assertCarMarkersVisible() {
        search("Car Markers should be visible")
                .descContains(getString(R.string.car_marker))
                .assertExist();
    }

    public static void assertCarMarkersVisible(long timeout) {
        search("Car Markers should be visible")
                .descContains(getString(R.string.car_marker))
                .assertExist(timeout);
    }

    public static void assertCarMarkersNotVisible() {
        search("Car Markers should not be visible")
                .descContains(getString(R.string.car_marker))
                .assertNotExist();
    }

    public static void assertRiderPositionMarkerVisible(long timeout) {
        search("Rider position marker should be visible")
                .descContains(getString(R.string.rider_marker_title))
                .assertExist(timeout);
    }

    public static void assertRiderPositionMarkerNotVisible(long timeout) {
        search("Rider position marker should not be visible")
                .descContains(getString(R.string.rider_marker_title))
                .assertNotExist(timeout);
    }

    public static void assertCarMarkersCount(int count) {
        search("Should have " + count + " car markers")
                .descContains(getString(R.string.car_marker))
                .assertCount(equalTo(count));
    }

    public static int getCarMarkersCount() {
        return search().descContains(getString(R.string.car_marker)).count();
    }


    public static void assertPickupMarkersVisible() {
        search("Pickup marker should be visible")
                .descContains(getString(R.string.pickup_marker))
                .assertExist();
    }

    public static void assertPickupMarkersVisible(long timeout) {
        search("Pickup marker should be visible")
                .descContains(getString(R.string.pickup_marker))
                .assertExist(timeout);
    }

    public static void assertPickupMarkersNotVisible() {
        search("Pickup marker should not be visible")
                .descContains(getString(R.string.pickup_marker))
                .assertNotExist();
    }

    public static void assertPickupMarkersNotVisible(long timeout) {
        search("Pickup marker should not be visible")
                .descContains(getString(R.string.pickup_marker))
                .assertNotExist(timeout);
    }

    public static void assertDestinationMarkersVisible() {
        search("Destination marker should be visible")
                .descContains(getString(R.string.destination_marker))
                .assertExist();
    }

    public static void assertDestinationMarkersVisible(long timeout) {
        search("Destination marker should be visible")
                .descContains(getString(R.string.destination_marker))
                .assertExist(timeout);
    }

    public static void assertDestinationMarkersNotVisible() {
        search("Destination marker should not be visible")
                .descContains(getString(R.string.destination_marker))
                .assertNotExist();
    }

    public static void assertDestinationMarkersNotVisible(long timeout) {
        search("Destination marker should not be visible")
                .descContains(getString(R.string.destination_marker))
                .assertNotExist(timeout);
    }

    public static void assertEtaMarkerVisible(String eta) {
        search("Eta \"" + eta + "\" should be visible")
                .descContains(getString(R.string.eta_marker_with_eta, eta))
                .assertExist();
    }

    public static void assertEtaMarkerNotVisible() {
        search("Eta should not be visible")
                .descContains(getString(R.string.eta_marker))
                .assertNotExist();
    }

    public static void waitEtaMarker(String eta) {
        waitEtaMarker(eta, IDLE_TIMEOUT_MS);
    }

    public static void waitEtaMarker(String eta, long timeoutMs) {
        search("Eta \"" + eta + "\" should be visible")
                .descContains(getString(R.string.eta_marker_with_eta, eta))
                .assertExist(timeoutMs);
    }

    public static void assertPrevRideMarkersVisible() {
        search("Prev ride marker should be visible")
                .descContains(getString(R.string.prev_ride_marker))
                .assertExist();
    }

    public static void assertPrevRideMarkersVisible(long timeout) {
        search("Prev ride marker should be visible")
                .descContains(getString(R.string.prev_ride_marker))
                .assertExist(timeout);
    }

    public static void assertPrevRideMarkersNotVisible() {
        search("Prev ride marker should not be visible")
                .descContains(getString(R.string.prev_ride_marker))
                .assertNotExist();
    }

    public static void assertPrevRideMarkersNotVisible(long timeout) {
        search("Prev ride marker should not be visible")
                .descContains(getString(R.string.prev_ride_marker))
                .assertNotExist(timeout);
    }

    public static void assertNextRideMarkersVisible() {
        search("Next ride marker should be visible")
                .descContains(getString(R.string.next_ride_marker))
                .assertExist();
    }

    public static void assertNextRideMarkersVisible(long timeout) {
        search("Next ride marker should be visible")
                .descContains(getString(R.string.next_ride_marker))
                .assertExist(timeout);
    }

    public static void assertNextRideMarkersNotVisible() {
        search("Next ride marker should not be visible")
                .descContains(getString(R.string.next_ride_marker))
                .assertNotExist();
    }

    public static void assertNextRideMarkersNotVisible(long timeout) {
        search("Next ride marker should not be visible")
                .descContains(getString(R.string.next_ride_marker))
                .assertNotExist(timeout);
    }

    public static void assertSurgeMarkerVisible(String title) {
        search("Surge area " + title + " should be visible")
                .descContains(title)
                .assertExist(10000);
    }

    public static void assertRouteVisible() {
        assertTrue("Route on map should be visible", MapUtils.hasRoute);
    }

    public static void assertRouteNotVisible() {
        assertFalse("Route on map should not be visible", MapUtils.hasRoute);
    }

    public static void zoomOut(final int percents) throws UiObjectNotFoundException {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject map = device.findObject(new UiSelector().descriptionContains(getString(R.string.google_map)));
        int maxPercentsPerPinch = 80; // can't pinch by 100% - it will trigger navigation drawer
        int steps = 10; // steps per gesture
        int percentsToPinch = percents;
        if (DeviceInfoUtil.isSmallScreen()) {
            // add one more pinch for small screens
            percentsToPinch += maxPercentsPerPinch;
        }
        if (percentsToPinch < maxPercentsPerPinch) {
            map.pinchIn(percentsToPinch, steps);
        } else {
            int numPinches = percentsToPinch / maxPercentsPerPinch;
            int left = percentsToPinch % maxPercentsPerPinch;
            for (int i = 0; i < numPinches; i++) {
                map.pinchIn(maxPercentsPerPinch, steps);
            }
            if (left > 0) {
                map.pinchIn(left, steps);
            }
        }
    }

    public static void zoomIn(final int percents) throws UiObjectNotFoundException {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject map = device.findObject(new UiSelector().descriptionContains(getString(R.string.google_map)));
        int maxPercentsPerPinch = 80; // can't pinch by 100% - it will trigger navigation drawer
        int steps = 10; // steps per gesture
        int percentsToPinch = percents;
        if (DeviceInfoUtil.isSmallScreen()) {
            // add one more pinch for small screens
            percentsToPinch += maxPercentsPerPinch;
        }
        if (percentsToPinch < maxPercentsPerPinch) {
            map.pinchOut(percentsToPinch, steps);
        } else {
            int numPinches = percentsToPinch / maxPercentsPerPinch;
            int left = percentsToPinch % maxPercentsPerPinch;
            for (int i = 0; i < numPinches; i++) {
                map.pinchOut(maxPercentsPerPinch, steps);
            }
            if (left > 0) {
                map.pinchOut(left, steps);
            }
        }
    }

    public static void zoomAndSearch(DeviceTestUtils.SearchBuilder searchBuilder, final int stepPercents, final int maxSteps, final int timeout) throws UiObjectNotFoundException {
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("Parameter 'maxSteps' should be positive");
        }
        for (int i = 0; i < maxSteps; i++) {
            if (stepPercents > 0) {
                zoomOut(stepPercents);
            } else {
                zoomIn(stepPercents);
            }
            if (searchBuilder.exists(timeout)) {
                // searched and found
                return;
            }
        }
        searchBuilder.fail();
    }

    public static void moveMapLeft(int value) throws UiObjectNotFoundException {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject map = device.findObject(new UiSelector().descriptionContains(getString(R.string.google_map)));
        Rect bounds = map.getBounds();
        int centerX = bounds.centerX();
        int centerY = bounds.centerY();
        device.drag(centerX, centerY, (centerX - value), centerY, 100);
    }

    public static void moveMapRight(int value) throws UiObjectNotFoundException {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject map = device.findObject(new UiSelector().descriptionContains(getString(R.string.google_map)));
        Rect bounds = map.getBounds();
        int centerX = bounds.centerX();
        int centerY = bounds.centerY();
        device.drag(centerX, centerY, (centerX + value), centerY, 100);
    }
}
