package cases.s5_core_flow.s5_summary.s1_main;

import android.os.RemoteException;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.App;
import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.Ride;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.DeviceTestUtils.pressBack;
import static com.rideaustin.utils.DeviceTestUtils.pressHome;
import static com.rideaustin.utils.DeviceTestUtils.restoreFromRecentApps;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-10228
 * Created by Sergey Petrov on 22/05/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SummaryMainSuit extends BaseUITest {

    private static final int USER_1_ID = 1443;

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
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CONFIG_RIDER_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.CONFIG_ZIPCODES_200_GET,
                RequestType.ACDR_REGULAR_200_GET, // has drivers
                RequestType.RIDE_MAP_200_GET,
                RequestType.RIDE_RATING_200_PUT,
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET,
                RequestType.CHARITIES_200_GET);
    }

    /**
     * Summary: Popup appears at the end of trip
     */
    @Test
    @TestCases("C1930762")
    public void popupAppearsAfterRide1() throws RemoteException, UiObjectNotFoundException, InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        //------------------------------------------------------------------------------------------
        // 1. Trip was ended by driver when Rider app is running in foreground
        //------------------------------------------------------------------------------------------

        // go through ride
        NavigationUtils.throughRideRequest(this);
        NavigationUtils.throughRide(this);
        NavigationUtils.throughRideSummary(this);

        // wait for map restore
        waitForDisplayed(R.id.set_pickup_location);
    }

    /**
     * Summary: Popup appears at the end of trip
     */
    @Test
    @TestCases("C1930762")
    public void popupAppearsAfterRide2() throws RemoteException, UiObjectNotFoundException, InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        //------------------------------------------------------------------------------------------
        // 2. Trip was ended by driver when Rider app is running in background
        // 3. Trip was ended by driver when Rider app is running in foreground and device is locked
        // 4. Trip was ended by driver when Rider app is running in background and device is locked
        //------------------------------------------------------------------------------------------

        // go through ride
        NavigationUtils.throughRideRequest(this);
        NavigationUtils.throughRide(this);

        // send app to background
        pressHome();

        // simulate ride complete
        removeRequests(RequestType.RIDE_ACTIVE_200_GET,
                RequestType.CURRENT_RIDE_ACTIVE_200_GET);
        mockRequests(RequestType.RIDE_COMPLETED_200_GET,
                RequestType.CURRENT_RIDE_COMPLETED_200_GET);

        // restore app
        restoreFromRecentApps(2000);

        // go through summary
        NavigationUtils.throughRideSummary(this);

        // wait for map restore
        waitForDisplayed(R.id.set_pickup_location);
    }


    /**
     * Summary: Popup appears at the end of trip
     */
    @Test
    @TestCases("C1930762")
    public void popupAppearsAfterRide3() throws RemoteException, UiObjectNotFoundException, InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        //------------------------------------------------------------------------------------------
        // 5. Trip was ended by driver when Rider app is running in background,
        // device is locked and not map screen is opened
        //------------------------------------------------------------------------------------------

        // go through ride
        NavigationUtils.throughRideRequest(this);
        NavigationUtils.throughRide(this);

        // go to settings screen
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        // send app to background
        pressHome();

        // simulate ride complete
        removeRequests(RequestType.RIDE_ACTIVE_200_GET,
                RequestType.CURRENT_RIDE_ACTIVE_200_GET);
        mockRequests(RequestType.RIDE_COMPLETED_200_GET,
                RequestType.CURRENT_RIDE_COMPLETED_200_GET);

        // restore app
        restoreFromRecentApps(2000);

        // summary should be visible
        // wait rate driver dialog
        waitForViewInWindow(withId(R.id.rb_rate_driver));

        // go through summary
        NavigationUtils.throughRideSummary(this);

        // still on settings
        onView(withId(R.id.toolbarTitle))
                .check(matches(withText(R.string.settings)))
                .check(matches(isDisplayed()));

        pressBack();

        // wait for map restore
        waitForDisplayed(R.id.set_pickup_location);
    }

    /**
     * Summary: Popup appears at the end of trip
     */
    @Test
    @TestCases("C1930762")
    public void popupAppearsAfterRide4() throws RemoteException, UiObjectNotFoundException, InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        //------------------------------------------------------------------------------------------
        // 6. Trip was ended by driver when Rider app is running in foreground
        // and there was no internet connection
        //------------------------------------------------------------------------------------------

        // go through ride
        NavigationUtils.throughRideRequest(this);
        NavigationUtils.throughRide(this);

        // lose internet
        setNetworkError(true);
        DeviceTestUtils.setAirplaneMode(true);
        waitForDisplayed(R.id.error_panel);

        // simulate ride complete
        removeRequests(RequestType.RIDE_ACTIVE_200_GET,
                RequestType.CURRENT_RIDE_ACTIVE_200_GET);
        mockRequests(RequestType.RIDE_COMPLETED_200_GET,
                RequestType.CURRENT_RIDE_COMPLETED_200_GET);

        // internet restored
        DeviceTestUtils.setAirplaneMode(false);
        Matchers.waitFor(condition().withMatcher(withId(R.id.error_panel))
                .withCheck(not(isDisplayed())));
        setNetworkError(false);

        // go through summary
        NavigationUtils.throughRideSummary(this);

        // wait for map restore
        waitForDisplayed(R.id.set_pickup_location);

    }

    /**
     * Summary: Popup appears when not map screen is opened
     * (Round Up / Settings / Payments / Trip History / Promotions / Driver with Austin / Female Driver Mode
     */
    @Test
    @TestCases("C1930763")
    public void popupAppearsOnOtherScreens() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");


        //------------------------------------------------------------------------------------------
        // Round Up
        //------------------------------------------------------------------------------------------

        // go through ride
        NavigationUtils.throughRideRequest(this);
        NavigationUtils.throughRide(this);

        // go to round up screen
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navDonate));

        // simulate ride complete
        removeRequests(RequestType.RIDE_ACTIVE_200_GET,
                RequestType.CURRENT_RIDE_ACTIVE_200_GET);
        mockRequests(RequestType.RIDE_COMPLETED_200_GET,
                RequestType.CURRENT_RIDE_COMPLETED_200_GET);

        // summary should be visible
        // wait rate driver dialog
        waitForViewInWindow(withId(R.id.rb_rate_driver));

        // go through summary
        NavigationUtils.throughRideSummary(this);

        waitForDisplayed(navigationIcon(), "Wait for toolbar");

        onView(navigationIcon()).perform(click());

        // wait for map restore
        waitForDisplayed(R.id.set_pickup_location);

        // TODO: need to fix
        // RA-9343, RA-9344, RA-9345, RA-9347
    }

    /**
     * Reference: RA-12461
     */
    @Test
    public void shouldReadRideSummaryFromServerConfig() {
        NavigationUtils.startActivity(activityRule);

        String rideDescription = "Custom ride description from server";
        removeRequests(RequestType.CONFIG_RIDER_200_GET);
        GlobalConfig config = getResponse("CONFIG_GLOBAL_200", GlobalConfig.class);
        config.getRides().setRideSummaryDescription(rideDescription);
        mockRequest(RequestType.CONFIG_RIDER_200_GET, config);
        delayRequest(RequestType.CONFIG_RIDER_200_GET, 3000);

        App.getPrefs().saveRideToRate(1L, USER_1_ID);
        mockRequests(RequestType.RIDE_COMPLETED_200_GET);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // wait for ride summary
        waitForDisplayed(R.id.tv_ride_summary);

        // Unfortunately Espresso/UIAutomator can't check empty text
        // because of delayed not-idle network call

        // first, it should be empty (not taken from embedded config)
        // onView(withId(R.id.tv_ride_summary)).check(matches(withText("")));

        // wait to be sure summary is updated once from server config
        waitFor("Need to wait 10 seconds to check summary text", 10000);
        onView(withId(R.id.tv_ride_summary))
                .check(matches(isDisplayed()))
                .check(matches(withText(rideDescription)));
    }

    /**
     * Reference: RA-12461
     */
    @Test
    public void shouldReadRideSummaryFromEmbeddedStrings() {
        NavigationUtils.startActivity(activityRule);

        String rideDescription = "Custom ride description from server";
        removeRequests(RequestType.CONFIG_RIDER_200_GET);
        GlobalConfig config = getResponse("CONFIG_GLOBAL_200", GlobalConfig.class);
        config.getRides().setRideSummaryDescription(rideDescription);
        mockRequest(RequestType.CONFIG_RIDER_200_GET, config);
        // too long to wait
        delayRequest(RequestType.CONFIG_RIDER_200_GET, 10000);

        App.getPrefs().saveRideToRate(1L, USER_1_ID);
        mockRequests(RequestType.RIDE_COMPLETED_200_GET);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // wait for ride summary
        waitForDisplayed(R.id.tv_ride_summary);

        // Unfortunately Espresso/UIAutomator can't check empty text
        // because of delayed not-idle network call

        // first, it should be empty (not taken from embedded config)
        // onView(withId(R.id.tv_ride_summary)).check(matches(withText("")));

        // wait to be sure summary is updated once from embedded strings
        waitFor("Need to wait 10 seconds to check summary text", 12000);
        onView(withId(R.id.tv_ride_summary))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.ride_summary_description)));
    }

    /**
     * Reference: RA-12879
     */
    @Test
    public void shouldPickCorrectRideSummaryWhenCreditsChanged() {
        NavigationUtils.startActivity(activityRule);

        String rideDescription = "Custom ride description from server when credits charged";
        removeRequests(RequestType.CONFIG_RIDER_200_GET);
        GlobalConfig config = getResponse("CONFIG_GLOBAL_200", GlobalConfig.class);
        config.getRides().setRideSummaryDescriptionFreeCreditCharged(rideDescription);
        mockRequest(RequestType.CONFIG_RIDER_200_GET, config);
        // too long to wait
        delayRequest(RequestType.CONFIG_RIDER_200_GET, 3000);

        App.getPrefs().saveRideToRate(1L, USER_1_ID);
        Ride ride = getResponse("RIDE_COMPLETED_200", Ride.class);
        ride.setFreeCreditCharged("5.0");
        mockRequest(RequestType.RIDE_COMPLETED_200_GET, ride);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // wait for ride summary
        waitForDisplayed(R.id.tv_ride_summary);

        // Unfortunately Espresso/UIAutomator can't check empty text
        // because of delayed not-idle network call

        // first, it should be empty (not taken from embedded config)
        // onView(withId(R.id.tv_ride_summary)).check(matches(withText("")));

        // wait to be sure summary is updated once from embedded strings
        waitFor("Need to wait 10 seconds to check summary text", 10000);
        onView(withId(R.id.tv_ride_summary))
                .check(matches(isDisplayed()))
                .check(matches(withText(rideDescription)));

    }
}
