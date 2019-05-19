package cases.s1_splash;

import android.content.pm.PackageManager;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.App;
import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.helpers.MockHelper;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.NavigationUtils;

import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.allowPermissionsIfNeeded;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static junit.framework.Assert.assertFalse;

/**
 * Created by hatak on 13.10.2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SplashSuite extends BaseUITest {

    public static final String RIDER_NAME = "RideOneTwoNine";
    private static final String PICKUP_ADDRESS = "11610 Jollyville Road, Austin, Texas";

    @Override
    public void setUp() {
        super.setUp();
        initMockResponseFactory(DriverMockResponseFactory.class);
        mockRequests(RequestType.GLOBAL_APP_INFO_200_GET,
                RequestType.LOGIN_SUCCESS_200_POST,
                RequestType.CURRENT_USER_200_GET,
                RequestType.CONFIG_DRIVER_200_GET,
                RequestType.TOKENS_200_POST,
                RequestType.CURRENT_DRIVER_200_GET,
                RequestType.QUEUES_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.ACTIVE_DRIVER_EMPTY_200_GET,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.LOGOUT_200_POST,
                RequestType.DRIVER_GO_ONLINE_200_POST,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET);
    }

    @Rule
    public RaActivityRule<NavigationDrawerActivity> activityRule = new RaActivityRule<>(NavigationDrawerActivity.class, true, false);

    @Test
    @TestCases("C1929179")
    public void testSplashScreenOnFreshInstall() throws UiObjectNotFoundException, PackageManager.NameNotFoundException {
        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(R.id.signIn);

        // logo is visible
        onView(withId(R.id.logo)).check(matches(isDisplayed()));

        // correct version name is displayed
        onView(withId(R.id.label_version)).check(matches(withText(AppInfoUtil.getAppVersionName()))).check(matches(isDisplayed()));

        // sign-in/register buttons are displayed and clickable
        onView(withId(R.id.signIn)).check(matches(withText(R.string.sign_in))).check(matches(isDisplayed())).perform(click());

        waitForDisplayed(R.id.email);
        waitForDisplayed(R.id.password);

        pressBack();

        onView(withId(R.id.logo)).check(matches(isDisplayed()));
        onView(withId(R.id.label_version)).check(matches(withText(AppInfoUtil.getAppVersionName()))).check(matches(isDisplayed()));

        onView(withId(R.id.signIn)).check(matches(withText(R.string.sign_in))).check(matches(isDisplayed())).perform(click());

        // enter arbitrary data as we are mocking server
        onView(withId(R.id.email)).perform(typeText("anyvalid@email.com"));
        onView(withId(R.id.password)).perform(typeText("doesnotmatter"), closeSoftKeyboard());

        // perform login and wait
        onView(withId(R.id.done)).perform(click());

        // allow permission
        allowPermissionsIfNeeded();

        // check map is visible
        onView(withId(R.id.mapContainer)).check(matches(isDisplayed()));

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        // navigate to settings
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        // sign out
        onView(withId(R.id.signOut)).perform(scrollTo(), click());

        // check toast
        waitForViewInWindow(onView(withText(R.string.signed_out)).inRoot(isToast()));

        // logo is visible
        onView(withId(R.id.logo)).check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1929181")
    public void testNoConnectionOnStart() throws UiObjectNotFoundException, PackageManager.NameNotFoundException {
        setNetworkError(true);
        NavigationUtils.startActivity(activityRule);

        waitForViewInWindow(onView(withText(R.string.network_error)));

        // sign-in/register buttons are displayed and clickable
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).check(matches(withText(R.string.retry))).check(matches(isDisplayed()));
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultNegative)).check(matches(withText(R.string.cancel))).check(matches(isDisplayed()));

        setNetworkError(false);

        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());

        waitForDisplayed(R.id.signIn);

        // logo is visible
        onView(withId(R.id.logo)).check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1929183")
    public void testDoNotAppearIfUserAlreadyLoggedInAndNotInRide() throws UiObjectNotFoundException, PackageManager.NameNotFoundException {
        activityRule.beforeLaunched = () -> {
            App.getPrefs().putString(Constants.TOKEN_KEY, "token");
            App.getPrefs().putString(Constants.X_TOKEN_KEY, "token");
        };
        NavigationUtils.startActivity(activityRule);
        allowPermissionsIfNeeded();
        waitForDisplayed(R.id.mapView);
    }

    @Test
    @TestCases("C1929182")
    public void testDoNotAppearIfUserAlreadyLoggedInAndInRide() throws UiObjectNotFoundException, PackageManager.NameNotFoundException, InterruptedException {
        activityRule.beforeLaunched = () -> {
            setRideAssignedSate();
            App.getPrefs().putString(Constants.TOKEN_KEY, "token");
            App.getPrefs().putString(Constants.X_TOKEN_KEY, "token");
        };
        NavigationUtils.startActivity(activityRule);
        allowPermissionsIfNeeded();
        waitForDisplayed(R.id.mapView);
        waitForDisplayed(R.id.navigate);
        cancelRideByRider();
    }

    private void setRideAssignedSate() {
        Ride ride = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
        ride.getRequestedCarType().setCarCategory("REGULAR");
        ride.getRequestedCarType().setTitle("STANDARD");
        ride.setStartAddress(PICKUP_ADDRESS);
        ride.getRider().setFirstname(RIDER_NAME);
        removeRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
        ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, ride, this);
        mockRequest(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, activeDriver);
        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, ride);
    }

    private void cancelRideByRider() throws InterruptedException {
        // As Rider cancel the ride
        removeRequests(RequestType.RIDE_DRIVER_REACHED_200_GET,
                RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.EVENT_RIDER_LOCATION_UPDATED,
                RequestType.RIDE_ACTIVE_200_GET,
                RequestType.EVENT_END_LOCATION_UPDATED,
                RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET,
                RequestType.ACTIVE_DRIVER_REACHED_200_GET);
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
        mockEvent(RequestType.EVENT_RIDE_CANCELLED_BY_RIDER);

        // Close the dialog
        String message = getString(R.string.ride_cancelled_by_rider, RIDER_NAME);
        waitFor(condition().withMatcher(withText(message)));
        onView(AllOf.allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_ok))).perform(click());
        onView(withId(R.id.sliderText)).check(doesNotExist());

        waitFor(condition("Should restore online state")
                .withMatcher(withId(R.id.toolbarActionButton))
                .withMatcher(withText(R.string.action_go_offline)));

    }

}
