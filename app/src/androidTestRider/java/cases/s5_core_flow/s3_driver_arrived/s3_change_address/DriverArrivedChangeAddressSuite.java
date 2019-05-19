package cases.s5_core_flow.s3_driver_arrived.s3_change_address;

import android.support.test.espresso.Espresso;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.Ride;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.MapTestUtil;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.location.LocationHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.supportsInputMethods;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.ViewActionUtils.applyPrediction;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCondition;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-11440
 * Created by Sergey Petrov on 20/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverArrivedChangeAddressSuite extends BaseUITest {

    private static final String NEAR_PLACE_ID = "newarPlaceId";
    private static final String NEAR_PLACE_TEXT = "11624 Jollyville Rd";
    private static final double NEAR_PLACE_LAT = 30.417174;
    private static final double NEAR_PLACE_LNG = -97.750211;

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

    @Override
    public void tearDown() {
        super.tearDown();
        LocationHelper.resetProviders();
    }

    /**
     * C1930713: Not possible to change pickup location
     * C1930714: Entering destination location
     * C1930715: Update destination location
     */
    @Test
    @TestCases({"C1930713", "C1930714", "C1930715"})
    public void changeAddress() throws InterruptedException {

        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);

        // simulate reached state
        NavigationUtils.toAssignedState(this);
        NavigationUtils.toReachedState(this);

        //------------------------------------------------------------------------------------------
        // C1930713: Not possible to change pickup location
        //------------------------------------------------------------------------------------------

        waitForDisplayed(R.id.pickup_address);
        onView(withId(R.id.pickup_address)).check(matches(withText(not(""))));
        onView(withId(R.id.pickup_address)).check(matches(not(isEnabled())));
        onView(withId(R.id.pickup_address)).check(matches(not(supportsInputMethods())));

        //------------------------------------------------------------------------------------------
        // C1930714: Entering destination location
        //------------------------------------------------------------------------------------------

        onView(allOf(withId(R.id.destination_address), withHint(R.string.enter_destination_address), isEnabled())).check(matches(isDisplayed()));
        String address = "Austin-Bergstrom International Airport";
        NavigationUtils.applyAddressPrediction(R.id.destination_address,
                "Airport",
                address,
                () -> {
                    // confirmation should appear
                    waitForViewInWindow(withText(R.string.update_ride_dialog_content));
                    // update destination
                    atomicOnRequests(() -> {
                        removeRequests(RequestType.CURRENT_RIDE_DRIVER_REACHED_200_GET,
                                RequestType.RIDE_DRIVER_REACHED_200_GET);
                        Ride ride = getResponse("RIDE_DRIVER_REACHED_200", Ride.class);
                        ride.setEndAddress(address);
                        mockRequest(RequestType.CURRENT_RIDE_DRIVER_REACHED_200_GET, ride);
                        mockRequest(RequestType.RIDE_DRIVER_REACHED_200_GET, ride);
                        mockRequests(RequestType.RIDE_UPDATE_200_PUT);
                    });
                    onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
                    waitForDisplayed(R.id.destination_address);
                    onView(allOf(withId(R.id.destination_address), withText(containsString(address))))
                            .check(matches(isDisplayed()));
                });

        //------------------------------------------------------------------------------------------
        // C1930715: Update destination location
        //------------------------------------------------------------------------------------------

        applyPrediction(NEAR_PLACE_ID, NEAR_PLACE_TEXT, NEAR_PLACE_LAT, NEAR_PLACE_LNG);
        NavigationUtils.applyAddressPrediction(R.id.destination_address,
                "whatever",
                NEAR_PLACE_TEXT,
                () -> {
                    // confirmation should appear
                    waitForViewInWindow(withText(R.string.update_ride_dialog_content));
                    // update destination
                    atomicOnRequests(() -> {
                        removeRequests(RequestType.CURRENT_RIDE_DRIVER_REACHED_200_GET,
                                RequestType.RIDE_DRIVER_REACHED_200_GET);
                        Ride ride = getResponse("RIDE_DRIVER_REACHED_200", Ride.class);
                        ride.setEndAddress(NEAR_PLACE_TEXT);
                        mockRequest(RequestType.CURRENT_RIDE_DRIVER_REACHED_200_GET, ride);
                        mockRequest(RequestType.RIDE_DRIVER_REACHED_200_GET, ride);
                        mockRequests(RequestType.RIDE_UPDATE_200_PUT);
                    });
                    onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
                    waitForDisplayed(R.id.destination_address);
                    onView(allOf(withId(R.id.destination_address), withText(containsString(NEAR_PLACE_TEXT))))
                            .check(matches(isDisplayed()));
                });
    }

    /**
     * RA-11512: App should not update location locally without internet
     */
    @Test
    public void changeAddressWithoutInternet() throws InterruptedException {
        mockRequests(RequestType.RIDE_UPDATE_200_PUT);

        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);

        // simulate reached state
        NavigationUtils.toAssignedState(this);
        NavigationUtils.toReachedState(this);

        //------------------------------------------------------------------------------------------
        // Check destination empty
        //------------------------------------------------------------------------------------------

        onView(allOf(withId(R.id.destination_address), withHint(R.string.enter_destination_address), isEnabled())).check(matches(isDisplayed()));
        MapTestUtil.assertDestinationMarkersNotVisible();

        //------------------------------------------------------------------------------------------
        // Change destination and cancel
        //------------------------------------------------------------------------------------------

        applyPrediction(NEAR_PLACE_ID, NEAR_PLACE_TEXT, NEAR_PLACE_LAT, NEAR_PLACE_LNG);

        NavigationUtils.applyAddressPrediction(R.id.destination_address,
                "whatever",
                NEAR_PLACE_TEXT,
                () -> {
                    // confirmation should appear
                    waitForViewInWindow(withText(R.string.update_ride_dialog_content));
                    // cancel
                    onView(withId(R.id.md_buttonDefaultNegative)).perform(click());

                    Espresso.pressBack(); // back to recents
                    Espresso.pressBack(); // back to map

                    waitForDisplayed(R.id.destination_address);

                    // check destination is empty
                    onView(allOf(withId(R.id.destination_address), withHint(R.string.enter_destination_address), isEnabled())).check(matches(isDisplayed()));
                    MapTestUtil.assertDestinationMarkersNotVisible();
                });

        //------------------------------------------------------------------------------------------
        // Change destination, lose internet and cancel
        //------------------------------------------------------------------------------------------

        // select destination
        NavigationUtils.applyAddressPrediction(R.id.destination_address,
                "whatever",
                NEAR_PLACE_TEXT,
                () -> {
                    // confirmation should appear
                    waitForViewInWindow(withText(R.string.update_ride_dialog_content));

                    // lose internet
                    setNetworkError(true);

                    // try to apply destination
                    onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

                    // wait for error dialog
                    waitForViewInWindow(withText(R.string.update_ride_failed_dialog_content));

                    // cancel
                    onView(withId(R.id.md_buttonDefaultNegative)).perform(click());

                    Espresso.pressBack(); // back to recents
                    Espresso.pressBack(); // back to map

                    waitForDisplayed(R.id.destination_address);

                    // check destination is empty
                    onView(allOf(withId(R.id.destination_address), withHint(R.string.enter_destination_address), isEnabled())).check(matches(isDisplayed()));
                    MapTestUtil.assertDestinationMarkersNotVisible();

                    setNetworkError(false);
                });

        //------------------------------------------------------------------------------------------
        // Change destination, lose internet and retry
        //------------------------------------------------------------------------------------------

        // wait for network toast disappear
        waitForCondition("Should be no toast", () -> !exists(onView(withText(R.string.network_error)).inRoot(isToast())));

        // select destination
        NavigationUtils.applyAddressPrediction(R.id.destination_address,
                "whatever",
                NEAR_PLACE_TEXT,
                () -> {
                    // confirmation should appear
                    waitForViewInWindow(withText(R.string.update_ride_dialog_content));

                    // lose internet
                    setNetworkError(true);

                    // try to apply destination
                    onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

                    // wait for error dialog
                    waitForViewInWindow(withText(R.string.update_ride_failed_dialog_content));

                    // retry
                    onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

                    // wait for error dialog again
                    waitForViewInWindow(withText(R.string.update_ride_failed_dialog_content));
                });

        //------------------------------------------------------------------------------------------
        // Change destination after network recovered
        //------------------------------------------------------------------------------------------

        setNetworkError(false);

        // wait for network toast disappear
        waitForCondition("Should be no toast", () -> !exists(onView(withText(R.string.network_error)).inRoot(isToast())));

        // update destination
        atomicOnRequests(() -> {
            removeRequests(RequestType.CURRENT_RIDE_DRIVER_REACHED_200_GET,
                    RequestType.RIDE_DRIVER_REACHED_200_GET);
            Ride ride = getResponse("RIDE_DRIVER_REACHED_200", Ride.class);
            ride.setEndAddress(NEAR_PLACE_TEXT);
            mockRequest(RequestType.CURRENT_RIDE_DRIVER_REACHED_200_GET, ride);
            mockRequest(RequestType.RIDE_DRIVER_REACHED_200_GET, ride);
        });

        // retry
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // wait for destination changed
        waitFor(R.id.destination_address, allOf(withText(NEAR_PLACE_TEXT), isDisplayed()));
        MapTestUtil.assertDestinationMarkersVisible();

        verifyRequest(RequestType.RIDE_UPDATE_200_PUT, hasQueryParam("endLocationLat", is(String.valueOf(NEAR_PLACE_LAT))));
        verifyRequest(RequestType.RIDE_UPDATE_200_PUT, hasQueryParam("endLocationLong", is(String.valueOf(NEAR_PLACE_LNG))));
    }
}
