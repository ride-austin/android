package cases.s5_core_flow.s_stacked_rides;

import android.support.annotation.Nullable;
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
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.helpers.MockHelper;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.MapTestUtil;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;

import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

/**
 * Created on 20/12/2017
 *
 * @author sdelaysam
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class StackedRidesSuite extends BaseUITest {

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
                RequestType.PENDING_EVENTS_200_POST,
                RequestType.ACCEPT_RIDE_200_POST,
                RequestType.DECLINE_RIDE_200_DELETE);
    }

    @Test
    @TestCases({"C3392281", "C3503237"})
    public void acceptAndStackedRequest() throws InterruptedException, UiObjectNotFoundException {
        Ride current = startTrip();
        Ride next = requestNextRide(current);
        current.setNextRide(next);
        NavigationUtils.acceptNextRideRequest(this, current, next);

        waitFor("Auto-zooming", 1000);
        MapTestUtil.zoomOut(200);
        MapTestUtil.assertNextRideMarkersVisible(5000);

        cancelNextRide(current);

        waitFor("Auto-zooming", 1000);
        MapTestUtil.zoomOut(200);
        MapTestUtil.assertNextRideMarkersNotVisible(5000);
    }

    @Test
    @TestCases("C3502754")
    public void activeRideCancelledByAdmin() throws InterruptedException, UiObjectNotFoundException {
        Ride current = startTrip();
        Ride next = requestNextRide(current);
        current.setNextRide(next);
        NavigationUtils.acceptNextRideRequest(this, current, next);

        waitFor("Auto-zooming", 1000);
        MapTestUtil.zoomOut(200);
        MapTestUtil.assertNextRideMarkersVisible(5000);

        NavigationUtils.toAdminCancelledWithNextRide(this, current, next);

        waitForViewInWindow(withText(R.string.ride_cancelled_by_admin));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        Matchers.waitFor(condition().withMatcher(withId(R.id.pickup_destination_card)));
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive))).check(matches(isDisplayed()));

        waitFor("Auto-zooming", 1000);
        MapTestUtil.zoomOut(200);
        MapTestUtil.assertNextRideMarkersNotVisible(5000);
    }

    @Test
    @TestCases("C3392282")
    public void cancelRequest() throws InterruptedException, UiObjectNotFoundException {
        Ride current = startTrip();
        requestNextRide(current);

        removeRequests(RequestType.RIDE_200_GET_ID_1,
                RequestType.RIDE_200_GET_ID_2,
                RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET);
        mockRequests(RequestType.RIDE_CANCEL_200_DELETE);
        current.setNextRide(null);
        mockRequest(RequestType.RIDE_200_GET_ID_1, current);
        ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, current, this);
        mockRequest(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, activeDriver);

        onView(withId(R.id.button))
                .check(matches(withText(R.string.menu_decline)))
                .perform(click());

        Matchers.waitFor(condition("Wait for finish trip button")
                .withMatcher(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))));

        waitFor("Auto-zooming", 1000);
        MapTestUtil.zoomOut(200);
        MapTestUtil.assertNextRideMarkersNotVisible(5000);

        NavigationUtils.endTrip(this, current);

        NavigationUtils.rateRide(this);

        onView(withId(R.id.toolbarActionButton))
                .check(matches(withText(R.string.action_go_offline)))
                .check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C10172151")
    public void missRequest() throws InterruptedException, UiObjectNotFoundException {
        Ride current = startTrip();
        requestNextRide(current);

        removeRequests(RequestType.RIDE_200_GET_ID_1,
                RequestType.RIDE_200_GET_ID_2,
                RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET);
        mockRequest(RequestType.RIDE_200_GET_ID_1, current);
        ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, current, this);
        mockRequest(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, activeDriver);

        Matchers.waitFor(condition("Should show pickup/destination").withMatcher(withId(R.id.pickup_destination_card)));
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).check(matches(isDisplayed()));

        waitFor("Auto-zooming", 1000);
        MapTestUtil.zoomOut(200);
        MapTestUtil.assertNextRideMarkersNotVisible(5000);
    }

    @Test
    public void testStackedRidesFlow() throws InterruptedException {
        Ride current = startTrip();

        Ride next = requestNextRide(current);

        current.setNextRide(next);

        NavigationUtils.acceptNextRideRequest(this, current, next);

        cancelNextRide(current);

        NavigationUtils.toNextRideRequestedState(this, current, next);

        current.setNextRide(next);

        NavigationUtils.acceptNextRideRequest(this, current, next);

        waitFor("", 2000);

        NavigationUtils.endTripWithNextRide(this, current, next);

        waitFor("", 2000);

        NavigationUtils.rateWithNextRide(this, current, next);

        Matchers.waitFor(condition().withMatcher(withId(R.id.pickup_destination_card)));
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive))).check(matches(isDisplayed()));
    }

    @Test
    public void testStackedRideProcessedOffline() throws InterruptedException, UiObjectNotFoundException {
        Ride current = startTrip();

        Ride next = requestNextRide(current);

        current.setNextRide(next);

        NavigationUtils.acceptNextRideRequest(this, current, next);

        waitFor("Auto-zooming", 1000);
        MapTestUtil.zoomOut(200);
        MapTestUtil.assertNextRideMarkersVisible(5000);

        setNetworkError(true);
        DeviceTestUtils.setAirplaneMode(true);

        NavigationUtils.endTripWithNextRide(this, current, next, () -> {
            Matchers.waitFor(condition("Should switch to driver assigned")
                    .withMatcher(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive))));
            onView(withId(R.id.pickup_destination_card)).check(matches(isDisplayed()));
        });

        waitFor("Auto-zooming", 1000);
        MapTestUtil.zoomOut(200);
        MapTestUtil.assertNextRideMarkersNotVisible(5000);

        NavigationUtils.arrive(this, next);
        NavigationUtils.startTrip(this, next);
        NavigationUtils.endTripWithoutInternet(this, next);


        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
        resetRequestStats(RequestType.PENDING_EVENTS_200_POST);

        current.setId(1L);
        current.setNextRide(null);
        current.setStatus(RideStatus.COMPLETED.toString());
        mockRequest(RequestType.RIDE_200_GET_ID_1, current);

        next.setId(2L);
        next.setStatus(RideStatus.COMPLETED.toString());
        mockRequest(RequestType.RIDE_200_GET_ID_2, next);

        DeviceTestUtils.setAirplaneMode(false);
        DeviceTestUtils.waitForInternet();
        setNetworkError(false);

        Matchers.waitFor(Matchers.condition("Should send pending events")
                .withBool(() -> getRequestCount(RequestType.PENDING_EVENTS_200_POST) == 1));

        Matchers.waitFor(condition("Should show rate ride after sending events")
                .withMatcher(withId(R.id.rb_rate_driver)));

        // NOTE: passing next ride as parameter
        // App will request next unrated ride, IDs must match
        NavigationUtils.rateRide(this, next, () -> {
            Matchers.waitFor(condition("Should show rate ride after sending events")
                    .withMatcher(withId(R.id.rb_rate_driver)));
        });

        NavigationUtils.rateRide(this);
    }

    @Test
    public void testRideRatingsSent() throws InterruptedException {
        Ride current = startTrip();

        Ride next = requestNextRide(current);

        current.setNextRide(next);

        NavigationUtils.acceptNextRideRequest(this, current, next);

        setNetworkError(true);
        DeviceTestUtils.setAirplaneMode(true);

        NavigationUtils.endTripWithNextRide(this, current, next, () -> {
            Matchers.waitFor(condition("Should switch to driver assigned")
                    .withMatcher(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive))));
            onView(withId(R.id.pickup_destination_card)).check(matches(isDisplayed()));
        });

        NavigationUtils.arrive(this, next);
        NavigationUtils.startTrip(this, next);
        NavigationUtils.endTripWithoutInternet(this, next);

        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
        resetRequestStats(RequestType.PENDING_EVENTS_200_POST);

        current.setId(1L);
        current.setNextRide(null);
        current.setStatus(RideStatus.COMPLETED.toString());
        mockRequest(RequestType.RIDE_200_GET_ID_1, current);

        next.setId(2L);
        next.setStatus(RideStatus.COMPLETED.toString());
        mockRequest(RequestType.RIDE_200_GET_ID_2, next);

        DeviceTestUtils.setAirplaneMode(false);
        DeviceTestUtils.waitForInternet();
        setNetworkError(false);

        Matchers.waitFor(Matchers.condition("Should send pending events")
                .withBool(() -> getRequestCount(RequestType.PENDING_EVENTS_200_POST) == 1));

        Matchers.waitFor(condition("Should show rate ride after sending events")
                .withMatcher(withId(R.id.rb_rate_driver)));

        setNetworkError(true);
        DeviceTestUtils.setAirplaneMode(true);

        // NOTE: passing next ride as parameter
        // App will request next unrated ride, IDs must match
        NavigationUtils.rateRide(this, next, () -> {
            Matchers.waitFor(condition("Should switch to offline state")
                    .withMatcher(AllOf.allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online)))
                    .withCheck(AllOf.allOf(isDisplayed(), isEnabled(), isClickable())));
        });

        verifyRequestsCount(RequestType.RIDE_RATING_200_PUT, is(0));

        DeviceTestUtils.setAirplaneMode(false);
        DeviceTestUtils.waitForInternet();
        setNetworkError(false);

        Matchers.waitFor(condition("Should send pending ride rating")
                .withBool(() -> getRequestCount(RequestType.RIDE_RATING_200_PUT) == 1));
        resetRequestStats(RequestType.RIDE_RATING_200_PUT);

        Matchers.waitFor(condition("Should show rate ride after sending events")
                .withMatcher(withId(R.id.rb_rate_driver)));

        setNetworkError(true);
        DeviceTestUtils.setAirplaneMode(true);

        NavigationUtils.rateRide(this, null, () -> {
            Matchers.waitFor(condition("Should switch to offline state")
                    .withMatcher(AllOf.allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online)))
                    .withCheck(AllOf.allOf(isDisplayed(), isEnabled(), isClickable())));
        });

        verifyRequestsCount(RequestType.RIDE_RATING_200_PUT, is(0));
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);

        DeviceTestUtils.setAirplaneMode(false);
        DeviceTestUtils.waitForInternet();
        setNetworkError(false);

        Matchers.waitFor(condition("Should send pending ride rating")
                .withBool(() -> getRequestCount(RequestType.RIDE_RATING_200_PUT) == 1));

        Matchers.waitFor(condition("Should switch to online state")
                .withMatcher(AllOf.allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline)))
                .withCheck(AllOf.allOf(isDisplayed(), isEnabled(), isClickable())));
    }

    @Test
    public void stackedRideReassigned() throws InterruptedException {
        Ride current = startTrip();
        Ride next = requestNextRide(current);
        current.setNextRide(next);
        NavigationUtils.acceptNextRideRequest(this, current, next);

        onView(withId(R.id.next))
                .check(matches(withText(R.string.stacked_ride_next_ride)))
                .check(matches(isDisplayed()))
                .perform(click());
        waitForViewInWindow(allOf(withId(R.id.title), withText(R.string.stacked_ride_dialog_title)));

        String message = "Ride reassigned by test";

        mockEvents(RequestType.EVENT_END_LOCATION_UPDATED, "EVENT_END_LOCATION_UPDATED", events -> {
            events.get(0).getRide().setId(current.getId());
            events.get(0).getRide().setNextRide(null);

            Event event = getEventsResponse("EVENT_RIDE_REASSIGNED").get(0);
            event.setRide(next);
            event.setMessage(message);
            events.add(event);

            return events;
        });

        Matchers.waitFor(condition("Reassigned notification shown")
                .withMatcher(withText(message))
                .withCheck(isDisplayed()));

        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        Matchers.waitFor(condition("Next ride window is hidden")
                .withMatcher(withId(R.id.title))
                .withMatcher(withText(R.string.stacked_ride_dialog_title))
                .withAssertion(doesNotExist()));

        Matchers.waitFor(condition("Next button disappeared")
                .withMatcher(withId(R.id.next))
                .withCheck(not(isDisplayed())));

        Matchers.waitFor(condition("Stays in active state").withMatcher(withId(R.id.pickup_destination_card)));
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).check(matches(isDisplayed()));
    }

    @Test
    public void nextRideCancelled() throws InterruptedException {
        Ride current = startTrip();
        Ride next = requestNextRide(current);
        current.setNextRide(next);
        NavigationUtils.acceptNextRideRequest(this, current, next);

        onView(withId(R.id.next))
                .check(matches(withText(R.string.stacked_ride_next_ride)))
                .check(matches(isDisplayed()))
                .perform(click());
        waitForViewInWindow(allOf(withId(R.id.title), withText(R.string.stacked_ride_dialog_title)));

        mockEvent(RequestType.EVENT_RIDE_CANCELLED_BY_RIDER, "EVENT_RIDE_CANCELLED_BY_RIDER", response -> {
            next.setStatus(RideStatus.RIDER_CANCELLED.name());
            next.setDriverPayment(0.0);
            response.setRide(next);
            return response;
        });

        current.setNextRide(null);
        ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, current, this);
        removeRequests(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET);
        mockRequest(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, activeDriver);

        String message = getString(R.string.ride_cancelled_by_rider, next.getRider().getFirstname());
        Matchers.waitFor(condition("Ride cancelled by rider")
                .withMatcher(withText(message)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        Matchers.waitFor(condition("Next ride window is hidden")
                .withMatcher(withId(R.id.title))
                .withMatcher(withText(R.string.stacked_ride_dialog_title))
                .withAssertion(doesNotExist()));

        Matchers.waitFor(condition("Next button disappeared")
                .withMatcher(withId(R.id.next))
                .withCheck(not(isDisplayed())));

        Matchers.waitFor(condition("Stays in active state").withMatcher(withId(R.id.pickup_destination_card)));
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).check(matches(isDisplayed()));
    }

    /**
     * RA-14872: due to network latency stack request can arrive after driver ends current trip.
     */
    @Test
    public void acceptStackedRequestAfterRideCompleted() throws InterruptedException {
        Ride current = startTrip();
        NavigationUtils.endTrip(this, current);
        Ride next = requestNextRide(null);
        NavigationUtils.acceptNextRideRequest(this, null, next);

        Matchers.waitFor(condition().withMatcher(withId(R.id.pickup_destination_card)));
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive))).check(matches(isDisplayed()));
    }

    @Test
    public void missStackedRequestAfterRideCompleted() throws InterruptedException {
        Ride current = startTrip();
        NavigationUtils.endTrip(this, current);
        requestNextRide(null);

        // miss request
        Matchers.waitFor(condition("Wait for rate rider")
                .withMatcher(withId(R.id.rb_rate_driver)));

    }

    private Ride startTrip() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        Ride current = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
        current.setId(1L);
        current.getRider().getUser().setPhotoUrl("https://media.istockphoto.com/photos/beautiful-woman-takes-a-selfie-picture-id535921215?k=6&m=535921215&s=612x612&w=0&h=wArEuW1GDXFX1i9fUdwMSRcHnIod-6pe29OU8qve5ZU=");
        current.getRider().setFirstname("Current");
        current.getRider().setLastname("Rider");
        current.getRider().setFullName("Current Rider");

        goOnline();
        NavigationUtils.toRequestedState(this, current);

        waitFor("", 2000);

        onView(withId(R.id.toolbarActionButton))
                .check(matches(withText(R.string.action_go_offline)))
                .check(matches(isDisplayed()));
        onView(withId(R.id.button))
                .check(matches(withText(R.string.menu_decline)))
                .check(matches(isDisplayed()));
        onView(withId(R.id.app_title_logo)).check(matches(not(isDisplayed())));

        NavigationUtils.acceptRideRequest(this, current);
        NavigationUtils.arrive(this, current);
        NavigationUtils.startTrip(this, current);

        onView(withId(R.id.next)).check(matches(not(isDisplayed())));
        return current;
    }

    private Ride requestNextRide(@Nullable Ride current) {
        Ride next = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
        next.setId(2L);
        next.getRider().getUser().setPhotoUrl("http://2012.404fest.ru/data/profile/631/userpic-631-300.jpg");
        next.getRider().setFirstname("Next");
        next.getRider().setLastname("Rider");
        next.getRider().setFullName("Next Rider");
        next.setStartAddress("9121-9243 Ramirez Lane");
        if (current != null) {
            next.setStartLocationLat(current.getStartLocationLat() + 0.0001);
            next.setStartLocationLong(current.getStartLocationLong() + 0.0001);
        }

        NavigationUtils.toNextRideRequestedState(this, current, next);

        waitFor("", 2000);

        onView(withId(R.id.toolbarActionButton))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.button))
                .check(matches(withText(R.string.menu_decline)))
                .check(matches(isDisplayed()));
        onView(withId(R.id.app_title_logo)).check(matches(isDisplayed()));

        return next;
    }

    private void cancelNextRide(Ride current) throws InterruptedException {
        waitFor("", 2000);

        onView(withId(R.id.next))
                .check(matches(withText(R.string.stacked_ride_next_ride)))
                .check(matches(isDisplayed()))
                .perform(click());

        waitForViewInWindow(allOf(withId(R.id.title), withText(R.string.stacked_ride_dialog_title)));
        onView(withId(R.id.avatar)).check(matches(isDisplayed()));
        onView(withId(R.id.name))
                .check(matches(withText("Next Rider")))
                .check(matches(isDisplayed()));
        onView(withId(R.id.category))
                .check(matches(withText("STANDARD")))
                .check(matches(isDisplayed()));
        onView(withId(R.id.address))
                .check(matches(withText("9121-9243 Ramirez Lane")))
                .check(matches(isDisplayed()));
        onView(withId(R.id.call))
                .check(matches(isEnabled()))
                .check(matches(isDisplayed()));
        onView(withId(R.id.sms))
                .check(matches(isEnabled()))
                .check(matches(isDisplayed()));
        onView(withId(R.id.cancel))
                .check(matches(isEnabled()))
                .check(matches(isDisplayed()));

        waitFor("", 2000);

        onView(withId(R.id.cancel)).perform(click());
        waitForViewInWindow(withText(R.string.text_cancel_ride_confirmation));

        waitFor("", 1000);

        removeRequests(RequestType.RIDE_200_GET_ID_1, RequestType.RIDE_200_GET_ID_2);
        mockRequests(RequestType.RIDE_CANCEL_200_DELETE);
        current.setNextRide(null);
        mockRequest(RequestType.RIDE_200_GET_ID_2, current);

        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        Matchers.waitFor(condition("Next button should disappear")
                .withMatcher(withId(R.id.next))
                .withCheck(not(isDisplayed())));
    }

    private void goOnline() {
        mockRequests(RequestType.DRIVER_GO_ONLINE_200_POST,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_online), isDisplayed(), isEnabled(), isClickable()));
        onView(withId(R.id.toolbarActionButton)).perform(click());
        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_offline), isDisplayed(), isEnabled(), isClickable()));
    }
}
