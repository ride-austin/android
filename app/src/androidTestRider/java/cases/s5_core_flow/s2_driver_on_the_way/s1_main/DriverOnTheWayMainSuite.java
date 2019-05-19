package cases.s5_core_flow.s2_driver_on_the_way.s1_main;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.RideCancellationConfig;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.utils.UIUtils;
import com.rideaustin.utils.MapTestUtil;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.TimeUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.MatcherUtils.withEmptyText;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-10225
 * Created by Sergey Petrov on 02/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverOnTheWayMainSuite extends BaseUITest {

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
                RequestType.ACDR_REGULAR_200_GET); // has drivers
    }

    /**
     * C1930656: Route, Driver position and course, ETA pin
     * C1930657: Driver bar - name, car name & license plate, ratings, car type
     * C1930660: Driver photo is correct and can be expanded
     * C1930659: Car photo is correct and can be expanded
     */
    @Test
    @TestCases({"C1930656", "C1930657", "C1930660", "C1930659"})
    public void showDriverInAssignedState() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);

        // simulate assigned state
        NavigationUtils.toAssignedState(this);

        //------------------------------------------------------------------------------------------
        //  C1930656: Route, Driver position and course, ETA pin
        //------------------------------------------------------------------------------------------

        // wait for map animation
        waitFor("Map animation", 1000);

        // check car is visible
        MapTestUtil.assertCarMarkersVisible();
        MapTestUtil.assertCarMarkersCount(1);

        // check eta is visible and correct (according to RIDE_DRIVER_ASSIGNED_200)
        MapTestUtil.assertEtaMarkerVisible(getString(R.string.zero_mins));

        // mock empty ETA
        Ride ride = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
        ride.getActiveDriver().setDrivingTimeToRider(null);
        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, ride);
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, ride);
        MapTestUtil.waitEtaMarker(getString(R.string.no_mins));

        // mock large ETA
        ride = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
        ride.getActiveDriver().setDrivingTimeToRider(25 * 60L); // 25 mins
        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, ride);
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, ride);
        MapTestUtil.waitEtaMarker("25 mins");

        //------------------------------------------------------------------------------------------
        //  C1930657: Driver bar - name, car name & license plate, ratings, car type
        //------------------------------------------------------------------------------------------

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

        //------------------------------------------------------------------------------------------
        //  C1930660: Driver photo is correct and can be expanded
        //------------------------------------------------------------------------------------------

        waitForDisplayed(R.id.driver_image);

        // tap on driver photo
        onView(withId(R.id.driver_image)).perform(click());

        // check the dialog
        waitForViewInWindow(allOf(withId(R.id.dialogImageView), isDisplayed(), hasDrawable()));
        onView(allOf(withId(R.id.btnClose), isDisplayed(), isClickable())).perform(click());
        waitFor("Animation", 1000);

        //------------------------------------------------------------------------------------------
        //  C1930659: Car photo is correct and can be expanded
        //------------------------------------------------------------------------------------------

        waitForDisplayed(R.id.car_image);

        // tap on car photo
        onView(withId(R.id.car_image)).perform(click());

        // check the dialog
        waitForViewInWindow(allOf(withId(R.id.dialogImageView), isDisplayed(), hasDrawable()));
        onView(allOf(withId(R.id.btnClose), isDisplayed(), isClickable())).perform(click());
    }

    /**
     * Route, driver position and ETA after previous ride was cancelled
     */
    @Test
    @TestCases("C1930661")
    public void driverFromPreviousRideInCancelled() throws UiObjectNotFoundException, InterruptedException {

        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);

        // simulate assigned state
        NavigationUtils.toAssignedState(this);

        // check eta is visible and correct (according to RIDE_DRIVER_ASSIGNED_200)
        MapTestUtil.assertEtaMarkerVisible(getString(R.string.zero_mins));

        MapTestUtil.assertRouteVisible();

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

        //------------------------------------------------------------------------------------------
        // Driver cancelled, ride move to requested state
        //------------------------------------------------------------------------------------------

        // simulate requested state
        NavigationUtils.toRequestedState(this);

        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject map = device.findObject(new UiSelector().descriptionContains(getString(R.string.google_map)));

        // zoom out to check markers
        MapTestUtil.zoomOut(160);

        // check there are two cars (according to ACDR_REGULAR_200_GET)
        MapTestUtil.assertCarMarkersCount(2);

        // check there is no eta
        MapTestUtil.assertEtaMarkerNotVisible();

        // check pickup is visible
        MapTestUtil.assertPickupMarkersVisible();

        MapTestUtil.assertRouteNotVisible();

        //------------------------------------------------------------------------------------------
        // Next driver assigned
        //------------------------------------------------------------------------------------------

        String name1 = "Ride16";
        String rating1 = UIUtils.formatRating(4.0);
        String category1 = "PREMIUM";
        String car1 = "White Tesla 3";
        String plate1 = "TESLA";

        // simulate assigned state with other driver
        atomicOnRequests(() -> {
            NavigationUtils.clearRideState(this);
            mockRequests(RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                    RequestType.SPLIT_FARE_EMPTY_200_GET);



            Ride ride = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
            ride.getActiveDriver().setDrivingTimeToRider(25 * 60L); // 25 mins
            ride.getActiveDriver().getDriver().setFirstname(name1);
            ride.getActiveDriver().getDriver().setRating(4.0);
            ride.getRequestedCarType().setTitle(category1);
            ride.getActiveDriver().getSelectedCar().setColor("White");
            ride.getActiveDriver().getSelectedCar().setMake("Tesla");
            ride.getActiveDriver().getSelectedCar().setModel("3");
            ride.getActiveDriver().getSelectedCar().setLicense(plate1);

            // mock ride
            mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, ride);
            mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, ride);

        });

        // wait for driver details collapsed
        waitForCompletelyDisplayed(R.id.driver_image_small);

        // check collapsed state
        onView(allOf(withId(R.id.driver_name_small), isDisplayed())).check(matches(withText(name1)));
        onView(allOf(withId(R.id.driver_rate_small), isDisplayed())).check(matches(withText(rating1)));
        onView(allOf(withId(R.id.car_category_small), isDisplayed())).check(matches(withText(category1)));
        onView(allOf(withId(R.id.car_make_small), isDisplayed())).check(matches(withText(car1)));
        onView(allOf(withId(R.id.car_plate_small), isDisplayed())).check(matches(withText(plate1)));

        MapTestUtil.waitEtaMarker("25 mins");

        MapTestUtil.assertRouteVisible();
    }

    @Test
    public void checkRideCancellationFeedback() throws InterruptedException {
        long threshold = 10; // 10 sec

        removeRequests(RequestType.CONFIG_RIDER_200_GET);
        GlobalConfig config = getResponse("CONFIG_GLOBAL_200", GlobalConfig.class);
        config.setRideCancellationConfig(new RideCancellationConfig());
        config.getRideCancellationConfig().setEnabled(true);
        config.getRideCancellationConfig().setCancellationThreshold(threshold);
        mockRequest(RequestType.CONFIG_RIDER_200_GET, config);
        mockRequests(RequestType.RIDE_CANCELLATION_REASONS_200_GET,
                RequestType.RIDE_CANCELLATION_FEEDBACK_200_POST,
                RequestType.RIDE_CANCEL_200_DELETE);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "whatever");
        NavigationUtils.throughRideRequest(this);
        NavigationUtils.toAssignedState(this);

        // cancel ride
        onView(withId(R.id.driver_image_small)).perform(click());
        waitForCompletelyDisplayed(R.id.ride_details);
        onView(allOf(withId(R.id.cancel_trip), withText(R.string.cancel_trip), isEnabled())).perform(click());
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        NavigationUtils.toCancelledState(this, response -> {
            response.setStatus(RideStatus.RIDER_CANCELLED.name());
            response.setDriverAcceptedOn(TimeUtils.currentTimeMillis() - (threshold + 1) * 1000);
            return response;
        });

        // show ride cancellation feedback
        Matchers.waitFor(condition("Ride cancellation reasons")
                .withMatcher(withText(R.string.cancel_feedback_rider_title)));

        onView(withText(R.string.cancel_feedback_rider_description))
                .check(matches(isDisplayed()));

        Matchers.waitFor(condition("Ride cancellation reason 1")
                .withMatcher(withText("First reason")));

        Matchers.waitFor(condition("Ride cancellation reason 2")
                .withMatcher(withText("Second reason")));

        Matchers.waitFor(condition("Ride cancellation reason 3")
                .withMatcher(withText("Third reason")));

        Matchers.waitFor(condition("Ride cancellation reason 4")
                .withMatcher(withText("Other")));

        onView(withHint(R.string.cancel_feedback_message_hint)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btn_submit))
                .check(matches(withText(R.string.cancel_feedback_rider_ok)))
                .check(matches(not(isEnabled())))
                .check(matches(isDisplayed()));
        onView(withId(R.id.btn_cancel))
                .check(matches(withText(R.string.cancel_feedback_rider_cancel)))
                .check(matches(isEnabled()))
                .check(matches(isDisplayed()));

        onView(withText("Other"))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withHint(R.string.cancel_feedback_message_hint))
                .check(matches(withEmptyText()))
                .check(matches(isDisplayed()))
                .perform(typeText("Other reason"), closeSoftKeyboard());


        // submit feedback
        onView(withId(R.id.btn_submit))
                .check(matches(isEnabled()))
                .check(matches(isDisplayed()))
                .perform(click());

        verifyRequest(RequestType.RIDE_CANCELLATION_FEEDBACK_200_POST, hasQueryParam("reason", is("OTHER")));
        verifyRequest(RequestType.RIDE_CANCELLATION_FEEDBACK_200_POST, hasQueryParam("comment", is("Other reason")));

        // no regular popup must be shown
        onView(withText(R.string.message_ride_cancelled_by_user)).check(doesNotExist());

        // process another ride request
        NavigationUtils.throughRideRequest(this);
        NavigationUtils.toAssignedState(this);

        // cancel ride
        onView(withId(R.id.driver_image_small)).perform(click());
        waitForCompletelyDisplayed(R.id.ride_details);
        onView(allOf(withId(R.id.cancel_trip), withText(R.string.cancel_trip), isEnabled())).perform(click());
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        NavigationUtils.toCancelledState(this, response -> {
            response.setStatus(RideStatus.RIDER_CANCELLED.name());
            response.setDriverAcceptedOn(TimeUtils.currentTimeMillis());
            return response;
        });

        // threshold requirement not met - no feedback offered, just regular popup
        Matchers.waitFor(condition().withView(onView(withText(R.string.message_ride_cancelled_by_user))));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

    }
}
