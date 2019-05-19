package cases.s5_core_flow.s1_requesting.s1_main;

import android.os.RemoteException;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.RecentPlacesUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import timber.log.Timber;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.CarSliderUtils.carTypeSelected;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertDestinationMarkersNotVisible;
import static com.rideaustin.utils.MapTestUtil.assertDestinationMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertPickupMarkersNotVisible;
import static com.rideaustin.utils.MapTestUtil.assertPickupMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.moveMapLeft;
import static com.rideaustin.utils.MatcherUtils.withListSize;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.getText;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotEmptyText;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

/**
 * Created by crossover on 05/05/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainSuite1 extends BaseUITest {

    public static final String RAMIREZ_LANE = "9121-9243 Ramirez Lane";
    public static final int USER_1_ID = 1443;

    @Rule
    public ActivityTestRule<NavigationDrawerActivity> activityRule = new RaActivityRule<>(NavigationDrawerActivity.class, false, false);

    @Override
    public void setUp() {
        super.setUp();
        initMockResponseFactory(RiderMockResponseFactory.class);
        mockRequests(RequestType.GLOBAL_APP_INFO_200_GET,
                RequestType.LOGIN_SUCCESS_200_POST,
                RequestType.TOKENS_200_POST,
                RequestType.RIDER_DATA_NO_RIDE_200_GET,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.ACDR_REGULAR_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CONFIG_RIDER_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.CONFIG_ZIPCODES_200_GET);
    }

    private void doInitialTests() {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("sinan@rider.com", "123987456");

        // Map screen is opened
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // Pick-up location is automatically set to the address that corresponds to the rider's GPS location
        waitForNotEmptyText(R.id.pickup_address);

        // "SET PICKUP LOCATION" pin is shown
        waitForDisplayed(R.id.set_pickup_location);

        // Driver's car is visible on the screen
        assertCarMarkersVisible();

        // Car types slider is displayed in the bottom and 'REGULAR' is selected by default
        waitForDisplayed(R.id.car_types_slider);
        onView(allOf(withId(R.id.car_types_slider), isDisplayed())).check(matches(carTypeSelected(0)));
    }

    /**
     * Requesting: Design and components
     */
    @Test
    @TestCases("C1930554")
    public void testRequestingDesignAndComponents() throws UiObjectNotFoundException {

        doInitialTests();

        // Save address
        String address = getText(allOf(withId(R.id.pickup_address), isDisplayed()));

        // Tap pin to set pick up location
        onView(allOf(withId(R.id.pickup_location), isDisplayed())).perform(click());

        // Request panel appears
        waitForDisplayed(R.id.requestPanel);

        // Pickup location address is set to the same
        ViewInteraction pickupAddressView = onView(allOf(withId(R.id.pickup_address), isDisplayed()));
        pickupAddressView.check(matches(withText(address)));

        // Pin disappeared
        onView(withId(R.id.pickup_location)).check(matches(not(isDisplayed())));

        // All following buttons and words are visible and complete, regardless of the screen size
        // Estimate fare
        onView(allOf(withId(R.id.btn_fare_estimate), withText(R.string.fare_estimate))).check(matches(isDisplayed()));
        // Promo code
        onView(allOf(withId(R.id.btn_promo), withText(R.string.promo_codes))).check(matches(isDisplayed()));
        // Cancel
        onView(allOf(withId(R.id.btn_cancel_pickup), withText(R.string.btn_cancel))).check(matches(isDisplayed()));
        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).check(matches(isDisplayed()));

        waitFor(R.id.tv_estimate_pickup_time, allOf(withText(allOf(startsWith("Pickup time is approximately "))), isDisplayed()), IDLE_TIMEOUT_MS);

        // Tap [Cancel]
        onView(withId(R.id.btn_cancel_pickup)).perform(click());

        // Request panel disappears
        onView(withId(R.id.requestPanel)).check(doesNotExist());

        // "SET PICKUP LOCATION" pin is shown
        waitForDisplayed(R.id.set_pickup_location);
        // "SET PICKUP LOCATION" pin is shown with ETA (estimated time of arrival) at the current GPS location
        onView(allOf(withId(R.id.set_pickup_location), isDisplayed())).check(matches(withText(R.string.set_pickup_location)));
        waitForNotEmptyText(R.id.time_to_pickup);
        onView(allOf(withId(R.id.minutes), isDisplayed())).check(matches(withText(R.string.minutes)));

        // Save pickup time
        String timeToPickup = getText(allOf(withId(R.id.time_to_pickup), isDisplayed()));
        waitForNotEmptyText(R.id.pickup_address);

        waitForDisplayed(R.id.car_types_slider);
        onView(allOf(withId(R.id.car_types_slider), isDisplayed())).check(matches(carTypeSelected(0)));

        removeRequests(RequestType.ACDR_REGULAR_200_GET);
        mockRequests(RequestType.ACDR_REGULAR_MOVED_200_GET);

        UiDevice device = UiDevice.getInstance(getInstrumentation());
        // Move pin
        UiObject map = device.findObject(new UiSelector().descriptionContains(getString(R.string.google_map)));
        map.swipeDown(5);
        waitFor("Swipe Animation", 1500);

        // Address has changed correspondingly
        waitFor(R.id.pickup_address, not(withText(address)), IDLE_TIMEOUT_MS);

        // ETA (estimated time of arrival) has changed on "SET PICKUP LOCATION" pin
        waitForNotEmptyText(R.id.time_to_pickup);
        waitFor(R.id.time_to_pickup, not(withText(timeToPickup)), IDLE_TIMEOUT_MS);

        // Save pickup time
        timeToPickup = getText(allOf(withId(R.id.time_to_pickup), isDisplayed()));

        address = getText(allOf(withId(R.id.pickup_address), isDisplayed()));

        // Tap pin
        onView(withId(R.id.pickup_location)).perform(click());
        tryToCloseFarAwayDialog();

        // Request panel appears
        waitForDisplayed(R.id.requestPanel);

        // Pickup location address is set to the same
        waitFor(R.id.pickup_address, withText(address), IDLE_TIMEOUT_MS);
        //pickupAddressView.check(matches(withText(address)));

        // Pin disappeared
        onView(withId(R.id.pickup_location)).check(matches(not(isDisplayed())));

        // 'pick up time is approximately .. mins' text at the bottom
        waitFor(R.id.tv_estimate_pickup_time, allOf(withText(getAppContext().getResources().getQuantityString(R.plurals.text_pickup_time, Integer.parseInt(timeToPickup), timeToPickup)), isDisplayed()), IDLE_TIMEOUT_MS);
    }

    @Test
    @TestCases("C1930555")
    public void testRequestingMapVerificationZoomInAndOutMove() throws UiObjectNotFoundException {

        doInitialTests();

        // Save address
        String address = getText(allOf(withId(R.id.pickup_address), isDisplayed()));

        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject map = device.findObject(new UiSelector().descriptionContains(getString(R.string.google_map)));

        // Zoom out 60%
        map.pinchIn(60, 60);
        waitFor("pinchIn Animation", 1000);
        assertCarMarkersVisible();

        // Zoom in 50% so cars should not be out of map
        map.pinchOut(50, 50);
        waitFor("pinchOut Animation", 1000);
        assertCarMarkersVisible();

        // Swipe and click My Location
        map.swipeLeft(50);
        waitFor("swipeLeft Animation", 1000);
        onView(allOf(withId(R.id.myLocationButton), isDisplayed())).perform(click());
        waitFor("My location Animation", 1000);
        assertCarMarkersVisible();

        // Tap pin to set pick up location
        onView(allOf(withId(R.id.pickup_location), isDisplayed())).perform(click());

        // Pickup location address is set to the same
        ViewInteraction pickupAddressView = onView(allOf(withId(R.id.pickup_address), isDisplayed()));
        pickupAddressView.check(matches(withText(address)));

        // Pin disappeared
        onView(withId(R.id.pickup_location)).check(matches(not(isDisplayed())));

        // Request panel appears
        waitForDisplayed(R.id.requestPanel);

        // 'pick up time is approximately .. mins' text at the bottom
        waitFor(R.id.tv_estimate_pickup_time, allOf(withText(allOf(startsWith("Pickup time is approximately "))), isDisplayed()), IDLE_TIMEOUT_MS);

        assertCarMarkersVisible();

        // Zoom out 60%
        map.pinchIn(60, 60);
        assertCarMarkersVisible();

        // Zoom out 50%
        map.pinchOut(50, 50);
        assertCarMarkersVisible();

        // Move
        map.swipeLeft(50);
        onView(allOf(withId(R.id.myLocationButton), isDisplayed())).perform(click());
        waitFor("My location Animation", 1000);
        assertCarMarkersVisible();
    }

    @Test
    @TestCases("C1930557")
    public void testPickupLocation() throws UiObjectNotFoundException, RemoteException, InterruptedException {
        NavigationUtils.startActivity(activityRule);
        mockLocation(30.245110, -97.653880);
        NavigationUtils.throughLogin("sinan@rider.com", "123987456");

        // Map screen is opened
        waitForCompletelyDisplayed(withContentDescription(getString(R.string.google_map)), "map");

        // Pick-up location is automatically set to the address that corresponds to the rider's GPS location
        Matchers.waitFor(condition("Pickup address is set")
                .withMatcher(allOf(withId(R.id.pickup_address), withText(RAMIREZ_LANE))));

        // "SET PICKUP LOCATION" pin is shown
        waitForDisplayed(R.id.set_pickup_location);

        // Car types slider is displayed in the bottom and 'REGULAR' is selected by default
        waitForDisplayed(R.id.car_types_slider);
        onView(allOf(withId(R.id.car_types_slider), isDisplayed())).check(matches(carTypeSelected(0)));

        // green pin is displayed
        onView(withId(R.id.set_pickup_location)).perform(click());
        tryToCloseFarAwayDialog();
        assertPickupMarkersVisible(5000);

        // request panel visible
        waitForDisplayed(R.id.requestPanel);
        waitForDisplayed(R.id.btn_request_ride);

        // cancel request
        onView(withId(R.id.btn_cancel_pickup)).perform(click());

        waitForDisplayed(R.id.set_pickup_location);
        assertPickupMarkersNotVisible(1000);
        onView(allOf(withId(R.id.pickup_address), withText(RAMIREZ_LANE))).check(matches(isDisplayed()));

        moveMapLeft(500);
        waitFor("for map", 3000);

        // green pin is displayed
        onView(withId(R.id.set_pickup_location)).perform(click());
        tryToCloseFarAwayDialog();
        assertPickupMarkersVisible(5000);

        // request panel visible
        waitForDisplayed(R.id.requestPanel);
        waitForDisplayed(R.id.btn_request_ride);

        onView(withId(R.id.pickup_address)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.listView)).atPosition(4).perform(click());

        waitForDisplayed(R.id.btn_request_ride);
        onView(allOf(withId(R.id.pickup_address), withText(RAMIREZ_LANE))).check(matches(isDisplayed()));
        assertPickupMarkersVisible(5000);

        // cancel request
        onView(withId(R.id.btn_cancel_pickup)).perform(click());

        waitForDisplayed(R.id.set_pickup_location);
        assertPickupMarkersNotVisible(1000);
        onView(allOf(withId(R.id.pickup_address), withText(RAMIREZ_LANE))).check(matches(isDisplayed()));

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();
        onView(allOf(withId(R.id.pickup_address), withText(RAMIREZ_LANE))).check(matches(isDisplayed()));

        onView(withId(R.id.pickup_address)).perform(click());
        onView(withId(R.id.clearButton)).perform(click());
        onView(withId(R.id.addressInput)).perform(typeText("1sdfhhsfkh434kdjh"));
        onView(withId(R.id.listView)).check(matches(withListSize(1)));
    }

    @Test
    @TestCases("C1930559")
    public void testSetUpdateClearDestination() throws UiObjectNotFoundException, RemoteException, InterruptedException {
        NavigationUtils.startActivity(activityRule);
        mockLocation(30.245110, -97.653880);
        RecentPlacesUtils.mockRecentPlaces(USER_1_ID);
        NavigationUtils.throughLogin("sinan@rider.com", "123987456");

        // Map screen is opened
        waitForCompletelyDisplayed(withContentDescription(getString(R.string.google_map)), "map");

        // green pin is displayed
        onView(withId(R.id.set_pickup_location)).perform(click());
        tryToCloseFarAwayDialog();
        assertPickupMarkersVisible(5000);

        // set destination from recent places
        onView(withId(R.id.destination_address)).perform(click());
        waitForCompletelyDisplayed(R.id.addressInput);
        onData(anything()).inAdapterView(withId(R.id.listView)).atPosition(4).perform(click());
        assertDestinationMarkersVisible(3000);
        assertPickupMarkersVisible();

        // update destination from recent places
        onView(withId(R.id.destination_address)).perform(click());
        waitForCompletelyDisplayed(R.id.addressInput);
        onData(anything()).inAdapterView(withId(R.id.listView)).atPosition(5).perform(click());
        assertDestinationMarkersVisible(3000);
        assertPickupMarkersVisible();

        // update destination by search address
        onView(withId(R.id.destination_address)).perform(click());
        waitForCompletelyDisplayed(R.id.addressInput);
        onView(withId(R.id.clearButton)).perform(click());
        onView(withId(R.id.addressInput)).perform(typeText("Airport"));

        DataInteraction dataInteraction = onData(hasToString(containsString("Airport")))
                .atPosition(0)
                .inAdapterView(withId(R.id.listView));
        Matchers.waitFor(condition().withData(dataInteraction));

        dataInteraction
                .check(matches(isDisplayed()))
                .perform(click());

        assertDestinationMarkersVisible(3000);
        assertPickupMarkersVisible();

        // remove destination
        onView(withId(R.id.destination_address)).perform(click());
        waitForCompletelyDisplayed(R.id.addressInput);
        onView(withId(R.id.clearButton)).perform(click());
        pressBack();

        assertDestinationMarkersNotVisible(3000);
        assertPickupMarkersVisible();
    }

    /**
     * This will run correctly only when executing by script.
     * It require to reset permissions before test is started
     *
     * @throws UiObjectNotFoundException
     * @throws RemoteException
     * @throws InterruptedException
     */
    @Test
    @TestCases("C1930578")
    public void testNoLocationPermissionsButAbleToRequest() throws UiObjectNotFoundException, RemoteException, InterruptedException {
        NavigationUtils.startActivity(activityRule);
        mockLocation(30.245110, -97.653880);
        RecentPlacesUtils.mockRecentPlaces(USER_1_ID);
        NavigationUtils.throughLogin("sinan@rider.com", "123987456", false);

        // set destination from recent places
        onView(withId(R.id.pickup_address)).perform(click());
        waitForCompletelyDisplayed(R.id.addressInput);
        onData(anything()).inAdapterView(withId(R.id.listView)).atPosition(3).perform(click());
        NavigationUtils.tryCloseLocationWarning();

        waitFor("for black pin ready", 5000);

        // Car types slider is displayed in the bottom and 'REGULAR' is selected by default
        waitForDisplayed(R.id.car_types_slider);
        onView(allOf(withId(R.id.car_types_slider), isDisplayed())).check(matches(carTypeSelected(0)));

        // green pin is displayed
        onView(withId(R.id.set_pickup_location)).perform(click());
        assertPickupMarkersVisible(3000);

        // request panel visible
        waitForDisplayed(R.id.requestPanel);
        waitForDisplayed(R.id.btn_request_ride);

        // update destination from recent places
        onView(withId(R.id.destination_address)).perform(click());
        waitForCompletelyDisplayed(R.id.addressInput);
        onData(anything()).inAdapterView(withId(R.id.listView)).atPosition(4).perform(click());
        assertDestinationMarkersVisible(3000);
        assertPickupMarkersVisible();

        // request panel visible
        waitForDisplayed(R.id.requestPanel);
        waitForDisplayed(R.id.btn_request_ride);

        atomicOnRequests(() -> {
            // mock requested state
            removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);
            mockRequests(RequestType.RIDE_REQUESTED_200_GET,
                    RequestType.CURRENT_RIDE_REQUESTED_200_GET,
                    RequestType.RIDE_REQUEST_200_POST);
        });

        onView(withId(R.id.btn_request_ride)).perform(click());
        waitForCompletelyDisplayed(R.id.cancel_pending_request);

        NavigationUtils.toCancelledState(this);
        NavigationUtils.clearRideState(this);
    }

    @Test
    @TestCases("C1930560")
    public void testSetUpdateDestinationAfterRequestIsSent() throws UiObjectNotFoundException, RemoteException, InterruptedException {
        NavigationUtils.startActivity(activityRule);
        mockLocation(30.245110, -97.653880);
        RecentPlacesUtils.mockRecentPlaces(USER_1_ID);
        NavigationUtils.throughLogin("sinan@rider.com", "123987456", true);

        // green pin is displayed
        onView(withId(R.id.set_pickup_location)).perform(click());
        assertPickupMarkersVisible(3000);

        // request panel visible
        waitForDisplayed(R.id.requestPanel);
        waitForDisplayed(R.id.btn_request_ride);


        atomicOnRequests(() -> {
            // mock requested state
            removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);
            mockRequests(RequestType.RIDE_REQUESTED_200_GET,
                    RequestType.CURRENT_RIDE_REQUESTED_200_GET,
                    RequestType.RIDE_REQUEST_200_POST,
                    RequestType.RIDE_UPDATE_200_PUT);
        });

        onView(withId(R.id.btn_request_ride)).perform(click());
        waitForCompletelyDisplayed(R.id.cancel_pending_request);


        // update destination from recent places
        onView(withId(R.id.destination_address)).perform(click());
        waitForCompletelyDisplayed(R.id.addressInput);
        onData(anything()).inAdapterView(withId(R.id.listView)).atPosition(5).perform(click());

        waitForViewInWindow(withText(R.string.update_ride_dialog_content));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        assertDestinationMarkersVisible(3000);
        assertPickupMarkersVisible();

        // update destination by search address
        onView(withId(R.id.destination_address)).perform(click());
        waitForCompletelyDisplayed(R.id.addressInput);
        onView(withId(R.id.clearButton)).perform(click());
        onView(withId(R.id.addressInput)).perform(typeText("Airport"));

        DataInteraction dataInteraction = onData(hasToString(containsString("Airport")))
                .atPosition(0)
                .inAdapterView(withId(R.id.listView));
        Matchers.waitFor(condition().withData(dataInteraction));

        dataInteraction
                .check(matches(isDisplayed()))
                .perform(click());

        waitForViewInWindow(withText(R.string.update_ride_dialog_content));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        assertDestinationMarkersVisible(3000);
        assertPickupMarkersVisible();

        // remove destination - destination should not be removed
        onView(withId(R.id.destination_address)).perform(click());
        waitForCompletelyDisplayed(R.id.addressInput);
        onView(withId(R.id.clearButton)).perform(click());
        pressBack();

        assertDestinationMarkersVisible(3000);
        assertPickupMarkersVisible();


        NavigationUtils.toCancelledState(this);
        NavigationUtils.clearRideState(this);
    }

    private void tryToCloseFarAwayDialog() {
        try {
            onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_ok))).perform(click());
        } catch (Exception e) {
            Timber.i(e, "Far away dialog is not shown");
        }
    }
}
