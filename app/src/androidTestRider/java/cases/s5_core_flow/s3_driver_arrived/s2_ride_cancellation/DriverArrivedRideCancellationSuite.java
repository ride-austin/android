package cases.s5_core_flow.s3_driver_arrived.s2_ride_cancellation;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.Html;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.MapTestUtil;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.TimeUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.allOf;

/**
 * Reference: RA-11291
 * Created by Sergey Petrov on 14/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverArrivedRideCancellationSuite extends BaseUITest {

    private static final int CANCELLATION_PERIOD = 5; // seconds
    private static final float CANCELLATION_FEE = 1.23f;
    private static final double DRIVER_PAYMENT = 3.21;


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
                RequestType.RIDE_CANCEL_200_DELETE);
    }

    /**
     * C1930708: Cancel a request with fee
     * C1930709: Cancel a request w\o fee
     */
    @Test
    @TestCases({"C1930708", "C1930709"})
    public void rideCancellation() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);

        // simulate reached state
        NavigationUtils.toAssignedState(this, this::changeCancellationFee);
        NavigationUtils.toReachedState(this, this::changeCancellationFee);

        // tap on driver image should cause panel expand
        onView(withId(R.id.driver_image_small)).perform(click());
        waitForCompletelyDisplayed(R.id.ride_details);

        // cancel trip
        onView(allOf(withId(R.id.cancel_trip), withText(R.string.cancel_trip), isEnabled())).perform(click());

        String expectedText = Html.fromHtml(getString(R.string.cancel_trip_without_cancellation_fee, 1, CANCELLATION_FEE)).toString();
        waitForViewInWindow(withText(expectedText));

        onView(withId(R.id.md_buttonDefaultNegative)).perform(click());

        // check ride is active
        waitForCompletelyDisplayed(R.id.ride_details);

        waitFor("One second", 1000);

        // cancel trip again
        onView(allOf(withId(R.id.cancel_trip), withText(R.string.cancel_trip), isEnabled())).perform(click());

        expectedText = Html.fromHtml(getString(R.string.cancel_trip_with_cancellation_fee, CANCELLATION_FEE)).toString();
        waitForViewInWindow(withText(expectedText));

        // cancel with driver payments
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        NavigationUtils.toCancelledState(this, this::changeRiderCancelled);

        // check ride is cancelled
        waitForViewInWindow(withText(getString(R.string.message_ride_cancelled_by_user_with_fee, DRIVER_PAYMENT)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        assertFalse(exists(withId(R.id.ride_details)));

        // "SET PICKUP LOCATION" pin is shown
        waitForDisplayed(R.id.set_pickup_location);

        // Driver's car is visible on the screen
        MapTestUtil.assertCarMarkersVisible();

        // assert pickup marker removed
        MapTestUtil.assertPickupMarkersNotVisible();

        // Car types slider is displayed in the bottom and 'REGULAR' is selected by default
        waitForDisplayed(R.id.car_types_slider);
    }

    private Ride changeCancellationFee(Ride ride) {
        ride.getRequestedCarType().setCancellationFee(String.valueOf(CANCELLATION_FEE));
        ride.setFreeCancellationExpiresOn(TimeUtils.currentTimeMillis() + CANCELLATION_PERIOD * 1000);
        return ride;
    }

    private Ride changeRiderCancelled(Ride ride) {
        ride.setStatus(RideStatus.RIDER_CANCELLED.toString());
        ride.setDriverPayment(DRIVER_PAYMENT);
        return ride;
    }


}
