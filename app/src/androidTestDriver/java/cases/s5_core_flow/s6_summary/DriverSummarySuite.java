package cases.s5_core_flow.s6_summary;

import android.os.RemoteException;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.App;
import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.SerializationHelper;

import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.setRating;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-10235
 * Created by Sergey Petrov on 06/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverSummarySuite extends BaseUITest {

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
                RequestType.LOGOUT_200_POST);
    }

    @Test
    @TestCases("C1929353")
    public void testDriverEarnings() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        mockUnratedRide();
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // wait rate driver dialog
        waitForViewInWindow(withId(R.id.rb_rate_driver));
        Matchers.waitFor(Matchers.condition("Price should be displayed")
                .withMatcher(withId(R.id.tv_price))
                .withMatcher(withText("$11.25")));
    }

    @Test
    @TestCases("C1929354")
    public void testRateRider() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        mockUnratedRide();
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // wait rate driver dialog
        waitForViewInWindow(withId(R.id.rb_rate_driver));
        onView(withId(R.id.rb_rate_driver)).perform(setRating(5.0f));

        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));

        mockRequests(RequestType.RIDE_RATING_200_PUT);

        // submit
        onView(withId(R.id.btn_submit)).perform(click());

        Matchers.waitFor(Matchers.condition("Should be offline")
                .withMatcher(withId(R.id.toolbarActionButton))
                .withMatcher(withText(R.string.action_go_online)));

        verifyRequest(RequestType.RIDE_RATING_200_PUT, hasQueryParam("rating", is("5.0")));

        NavigationUtils.toRequestedState(this, null);
        NavigationUtils.acceptRideRequest(this, null);
        NavigationUtils.arrive(this, null);
        NavigationUtils.startTrip(this, null);
        NavigationUtils.endTrip(this, null);
        NavigationUtils.rateRide(this);
    }

    /**
     * Also need for RA-11057
     */
    @Test
    @TestCases({"C1929355", "C1929356"})
    public void recovery() throws UiObjectNotFoundException, InterruptedException, RemoteException {
        NavigationUtils.startActivity(activityRule);
        mockUnratedRide();
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // wait rate driver dialog
        waitForViewInWindow(withId(R.id.rb_rate_driver));

        // check submit button hidden
        onView(withId(R.id.btn_submit)).check(matches(not(isDisplayed())));

        // set rating
        onView(withId(R.id.rb_rate_driver)).perform(setRating(1.0f));

        setNetworkError(true);

        // check submit button visible
        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        // check submit button visible
        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));


        NavigationUtils.rateRide(this, null, () -> {
            Matchers.waitFor(condition("Should switch to offline state")
                    .withMatcher(AllOf.allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online)))
                    .withCheck(AllOf.allOf(isDisplayed(), isEnabled(), isClickable())));
        });

        verifyRequestsCount(RequestType.RIDE_RATING_200_PUT, is(0));

        Matchers.waitFor(Matchers.condition("Should show network error")
                .withView(onView(withText(R.string.network_error)).inRoot(isToast())));

        setNetworkError(false);

        mockRequests(RequestType.RIDE_RATING_200_PUT,
                RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);

        Matchers.waitFor(condition("Should send pending ride rating")
                .withBool(() -> getRequestCount(RequestType.RIDE_RATING_200_PUT) == 1));

        Matchers.waitFor(Matchers.condition("Should be online")
                .withMatcher(withId(R.id.toolbarActionButton))
                .withMatcher(withText(R.string.action_go_offline)));

        verifyRequest(RequestType.RIDE_RATING_200_PUT, hasQueryParam("rating", is("4.0")));
    }

    private void mockUnratedRide() {
        List<Long> unratedRides = Collections.singletonList(1227302L);
        App.getPrefs().getUserSpecificPreferences(2581)
                .edit()
                .putString("unrated_rides", SerializationHelper.serialize(unratedRides))
                .apply();
        mockRequests(RequestType.RIDE_COMPLETED_200_GET, RequestType.PENDING_EVENTS_200_POST);
    }

}
