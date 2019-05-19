package cases.s5_core_flow;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseUITest;
import com.rideaustin.MockDelegate;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.CarTypeConfiguration;
import com.rideaustin.api.model.Gender;
import com.rideaustin.api.model.RiderData;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.MapTestUtil;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.SerializationHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java8.util.stream.StreamSupport;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.ViewActionUtils.setRating;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotEmptyText;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-10605
 *
 * Methods:
 * {@link NavigationUtils#throughRideRequest(MockDelegate)}
 * {@link NavigationUtils#throughRide(MockDelegate)}
 * {@link NavigationUtils#throughRideSummary(MockDelegate)}
 * are based on current suite
 *
 * Created by Sergey Petrov on 19/05/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RideSuite extends BaseUITest {

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
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET,
                RequestType.ACDR_REGULAR_200_GET, // has drivers
                RequestType.RIDE_REQUEST_200_POST); // request ride
    }

    @Test
    public void shouldRequestRideWithNoAvailableDrivers() {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // map is displayed
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // wait while pickup address is set
        waitForNotEmptyText(R.id.pickup_address);

        // wait for slider and pin loaded
        waitForDisplayed(R.id.set_pickup_location);
        waitForDisplayed(R.id.car_types_slider);

        // go to request ride
        onView(allOf(withId(R.id.set_pickup_location), isDisplayed())).perform(click());

        // wait for request panel
        waitForDisplayed(R.id.requestPanel);

        // check pin view hidden
        onView(withId(R.id.set_pickup_location)).check(matches(not(isDisplayed())));

        // wait for ETA on requested panel
        waitForDisplayed(R.id.tv_estimate_pickup_time);

        // check ETA according to ACDR_REGULAR_200_GET
        onView(allOf(withId(R.id.tv_estimate_pickup_time), isDisplayed())).check(matches(withText(getAppContext().getResources().getQuantityString(R.plurals.text_pickup_time, 2, "2"))));

        mockRequests(RequestType.RIDE_REQUESTED_200_GET);

        // request a ride
        onView(allOf(withId(R.id.btn_request_ride), isDisplayed(), isEnabled())).perform(click());

        // wait for cancel button
        waitForDisplayed(R.id.cancel_pending_request);

        // check cancel button
        onView(withId(R.id.cancel_pending_request)).check(matches(allOf(isDisplayed(), isEnabled(), isClickable())));

        // check requesting label
        onView(allOf(withId(R.id.requesting), withText(R.string.requesting))).check(matches(isDisplayed()));

        // simulate no drivers available
        removeRequests(RequestType.RIDE_REQUESTED_200_GET);
        mockRequests(RequestType.RIDE_NO_AVAILABLE_DRIVER_200_GET);

        // wait for state changed
        waitForViewInWindow(allOf(withText(R.string.message_no_driver_available), isDisplayed()));

        // close dialog
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());

        // check requesting hidden
        waitForNotDisplayed(R.id.requesting);

        // request again
        removeRequests(RequestType.RIDE_NO_AVAILABLE_DRIVER_200_GET);
        mockRequests(RequestType.RIDE_REQUESTED_200_GET);

        // wait for request button
        waitForCompletelyDisplayed(R.id.btn_request_ride);

        // check request ride button is visible and enabled
        onView(allOf(withId(R.id.btn_request_ride), isDisplayed(), isEnabled())).perform(click());

        // wait for cancel button
        waitForDisplayed(R.id.cancel_pending_request);

        // simulate driver assigned
        removeRequests(RequestType.RIDE_REQUESTED_200_GET);
        mockRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET,
                RequestType.SPLIT_FARE_EMPTY_200_GET,
                RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET);

        // wait for driver details collapsed
        waitForCompletelyDisplayed(R.id.driver_image_small);

        // open driver details
        onView(withId(R.id.driver_image_small)).perform(click());

        // wait for driver details expanded
        waitForCompletelyDisplayed(R.id.driver_image);
        waitForCompletelyDisplayed(R.id.ride_details);

        // close driver details
        onView(withId(R.id.ride_details)).perform(click());

        // wait for driver details collapsed
        waitForDisplayed(R.id.driver_image_small);

        // TODO: check route is displayed
        // TODO: check ETA marker is visible

        // simulate driver reached
        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET);
        mockRequests(RequestType.RIDE_DRIVER_REACHED_200_GET,
                RequestType.CURRENT_RIDE_DRIVER_REACHED_200_GET);

        // TODO: check no route is displayed
        // TODO: check no ETA marker is visible

        // wait for state changed
        waitForViewInWindow(allOf(withText(R.string.driver_reached_notification_msg), isDisplayed()));

        // close dialog
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());

        // simulate ride active
        removeRequests(RequestType.RIDE_DRIVER_REACHED_200_GET,
                RequestType.CURRENT_RIDE_DRIVER_REACHED_200_GET);
        mockRequests(RequestType.RIDE_ACTIVE_200_GET,
                RequestType.CURRENT_RIDE_ACTIVE_200_GET);


        // wait for driver details collapsed
        waitForCompletelyDisplayed(R.id.driver_image_small);

        // open driver details
        onView(withId(R.id.driver_image_small)).perform(click());

        // wait for driver details expanded
        waitForCompletelyDisplayed(R.id.driver_image);
        waitForCompletelyDisplayed(R.id.ride_details);

        // close driver details
        onView(withId(R.id.ride_details)).perform(click());

        // wait for driver details collapsed
        waitForDisplayed(R.id.driver_image_small);

        // TODO: check ride can't be paused

        // simulate ride complete
        removeRequests(RequestType.RIDE_ACTIVE_200_GET,
                RequestType.CURRENT_RIDE_ACTIVE_200_GET);
        mockRequests(RequestType.RIDE_COMPLETED_200_GET,
                RequestType.CURRENT_RIDE_COMPLETED_200_GET,
                RequestType.RIDE_MAP_200_GET,
                RequestType.RIDE_RATING_200_PUT);

        // wait for map loaded
        waitForViewInWindow(allOf(withId(R.id.iv_ride_map), isDisplayed(), hasDrawable()));

        // [C1930765]: check map is loaded successfully
        onView(withId(R.id.tv_ride_map)).check(matches(not(isDisplayed())));
        onView(withId(R.id.pb_ride_map)).check(matches(not(isDisplayed())));

        // wait rate driver dialog
        waitForViewInWindow(withId(R.id.rb_rate_driver));

        // set rating
        onView(withId(R.id.rb_rate_driver))
                .check(matches(isEnabled()))
                .check(matches(isCompletelyDisplayed()))
                .perform(setRating(4.0f));

        // set comment
        onView(allOf(withId(R.id.edt_leave_comment), isDisplayed())).perform(typeText("Nice ride"), closeSoftKeyboard());

        if (exists(onView(allOf(withId(R.id.radio_group_tips), isDisplayed())))) {
            // set tip
            onView(withId(R.id.tips_two)).perform(scrollTo(), click());
        }

        // submit
        onView(allOf(withId(R.id.btn_submit), withText(R.string.btn_submit), isEnabled(), isClickable())).perform(scrollTo(), click());
    }

    /**
     * Reference: RA-8592
     */
    @Test
    public void shouldReturnFromFemaleModeDuringRide() throws InterruptedException {
        removeRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);
        RiderData riderData = getResponse("RIDER_DATA_NO_RIDE_200", RiderData.class);
        riderData.getRider().getUser().setGender(Gender.FEMALE.toString());
        mockRequest(RequestType.RIDER_DATA_NO_RIDE_200_GET, riderData);

        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // go through ride request
        NavigationUtils.throughRideRequest(this);

        //------------------------------------------------------------------------------------------
        // simulate driver assigned
        //------------------------------------------------------------------------------------------

        // clear ride state
        removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET,
                RequestType.RIDE_REQUESTED_200_GET,
                RequestType.CURRENT_RIDE_REQUESTED_200_GET,
                RequestType.RIDE_NO_AVAILABLE_DRIVER_200_GET,
                RequestType.CURRENT_RIDE_NO_AVAILABLE_DRIVER_200_GET,
                RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.RIDE_DRIVER_REACHED_200_GET,
                RequestType.CURRENT_RIDE_DRIVER_REACHED_200_GET,
                RequestType.RIDE_ACTIVE_200_GET,
                RequestType.CURRENT_RIDE_ACTIVE_200_GET,
                RequestType.RIDE_COMPLETED_200_GET,
                RequestType.CURRENT_RIDE_COMPLETED_200_GET,
                RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET);

        // simulate driver assigned
        mockRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET);

        // wait for driver details collapsed
        waitForCompletelyDisplayed(R.id.driver_image_small);

        //------------------------------------------------------------------------------------------
        // go to female only fragment
        //------------------------------------------------------------------------------------------

        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navFemaleOnly));

        waitForDisplayed(R.id.female_only_container);
        waitForDisplayed(R.id.switch_enable_pink_drivers);

        //------------------------------------------------------------------------------------------
        // simulate driver reached
        //------------------------------------------------------------------------------------------

        // simulate driver reached
        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET);
        mockRequests(RequestType.RIDE_DRIVER_REACHED_200_GET,
                RequestType.CURRENT_RIDE_DRIVER_REACHED_200_GET);

        // wait for state changed
        waitForViewInWindow(allOf(withText(R.string.driver_reached_notification_msg), isDisplayed()));

        // close dialog
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());

        //------------------------------------------------------------------------------------------
        // go back to map
        //------------------------------------------------------------------------------------------

        waitForDisplayed(navigationIcon(), "Wait for toolbar");

        onView(navigationIcon()).perform(click());

        //------------------------------------------------------------------------------------------
        // check there is driver reached state and no UI elements from female only screen
        //------------------------------------------------------------------------------------------

        // map is displayed
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // driver panel is displayed
        waitForCompletelyDisplayed(R.id.driver_image_small);

        // car marker is visible
        MapTestUtil.assertCarMarkersCount(1);
        MapTestUtil.assertCarMarkersVisible();

        // check there is no UI elements from female only screen
        assertFalse(exists(onView(withId(R.id.female_only_container))));
        assertFalse(exists(onView(withId(R.id.switch_enable_pink_drivers))));
    }

    /**
     * Reference: RA-12745
     */
    @Test
    public void shouldShowZeroFareFromConfig() throws InterruptedException {
        String zeroFare = "ZERO_FARE";
        CarTypeConfiguration configuration = new CarTypeConfiguration();
        configuration.setDisableTipping(false);
        configuration.setZeroFareLabel(zeroFare);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // go through ride
        NavigationUtils.throughRideRequest(this);
        NavigationUtils.throughRide(this);
        NavigationUtils.throughRideSummary(this, response -> {
            response.getRequestedCarType().setConfiguration(SerializationHelper.serialize(configuration));
            return response;
        }, () -> {
            onView(withId(R.id.tv_price))
                    .check(matches(withText(zeroFare)))
                    .check(matches(isDisplayed()));
        });
    }

    /**
     * Reference: RA-12745
     */
    @Test
    public void shouldShowFareFromRide() throws InterruptedException {
        GlobalConfig config = getResponse("CONFIG_GLOBAL_200", GlobalConfig.class);
        RequestedCarType carType = StreamSupport.stream(config.getCarTypes())
                .filter(requestedCarType -> requestedCarType.getTitle().equals("STANDARD"))
                .findAny().get();
        CarTypeConfiguration configuration = new CarTypeConfiguration();
        configuration.setDisableTipping(false);
        // no zero fare in configuration
        carType.setConfiguration(SerializationHelper.serialize(configuration));

        removeRequests(RequestType.CONFIG_RIDER_200_GET);
        mockRequest(RequestType.CONFIG_RIDER_200_GET, config);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        Double fare = 1234.56;
        // go through ride
        NavigationUtils.throughRideRequest(this);
        NavigationUtils.throughRide(this);
        NavigationUtils.throughRideSummary(this,
                response -> {
                    response.setTotalFare(fare);
                    return response;
                },
                () -> {
                    onView(withId(R.id.tv_price))
                            .check(matches(withText(getString(R.string.money, fare.toString()))))
                            .check(matches(isDisplayed()));
                });

    }
}
