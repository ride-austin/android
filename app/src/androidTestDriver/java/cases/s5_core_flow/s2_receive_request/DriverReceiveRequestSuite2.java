package cases.s5_core_flow.s2_receive_request;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.MapTestUtil;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.matchesRegex;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.allOf;

/**
 * Reference: RA-10231, RA-11252
 * Created by Sergey Petrov on 14/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverReceiveRequestSuite2 extends BaseUITest {

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
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.DRIVERS_CARTYPES_200_GET,
                RequestType.CURRENT_DRIVER_200_GET,
                RequestType.ACTIVE_DRIVER_EMPTY_200_GET,
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET,
                RequestType.DRIVER_GO_ONLINE_200_POST,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.PENDING_EVENTS_200_POST,
                RequestType.ACCEPT_RIDE_200_POST,
                RequestType.DECLINE_RIDE_200_DELETE,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE);
    }

    /**
     * C1929259: Driver can go offline when receiving ride request
     * C1929264: Driver is able to receive ride request when app in background
     * C1929265: Driver receives ride request after app is resumed from background
     * C1929266: Driver is able to receive ride request when device is locked
     * C1929267: Current screen is replaced with Accept screen when ride request is received
     */
    @Test
    @TestCases({"C1929259", "C1929264", "C1929265", "C1929266", "C1929267"})
    public void test_ReceiveRequestFlow() {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        //------------------------------------------------------------------------------------------
        // Go online
        //------------------------------------------------------------------------------------------

        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_online), isDisplayed(), isEnabled(), isClickable()));
        onView(withId(R.id.toolbarActionButton)).perform(click());
        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_offline), isDisplayed(), isEnabled(), isClickable()));

        //------------------------------------------------------------------------------------------
        // C1929264: Driver is able to receive ride request when app in background
        // C1929266: Driver is able to receive ride request when device is locked
        //------------------------------------------------------------------------------------------

        DeviceTestUtils.pressHome();

        NavigationUtils.toRequestedState(this, null);

        onView(withId(R.id.accept)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline), isClickable())).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.button), withText(R.string.menu_decline), isClickable())).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.counter_text), withText(matchesRegex("\\d+")))).check(matches(isDisplayed()));

        //------------------------------------------------------------------------------------------
        // C1929259: Driver can go offline when receiving ride request
        //------------------------------------------------------------------------------------------

        NavigationUtils.toEmptyState(this);

        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline))).perform(click());

        // wait for state switch to offline
        waitFor(R.id.button, allOf(withText(R.string.menu_support), isDisplayed()));
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online), isClickable())).check(matches(isDisplayed()));
        assertFalse(exists(withId(R.id.pending_pickup)));

        // assert one car marker (no other drivers according to ACTIVE_DRIVERS_EMPTY_200)
        MapTestUtil.assertCarMarkersCount(1);
        // assert pickup marker removed
        MapTestUtil.assertPickupMarkersNotVisible();

        //------------------------------------------------------------------------------------------
        // C1929267: Current screen is replaced with Accept screen when ride request is received
        // C1929265: Driver receives ride request after app is resumed from background
        //------------------------------------------------------------------------------------------

        // go online
        onView(withId(R.id.toolbarActionButton)).perform(click());
        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_offline), isDisplayed(), isEnabled(), isClickable()));

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        // navigate to settings
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        // go to edit account
        onView(allOf(withId(R.id.editAccount), isDisplayed())).perform(click());

        // receive ride request
        NavigationUtils.toRequestedState(this, null);
        onView(withId(R.id.accept)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline), isClickable())).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.button), withText(R.string.menu_decline), isClickable())).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.counter_text), withText(matchesRegex("\\d+")))).check(matches(isDisplayed()));

        // decline
        NavigationUtils.toEmptyState(this);
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);

        onView(allOf(withId(R.id.button), withText(R.string.menu_decline))).perform(click());

        // wait for state switch back to online
        waitFor(R.id.button, allOf(withText(R.string.menu_support), isDisplayed()));
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline), isClickable())).check(matches(isDisplayed()));
        assertFalse(exists(withId(R.id.pending_pickup)));

        // assert one car marker (no other drivers according to ACTIVE_DRIVERS_EMPTY_200)
        MapTestUtil.assertCarMarkersCount(1);
        // assert pickup marker removed
        MapTestUtil.assertPickupMarkersNotVisible();
    }


    /**
     * Reference: RA-12071
     */
    @Test
    public void shouldReceiveRequestInOffline() {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        NavigationUtils.toRequestedState(this, null);

        onView(withId(R.id.accept)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline), isClickable())).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.button), withText(R.string.menu_decline), isClickable())).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.counter_text), withText(matchesRegex("\\d+")))).check(matches(isDisplayed()));

        // decline
        NavigationUtils.toEmptyState(this);
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);

        onView(allOf(withId(R.id.button), withText(R.string.menu_decline))).perform(click());

        // wait for state switch back to online
        waitFor(R.id.button, allOf(withText(R.string.menu_support), isDisplayed()));
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline), isClickable())).check(matches(isDisplayed()));
        assertFalse(exists(withId(R.id.pending_pickup)));

        // assert one car marker (no other drivers according to ACTIVE_DRIVERS_EMPTY_200)
        MapTestUtil.assertCarMarkersCount(1);
        // assert pickup marker removed
        MapTestUtil.assertPickupMarkersNotVisible();

    }

}
