package cases.s5_core_flow.s3_driver_arrived.s5_recovery;

import android.os.RemoteException;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.utils.UIUtils;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-11582
 * Created by Sergey Petrov on 05/07/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverArrivedRecoverySuite extends BaseUITest {

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
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET);
    }

    /**
     * C1930729: Put into background and bring back to foreground
     * C1930730: Lock, unlock device
     * C1930731: Switch off/on internet connection
     */
    @Test
    @TestCases({"C1930729", "C1930730", "C1930731"})
    public void test_Recovery() throws InterruptedException, RemoteException, UiObjectNotFoundException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);

        // simulate reached state
        NavigationUtils.toAssignedState(this);
        NavigationUtils.toReachedState(this);

        checkUiState();

        //------------------------------------------------------------------------------------------
        // C1930729: Put into background and bring back to foreground
        // C1930730: Lock, unlock device
        //------------------------------------------------------------------------------------------

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        checkUiState();

        //------------------------------------------------------------------------------------------
        // C1930731: Switch off/on internet connection
        //------------------------------------------------------------------------------------------

        DeviceTestUtils.setWifiEnabled(false);

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        tryCloseNetworkError();

        checkUiState();

        DeviceTestUtils.setWifiEnabled(true);

        tryCloseNetworkError();

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        checkUiState();
    }

    private void checkUiState() throws InterruptedException {
        String name = "Ride15";
        String rating = UIUtils.formatRating(5.0);
        String category = "STANDARD";
        String car = "Blue Cadillac CTS Wagon";
        String plate = "CADILLAC";

        // check collapsed state
        onView(allOf(withId(R.id.driver_name_small), isDisplayed())).check(matches(withText(name)));
        onView(allOf(withId(R.id.driver_rate_small), isDisplayed())).check(matches(withText(rating)));
        onView(allOf(withId(R.id.car_category_small), isDisplayed())).check(matches(withText(category)));
        onView(allOf(withId(R.id.car_make_small), isDisplayed())).check(matches(withText(car)));
        onView(allOf(withId(R.id.car_plate_small), isDisplayed())).check(matches(withText(plate)));
        onView(allOf(withId(R.id.driver_image_small), isDisplayed())).check(matches(hasDrawable()));

        // tap on driver image should cause panel expand
        onView(withId(R.id.driver_image_small)).perform(click());
        waitForCompletelyDisplayed(R.id.ride_details);
        Matchers.waitFor(condition("Driver small image should be hidden")
                .withMatcher(withId(R.id.driver_image_small))
                .withCheck(not(isDisplayed())));

        onView(withId(R.id.driver_name_small)).check(matches(not(isDisplayed())));
        onView(withId(R.id.driver_rate_small)).check(matches(not(isDisplayed())));
        onView(withId(R.id.car_category_small)).check(matches(not(isDisplayed())));
        onView(withId(R.id.car_make_small)).check(matches(not(isDisplayed())));
        onView(withId(R.id.car_plate_small)).check(matches(not(isDisplayed())));

        // check expanded state
        onView(allOf(withId(R.id.tv_driver_name), isDisplayed())).check(matches(withText(name)));
        onView(allOf(withId(R.id.tv_driver_rate), isDisplayed())).check(matches(withText(rating)));
        onView(allOf(withId(R.id.car_category), isDisplayed())).check(matches(withText(category)));
        onView(allOf(withId(R.id.car_color_make_model), isDisplayed())).check(matches(withText(car)));
        onView(allOf(withId(R.id.car_plate), isDisplayed())).check(matches(withText(plate)));
        onView(allOf(withId(R.id.driver_image), isDisplayed())).check(matches(hasDrawable()));
        onView(allOf(withId(R.id.car_image), isDisplayed())).check(matches(hasDrawable()));

        // collapse panel
        onView(withId(R.id.ride_details)).perform(swipeDown());
        Matchers.waitFor(condition("Driver small image should become visible")
                .withMatcher(withId(R.id.driver_image_small)));

    }

    private void tryCloseNetworkError() {
        if (exists(withText(R.string.network_error))) {
            onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        }
    }
}
