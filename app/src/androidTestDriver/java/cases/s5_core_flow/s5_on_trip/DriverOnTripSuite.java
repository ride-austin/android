package cases.s5_core_flow.s5_on_trip;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.helpers.MockHelper;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;

import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.DeviceTestUtils.search;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersCount;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertDestinationMarkersNotVisible;
import static com.rideaustin.utils.MapTestUtil.assertDestinationMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertPickupMarkersNotVisible;
import static com.rideaustin.utils.MapTestUtil.assertPickupMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertRouteNotVisible;
import static com.rideaustin.utils.MapTestUtil.assertRouteVisible;
import static com.rideaustin.utils.MapTestUtil.moveMapLeft;
import static com.rideaustin.utils.MapTestUtil.moveMapRight;
import static com.rideaustin.utils.MapTestUtil.zoomIn;
import static com.rideaustin.utils.MapTestUtil.zoomOut;
import static com.rideaustin.utils.MatcherUtils.withEmptyText;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;

/**
 * Reference: RA-10234
 * Created by Sergey Petrov on 06/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverOnTripSuite extends BaseUITest {

    private static final String RIDER_NAME = "RideOneTwoNine";
    private static final String PICKUP_ADDRESS = "11610 Jollyville Road, Austin, Texas";
    private static final String DESTINATION_ADDRESS = "11600 Research Blvd, Austin, Texas";

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new RaActivityRule<>(SplashActivity.class, false, false);

    @Override
    public void setUp() {
        super.setUp();
        initMockResponseFactory(DriverMockResponseFactory.class);
        mockRequests(RequestType.GLOBAL_APP_INFO_200_GET,
                RequestType.LOGIN_SUCCESS_200_POST,
                RequestType.CURRENT_USER_200_GET,
                RequestType.CONFIG_DRIVER_200_GET,
                RequestType.TOKENS_200_POST,
                RequestType.QUEUES_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CURRENT_DRIVER_200_GET,
                RequestType.ACTIVE_DRIVER_EMPTY_200_GET,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET,
                RequestType.ACCEPT_DRIVER_TERMS_200_PUT,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.LOGOUT_200_POST);
    }

    /**
     * Reference: RA-13247
     */
    @Test
    @TestCases("C1929335")
    public void lookAndFeel() {
        setActiveState();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        assertPickupMarkersVisible();
        assertDestinationMarkersNotVisible();

        onView(allOf(withId(R.id.navigate), withText(R.string.navigate))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.cancel), withText(R.string.cancel))).check(matches(not(isDisplayed())));

        onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.starting_address), withText(containsString(PICKUP_ADDRESS)))).check(matches(isDisplayed()));
        onView(withId(R.id.finish_address)).check(matches(isDisplayed())).check(matches(withEmptyText()));
        onView(allOf(withId(R.id.button), withText(R.string.contact))).check(matches(isDisplayed()));
        assertPickupMarkersVisible();

        onView(withId(R.id.myLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.upgradeCarType)).check(matches(not(isDisplayed())));
    }

    /**
     * Reference: RA-13248
     */
    @Test
    @TestCases("C1929336")
    public void pickupDestinationCarRoute() {
        setActiveStateWithDestination();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        assertPickupMarkersNotVisible(5000);
        assertDestinationMarkersVisible();

        onView(allOf(withId(R.id.starting_address), withText(containsString(PICKUP_ADDRESS))))
                .check(matches(isDisplayed()));
        onView(allOf(withId(R.id.finish_address), withText(containsString(DESTINATION_ADDRESS))))
                .check(matches(isDisplayed()));

        onView(AllOf.allOf(withId(R.id.toolbarTitle), withText(containsString(RIDER_NAME))))
                .check(matches(isDisplayed()));
        onView(AllOf.allOf(withId(R.id.toolbarTitle), withText(containsString("STANDARD"))))
                .check(matches(isDisplayed()));
    }

    /**
     * Reference: RA-13249
     */
    @Test
    @TestCases("C1929337")
    public void mapVerification() throws UiObjectNotFoundException {
        setActiveState();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        assertPickupMarkersVisible();

        zoomOut(20);

        assertCarMarkersCount(1);
        assertPickupMarkersVisible();

        zoomIn(20);

        assertCarMarkersCount(1);
        assertPickupMarkersVisible();

        moveMapLeft(1500);

        assertCarMarkersCount(0);

        moveMapRight(1500);

        assertCarMarkersCount(1);
        assertPickupMarkersVisible();
    }

    /**
     * Reference: RA-13251
     */
    @Test
    @TestCases("C1929338")
    public void routeChangedWhenLeavingTheRoute() {
        // start/end from RIDE_ROUTE_DRIVER_ASSIGNED_200_GET
        LatLng start = new LatLng(30.4171634, -97.7502001);
        LatLng middle = new LatLng(30.4172773, -97.75005159);
        LatLng out = new LatLng(30.417417, -97.749871);
        LatLng end = new LatLng(30.4169959, -97.74972219);

        mockLocation(start.latitude, start.longitude);

        Ride ride = getResponse("RIDE_ACTIVE_200", Ride.class);
        ride.getRequestedCarType().setCarCategory("REGULAR");
        ride.getRequestedCarType().setTitle("STANDARD");
        ride.setStartAddress("Pickup");
        ride.setStartLocationLat(start.latitude);
        ride.setStartLocationLong(start.longitude);
        ride.setEndAddress("Destination");
        ride.setEndLocationLat(end.latitude);
        ride.setEndLocationLong(end.longitude);
        ride.getRider().setFirstname(RIDER_NAME);
        removeRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
        ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, ride, this);
        mockRequest(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, activeDriver);
        mockRequest(RequestType.RIDE_ACTIVE_200_GET, ride);
        mockRequests(RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        assertPickupMarkersVisible();
        assertDestinationMarkersVisible();

        resetRequestStats(RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET);

        // move along the route
        mockLocation(middle.latitude, middle.longitude);
        assertCarMarkersVisible(5000);
        assertPickupMarkersNotVisible(5000);
        assertDestinationMarkersVisible(5000);
        verifyRequestsCount(RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET, is(0));

        // move out of route
        mockLocation(out.latitude, out.longitude);
        assertCarMarkersVisible(5000);
        assertPickupMarkersNotVisible(5000);
        assertDestinationMarkersVisible(5000);
        verifyRequestsCount(RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET, is(1));
    }

    /**
     * Reference: RA-13366, RA-13371
     */
    @Test
    @TestCases({"C1929339", "C1929340"})
    public void riderDestinationUpdates() throws InterruptedException {
        setActiveState();
        mockRequests(RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        assertDestinationMarkersNotVisible();
        assertRouteNotVisible();

        onView(allOf(withId(R.id.starting_address), withText(containsString(PICKUP_ADDRESS))))
                .check(matches(isDisplayed()));
        onView(allOf(withId(R.id.finish_address), withEmptyText()))
                .check(matches(isDisplayed()));

        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        chainRequests(RequestType.EVENT_END_LOCATION_UPDATED, RequestType.EVENTS_EMPTY_200_GET);

        Matchers.waitFor(condition("Destination should be updated")
                .withMatcher(withId(R.id.finish_address))
                .withCheck(withText("Austin")));

        waitFor("camera animation", 2000);

        assertCarMarkersCount(1);
        assertDestinationMarkersVisible();
        assertRouteVisible();
    }

    /**
     * Reference: RA-13378
     */
    @Test
    @TestCases("C1929342")
    public void notReceivingNewRideRequest() throws InterruptedException {
        setActiveState();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        waitFor("camera animation", 2000);
        onView(withId(R.id.finish_address)).check(matches(withEmptyText()));
        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        chainRequests(RequestType.EVENT_RIDE_REQUESTED, RequestType.EVENTS_EMPTY_200_GET);

        waitFor("wait for event", 5000);

        onView(withId(R.id.pending_pickup)).check(doesNotExist());
    }

    /**
     * Reference: RA-13379
     */
    @Test
    @TestCases("C1929343")
    public void adminCancelsRide() throws InterruptedException {
        setActiveState();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        waitFor("camera animation", 2000);
        onView(withId(R.id.finish_address)).check(matches(withEmptyText()));

        NavigationUtils.toEmptyState(this);
        mockEvent(RequestType.EVENT_ADMIN_CANCELLED);
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);

        Matchers.waitFor(condition("Should show admin cancelled message")
                .withMatcher(withText(R.string.ride_cancelled_by_admin)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        assertCarMarkersVisible(5000);

        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline)))
                .check(matches(isDisplayed()));
    }

    /**
     * Reference: RA-13380
     */
    @Test
    @TestCases("C1929344")
    public void endTripReminderWhileInBackground() throws UiObjectNotFoundException {
        setActiveStateWithDestination();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        waitFor("camera animation", 2000);

        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.pressHome();
        device.openNotification();

        search().pkg("com.android.systemui")
                .res("com.android.systemui:id/notification_stack_scroller")
                .assertExist(5000);

        mockLocation(30.415711, -97.747448);

        search().text(R.string.finish_ride_notification_msg).assertExist(5000);
        search().text(R.string.finish_ride_notification_msg).object().click();

        onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish)))
                .check(matches(isDisplayed()));
    }

    /**
     * Recovery - kill app, restore from background, lock/unlock device
     * Also need for RA-11057
     */
    @Test
    @TestCases("C1929347")
    public void recovery() {
        removeRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
        mockRequests(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET,
                RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                RequestType.PENDING_EVENTS_200_POST);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // check button shows in ride state
        onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).check(matches(isDisplayed()));
    }

    private void setActiveState() {
        Ride ride = getResponse("RIDE_ACTIVE_200", Ride.class);
        ride.getRequestedCarType().setCarCategory("REGULAR");
        ride.getRequestedCarType().setTitle("STANDARD");
        ride.setStartAddress(PICKUP_ADDRESS);
        ride.setEndAddress(null);
        ride.setEndLocationLat(null);
        ride.setEndLocationLong(null);
        ride.getRider().setFirstname(RIDER_NAME);
        removeRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
        ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, ride, this);
        mockRequest(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, activeDriver);
        mockRequest(RequestType.RIDE_ACTIVE_200_GET, ride);
    }

    private void setActiveStateWithDestination() {
        Ride ride = getResponse("RIDE_ACTIVE_200", Ride.class);
        ride.getRequestedCarType().setCarCategory("REGULAR");
        ride.getRequestedCarType().setTitle("STANDARD");
        ride.setStartAddress(PICKUP_ADDRESS);
        ride.setEndAddress(DESTINATION_ADDRESS);
        ride.setEndLocationLat(30.415711);
        ride.setEndLocationLong(-97.747448);
        ride.getRider().setFirstname(RIDER_NAME);
        removeRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
        ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, ride, this);
        mockRequest(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, activeDriver);
        mockRequest(RequestType.RIDE_ACTIVE_200_GET, ride);
    }

}
