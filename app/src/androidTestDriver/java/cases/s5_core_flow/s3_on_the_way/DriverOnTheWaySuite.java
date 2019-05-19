package cases.s5_core_flow.s3_on_the_way;

import android.os.RemoteException;
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
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.RideCancellationConfig;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.RiderLocationUpdate;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.SerializationHelper;
import com.rideaustin.utils.ViewActionUtils;

import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersCount;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertDestinationMarkersNotVisible;
import static com.rideaustin.utils.MapTestUtil.assertPickupMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertRiderPositionMarkerVisible;
import static com.rideaustin.utils.MapTestUtil.moveMapLeft;
import static com.rideaustin.utils.MapTestUtil.moveMapRight;
import static com.rideaustin.utils.MapTestUtil.zoomIn;
import static com.rideaustin.utils.MapTestUtil.zoomOut;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.MatcherUtils.isNotDecorView;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Reference: RA-10232
 * Created by Sergey Petrov on 06/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverOnTheWaySuite extends BaseUITest {

    public static final String RIDER_NAME = "RideOneTwoNine";
    private static final String PICKUP_ADDRESS = "11610 Jollyville Road, Austin, Texas";

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
                RequestType.ACCEPT_RIDE_200_POST,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CURRENT_DRIVER_200_GET,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                RequestType.PENDING_EVENTS_200_POST,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE);
    }


    @Test
    @TestCases("C1929287")
    public void lookAndFeel() throws InterruptedException {
        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        onView(allOf(withId(R.id.navigate), withText(R.string.navigate))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.cancel), withText(R.string.cancel))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.starting_address), withText(containsString(PICKUP_ADDRESS)))).check(matches(isDisplayed()));
        onView(withId(R.id.finish_address)).check(matches(not(isDisplayed())));
        onView(allOf(withId(R.id.button), withText(R.string.contact))).check(matches(isDisplayed()));
        assertPickupMarkersVisible();

        onView(withId(R.id.myLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.upgradeCarType)).check(matches(isDisplayed()));

        cancelRideByRider();
    }

    /**
     * Recovery - Loose internet connection, kill app, restore from background, lock/unlock device
     * Also need for RA-11057
     */
    @Test
    @TestCases("C1929305")
    public void recovery() throws UiObjectNotFoundException, InterruptedException, RemoteException {
        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        assertPickupMarkersVisible();
        assertDestinationMarkersNotVisible();

        onView(AllOf.allOf(withId(R.id.toolbarTitle), withText(containsString(RIDER_NAME)))).check(matches(isDisplayed()));
        onView(AllOf.allOf(withId(R.id.toolbarTitle), withText(containsString("STANDARD")))).check(matches(isDisplayed()));
        onView(AllOf.allOf(withId(R.id.starting_address), withText(containsString(PICKUP_ADDRESS)))).check(matches(isDisplayed()));

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        onView(AllOf.allOf(withId(R.id.toolbarTitle), withText(containsString(RIDER_NAME)))).check(matches(isDisplayed()));
        onView(AllOf.allOf(withId(R.id.toolbarTitle), withText(containsString("STANDARD")))).check(matches(isDisplayed()));
        onView(AllOf.allOf(withId(R.id.starting_address), withText(containsString(PICKUP_ADDRESS)))).check(matches(isDisplayed()));

        assertCarMarkersCount(1);
        assertPickupMarkersVisible();
        assertDestinationMarkersNotVisible();

        DeviceTestUtils.setAirplaneMode(true);
        setNetworkError(true);

        waitFor(condition().withView(onView(withText(R.string.connection_problems))));

        DeviceTestUtils.setAirplaneMode(false);
        setNetworkError(false);

        waitFor(condition().withMatcher(withText(R.string.connection_problems)).withCheck(not(isDisplayed())));

        // it doesn't recover connection immediately after airplane mode is enabled
        // so I need to wait with cancel call
        ViewActionUtils.waitFor("connection established again", 15000);

        onView(AllOf.allOf(withId(R.id.toolbarTitle), withText(containsString(RIDER_NAME)))).check(matches(isDisplayed()));
        onView(AllOf.allOf(withId(R.id.toolbarTitle), withText(containsString("STANDARD")))).check(matches(isDisplayed()));
        onView(AllOf.allOf(withId(R.id.starting_address), withText(containsString(PICKUP_ADDRESS)))).check(matches(isDisplayed()));

        assertCarMarkersCount(1);
        assertPickupMarkersVisible();
        assertDestinationMarkersNotVisible();

        cancelRideByRider();
    }

    @Test
    @TestCases({"C1929288", "C1929289"})
    public void riderNameCarTypePickupLocationRiderLocationDriverCar() throws InterruptedException {
        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        assertPickupMarkersVisible();

        onView(AllOf.allOf(withId(R.id.toolbarTitle), withText(containsString(RIDER_NAME)))).check(matches(isDisplayed()));
        onView(AllOf.allOf(withId(R.id.toolbarTitle), withText(containsString("STANDARD")))).check(matches(isDisplayed()));
        onView(AllOf.allOf(withId(R.id.starting_address), withText(containsString(PICKUP_ADDRESS)))).check(matches(isDisplayed()));

        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        cancelRideByRider();
    }

    @Test
    @TestCases("C1929290")
    public void mapVerificationZoomInZoomOutMoveMap() throws InterruptedException, UiObjectNotFoundException {
        setRideAssignedSate();

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
        assertPickupMarkersVisible(5000);

        cancelRideByRider();

    }

    @Test
    @TestCases("C1929293")
    public void riderLocationIsUpdatedWhenRiderIsMoving() throws InterruptedException, UiObjectNotFoundException {
        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        ViewActionUtils.waitFor("camera animation", 2000);

        mockRiderLocationUpdateEvent(30.416855, -97.749884);
        assertRiderPositionMarkerVisible(4000);

        mockRiderLocationUpdateEvent(30.416855, -97.749784);
        ViewActionUtils.waitFor("update event", 4000);
        assertRiderPositionMarkerVisible(4000);

        setRideArrivedSate();

        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive)))
                .check(matches(isDisplayed()))
                .perform(swipeRight());

        waitFor(condition().withView(onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))));

        mockRiderLocationUpdateEvent(30.416855, -97.749884);
        assertRiderPositionMarkerVisible(4000);

        cancelRideByRider();

    }

    @Test
    @TestCases("C1929291")
    public void destinationShouldNotBeVisible() throws InterruptedException, UiObjectNotFoundException {
        setRideAssignedSateWithDestination();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        ViewActionUtils.waitFor("camera animation", 2000);
        onView(withId(R.id.finish_address)).check(matches(not(isDisplayed())));

        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        mockRequests(RequestType.EVENT_END_LOCATION_UPDATED);

        ViewActionUtils.waitFor("event", 4000);

        onView(withId(R.id.finish_address)).check(matches(not(isDisplayed())));

        cancelRideByRider();

    }

    @Test
    @TestCases("C1929297")
    public void driverCancelRide() throws InterruptedException, UiObjectNotFoundException {
        setRideAssignedSateWithDestination();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        ViewActionUtils.waitFor("camera animation", 2000);
        onView(withId(R.id.cancel)).perform(click());
        waitFor(condition("confirmation dialog").withView(onView(withText(R.string.text_cancel_ride_confirmation))));

        mockRequests(RequestType.RIDE_CANCEL_200_DELETE, RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
        removeRequests(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET);

        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());

        waitFor(condition().withView(onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline)))));

    }

    @Test
    public void driverCancelRideWithFeedback() throws InterruptedException {
        removeRequests(RequestType.CONFIG_DRIVER_200_GET);
        GlobalConfig config = getResponse("CONFIG_GLOBAL_200", GlobalConfig.class);
        config.setRideCancellationConfig(new RideCancellationConfig());
        config.getRideCancellationConfig().setEnabled(true);
        mockRequest(RequestType.CONFIG_DRIVER_200_GET, config);
        mockRequests(RequestType.RIDE_CANCELLATION_REASONS_200_GET);

        setRideAssignedSateWithDestination();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");
        ViewActionUtils.waitFor("camera animation", 2000);
        onView(withId(R.id.cancel)).perform(click());

        // show ride cancellation feedback
        waitFor(condition("Ride cancellation reasons")
                .withMatcher(withText(R.string.cancel_feedback_driver_title)));

        waitFor(condition("Ride cancellation reason 1")
                .withMatcher(withText("First reason")));

        waitFor(condition("Ride cancellation reason 2")
                .withMatcher(withText("Second reason")));

        waitFor(condition("Ride cancellation reason 3")
                .withMatcher(withText("Third reason")));

        waitFor(condition("Ride cancellation reason 4")
                .withMatcher(withText("Other")));

        onView(withHint(R.string.cancel_feedback_message_hint)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btn_submit))
                .check(matches(withText(R.string.cancel_feedback_driver_ok)))
                .check(matches(not(isEnabled())))
                .check(matches(isDisplayed()));
        onView(withId(R.id.btn_cancel))
                .check(matches(withText(R.string.cancel_feedback_driver_cancel)))
                .check(matches(isEnabled()))
                .check(matches(isDisplayed()));

        // cancel
        onView(withId(R.id.btn_cancel)).perform(click());
        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        onView(allOf(withId(R.id.navigate), withText(R.string.navigate))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.cancel), withText(R.string.cancel))).check(matches(isDisplayed()));

        onView(withId(R.id.cancel)).perform(click());
        waitFor(condition("Ride cancellation reasons")
                .withMatcher(withText(R.string.cancel_feedback_driver_title)));

        onView(withText("First reason"))
                .check(matches(isDisplayed()))
                .perform(click());

        mockRequests(RequestType.RIDE_CANCEL_200_DELETE, RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
        removeRequests(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET);

        onView(withId(R.id.btn_submit))
                .check(matches(isEnabled()))
                .check(matches(isDisplayed()))
                .perform(click());

        waitFor(condition("Switch to online state")
                .withMatcher(withId(R.id.toolbarActionButton))
                .withMatcher(withText(R.string.action_go_offline)));

        verifyRequest(RequestType.RIDE_CANCEL_200_DELETE, hasQueryParam("reason", is("CODE_1")));
    }

    @Test
    @TestCases("C1929298")
    public void adminCancelRide() throws InterruptedException, UiObjectNotFoundException {
        setRideAssignedSateWithDestination();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        ViewActionUtils.waitFor("camera animation", 2000);

        mockEvent(RequestType.EVENT_ADMIN_CANCELLED);
        removeRequests(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET);
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);

        waitFor(condition().withView(onView(allOf(withId(com.afollestad.materialdialogs.R.id.md_content), withText(R.string.ride_cancelled_by_admin)))));

    }

    @Test
    @TestCases("C1929303")
    public void driverIsNotAbleToLogOut() throws InterruptedException, UiObjectNotFoundException {
        setRideAssignedSateWithDestination();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        ViewActionUtils.waitFor("camera animation", 2000);

        onView(allOf(withContentDescription(R.string.navigation_drawer_open), isDisplayed())).perform(click());
        onView(allOf(withId(R.id.design_menu_item_text), withText(R.string.settings), isDisplayed())).perform(click());
        onView(withId(R.id.signOut)).perform(scrollTo(), click());

        waitFor(condition().withView(onView(withText(containsString(getAppContext().getString(R.string.cannot_logout_during_ride)))).inRoot(isNotDecorView(activityRule))));

        cancelRideByRider();

    }

    @Test
    @TestCases("C1929304")
    public void arrive() throws InterruptedException, UiObjectNotFoundException {
        setRideAssignedSateWithDestination();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        ViewActionUtils.waitFor("camera animation", 2000);

        setRideArrivedSateWitDestination();

        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive)))
                .check(matches(isDisplayed()))
                .perform(swipeRight());

        waitFor(condition().withView(onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))));
        onView(allOf(withId(R.id.finish_address), withText("some address")));

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        assertPickupMarkersVisible();

        cancelRideByRider();

    }

    @Test
    @TestCases("C1929295")
    public void autoArrive() throws InterruptedException, UiObjectNotFoundException {
        setRideAssignedSateWithDestination();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        ViewActionUtils.waitFor("camera animation", 2000);

        mockLocation(30.416855, -97.749884);
        setRideArrivedSateWitDestination();

        waitFor(condition().withView(onView(allOf(withId(com.afollestad.materialdialogs.R.id.md_content), withText(R.string.you_have_arrived_to_pickup_location)))));
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());
        waitFor(condition().withView(onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))));
        onView(allOf(withId(R.id.finish_address), withText("some address")));
        assertCarMarkersCount(1);
        assertPickupMarkersVisible();

        cancelRideByRider();

    }

    private void mockRiderLocationUpdateEvent(double lat, double lng) {
        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        removeRequests(RequestType.EVENT_RIDER_LOCATION_UPDATED);

        RiderLocationUpdate locationUpdate = new RiderLocationUpdate();
        locationUpdate.setLat(lat);
        locationUpdate.setLng(lng);
        locationUpdate.setTimeRecorded(System.currentTimeMillis());

        Event event = new Event();
        event.setId(12345l);
        event.setEventType(RideStatus.RIDER_LOCATION_UPDATED);
        event.setParameters(SerializationHelper.serialize(locationUpdate));

        List<Event> events = new ArrayList<>();
        events.add(event);

        mockRequest(RequestType.EVENT_RIDER_LOCATION_UPDATED, events);
    }

    private void setRideAssignedSate() {
        ActiveDriver driver = getResponse("ACTIVE_DRIVER_ASSIGNED_200", ActiveDriver.class);
        driver.getRide().getRequestedCarType().setCarCategory("REGULAR");
        driver.getRide().getRequestedCarType().setTitle("STANDARD");
        driver.getRide().setStartAddress(PICKUP_ADDRESS);
        driver.getRide().getRider().setFirstname(RIDER_NAME);
        mockRequest(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, driver);
    }

    private void setRideAssignedSateWithDestination() {
        ActiveDriver driver = getResponse("ACTIVE_DRIVER_ASSIGNED_200", ActiveDriver.class);

        driver.getRide().getRequestedCarType().setCarCategory("REGULAR");
        driver.getRide().getRequestedCarType().setTitle("STANDARD");
        driver.getRide().setStartAddress(PICKUP_ADDRESS);

        driver.getRide().setEndLocationLat(30.417855);
        driver.getRide().setEndLocationLong(-97.749784);
        driver.getRide().setEndAddress("some address");

        driver.getRide().getRider().setFirstname(RIDER_NAME);
        mockRequest(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, driver);
    }

    private void setRideArrivedSate() {
        ActiveDriver driver = getResponse("ACTIVE_DRIVER_REACHED_200", ActiveDriver.class);

        driver.getRide().getRequestedCarType().setCarCategory("REGULAR");
        driver.getRide().getRequestedCarType().setTitle("STANDARD");
        removeRequests(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET);
        mockRequest(RequestType.ACTIVE_DRIVER_REACHED_200_GET, driver);
    }

    private void setRideArrivedSateWitDestination() {
        ActiveDriver driver = getResponse("ACTIVE_DRIVER_REACHED_200", ActiveDriver.class);

        driver.getRide().getRequestedCarType().setCarCategory("REGULAR");
        driver.getRide().getRequestedCarType().setTitle("STANDARD");
        driver.getRide().setEndLocationLat(30.415999);
        driver.getRide().setEndLocationLong(-97.749784);
        driver.getRide().setEndAddress("some address");

        removeRequests(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET);
        mockRequest(RequestType.ACTIVE_DRIVER_REACHED_200_GET, driver);
    }

    private void cancelRideByRider() throws InterruptedException {
        // As Rider cancel the ride
        removeRequests(RequestType.EVENTS_EMPTY_200_GET,
                RequestType.EVENT_RIDER_LOCATION_UPDATED,
                RequestType.EVENT_END_LOCATION_UPDATED,
                RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET,
                RequestType.ACTIVE_DRIVER_REACHED_200_GET);
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
        mockEvent(RequestType.EVENT_RIDE_CANCELLED_BY_RIDER);

        // Close the dialog
        String message = getString(R.string.ride_cancelled_by_rider, RIDER_NAME);
        waitFor(condition("Should show ride cancelled by rider dialod")
                .withMatcher(withText(message)));
        onView(AllOf.allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_ok))).perform(click());
        onView(withId(R.id.sliderText)).check(doesNotExist());

        waitFor(condition("Should restore online state")
                .withMatcher(withId(R.id.toolbarActionButton))
                .withMatcher(withText(R.string.action_go_offline)));

    }

}
