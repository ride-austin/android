package cases.s5_core_flow.s4_arrived;

import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.RiderLocationUpdate;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.helpers.MockHelper;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.SerializationHelper;

import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersCount;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertDestinationMarkersNotVisible;
import static com.rideaustin.utils.MapTestUtil.assertDestinationMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertPickupMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertRiderPositionMarkerNotVisible;
import static com.rideaustin.utils.MapTestUtil.assertRiderPositionMarkerVisible;
import static com.rideaustin.utils.MapTestUtil.moveMapLeft;
import static com.rideaustin.utils.MapTestUtil.moveMapRight;
import static com.rideaustin.utils.MapTestUtil.zoomIn;
import static com.rideaustin.utils.MapTestUtil.zoomOut;
import static com.rideaustin.utils.MatcherUtils.withEmptyText;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.swipeFromLeftEdge;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForToast;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Reference: RA-13085
 * Created by Sergey Petrov on 06/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverArrivedSuite extends BaseUITest {

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
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET,
                RequestType.ACCEPT_DRIVER_TERMS_200_PUT,
                RequestType.LOGOUT_200_POST);
    }

    @Test
    @TestCases("C1929309")
    public void lookAndFeel() {
        setArrivedState();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);

        onView(allOf(withId(R.id.navigate), withText(R.string.navigate))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.cancel), withText(R.string.cancel))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.starting_address), withText(containsString(PICKUP_ADDRESS)))).check(matches(isDisplayed()));
        onView(withId(R.id.finish_address)).check(matches(isDisplayed())).check(matches(withEmptyText()));
        onView(allOf(withId(R.id.button), withText(R.string.contact))).check(matches(isDisplayed()));
        assertPickupMarkersVisible();

        onView(withId(R.id.myLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.upgradeCarType)).check(matches(isDisplayed()));
    }

    @Test
    @TestCases({"C1929310", "C1929311"})
    public void testRiderInfo() {
        setArrivedState();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        assertPickupMarkersVisible();

        onView(AllOf.allOf(withId(R.id.toolbarTitle), withText(containsString(RIDER_NAME)))).check(matches(isDisplayed()));
        onView(AllOf.allOf(withId(R.id.toolbarTitle), withText(containsString("STANDARD")))).check(matches(isDisplayed()));
        onView(AllOf.allOf(withId(R.id.starting_address), withText(containsString(PICKUP_ADDRESS)))).check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1929312")
    public void mapVerification() throws UiObjectNotFoundException {
        setArrivedState();

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

    @Test
    @TestCases("C1929313")
    public void destinationShouldBeVisible() throws UiObjectNotFoundException {
        setArrivedStateWithDestination();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        onView(withId(R.id.finish_address))
                .check(matches(isDisplayed())).check(matches(withText(DESTINATION_ADDRESS)));

        waitFor("Map animation", 2000);

        // in arrived state car + pickup shown by default
        assertDestinationMarkersNotVisible();

        zoomOut(200);

        // but marker should be on map
        assertDestinationMarkersVisible(5000);
    }

    @Test
    @TestCases({"C1929315", "C1929316"})
    public void riderLocationUpdatesAndDisappears() {
        setArrivedState();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        assertPickupMarkersVisible();

        mockRiderLocationUpdateEvent(30.416855, -97.749884);
        assertRiderPositionMarkerVisible(5000);

        mockRiderLocationUpdateEvent(31.416855, -98.749884);
        assertRiderPositionMarkerNotVisible(5000);

        mockRiderLocationUpdateEvent(30.416855, -97.749884);
        assertRiderPositionMarkerVisible(5000);

        removeRequests(RequestType.EVENT_RIDER_LOCATION_UPDATED);
        mockRequests(RequestType.EVENTS_EMPTY_200_GET);

        // Marker should disappear in 5 seconds
        // riderLiveLocation.expirationTime = 5
        assertRiderPositionMarkerNotVisible(10000);
    }

    @Test
    @TestCases({"C1929317", "C1929318"})
    public void riderDestinationUpdates() throws InterruptedException {
        setArrivedState();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        waitFor("camera animation", 2000);
        onView(withId(R.id.finish_address)).check(matches(withEmptyText()));

        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        chainRequests(RequestType.EVENT_END_LOCATION_UPDATED, RequestType.EVENTS_EMPTY_200_GET);

        Matchers.waitFor(condition("Destination should be updated")
                .withMatcher(withId(R.id.finish_address))
                .withCheck(withText("Austin")));

        // NOTE: on Android destination marker in arrived state is not displayed
        // we still focus on car + pickup
        assertDestinationMarkersNotVisible();
    }

    /**
     * Reference: RA-13227
     */
    @Test
    @TestCases("C1929323")
    public void adminCancelsRide() throws InterruptedException {
        setArrivedState();

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
     * Reference: RA-13228
     */
    @Test
    @TestCases("C1929324")
    public void notReceivingNewRideRequest() throws InterruptedException {
        setArrivedState();

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
     * Reference: RA-13229
     */
    @Test
    @TestCases("C1929325")
    public void startTripReminder() throws InterruptedException {
        setArrivedState();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        waitFor("camera animation", 2000);
        onView(withId(R.id.finish_address)).check(matches(withEmptyText()));

        // move away more than 50m
        mockLocation(30.418414, -97.751308);
        Matchers.waitFor(condition("Should show trip start reminder")
                .withMatcher(withText(R.string.motion_detected_start_dialog_title)));

        // cancel
        onView(withId(R.id.md_buttonDefaultNegative)).perform(click());
        onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()));

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        assertPickupMarkersVisible();
        onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()));

        // TODO: rest of the testcase requires RA-9429
    }

    /**
     * Reference: RA-13231
     */
    @Test
    @TestCases("C1929327")
    public void notAbleToLogout() throws InterruptedException {
        setArrivedState();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");
        waitFor("camera animation", 2000);
        onView(withId(R.id.finish_address)).check(matches(withEmptyText()));

        onView(withId(R.id.mapContainer)).perform(swipeFromLeftEdge());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));
        onView(withId(R.id.signOut)).perform(scrollTo(), click());

        waitForToast(withText(R.string.cannot_logout_during_ride));
    }

    /**
     * Reference: RA-13234
     */
    @Test
    @TestCases("C1929328")
    public void shouldStartTrip() throws InterruptedException {
        setArrivedStateWithDestination();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");
        waitFor("camera animation", 2000);
        onView(withId(R.id.finish_address))
                .check(matches(isDisplayed())).check(matches(withText(DESTINATION_ADDRESS)));

        NavigationUtils.startTrip(this, null);
    }

    /**
     * Reference: RA-13377
     */
    @Test
    @TestCases("C1929341")
    public void navigateShouldStartTrip() throws InterruptedException {
        setArrivedState();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        waitForDisplayed(R.id.navigate);
        onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()));

        onView(withId(R.id.navigate)).perform(click());
        Matchers.waitFor(condition("Should show start trip confirmation")
                .withMatcher(withText(R.string.starting_trip_confirmation_dialog_msg)));

        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        Matchers.waitFor(condition("Should show navigation app choice")
                .withMatcher(withText(R.string.choose_navigation_app)));

        onView(withId(R.id.md_buttonDefaultNegative)).perform(click());

        onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish)))
                .check(matches(isDisplayed()));
    }

    /**
     * Recovery - kill app, restore from background, lock/unlock device
     * Also need for RA-11057
     */
    @Test
    @TestCases("C1929329")
    public void recovery() {
        setArrivedState();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // check button shows in ride state
        onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()));
    }

    private void setArrivedState() {
        Ride ride = getResponse("RIDE_DRIVER_REACHED_200", Ride.class);
        ride.getRequestedCarType().setCarCategory("REGULAR");
        ride.getRequestedCarType().setTitle("STANDARD");
        ride.setStartAddress(PICKUP_ADDRESS);
        ride.getRider().setFirstname(RIDER_NAME);

        ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_REACHED_200_GET, ride, this);
        mockRequest(RequestType.ACTIVE_DRIVER_REACHED_200_GET, activeDriver);
        mockRequest(RequestType.RIDE_DRIVER_REACHED_200_GET, ride);
    }

    private void setArrivedStateWithDestination() {
        Ride ride = getResponse("RIDE_DRIVER_REACHED_200", Ride.class);
        ride.getRequestedCarType().setCarCategory("REGULAR");
        ride.getRequestedCarType().setTitle("STANDARD");
        ride.setEndAddress(DESTINATION_ADDRESS);
        ride.setEndLocationLat(30.415711);
        ride.setEndLocationLong(-97.747448);

        ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_REACHED_200_GET, ride, this);
        mockRequest(RequestType.ACTIVE_DRIVER_REACHED_200_GET, activeDriver);
        mockRequest(RequestType.RIDE_DRIVER_REACHED_200_GET, ride);
    }

    private void mockRiderLocationUpdateEvent(double lat, double lng) {
        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        removeRequests(RequestType.EVENT_RIDER_LOCATION_UPDATED);

        RiderLocationUpdate locationUpdate = new RiderLocationUpdate();
        locationUpdate.setLat(lat);
        locationUpdate.setLng(lng);
        locationUpdate.setTimeRecorded(System.currentTimeMillis());

        Event event = new Event();
        event.setId(12345L);
        event.setEventType(RideStatus.RIDER_LOCATION_UPDATED);
        event.setParameters(SerializationHelper.serialize(locationUpdate));

        List<Event> events = new ArrayList<>();
        events.add(event);

        mockRequest(RequestType.EVENT_RIDER_LOCATION_UPDATED, events);
    }


}
