package cases_e2e.s5_ride_flow;

import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.App;
import com.rideaustin.BaseE2ETest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.helpers.ServerTestHelper;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.MapTestUtil;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.CarSliderUtils.carTypeSelected;
import static com.rideaustin.utils.CarSliderUtils.carTypeTitle;
import static com.rideaustin.utils.CarSliderUtils.selectCarType;
import static com.rideaustin.utils.DeviceTestUtils.search;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersCount;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertDestinationMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertPickupMarkersVisible;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.isNotDecorView;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.getText;
import static com.rideaustin.utils.ViewActionUtils.setRating;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotEmptyText;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-12420
 * Created by Sergey Petrov on 09/08/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RideSuite extends BaseE2ETest {

    private static final String RIDER_USERNAME = "austinrider@xo.com";
    private static final String RIDER_PASSWORD = "test123";

    private static final String DRIVER_ONE_USERNAME = "android.driver.one@automation.com";
    private static final String DRIVER_ONE_NAME = "Driver One";
    private static final String DRIVER_ONE_CAR = "Black Cadillac Catera";
    private static final String DRIVER_ONE_PLATE = "BLACKCATE";
    private static final String DRIVER_PASSWORD = "test123";

    private String driverToken1 = ServerTestHelper.getToken(DRIVER_ONE_USERNAME, DRIVER_PASSWORD);

    @Rule
    public ActivityTestRule<NavigationDrawerActivity> activityRule = new RaActivityRule<>(NavigationDrawerActivity.class, false, false);

    @Override
    public void setUp() {
        super.setUp();
        ServerTestHelper.tryCancelDriverRideByAdmin(driverToken1);
        ServerTestHelper.tryGoOffline(driverToken1);
    }

    @Override
    public void tearDown() {
        super.tearDown();
        ServerTestHelper.tryCancelDriverRideByAdmin(driverToken1);
        ServerTestHelper.tryGoOffline(driverToken1);
    }

    @Test
    public void shouldGoThroughRideFlow() throws UiObjectNotFoundException, InterruptedException {
        NavigationUtils.startActivity(activityRule);

        NavigationUtils.throughLogin(RIDER_USERNAME, RIDER_PASSWORD);

        //------------------------------------------------------------------------------------------
        // Open map, check no available drivers
        //------------------------------------------------------------------------------------------

        // Map screen is opened
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // wait while pickup address is set
        waitForNotEmptyText(R.id.pickup_address);

        // should be no available drivers
        waitForDisplayed(R.id.set_pickup_location);
        waitForDisplayed(R.id.car_types_slider);

        ViewInteraction slider = onView(allOf(withId(R.id.car_types_slider), isDisplayed()));
        // check slider has STANDARD selected by default
        slider.check(matches(carTypeSelected(0)));
        slider.check(matches(carTypeTitle("STANDARD")));
        checkNoDriverOnMap();

        // check no car markers
        MapTestUtil.zoomOut(200);
        checkNoDriverOnMap();

        // return to normal zoom
        onView(allOf(withId(R.id.myLocationButton), isDisplayed())).perform(click());

        //------------------------------------------------------------------------------------------
        // Set driver online, check slider
        //------------------------------------------------------------------------------------------

        final String eta = "1"; // setting driver to the same location

        // set driver online at REGULAR
        ServerTestHelper.goOnline(driverToken1, DEFAULT_LAT, DEFAULT_LNG, "REGULAR");
        checkDriverOnMap(1, eta);

        // select SUV category (no drivers)
        slider.perform(selectCarType(1));
        checkNoDriverOnMap();

        // select STANDARD category
        slider.perform(selectCarType(0));
        checkDriverOnMap(1, eta);

        // set driver offline
        ServerTestHelper.goOffline(driverToken1);
        checkNoDriverOnMap();

        // set driver online at REGULAR
        ServerTestHelper.goOnline(driverToken1, DEFAULT_LAT, DEFAULT_LNG, "REGULAR");
        checkDriverOnMap(1, eta);

        //------------------------------------------------------------------------------------------
        // Go to request ride pane, check approx.time, pickup & driver markers
        //------------------------------------------------------------------------------------------

        // tap on pin view
        onView(allOf(withId(R.id.pickup_location), isDisplayed())).perform(click());

        // wait for request panel
        waitForDisplayed(R.id.requestPanel);

        // check pin view hidden
        onView(withId(R.id.set_pickup_location)).check(matches(not(isDisplayed())));

        // check ETA
        waitForDisplayed(R.id.tv_estimate_pickup_time);
        onView(allOf(withId(R.id.tv_estimate_pickup_time), isDisplayed()))
                .check(matches(withText(getAppContext().getResources().getQuantityString(R.plurals.text_pickup_time, Integer.parseInt(eta), eta))));

        // check driver marker
        assertCarMarkersCount(1);

        // check pickup
        onView(withId(R.id.pickup_address))
                .check(matches(isDisplayed()))
                .check(matches(not(withText(""))));
        assertPickupMarkersVisible();

        //------------------------------------------------------------------------------------------
        // Request a ride
        //------------------------------------------------------------------------------------------

        // request a ride
        onView(allOf(withId(R.id.btn_request_ride), isDisplayed(), isEnabled())).perform(click());

        // wait for cancel button
        waitForDisplayed(R.id.cancel_pending_request);

        // check cancel button
        onView(withId(R.id.cancel_pending_request))
                .check(matches(allOf(isDisplayed(), isEnabled(), isClickable())));

        // check requesting label
        onView(allOf(withId(R.id.requesting), withText(R.string.requesting)))
                .check(matches(isDisplayed()));

        assertTrue(App.getPrefs().hasRideId());
        ServerTestHelper.acceptRide(driverToken1, App.getPrefs().getRideId());

        //------------------------------------------------------------------------------------------
        // Driver accepts, check driver & car info
        //------------------------------------------------------------------------------------------

        // check driver info
        waitForCompletelyDisplayed(R.id.driver_image_small);
        onView(allOf(withId(R.id.driver_name_small), isDisplayed()))
                .check(matches(withText(DRIVER_ONE_NAME)));
        onView(allOf(withId(R.id.car_category_small), isDisplayed()))
                .check(matches(withText("STANDARD")));
        onView(allOf(withId(R.id.car_make_small), isDisplayed()))
                .check(matches(withText(DRIVER_ONE_CAR)));
        onView(allOf(withId(R.id.car_plate_small), isDisplayed()))
                .check(matches(withText(DRIVER_ONE_PLATE)));
        onView(allOf(withId(R.id.driver_image_small), isDisplayed()))
                .check(matches(hasDrawable()));

        // check cancel enabled
        onView(withId(R.id.driver_image_small)).perform(click());
        waitForCompletelyDisplayed(R.id.ride_details);
        onView(allOf(withId(R.id.cancel_trip), withText(R.string.cancel_trip)))
                .check(matches(isEnabled()));

        //------------------------------------------------------------------------------------------
        // Driver arrives, check notification
        //------------------------------------------------------------------------------------------

        ServerTestHelper.arrive(driverToken1, App.getPrefs().getRideId());

        // check the dialog
        waitForViewInWindow(allOf(withText(R.string.driver_reached_notification_msg), isDisplayed()));
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());

        //------------------------------------------------------------------------------------------
        // Rider changes destination
        //------------------------------------------------------------------------------------------

        onView(allOf(withId(R.id.destination_address), withHint(R.string.enter_destination_address), isEnabled())).check(matches(isDisplayed()));
        onView(withId(R.id.destination_address)).perform(typeText(""));

        // start typing "Airport"
        onView(withId(R.id.destination_address)).perform(typeText("Airport"), closeSoftKeyboard());

        // check custom query hint
        ViewInteraction prediction = onView(withText("Austin-Bergstrom International Airport")).inRoot(isNotDecorView(activityRule));
        waitFor(condition("Airport prediction should appear").withView(prediction));

        // apply prediction
        final String destination = getText(prediction);
        prediction.perform(click());

        // check confirmation
        waitFor(condition("Update ride confirmation")
                .withMatcher(withText(R.string.update_ride_dialog_content)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // check address applied
        waitFor(condition("Destination with text: " + destination)
                .withMatcher(allOf(withId(R.id.destination_address), withText(destination))));

        // check markers visible
        assertDestinationMarkersVisible(5000);
        assertPickupMarkersVisible();
        assertCarMarkersCount(1);

        //------------------------------------------------------------------------------------------
        // Driver starts trip
        //------------------------------------------------------------------------------------------

        ServerTestHelper.startRide(driverToken1, App.getPrefs().getRideId());

        // check address applied
        waitFor(condition("Destination with text: " + destination)
                .withMatcher(allOf(withId(R.id.destination_address), withText(destination))));

        // check cancel disabled
        onView(withId(R.id.driver_image_small)).perform(click());
        waitForCompletelyDisplayed(R.id.ride_details);
        waitFor(condition("Cancel trip should been disabled")
                .withMatcher(allOf(withId(R.id.cancel_trip), withText(R.string.cancel_trip)))
                .withCheck(not(isEnabled()))
                .withTimeout(5000));

        // collapse panel
        onView(withId(R.id.ride_details)).perform(click());

        // check markers visible
        assertDestinationMarkersVisible(5000);
        assertPickupMarkersVisible();
        assertCarMarkersCount(1);

        //------------------------------------------------------------------------------------------
        // Driver ends trip, check ride summary UI
        //------------------------------------------------------------------------------------------

        ServerTestHelper.endRide(driverToken1, App.getPrefs().getRideId(), DEFAULT_LAT, DEFAULT_LNG);

        // wait rate driver dialog
        waitForViewInWindow(withId(R.id.rb_rate_driver));

        // wait for map loaded
        waitForViewInWindow(allOf(withId(R.id.iv_ride_map), isDisplayed(), hasDrawable()));

        // [C1930765]: check map is loaded successfully
        onView(withId(R.id.tv_ride_map)).check(matches(not(isDisplayed())));
        onView(withId(R.id.pb_ride_map)).check(matches(not(isDisplayed())));

        // [C1930766]: driver name is correctly displayed, according to RIDE_COMPLETED_200_GET
        onView(withId(R.id.tv_comment_title)).check(matches(withText(getString(R.string.rating_feedback_message_text, DRIVER_ONE_NAME))));

        // set rating
        onView(withId(R.id.rb_rate_driver))
                .check(matches(isEnabled()))
                .check(matches(isCompletelyDisplayed()))
                .perform(setRating(4.0f));

        // set comment
        onView(allOf(withId(R.id.edt_leave_comment), isDisplayed())).perform(typeText("Nice ride"), closeSoftKeyboard());

        if (exists(onView(allOf(withId(R.id.radio_group_tips), isDisplayed())))) {
            // set tip
            onView(withId(R.id.tips_one)).perform(scrollTo(), click());
        }

        // submit
        onView(allOf(withId(R.id.btn_submit), withText(R.string.btn_submit), isEnabled(), isClickable())).perform(scrollTo(), click());

        //------------------------------------------------------------------------------------------
        // Check app is on initial state
        //------------------------------------------------------------------------------------------

        // wait while pickup address is set
        waitForNotEmptyText(R.id.pickup_address);

        // should be no available drivers
        waitForDisplayed(R.id.set_pickup_location);
        waitForDisplayed(R.id.car_types_slider);

        // same driver on map
        checkDriverOnMap(1, eta);
    }


    private void checkDriverOnMap(int count, String eta) {
        assertCarMarkersVisible(5000);
        assertCarMarkersCount(count);
        onView(allOf(withId(R.id.set_pickup_location), isDisplayed()))
                .check(matches(withText(R.string.set_pickup_location)));
        onView(withId(R.id.time_to_pickup)).check(matches(allOf(withText(eta), isDisplayed())));
    }

    private void checkNoDriverOnMap() {
        search("Car Markers should disappear")
                .descContains(getString(R.string.car_marker))
                .assertNotExist(5000);
        onView(allOf(withId(R.id.set_pickup_location), isDisplayed()))
                .check(matches(withText(R.string.no_available_cars)));
        onView(withId(R.id.time_to_pickup)).check(matches(not(isDisplayed())));
    }
}
