package cases.s5_core_flow.s1_requesting.s5_recovery;

import android.os.RemoteException;
import android.support.test.espresso.ViewInteraction;
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
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.NavigationUtils;

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
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.getText;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotEmptyText;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Created by Sergey Petrov on 26/05/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RecoverySuite1 extends BaseUITest {

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
                RequestType.LOGOUT_200_POST);
    }

    /**
     * Requesting: Put into background and bring back to foreground
     * Verify when putting app in the background and foreground, app still shows the selected pickup and destination locations, and app is not stuck in loading.
     * see also RA-10542
     */
    @Test
    @TestCases("C1930649")
    public void recoveryAfterBackground() throws UiObjectNotFoundException, InterruptedException, RemoteException {
        NavigationUtils.startActivity(activityRule);

        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // map is displayed
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // wait while pickup address is set
        waitForNotEmptyText(R.id.pickup_address);

        // wait for slider and pin loaded
        waitForDisplayed(R.id.set_pickup_location);
        waitForDisplayed(R.id.car_types_slider);

        // pin view shows correct ETA from ACDR_REGULAR_200_GET
        onView(allOf(withId(R.id.set_pickup_location), isDisplayed())).check(matches(withText(R.string.set_pickup_location)));
        onView(withId(R.id.time_to_pickup)).check(matches(allOf(withText("2"), isDisplayed())));

        // Save pickup text
        String pickupText = getText(allOf(withId(R.id.pickup_address), isDisplayed()));

        //------------------------------------------------------------------------------------------
        // Put app in the background, restore and check
        //------------------------------------------------------------------------------------------

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        // check pickup address is the same
        waitForNotEmptyText(R.id.pickup_address);
        onView(allOf(withId(R.id.pickup_address), isDisplayed())).check(matches(withText(pickupText)));

        // wait for slider and pin loaded
        waitForDisplayed(R.id.set_pickup_location);
        waitForDisplayed(R.id.car_types_slider);

        //------------------------------------------------------------------------------------------
        // Check RA-10542: tap [Set pickup location] and immediately press home and restore
        //------------------------------------------------------------------------------------------

        onView(allOf(withId(R.id.pickup_location), isDisplayed())).perform(click());

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        // Request panel appears
        waitForDisplayed(R.id.requestPanel);

        // Pickup location address is set to the same
        ViewInteraction pickupAddressView = onView(allOf(withId(R.id.pickup_address), isDisplayed()));
        pickupAddressView.check(matches(withText(pickupText)));

        // Pin disappeared
        onView(withId(R.id.pickup_location)).check(matches(not(isDisplayed())));

        // Tap [Cancel]
        onView(withId(R.id.btn_cancel_pickup)).perform(click());

        // Request panel disappears
        onView(withId(R.id.requestPanel)).check(doesNotExist());

        //------------------------------------------------------------------------------------------
        // Specify destination and go to request
        //------------------------------------------------------------------------------------------

        NavigationUtils.applyAddressPrediction(R.id.destination_address, "Airport", "Austin-Bergstrom International Airport");

        // Save destination text
        String destinationText = getText(allOf(withId(R.id.destination_address), isDisplayed()));

        onView(allOf(withId(R.id.pickup_location), isDisplayed())).perform(click());

        //------------------------------------------------------------------------------------------
        // Check requesting panel
        //------------------------------------------------------------------------------------------

        // Request panel appears
        waitForDisplayed(R.id.requestPanel);

        // Pickup location address is set to the same
        onView(allOf(withId(R.id.pickup_address), isDisplayed())).check(matches(withText(pickupText)));

        // Destination location address is set to the same
        onView(allOf(withId(R.id.destination_address), isDisplayed())).check(matches(withText(destinationText)));

        // check pin view hidden
        onView(withId(R.id.set_pickup_location)).check(matches(not(isDisplayed())));

        // wait for ETA on requested panel
        waitForDisplayed(R.id.tv_estimate_pickup_time);

        // check ETA according to ACDR_REGULAR_200_GET
        onView(allOf(withId(R.id.tv_estimate_pickup_time), isDisplayed())).check(matches(withText(getAppContext().getResources().getQuantityString(R.plurals.text_pickup_time, 2, "2"))));

        //------------------------------------------------------------------------------------------
        // Put app in the background, restore and check
        //------------------------------------------------------------------------------------------

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        // Request panel appears
        waitForDisplayed(R.id.requestPanel);

        // Pickup location address is set to the same
        onView(allOf(withId(R.id.pickup_address), isDisplayed())).check(matches(withText(pickupText)));

        // Destination location address is set to the same
        onView(allOf(withId(R.id.destination_address), isDisplayed())).check(matches(withText(destinationText)));

        // check pin view hidden
        onView(withId(R.id.set_pickup_location)).check(matches(not(isDisplayed())));

        // wait for ETA on requested panel
        waitForDisplayed(R.id.tv_estimate_pickup_time);

        // check ETA according to ACDR_REGULAR_200_GET
        onView(allOf(withId(R.id.tv_estimate_pickup_time), isDisplayed())).check(matches(withText(getAppContext().getResources().getQuantityString(R.plurals.text_pickup_time, 2, "2"))));

        //------------------------------------------------------------------------------------------
        // Request a ride, switch app
        //------------------------------------------------------------------------------------------

        removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);
        mockRequests(RequestType.RIDE_REQUEST_200_POST, // request ride
                RequestType.RIDE_REQUESTED_200_GET,
                RequestType.CURRENT_RIDE_REQUESTED_200_GET);

        // request a ride
        onView(allOf(withId(R.id.btn_request_ride), isDisplayed(), isEnabled())).perform(click());

        // wait for cancel button
        waitForDisplayed(R.id.cancel_pending_request);

        // check cancel button
        onView(withId(R.id.cancel_pending_request)).check(matches(allOf(isDisplayed(), isEnabled(), isClickable())));

        // check requesting label
        onView(allOf(withId(R.id.requesting), withText(R.string.requesting))).check(matches(isDisplayed()));

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        waitForDisplayed(R.id.requesting);

        // check cancel button
        onView(withId(R.id.cancel_pending_request)).check(matches(allOf(isDisplayed(), isEnabled(), isClickable())));

        // check requesting label
        onView(allOf(withId(R.id.requesting), withText(R.string.requesting))).check(matches(isDisplayed()));

        // check request panel not displayed
        onView(withId(R.id.requestPanel)).check(matches(not(isDisplayed())));
    }

    /**
     * Reference: RA-10474
     */
    @Test
    public void shouldRestoreAfterInactiveAccountRequested() throws InterruptedException, RemoteException, UiObjectNotFoundException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // map is displayed
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // wait for pin loaded
        waitForDisplayed(R.id.set_pickup_location);

        // go to request ride
        onView(allOf(withId(R.id.set_pickup_location), isDisplayed())).perform(click());

        // wait for request panel
        waitForDisplayed(R.id.requestPanel);

        // wait for request button
        waitForCompletelyDisplayed(R.id.btn_request_ride);

        mockRequests(RequestType.RIDE_REQUEST_400_POST);

        // request a ride
        onView(allOf(withId(R.id.btn_request_ride), isDisplayed(), isEnabled())).perform(click());

        // wait for error message
        waitFor(condition().withMatcher(withText(RiderMockResponseFactory.ACCOUNT_NOT_ACTIVE)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // send to background and restore
        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        // check requesting state is correct
        waitFor(condition()
                .withMatcher(withId(R.id.requesting))
                .withAssertion(matches(not(isDisplayed())))
                .withTimeout(1000));

        onView(allOf(withId(R.id.btn_request_ride), isEnabled())).check(matches(isDisplayed()));
        onView(withId(R.id.set_pickup_location)).check(matches(not(isDisplayed())));
    }

}
