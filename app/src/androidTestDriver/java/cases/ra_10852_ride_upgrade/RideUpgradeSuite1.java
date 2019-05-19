package cases.ra_10852_ride_upgrade;

import android.os.RemoteException;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RequestWrapper;
import com.rideaustin.TestCases;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.Variant;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.helpers.MockHelper;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.NavigationUtils;

import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

/**
 * Created by crossover on 23/06/2017.
 */

public class RideUpgradeSuite1 extends BaseUITest {

    public static final String ERROR_CANNOT_SUBMIT = "Your request can't be submitted to rider, as this rider uses older version of application that doesn't support ride upgrades";
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
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET,
                RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                RequestType.PENDING_EVENTS_200_POST,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.DRIVERS_CARTYPES_200_GET,
                RequestType.DRIVER_TYPES_200_GET);
    }

    /**
     * SUV enabled - Show option to Upgrade to SUV when accepting Standard ride
     */
    @Test
    @TestCases("C1929473")
    public void testSUVEnabledShowOptionToUpgradeToSUVWhenAcceptingStandardRide() throws InterruptedException {

        setRideAssignedSate();
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // Driver app should show option to Upgrade to SUV
        onView(withId(R.id.upgradeCarType)).check(matches(isDisplayed()));
    }

    /**
     * SUV enabled - Show option to Upgrade to SUV when accepting Standard ride
     */
    @Test
    @TestCases("C1929474")
    public void testSUVEnabledDoNotShowOptionWhenAcceptingNonStandardRide() throws InterruptedException {

        Ride ride = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
        long rideId = ride.getId();
        ride.getRequestedCarType().setCarCategory("SUV");
        ride.getRequestedCarType().setTitle("SUV");
        ride.getRequestedCarType().setDescription("Non Standard Car");
        ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, ride, this);
        mockRequest(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, activeDriver);
        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, ride);


        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // Driver app should NOT show option to Upgrade to SUV
        onView(withId(R.id.myLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.upgradeCarType)).check(matches(not(isDisplayed())));

        // As Rider cancel the ride
        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET,
                RequestType.EVENTS_EMPTY_200_GET);
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
        mockEvent(RequestType.EVENT_RIDE_CANCELLED_BY_RIDER);

        // Close the dialog
        String message = getString(R.string.ride_cancelled_by_rider, "RideOneTwoNine");
        waitFor(condition().withMatcher(withText(message)));
        onView(AllOf.allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_ok))).perform(click());

        onView(withId(R.id.toolbarActionButton))
                .check(matches(withText(R.string.action_go_offline)))
                .check(matches(isDisplayed()));

        // Receive PREMIUM REQUEST
        rideId++;
        ride.setId(rideId);
        ride.getRequestedCarType().setCarCategory("PREMIUM");
        ride.getRequestedCarType().setTitle("PREMIUM");
        NavigationUtils.toRequestedState(this, ride);
        NavigationUtils.acceptRideRequest(this, ride);

        // Driver app should not show option to Upgrade to SUV
        onView(withId(R.id.myLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.upgradeCarType)).check(matches(not(isDisplayed())));

        // As Rider cancel the ride
        // Need to use another ride id - ride with same ID can't be cancelled twice
        List<Event> cancelEvent = createRideEvent("EVENT_RIDE_CANCELLED_BY_RIDER", rideId);
        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET,
                RequestType.EVENTS_EMPTY_200_GET);
        chainRequests(RequestWrapper.wrap(RequestType.EVENT_RIDE_CANCELLED_BY_RIDER, cancelEvent),
                RequestWrapper.wrap(RequestType.EVENTS_EMPTY_200_GET));
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);

        // Close the dialog
        waitFor(condition().withMatcher(withText(message)));
        onView(AllOf.allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_ok))).perform(click());

        onView(withId(R.id.toolbarActionButton))
                .check(matches(withText(R.string.action_go_offline)))
                .check(matches(isDisplayed()));

        // Receive PREMIUM REQUEST
        rideId++;
        ride.setId(rideId);
        ride.getRequestedCarType().setCarCategory("LUXURY");
        ride.getRequestedCarType().setTitle("LUXURY");
        NavigationUtils.toRequestedState(this, ride);
        NavigationUtils.acceptRideRequest(this, ride);

        // Driver app should not show option to Upgrade to SUV
        onView(withId(R.id.myLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.upgradeCarType)).check(matches(not(isDisplayed())));
    }

    /**
     * SUV enabled - Show option to Upgrade to SUV when accepting Standard ride
     */
    @Test
    @TestCases("C1929475")
    public void testSUVDisabledDoNotShowOptionToUpgradeToSUVWhenAcceptingStandardRide() throws InterruptedException {
        // Disable SUV upgrade
        removeRequests(RequestType.CONFIG_DRIVER_200_GET, RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
        GlobalConfig config = getResponse("CONFIG_GLOBAL_200", GlobalConfig.class);
        for (Variant variant : config.getRideUpgrade().getVariants()) {
            variant.setValidUpgrades(Collections.emptyList());
        }
        mockRequest(RequestType.CONFIG_DRIVER_200_GET, config);
        mockRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET,
                RequestType.DRIVER_GO_ONLINE_200_POST,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        //Go online
        waitFor(condition().withMatcher(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online), isDisplayed(), isEnabled(), isClickable()))); // FAILED
        onView(withId(R.id.toolbarActionButton)).perform(click());
        waitFor(condition().withMatcher(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline), isDisplayed(), isEnabled(), isClickable())));

        // Receive STANDARD Ride Request
        NavigationUtils.toRequestedState(this, null);
        NavigationUtils.acceptRideRequest(this, null);

        // Driver app should NOT show option to Upgrade to SUV
        onView(withId(R.id.myLocation)).check(matches(isDisplayed()));
        onView(withId(R.id.upgradeCarType)).check(matches(not(isDisplayed())));
    }

    @Test
    @TestCases("C1929476")
    public void testDoNotShowOptionToUpgradeToSUVWhenRideHasStarted() throws InterruptedException {

        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        validateUpgradeToSUVVisible();

        // mock arrived state
        setRideArrivedSate();
        // go to arrived sate
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive)))
                .check(matches(isDisplayed()))
                .perform(swipeRight());

        waitFor(condition().withView(onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()))));

        validateUpgradeToSUVVisible();

        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()))
                .perform(click());

        waitForViewInWindow(withText(R.string.starting_trip_confirmation_dialog_msg));

        // mock ride started
        setRideStartedSate();

        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        waitFor(condition()
                .withMatcher(withId(R.id.sliderText))
                .withMatcher(withText(R.string.slide_to_finish)));

        validateUpgradeToSUVNotVisible();

        cancelRideByRider();

    }

    @Test
    @TestCases("C1929489")
    public void testCancelSUVUpgradeRequestAfterRiderHasAccepted() throws InterruptedException {
        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);

        onView(withId(R.id.upgradeCarType)).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));

        mockRequests(RequestType.RIDES_UPGRADE_DECLINE_DRIVER);
        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        chainRequests(RequestType.EVENT_RIDE_UPGRADE_ACCEPTED, RequestType.EVENTS_EMPTY_200_GET);

        onView(withId(R.id.single_action)).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgraded_to_suv))));

        onView(withId(R.id.dismiss_button)).perform(click());
        waitFor(condition().withView(onView(allOf(withId(R.id.toolbarTitle), withText(containsString("SUV"))))));

        cancelRideByRider();

    }

    @Test
    @TestCases("C1929477")
    public void testRequestSUVUpgradeToRiderWhileOnTheWay() throws InterruptedException {
        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);

        onView(withId(R.id.upgradeCarType)).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));

        // dismiss popup
        onView(withId(R.id.dismiss_button)).perform(click());
        // check popup close
        waitFor(condition().withMatcher(withText(R.string.upgrade_waiting_rider)).withAssertion(doesNotExist()));

        onView(withId(R.id.upgradeCarType)).check(matches(isDisplayed())).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));
        onView(withId(R.id.dismiss_button)).perform(click());


        cancelRideByRider();
    }

    @Test
    @TestCases("1929478")
    public void testRequestSUVUpgradeToRiderAfterArrived() throws InterruptedException {
        setRideArrivedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);

        onView(withId(R.id.upgradeCarType)).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));

        // dismiss popup
        onView(withId(R.id.dismiss_button)).perform(click());
        // check popup close
        waitFor(condition().withMatcher(withText(R.string.upgrade_waiting_rider)).withAssertion(doesNotExist()));

        // check FAB status
        onView(withId(R.id.upgradeCarType)).check(matches(isDisplayed())).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));
        onView(withId(R.id.dismiss_button)).perform(click());

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929480")
    public void testReceiveSUVUpgradeAcceptedWhileOnTheWay() throws InterruptedException {
        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);

        onView(withId(R.id.upgradeCarType)).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));

        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        chainRequests(RequestType.EVENT_RIDE_UPGRADE_ACCEPTED, RequestType.EVENTS_EMPTY_200_GET);

        waitFor(condition().withView(onView(withText(R.string.upgraded_to_suv))));

        onView(withId(R.id.dismiss_button)).perform(click());
        waitFor(condition().withView(onView(allOf(withId(R.id.toolbarTitle), withText(containsString("SUV"))))));

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929481")
    public void testReceiveSUVUpgradeAcceptedAfterArrived() throws InterruptedException {
        setRideArrivedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);

        onView(withId(R.id.upgradeCarType)).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));

        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        chainRequests(RequestType.EVENT_RIDE_UPGRADE_ACCEPTED, RequestType.EVENTS_EMPTY_200_GET);

        waitFor(condition().withView(onView(withText(R.string.upgraded_to_suv))));

        onView(withId(R.id.dismiss_button)).perform(click());
        waitFor(condition().withView(onView(allOf(withId(R.id.toolbarTitle), withText(containsString("SUV"))))));

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929483")
    public void testReceiveSUVUpgradeDeclinedWhileOnTheWay() throws InterruptedException {
        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);

        onView(withId(R.id.upgradeCarType)).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));

        mockEvent(RequestType.EVENT_RIDE_UPGRADE_DECLINED);

        waitFor(condition().withView(onView(withText(R.string.upgrade_failed))));

        onView(withId(R.id.dismiss_button)).perform(click());
        waitFor(condition().withView(onView(allOf(withId(R.id.toolbarTitle), withText(containsString("STANDARD"))))));

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929484")
    public void testReceiveSUVUpgradeDeclinedAfterArrived() throws InterruptedException {
        setRideArrivedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);

        onView(withId(R.id.upgradeCarType)).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));

        mockEvent(RequestType.EVENT_RIDE_UPGRADE_DECLINED);

        waitFor(condition().withView(onView(withText(R.string.upgrade_failed))));

        onView(withId(R.id.dismiss_button)).perform(click());
        waitFor(condition().withView(onView(allOf(withId(R.id.toolbarTitle), withText(containsString("STANDARD"))))));

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929492")
    public void testShouldOnlyBeAbleToRequestSUVUpgradeOnce() throws InterruptedException {
        mockRequests(RequestType.RIDES_UPGRADE_DECLINE_DRIVER);
        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);

        //tap Upgrade to SUV
        onView(withId(R.id.upgradeCarType)).perform(click());

        //Popup should appear with text "Waiting for Rider's confirmation"
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));

        // As Driver tap [Cancel Upgrade] on SUV Upgrade popup
        onView(withId(R.id.single_action)).perform(click());

        //App header still shows Standard car
        waitFor(condition().withView(onView(allOf(withId(R.id.toolbarTitle), withText(containsString("STANDARD"))))));

        //Floating menu no longer shows option to Upgrade to SUV
        onView(withText(R.string.upgrade_to_suv)).check(doesNotExist());
        onView(withId(R.id.upgradeCarType)).check(matches(not(isDisplayed())));

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929486")
    public void testReceiveSUVUpgradeDeclinedFromServerAfterExpired() throws InterruptedException {
        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);

        onView(withId(R.id.upgradeCarType)).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));

        removeRequests(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET,
                RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.EVENTS_EMPTY_200_GET);
        chainRequests(RequestType.EVENT_RIDE_UPGRADE_EXPIRED, RequestType.EVENTS_EMPTY_200_GET);

        waitFor(condition().withView(onView(withText(R.string.upgrade_failed))));

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929488")
    public void testReceiveSUVUpgradeAcceptedFromRiderAfterCancellingRequest() throws InterruptedException {
        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);

        //As Driver tap Upgrade to SUV
        onView(withId(R.id.upgradeCarType)).perform(click());

        //Popup should appear with text "Waiting for Rider's confirmation"
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));

        //As Driver tap [Cancel Upgrade] in SUV Upgrade popup,
        mockRequests(RequestType.RIDES_UPGRADE_DECLINE_DRIVER);
        onView(withId(R.id.single_action)).perform(click());

        // and as Rider quickly accept the SUV Upgrade request
        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        mockRequests(RequestType.EVENT_RIDE_UPGRADE_ACCEPTED);

        waitFor(condition().withView(onView(withText(R.string.upgraded_to_suv))));
        removeRequests(RequestType.EVENT_RIDE_UPGRADE_ACCEPTED);
        mockRequests(RequestType.EVENTS_EMPTY_200_GET);

        waitFor(condition().withMatcher(withId(R.id.dismiss_button)).withCheck(isCompletelyDisplayed()));
        onView(withId(R.id.dismiss_button)).perform(click());
        waitFor(condition().withView(onView(allOf(withId(R.id.toolbarTitle), withText(containsString("SUV"))))));

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929487")
    public void testCancelPendingSUVUpgradeRequestToRider() throws InterruptedException {
        setRideAssignedSate();
        mockRequests(RequestType.RIDES_UPGRADE_DECLINE_DRIVER);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);

        //As Driver tap Upgrade to SUV
        onView(withId(R.id.upgradeCarType)).perform(click());

        //Popup should appear with text "Waiting for Rider's confirmation"
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));

        // As Driver tap [Cancel Upgrade] on SUV Upgrade popup
        onView(withId(R.id.single_action)).perform(click());

        //App header still shows Standard car
        waitFor(condition().withView(onView(allOf(withId(R.id.toolbarTitle), withText(containsString("STANDARD"))))));

        //Floating menu no longer shows option to Upgrade to SUV
        onView(withText(R.string.upgrade_to_suv)).check(doesNotExist());
        onView(withId(R.id.upgradeCarType)).check(matches(not(isDisplayed())));

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929479")
    public void testRequestSUVUpgradeToRiderWithNonSupportingApp() throws InterruptedException {

        setRideAssignedSate();
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_400_POST);

        // fab menu displayed
        onView(withId(R.id.upgradeCarType)).perform(click());

        // Wait for error toast
        waitFor(condition().withView(onView(withText(containsString(ERROR_CANNOT_SUBMIT))).inRoot(isToast())));

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929482")
    public void testUpgradeAcceptedAfterRideHasStarted() throws InterruptedException {

        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);
        onView(withId(R.id.upgradeCarType)).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));
        // dismiss popup
        onView(withId(R.id.dismiss_button)).perform(click());
        // check popup close
        waitFor(condition().withMatcher(withText(R.string.upgrade_waiting_rider)).withAssertion(doesNotExist()));

        // mock arrived state
        setRideArrivedSate();
        // go to arrived sate
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive)))
                .check(matches(isDisplayed()))
                .perform(swipeRight());

        waitFor(condition().withView(onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()))));

        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()))
                .perform(click());

        // mock ride started
        setRideStartedSate();

        waitForViewInWindow(withText(R.string.starting_trip_confirmation_dialog_msg));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        chainRequests(RequestType.EVENT_RIDE_UPGRADE_ACCEPTED, RequestType.EVENTS_EMPTY_200_GET);
        waitFor(condition().withView(onView(withText(R.string.upgraded_to_suv))));

        onView(withId(R.id.dismiss_button)).perform(click());
        waitFor(condition().withView(onView(allOf(withId(R.id.toolbarTitle), withText(containsString("SUV"))))));

        waitFor(condition().withView(onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish)))
                .check(matches(isDisplayed()))));

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929485")
    public void testUpgradeDeclinedAfterRideHasStarted() throws InterruptedException {
        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);
        onView(withId(R.id.upgradeCarType)).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));
        // dismiss popup
        onView(withId(R.id.dismiss_button)).perform(click());
        // check popup close
        waitFor(condition().withMatcher(withText(R.string.upgrade_waiting_rider)).withAssertion(doesNotExist()));

        // mock arrived state
        setRideArrivedSate();
        // go to arrived sate
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive)))
                .check(matches(isDisplayed()))
                .perform(swipeRight());

        waitFor(condition().withView(onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()))));

        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()))
                .perform(click());

        // mock ride started
        setRideStartedSate();

        waitForViewInWindow(withText(R.string.starting_trip_confirmation_dialog_msg));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        mockEvent(RequestType.EVENT_RIDE_UPGRADE_DECLINED);
        waitFor(condition().withView(onView(withText(R.string.upgrade_failed))));

        onView(withId(R.id.dismiss_button)).perform(click());
        waitFor(condition().withView(onView(allOf(withId(R.id.toolbarTitle), withText(containsString("STANDARD"))))));

        waitFor(condition().withView(onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish)))
                .check(matches(isDisplayed()))));

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929490")
    public void testUpgradeAcceptedWhenInOtherScreen() throws InterruptedException {

        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);
        onView(withId(R.id.upgradeCarType)).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));
        // dismiss popup
        onView(withId(R.id.dismiss_button)).perform(click());
        // check popup close
        waitFor(condition().withMatcher(withText(R.string.upgrade_waiting_rider)).withAssertion(doesNotExist()));

        // mock arrived state
        setRideArrivedSate();
        // go to arrived sate
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive)))
                .check(matches(isDisplayed()))
                .perform(swipeRight());

        waitFor(condition().withView(onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()))));

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        // navigate to promocodes
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        // check balance value
        // wait for request button
        waitForCompletelyDisplayed(R.id.usersPhoto);

        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        chainRequests(RequestType.EVENT_RIDE_UPGRADE_ACCEPTED, RequestType.EVENTS_EMPTY_200_GET);
        waitFor(condition().withView(onView(withText(R.string.upgraded_to_suv))));

        onView(withId(R.id.dismiss_button)).perform(click());
        waitFor(condition().withView(onView(allOf(withId(R.id.toolbarTitle), withText(containsString("SUV"))))));

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929491")
    public void testUpgradeWhileLosingInternetConnection() throws InterruptedException {

        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // switch off connection
        setNetworkError(true);

        // try to upgrade
        mockRequests(RequestType.RIDES_UPGRADE_200_POST);
        onView(withId(R.id.upgradeCarType)).perform(click());

        waitForViewInWindow(onView(withText(R.string.network_error)).inRoot(isToast()));

        // switch on connection
        setNetworkError(false);

        // try to upgrade
        onView(withId(R.id.upgradeCarType)).perform(click());

        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));
        // dismiss popup
        onView(withId(R.id.dismiss_button)).perform(click());
        // check popup close
        waitFor(condition().withMatcher(withText(R.string.upgrade_waiting_rider)).withAssertion(doesNotExist()));

        // mock arrived state
        setRideArrivedSate();
        // go to arrived sate
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive)))
                .check(matches(isDisplayed()))
                .perform(swipeRight());

        waitFor(condition().withView(onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()))));

        cancelRideByRider();
    }

    @Test
    @TestCases("C1929496")
    public void testRecovery() throws InterruptedException, RemoteException, UiObjectNotFoundException {

        setRideAssignedSate();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        mockRequests(RequestType.RIDES_UPGRADE_200_POST);
        onView(withId(R.id.upgradeCarType)).perform(click());
        waitFor(condition().withView(onView(withText(R.string.upgrade_waiting_rider))));
        // dismiss popup
        onView(withId(R.id.dismiss_button)).perform(click());
        // check popup close
        waitFor(condition().withMatcher(withText(R.string.upgrade_waiting_rider)).withAssertion(doesNotExist()));

        // mock arrived state
        setRideArrivedSate();
        // go to arrived sate
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive)))
                .check(matches(isDisplayed()))
                .perform(swipeRight());

        waitFor(condition().withView(onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip)))
                .check(matches(isDisplayed()))));

        DeviceTestUtils.pressHome();

        removeRequests(RequestType.EVENTS_EMPTY_200_GET);
        chainRequests(RequestType.EVENT_RIDE_UPGRADE_ACCEPTED, RequestType.EVENTS_EMPTY_200_GET);

        DeviceTestUtils.restoreFromRecentApps(3000);

        waitFor(condition().withView(onView(withText(R.string.upgraded_to_suv))));
        onView(withId(R.id.dismiss_button)).perform(click());
        waitFor(condition().withView(onView(allOf(withId(R.id.toolbarTitle), withText(containsString("SUV"))))));

        cancelRideByRider();
    }

    private void validateUpgradeToSUVVisible() {
        // Driver app should NOT show option to Upgrade to SUV
        onView(withId(R.id.upgradeCarType)).check(matches(isDisplayed()));
    }

    private void validateUpgradeToSUVNotVisible() {
        // Driver app should NOT show option to Upgrade to SUV
        onView(withId(R.id.upgradeCarType)).check(matches(not(isDisplayed())));
    }

    private void setRideAssignedSate() {
        removeRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
        mockRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET);
    }

    private void setRideArrivedSate() {
        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET);
        mockRequests(RequestType.RIDE_DRIVER_REACHED_200_GET,
                RequestType.ACTIVE_DRIVER_REACHED_200_GET);
    }

    private void setRideStartedSate() {
        removeRequests(RequestType.RIDE_DRIVER_REACHED_200_GET,
                RequestType.ACTIVE_DRIVER_REACHED_200_GET);
        mockRequests(RequestType.RIDE_ACTIVE_200_GET,
                RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET);
    }

    private void cancelRideByRider() throws InterruptedException {
        // As Rider cancel the ride
        removeRequests(RequestType.ACTIVE_DRIVER_REACHED_200_GET,
                RequestType.RIDE_DRIVER_REACHED_200_GET,
                RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET,
                RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.RIDE_ACTIVE_200_GET);
        mockEvent(RequestType.EVENT_RIDE_CANCELLED_BY_RIDER);
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);

        // Close the dialog
        String message = getString(R.string.ride_cancelled_by_rider, "RideOneTwoNine");
        waitFor(condition().withMatcher(withText(message)));
        onView(AllOf.allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_ok))).perform(click());

        // Check no duplicate dialog
        onView(withText(message)).check(doesNotExist());
        assertFalse(exists(withId(R.id.sliderText)));
    }

    private List<Event> createRideEvent(String resourceName, long rideId) {
        List<Event> list = getEventsResponse(resourceName);
        list.get(0).getRide().setId(rideId);
        return list;
    }
}
