package cases.s5_core_flow.s8_pending_events;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.api.model.events.PendingEvent;
import com.rideaustin.api.model.events.PendingEventType;
import com.rideaustin.api.model.events.PendingEvents;
import com.rideaustin.helpers.MockHelper;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.SerializationHelper;
import com.rideaustin.utils.TimeUtils;
import com.rideaustin.utils.location.LocationHelper;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import rx.functions.Func1;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersCount;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertDestinationMarkersNotVisible;
import static com.rideaustin.utils.MapTestUtil.assertPickupMarkersVisible;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.hasBodyContent;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.MatcherUtils.withEmptyText;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-11122
 * Created by Sergey Petrov on 09/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PendingEventsSuite extends BaseUITest {

    private static final String RIDER_NAME = "RideOneTwoNine";
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
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CURRENT_DRIVER_200_GET,
                RequestType.ACTIVE_DRIVER_EMPTY_200_GET,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET);
    }

    @Test
    @TestCases({"C1929361", "C1929363", "C1929370"})
    public void shouldCacheLocationUpdates() throws InterruptedException {
        setActiveState();

        mockRequests(RequestType.PENDING_EVENTS_200_POST,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        verifyActiveState();

        cacheLocationUpdates(3);

        resetRequestStats(RequestType.PENDING_EVENTS_200_POST);

        Matchers.waitFor(Matchers.condition("Should send pending events")
                .withBool(() -> getRequestCount(RequestType.PENDING_EVENTS_200_POST) == 1));

        verifyCachedEvents("Should have 3 cached location events", events -> {
            List<PendingEvent> list = events.getList();
            if (list.size() == 3) {
                for (int i = 1; i < list.size(); i++) {
                    PendingEvent prev = list.get(i - 1);
                    assertEquals("Event is location update", PendingEventType.UPDATE_LOCATION, prev.getPendingEventType());
                    assertNotNull("Location update has latitude", prev.getLatitude());
                    assertNotNull("Location update has longitude", prev.getLongitude());
                    assertTrue("Location is valid", LocationHelper.isLocationValid(new LatLng(prev.getLatitude(), prev.getLongitude())));

                    PendingEvent current = list.get(i);
                    assertEquals("Event is location update", PendingEventType.UPDATE_LOCATION, current.getPendingEventType());
                    assertNotNull("Location update has latitude", current.getLatitude());
                    assertNotNull("Location update has longitude", current.getLongitude());
                    assertTrue("Location is valid", LocationHelper.isLocationValid(new LatLng(current.getLatitude(), current.getLongitude())));

                    assertTrue("Correct order by timestamp", prev.getEventTimestamp() < current.getEventTimestamp());
                }
                return true;
            }
            return false;
        });
    }

    @Test
    @TestCases("C1929362")
    public void shouldSendPendingEventsBeforeEndingRide() throws InterruptedException {
        setActiveState();

        mockRequests(RequestType.PENDING_EVENTS_200_POST,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        verifyActiveState();
        cacheLocationUpdates(3);

        AtomicLong pendingSentAt = new AtomicLong();
        AtomicLong rideCompleteAt = new AtomicLong();

        setOnRequestAction(RequestType.PENDING_EVENTS_200_POST,
                () -> pendingSentAt.set(TimeUtils.currentTimeMillis()));
        setOnRequestAction(RequestType.END_RIDE_200_POST,
                () -> rideCompleteAt.set(TimeUtils.currentTimeMillis()));

        NavigationUtils.endTrip(this, null);

        assertTrue("Should send pending events before ending ride",
                pendingSentAt.get() < rideCompleteAt.get());
    }


    @Test
    @TestCases({"C1929366", "C1929367", "C1929368"})
    public void shouldCacheSwitchStates() throws InterruptedException {
        mockRequests(RequestType.PENDING_EVENTS_200_POST,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        NavigationUtils.goOnline(this);
        NavigationUtils.toRequestedState(this, null);
        NavigationUtils.acceptRideRequest(this, null);

        setNetworkError(true);

        NavigationUtils.arrive(this, null);
        NavigationUtils.startTrip(this, null);
        NavigationUtils.endTripWithoutInternet(this, null);

        setNetworkError(false);

        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET, RequestType.RIDE_COMPLETED_200_GET);
        resetRequestStats(RequestType.PENDING_EVENTS_200_POST);

        Matchers.waitFor(Matchers.condition("Should send pending events")
                .withBool(() -> getRequestCount(RequestType.PENDING_EVENTS_200_POST) == 1));

        Matchers.waitFor(condition("Should show rate ride after sending events")
                .withMatcher(withId(R.id.rb_rate_driver)));

        NavigationUtils.rateRide(this);

        verifyCachedEvents("Should have switch state events", events -> {
            List<PendingEvent> list = StreamSupport.stream(events.getList())
                    .filter(event -> !event.getPendingEventType().equals(PendingEventType.UPDATE_LOCATION))
                    .collect(Collectors.toList());
            if (list.size() == 3) {
                PendingEvent arrivedEvent = list.get(0);
                PendingEvent startedEvent = list.get(1);
                PendingEvent endedEvent = list.get(2);
                assertTrue("Arrived event first", arrivedEvent.getPendingEventType().equals(PendingEventType.DRIVER_REACHED));
                assertTrue("Started event second", startedEvent.getPendingEventType().equals(PendingEventType.START_RIDE));
                assertTrue("Ended event third", endedEvent.getPendingEventType().equals(PendingEventType.END_RIDE));
                assertTrue("Arrived before started", arrivedEvent.getEventTimestamp() < startedEvent.getEventTimestamp());
                assertTrue("Started before ended", startedEvent.getEventTimestamp() < endedEvent.getEventTimestamp());
                assertNotNull("End latitude not null", endedEvent.getEndLocationLat());
                assertNotNull("End longitude not null", endedEvent.getEndLocationLong());
                LatLng endLocation = new LatLng(endedEvent.getEndLocationLat(), endedEvent.getEndLocationLong());
                assertTrue("End location is valid", LocationHelper.isLocationValid(endLocation));
                return true;
            }
            return false;
        });
    }

    @Test
    @TestCases("C1929369")
    public void cancelNotCached() throws InterruptedException {
        mockRequests(RequestType.PENDING_EVENTS_200_POST,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        NavigationUtils.goOnline(this);
        NavigationUtils.toRequestedState(this, null);
        NavigationUtils.acceptRideRequest(this, null);

        setNetworkError(true);

        onView(withId(R.id.cancel))
                .check(matches(withText(R.string.cancel)))
                .check(matches(isDisplayed()))
                .perform(click());

        Matchers.waitFor(condition("Ride cancel confirmation dialog")
                .withMatcher(withText(R.string.text_cancel_ride_confirmation)));

        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        Matchers.waitFor(condition("Should show network error")
                .withView(onView(withText(R.string.network_error)).inRoot(isToast())));

        setNetworkError(false);

        waitFor("No cached events", 5000);
        verifyRequestsCount(RequestType.PENDING_EVENTS_200_POST, is(0));
    }

    @Test
    @TestCases("C1929371")
    public void reachedWhenRiderCancelled() throws InterruptedException {
        mockRequests(RequestType.PENDING_EVENTS_200_POST,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        NavigationUtils.goOnline(this);
        NavigationUtils.toRequestedState(this, null);
        NavigationUtils.acceptRideRequest(this, null);

        setNetworkError(true);

        NavigationUtils.arrive(this, null);
        NavigationUtils.startTrip(this, null);

        setNetworkError(false);

        NavigationUtils.toRiderCancelled(this, true);

        String message = getString(R.string.ride_cancelled_by_rider, RIDER_NAME);
        Matchers.waitFor(condition("Should show ride cancelled by rider dialog")
                .withMatcher(withText(message)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        onView(withId(R.id.sliderText)).check(doesNotExist());

        Matchers.waitFor(condition("Should restore online state")
                .withMatcher(withId(R.id.toolbarActionButton))
                .withMatcher(withText(R.string.action_go_offline)));

    }

    @Test
    @TestCases("C1929372")
    public void autoArrive() throws InterruptedException {
        mockRequests(RequestType.PENDING_EVENTS_200_POST,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT);

        Ride ride = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
        ride.setStartLocationLat(30.5);
        ride.setStartLocationLong(-97.6);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        NavigationUtils.goOnline(this);
        NavigationUtils.toRequestedState(this, ride);
        NavigationUtils.acceptRideRequest(this, ride);

        setNetworkError(true);

        mockLocation(ride.getStartLocationLat(), ride.getStartLocationLong());

        Matchers.waitFor(condition("Should show auto-arrive message")
                .withMatcher(withText(R.string.you_have_arrived_to_pickup_location)));

        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        onView(withId(R.id.sliderText))
                .check(matches(withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()));

        setNetworkError(false);

        Matchers.waitFor(Matchers.condition("Should send pending events")
                .withBool(() -> getRequestCount(RequestType.PENDING_EVENTS_200_POST) == 1));

        verifyCachedEvents("Should send DRIVER_REACHED event", events ->
                StreamSupport.stream(events.getList())
                        .filter(event -> event.getPendingEventType().equals(PendingEventType.DRIVER_REACHED))
                        .findAny()
                        .isPresent());
    }

    @Test
    @TestCases("C1929373")
    public void startTripReminder() throws InterruptedException {
        mockRequests(RequestType.PENDING_EVENTS_200_POST,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT);

        Ride ride = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
        ride.setStartLocationLat(30.415551);
        ride.setStartLocationLong(-97.752542);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        NavigationUtils.goOnline(this);
        NavigationUtils.toRequestedState(this, ride);
        NavigationUtils.acceptRideRequest(this, ride);

        setNetworkError(true);

        mockLocation(ride.getStartLocationLat(), ride.getStartLocationLong());

        Matchers.waitFor(condition("Should show auto-arrive message")
                .withMatcher(withText(R.string.you_have_arrived_to_pickup_location)));

        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        onView(withId(R.id.sliderText))
                .check(matches(withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()));

        NavigationUtils.startMoving(this, DEFAULT_ROUTE, 1000);

        Matchers.waitFor(condition("Should show start trip reminder")
                .withMatcher(withText(R.string.motion_detected_dialog_message)));

        NavigationUtils.stopMoving();

        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        setNetworkError(false);

        Matchers.waitFor(Matchers.condition("Should send pending events")
                .withBool(() -> getRequestCount(RequestType.PENDING_EVENTS_200_POST) == 1));

        verifyCachedEvents("Should have switch state events", events -> {
            List<PendingEvent> list = StreamSupport.stream(events.getList())
                    .filter(event -> !event.getPendingEventType().equals(PendingEventType.UPDATE_LOCATION))
                    .collect(Collectors.toList());
            if (list.size() == 2) {
                PendingEvent arrivedEvent = list.get(0);
                PendingEvent startedEvent = list.get(1);
                assertTrue("Arrived event first", arrivedEvent.getPendingEventType().equals(PendingEventType.DRIVER_REACHED));
                assertTrue("Started event second", startedEvent.getPendingEventType().equals(PendingEventType.START_RIDE));
                assertTrue("Arrived before started", arrivedEvent.getEventTimestamp() < startedEvent.getEventTimestamp());
                return true;
            }
            return false;
        });
    }

    @Test
    public void shouldSendAutomaticallyOnStart() throws InterruptedException {
        mockRequests(RequestType.PENDING_EVENTS_200_POST);
        delayRequest(RequestType.PENDING_EVENTS_200_POST, 5000L);

        PendingEvents pendingEvents = new PendingEvents();
        pendingEvents.add(PendingEvent.create(PendingEventType.UPDATE_LOCATION, 1));
        App.getPrefs().savePendingEvents(pendingEvents);
        App.getInstance().getPendingEventsManager().loadFromStorage();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        Matchers.waitFor(Matchers.condition("Should show syncing button")
                .withMatcher(withId(R.id.toolbarActionButton))
                .withMatcher(withText(R.string.action_in_progress))
                .withMatcher(not(isEnabled())));

        verifyRequestsCount(RequestType.PENDING_EVENTS_200_POST, is(1));

        Matchers.waitFor(Matchers.condition("Should be offline after sync")
                .withMatcher(withId(R.id.toolbarActionButton))
                .withMatcher(withText(R.string.action_go_online))
                .withMatcher(isEnabled()));
    }

    @Test
    public void shouldSendByUserInteraction() {
        mockRequests(RequestType.PENDING_EVENTS_400_POST);
        delayRequest(RequestType.PENDING_EVENTS_400_POST, 3000L);

        PendingEvents pendingEvents = new PendingEvents();
        pendingEvents.add(PendingEvent.create(PendingEventType.UPDATE_LOCATION, 1));
        App.getPrefs().savePendingEvents(pendingEvents);
        App.getInstance().getPendingEventsManager().loadFromStorage();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // check sync failed and button still says "Offline"
        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_online), isDisplayed(), isEnabled(), isClickable()), IDLE_TIMEOUT_MS);

        waitFor("Wait", 3000);

        // try to send
        waitForDisplayed(R.id.toolbarActionButton);
        onView(withId(R.id.toolbarActionButton)).perform(click());

        // check toast
        waitForViewInWindow(onView(withText(DriverMockResponseFactory.PENDING_EVENTS_FAILED)).inRoot(isToast()));

        waitFor("Wait for toast disappear", 3000);

        // check sync is failed and button still says "Offline"
        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_online), isDisplayed(), isEnabled(), isClickable()), IDLE_TIMEOUT_MS);

        removeRequests(RequestType.PENDING_EVENTS_400_POST,
                RequestType.ACTIVE_DRIVER_EMPTY_200_GET);

        mockRequests(RequestType.PENDING_EVENTS_200_POST,
                RequestType.DRIVER_GO_ONLINE_200_POST,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.DRIVERS_CARTYPES_200_GET);
        delayRequest(RequestType.PENDING_EVENTS_200_POST, 1000L);

        // try to send
        waitForDisplayed(R.id.toolbarActionButton);
        onView(withId(R.id.toolbarActionButton)).perform(click());

        // check sync succeed and button switched to "Online"
        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_offline), isDisplayed(), isEnabled(), isClickable()), IDLE_TIMEOUT_MS);
    }

    @Test
    public void shouldShowSyncAfterFinishingRideWithoutInternet() throws InterruptedException {
        mockRequests(RequestType.DRIVER_GO_ONLINE_200_POST,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.DRIVERS_CARTYPES_200_GET);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        //------------------------------------------------------------------------------------------
        // Go online
        //------------------------------------------------------------------------------------------

        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_online), isDisplayed(), isEnabled(), isClickable()), IDLE_TIMEOUT_MS);
        onView(withId(R.id.toolbarActionButton)).perform(click());
        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_offline), isDisplayed(), isEnabled(), isClickable()), IDLE_TIMEOUT_MS);

        //------------------------------------------------------------------------------------------
        // Receive ride request
        //------------------------------------------------------------------------------------------

        NavigationUtils.toRequestedState(this, null);

        waitFor("Waiting", 1000L);

        //------------------------------------------------------------------------------------------
        // Accept ride request
        //------------------------------------------------------------------------------------------

        NavigationUtils.acceptRideRequest(this, null);

        waitFor("Waiting", 1000L);

        //------------------------------------------------------------------------------------------
        // Arrive
        //------------------------------------------------------------------------------------------

        NavigationUtils.arrive(this, null);

        waitFor("Waiting", 1000L);

        //------------------------------------------------------------------------------------------
        // Lose internet and finish ride
        //------------------------------------------------------------------------------------------

        setNetworkError(true);
        DeviceTestUtils.setWifiEnabled(false);

        waitFor("Waiting", 3000L);

        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip))).perform(swipeRight());
        waitForViewInWindow(withText(R.string.starting_trip_confirmation_dialog_msg));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        waitFor("Waiting", 1000L);

        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).check(matches(isDisplayed()));

        waitFor("Waiting", 1000L);

        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).perform(swipeRight());
        closeEndingTripConfirmation();

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.EVENT_RIDE_DRIVER_ASSIGNED,
                RequestType.REACH_RIDE_200_POST,
                RequestType.RIDE_DRIVER_REACHED_200_GET,
                RequestType.EVENT_RIDE_DRIVER_REACHED,
                RequestType.ACTIVE_DRIVER_REACHED_200_GET);

        mockRequests(RequestType.RIDE_COMPLETED_200_GET,
                RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET,
                RequestType.EVENTS_EMPTY_200_GET);

        //------------------------------------------------------------------------------------------
        // Restore internet and wait for automatic sync
        //------------------------------------------------------------------------------------------

        mockRequests(RequestType.PENDING_EVENTS_200_POST);
        delayRequest(RequestType.PENDING_EVENTS_200_POST, 3000L);

        setNetworkError(false);
        DeviceTestUtils.setWifiEnabled(true);

        // wait rate driver dialog
        waitForViewInWindow(withId(R.id.rb_rate_driver));

        waitFor("Waiting", 1000L);

        // check submit button hidden
        onView(withId(R.id.btn_submit)).check(matches(not(isDisplayed())));

        // set rating
        onView(withId(R.id.rb_rate_driver)).perform(click());

        removeRequests(RequestType.RIDE_COMPLETED_200_GET);
        mockRequests(RequestType.RIDE_RATING_200_PUT,
                RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);

        // submit
        onView(AllOf.allOf(withId(R.id.btn_submit), isDisplayed())).perform(click());

        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_offline), isDisplayed(), isEnabled(), isClickable()), IDLE_TIMEOUT_MS);
    }

    /**
     * Reference: RA-12795
     */
    @Test
    public void shouldBeOnlineAfterRideCancelled() throws InterruptedException {
        mockRequests(RequestType.DRIVER_GO_ONLINE_200_POST,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.DRIVERS_CARTYPES_200_GET);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_online), isDisplayed(), isEnabled(), isClickable()));
        onView(withId(R.id.toolbarActionButton)).perform(click());
        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_offline), isDisplayed(), isEnabled(), isClickable()));

        mockRequests(RequestType.PENDING_EVENTS_200_POST);
        delayRequest(RequestType.PENDING_EVENTS_200_POST, 3000L);

        NavigationUtils.toRequestedState(this, null);
        NavigationUtils.acceptRideRequest(this, null);
        NavigationUtils.arrive(this, null);
        NavigationUtils.startTrip(this, null);

        setNetworkError(true);
        DeviceTestUtils.setAirplaneMode(true);

        onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).perform(swipeRight());
        closeEndingTripConfirmation();

        NavigationUtils.toAdminCancelled(this, true);

        setNetworkError(false);
        DeviceTestUtils.setAirplaneMode(false);

        waitForViewInWindow(withText(R.string.ride_cancelled_by_admin));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_offline), isDisplayed(), isEnabled(), isClickable()), IDLE_TIMEOUT_MS);

        verifyRequestsCount(RequestType.PENDING_EVENTS_200_POST, is(1));
    }

    private void closeEndingTripConfirmation() {
        if (exists(withText(R.string.ending_trip_confirmation_dialog_msg))) {
            onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        }
    }

    private void setActiveState() {
        Ride ride = getResponse("RIDE_ACTIVE_200", Ride.class);
        ride.getRequestedCarType().setCarCategory("REGULAR");
        ride.getRequestedCarType().setTitle("STANDARD");
        ride.setStartAddress(PICKUP_ADDRESS);
        ride.setStartLocationLat(DEFAULT_LAT);
        ride.setStartLocationLong(DEFAULT_LNG);
        ride.setEndAddress(null);
        ride.setEndLocationLat(null);
        ride.setEndLocationLong(null);
        ride.getRider().setFirstname(RIDER_NAME);
        removeRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
        ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, ride, this);
        mockRequest(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, activeDriver);
        mockRequest(RequestType.RIDE_ACTIVE_200_GET, ride);
    }

    private void verifyActiveState() {
        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        assertPickupMarkersVisible();
        assertDestinationMarkersNotVisible();

        onView(AllOf.allOf(withId(R.id.navigate), withText(R.string.navigate))).check(matches(isDisplayed()));
        onView(AllOf.allOf(withId(R.id.cancel), withText(R.string.cancel))).check(matches(not(isDisplayed())));

        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).check(matches(isDisplayed()));
        onView(AllOf.allOf(withId(R.id.starting_address), withText(containsString(PICKUP_ADDRESS)))).check(matches(isDisplayed()));
        onView(withId(R.id.finish_address)).check(matches(isDisplayed())).check(matches(withEmptyText()));
        onView(AllOf.allOf(withId(R.id.button), withText(R.string.contact))).check(matches(isDisplayed()));
        assertPickupMarkersVisible();
    }

    private void cacheLocationUpdates(int count) throws InterruptedException {
        setNetworkError(true);

        NavigationUtils.startMoving(this, DEFAULT_ROUTE, 1000);

        Matchers.waitFor(Matchers.condition("Waiting for pending events")
                .withBool(() -> App.getInstance().getPendingEventsManager().getEvents().getList().size() == count));

        NavigationUtils.stopMoving();

        resetRequestStats(RequestType.PENDING_EVENTS_200_POST);

        setNetworkError(false);
    }

    private void verifyCachedEvents(String message, Func1<PendingEvents, Boolean> func) {
        Matcher<String> requestMatcher = new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String item) {
                PendingEvents events = SerializationHelper.deSerialize(item, PendingEvents.class);
                if (events != null && events.getList() != null) {
                    return func.call(events);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(message);
            }
        };

        verifyRequest(RequestType.PENDING_EVENTS_200_POST, hasBodyContent(requestMatcher));

    }
}
