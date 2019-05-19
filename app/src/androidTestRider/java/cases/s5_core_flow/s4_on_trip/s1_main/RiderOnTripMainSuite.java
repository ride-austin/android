package cases.s5_core_flow.s4_on_trip.s1_main;

import android.os.RemoteException;
import android.support.test.espresso.Espresso;
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
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.utils.UIUtils;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.MapTestUtil;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.applyPrediction;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Created by hatak on 30.08.2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RiderOnTripMainSuite extends BaseUITest {

    private static final String NEAR_PLACE_ID_1 = "PlaceId1";
    private static final String NEAR_PLACE_TEXT_1 = "11624 Jollyville Rd";
    private static final double NEAR_PLACE_LAT_1 = 30.427174;
    private static final double NEAR_PLACE_LNG_1 = -97.750211;

    private static final String NEAR_PLACE_ID_2 = "PlaceId2";
    private static final String NEAR_PLACE_TEXT_2 = "Different place";
    private static final double NEAR_PLACE_LAT_2 = 30.447174;
    private static final double NEAR_PLACE_LNG_2 = -97.750211;

    private static final String name = "Ride15";
    private static final String rating = UIUtils.formatRating(5.0);
    private static final String category = "STANDARD";
    private static final String car = "Blue Cadillac CTS Wagon";
    private static final String plate = "CADILLAC";


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
     * C1930735 On trip: Route, pickup pin, destination pin
     * C1930737 On trip: Driver bar - name, car name & license plate, ratings
     * C1930742 On trip: Rider is unable to cancel active trip
     * C1930739 On trip: Driver photo is correct and can be expanded
     * C1930740 On trip: Car photo is correct and can be expanded
     * C1930741 Comment field should be hidden once ride has started
     * C1930736 On trip: Correct driver car position and no other drivers are shown on map
     * C1930759 On trip: Rider is unable to logout during active trip
     * C1930743 On trip: Admin cancels ride - no fee
     *
     * @throws InterruptedException
     */
    @Test
    @TestCases({"C1930735", "C1930737", "C1930742",
            "C1930739", "C1930740", "C1930741",
            "C1930736", "C1930759", "C1930743"})
    public void showOnTripState() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);


        // simulate on trip state
        NavigationUtils.toAssignedState(this);
        NavigationUtils.toReachedState(this);
        NavigationUtils.toActiveState(this);

        // wait for map animation
        waitFor("Map animation", 4000);

        //C1930735 On trip: Route, pickup pin, destination pin
        //C1930736 On trip: Correct driver car position and no other drivers are shown on map
        // check car is visible
        MapTestUtil.assertCarMarkersVisible();
        MapTestUtil.assertCarMarkersCount(1);


        // check pickup and destination is visible
        MapTestUtil.assertPickupMarkersVisible();
        MapTestUtil.assertDestinationMarkersVisible();

        //C1930737 On trip: Driver bar - name, car name & license plate, ratings

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

        //C1930742 On trip: Rider is unable to cancel active trip
        onView(withId(R.id.cancel_trip)).check(matches(not(isEnabled())));

        //C1930739 On trip: Driver photo is correct and can be expanded
        waitForDisplayed(R.id.driver_image);

        // tap on driver photo
        onView(withId(R.id.driver_image)).perform(click());

        // check the dialog
        waitForViewInWindow(allOf(withId(R.id.dialogImageView), isDisplayed(), hasDrawable()));
        onView(allOf(withId(R.id.btnClose), isDisplayed(), isClickable())).perform(click());
        waitFor("Animation", 1000);

        //C1930740 On trip: Car photo is correct and can be expanded
        waitForDisplayed(R.id.car_image);

        // tap on car photo
        onView(withId(R.id.car_image)).perform(click());

        // check the dialog
        waitForViewInWindow(allOf(withId(R.id.dialogImageView), isDisplayed(), hasDrawable()));
        onView(allOf(withId(R.id.btnClose), isDisplayed(), isClickable())).perform(click());

        //C1930741 Comment field should be hidden once ride has started
        onView(withId(R.id.comment)).check(doesNotExist());

        // C1930759 Rider is unable to logout during active trip
        waitFor("Animation", 1000);
        NavigationUtils.throughLogout();
        waitForViewInWindow(onView(withText(R.string.cannot_logout_during_ride)).inRoot(isToast()));

        // C1930743 On trip: Admin cancels ride - no fee
        NavigationUtils.toCancelledState(this, this::changeRiderCancelled);
        waitForViewInWindow(allOf(withId(com.afollestad.materialdialogs.R.id.md_content), withText(R.string.message_ride_cancelled_by_admin), isDisplayed()));

        NavigationUtils.clearRideState(this);
    }

    @Test
    @TestCases({"C1930756", "C1930758"})
    public void recovery() throws InterruptedException, RemoteException, UiObjectNotFoundException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);


        // simulate on trip state
        NavigationUtils.toAssignedState(this);
        NavigationUtils.toReachedState(this);
        NavigationUtils.toActiveState(this);

        // wait for map animation
        waitFor("Map animation", 4000);

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        //C1930737 On trip: Driver bar - name, car name & license plate, ratings

        // check collapsed state
        onView(allOf(withId(R.id.driver_name_small), isDisplayed())).check(matches(withText(name)));
        onView(allOf(withId(R.id.driver_rate_small), isDisplayed())).check(matches(withText(rating)));
        onView(allOf(withId(R.id.car_category_small), isDisplayed())).check(matches(withText(category)));
        onView(allOf(withId(R.id.car_make_small), isDisplayed())).check(matches(withText(car)));
        onView(allOf(withId(R.id.car_plate_small), isDisplayed())).check(matches(withText(plate)));
        onView(allOf(withId(R.id.driver_image_small), isDisplayed())).check(matches(hasDrawable()));

        MapTestUtil.assertCarMarkersVisible(5000);
        int numMarkers = MapTestUtil.getCarMarkersCount();
        MapTestUtil.assertCarMarkersCount(1);
        MapTestUtil.assertPickupMarkersVisible();
        MapTestUtil.assertDestinationMarkersVisible();

        DeviceTestUtils.setAirplaneMode(true);
        waitForDisplayed(R.id.error_panel);
        DeviceTestUtils.setAirplaneMode(false);
        Matchers.waitFor(condition("Yellow bar should disappear")
                .withMatcher(withId(R.id.error_panel))
                .withCheck(not(isDisplayed())));

        // check collapsed state
        onView(allOf(withId(R.id.driver_name_small), isDisplayed())).check(matches(withText(name)));
        onView(allOf(withId(R.id.driver_rate_small), isDisplayed())).check(matches(withText(rating)));
        onView(allOf(withId(R.id.car_category_small), isDisplayed())).check(matches(withText(category)));
        onView(allOf(withId(R.id.car_make_small), isDisplayed())).check(matches(withText(car)));
        onView(allOf(withId(R.id.car_plate_small), isDisplayed())).check(matches(withText(plate)));
        onView(allOf(withId(R.id.driver_image_small), isDisplayed())).check(matches(hasDrawable()));

        MapTestUtil.assertCarMarkersVisible();
        MapTestUtil.assertCarMarkersCount(1);
        MapTestUtil.assertPickupMarkersVisible();
        MapTestUtil.assertDestinationMarkersVisible();

        NavigationUtils.clearRideState(this);
    }

    @Test
    @TestCases({"C1930744", "C1930745", "C1930747"})
    public void changeDestination() throws InterruptedException, RemoteException, UiObjectNotFoundException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);


        // simulate on trip state
        NavigationUtils.toAssignedState(this);
        NavigationUtils.toReachedState(this);
        NavigationUtils.toActiveState(this);

        // wait for map animation
        waitFor("Map animation", 4000);

        mockRequests(RequestType.RIDE_UPDATE_200_PUT);

        applyPrediction(NEAR_PLACE_ID_1, NEAR_PLACE_TEXT_1, NEAR_PLACE_LAT_1, NEAR_PLACE_LNG_1);
        NavigationUtils.applyAddressPredictionDuringRide(R.id.destination_address, "whatever", NEAR_PLACE_TEXT_1);

        // check collapsed state
        onView(allOf(withId(R.id.driver_name_small), isDisplayed())).check(matches(withText(name)));
        onView(allOf(withId(R.id.driver_rate_small), isDisplayed())).check(matches(withText(rating)));
        onView(allOf(withId(R.id.car_category_small), isDisplayed())).check(matches(withText(category)));
        onView(allOf(withId(R.id.car_make_small), isDisplayed())).check(matches(withText(car)));
        onView(allOf(withId(R.id.car_plate_small), isDisplayed())).check(matches(withText(plate)));
        onView(allOf(withId(R.id.driver_image_small), isDisplayed())).check(matches(hasDrawable()));

        MapTestUtil.assertCarMarkersVisible(5000);
        MapTestUtil.assertCarMarkersCount(1);
        MapTestUtil.assertPickupMarkersVisible(5000);
        MapTestUtil.assertDestinationMarkersVisible(5000);

        // C1930747 On trip: Remove destination when set
        onView(withId(R.id.destination_address)).perform(click());
        waitForDisplayed(R.id.addressInput);
        onView(withId(R.id.addressInput)).perform(clearText());
        onView(withId(R.id.addressInput)).perform(clearText());
        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();
        Espresso.pressBack();
        onView(allOf(withId(R.id.destination_address), withText(NEAR_PLACE_TEXT_1)));

        // C1930745 On trip: Update destination several times
        applyPrediction(NEAR_PLACE_ID_2, NEAR_PLACE_TEXT_2, NEAR_PLACE_LAT_2, NEAR_PLACE_LNG_2);
        NavigationUtils.applyAddressPredictionDuringRide(R.id.destination_address, "whatever", NEAR_PLACE_TEXT_2);

        // check collapsed state
        onView(allOf(withId(R.id.driver_name_small), isDisplayed())).check(matches(withText(name)));
        onView(allOf(withId(R.id.driver_rate_small), isDisplayed())).check(matches(withText(rating)));
        onView(allOf(withId(R.id.car_category_small), isDisplayed())).check(matches(withText(category)));
        onView(allOf(withId(R.id.car_make_small), isDisplayed())).check(matches(withText(car)));
        onView(allOf(withId(R.id.car_plate_small), isDisplayed())).check(matches(withText(plate)));
        onView(allOf(withId(R.id.driver_image_small), isDisplayed())).check(matches(hasDrawable()));

        NavigationUtils.clearRideState(this);
    }

    @Test
    @TestCases("C1930746")
    public void setDestinationWhenNotSet() throws InterruptedException, RemoteException, UiObjectNotFoundException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);


        // simulate on trip state
        NavigationUtils.toAssignedState(this);
        NavigationUtils.toReachedState(this);
        NavigationUtils.toActiveState(this, this::rideWithoutDestination);

        // wait for map animation
        waitFor("Map animation", 2000);

        mockRequests(RequestType.RIDE_UPDATE_200_PUT);

        applyPrediction(NEAR_PLACE_ID_2, NEAR_PLACE_TEXT_2, NEAR_PLACE_LAT_2, NEAR_PLACE_LNG_2);
        // select destination
        NavigationUtils.applyAddressPredictionDuringRide(R.id.destination_address,
                "whatever",
                NEAR_PLACE_TEXT_2);

        // check collapsed state
        onView(allOf(withId(R.id.driver_name_small), isDisplayed())).check(matches(withText(name)));
        onView(allOf(withId(R.id.driver_rate_small), isDisplayed())).check(matches(withText(rating)));
        onView(allOf(withId(R.id.car_category_small), isDisplayed())).check(matches(withText(category)));
        onView(allOf(withId(R.id.car_make_small), isDisplayed())).check(matches(withText(car)));
        onView(allOf(withId(R.id.car_plate_small), isDisplayed())).check(matches(withText(plate)));
        onView(allOf(withId(R.id.driver_image_small), isDisplayed())).check(matches(hasDrawable()));

        NavigationUtils.clearRideState(this);
    }

    @Test
    @TestCases("C1930734")
    public void mapVerification() throws InterruptedException, RemoteException, UiObjectNotFoundException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);


        // simulate on trip state
        NavigationUtils.toAssignedState(this);
        NavigationUtils.toReachedState(this);
        NavigationUtils.toActiveState(this);

        // wait for map animation

        MapTestUtil.assertPickupMarkersVisible(4000);
        MapTestUtil.assertDestinationMarkersVisible(4000);
        MapTestUtil.assertCarMarkersVisible(4000);
        MapTestUtil.assertCarMarkersCount(1);

        MapTestUtil.moveMapLeft(2000);
        MapTestUtil.assertPickupMarkersNotVisible();
        MapTestUtil.assertDestinationMarkersNotVisible();

        MapTestUtil.assertPickupMarkersVisible(20000);
        MapTestUtil.assertDestinationMarkersVisible(20000);
        MapTestUtil.assertCarMarkersVisible(20000);
        MapTestUtil.assertCarMarkersCount(1);

        MapTestUtil.moveMapLeft(2000);
        MapTestUtil.assertPickupMarkersNotVisible();
        MapTestUtil.assertDestinationMarkersNotVisible();

        onView(withId(R.id.myLocationButton)).perform(click());

        MapTestUtil.assertCarMarkersVisible(20000);
        MapTestUtil.assertCarMarkersCount(1);
        MapTestUtil.assertPickupMarkersVisible(20000);
        MapTestUtil.assertDestinationMarkersVisible(20000);

        NavigationUtils.toActiveState(this, ride -> {
            ride.getActiveDriver().setLatitude(30.527174);
            ride.getActiveDriver().setLongitude(-97.750211);
            return ride;
        });
        waitFor("update", 5000);

        MapTestUtil.assertCarMarkersVisible(20000);
        MapTestUtil.assertCarMarkersCount(1);

        NavigationUtils.toActiveState(this, ride -> {
            ride.getActiveDriver().setLatitude(30.527174);
            ride.getActiveDriver().setLongitude(-97.350211);
            return ride;
        });
        waitFor("update", 5000);

        MapTestUtil.assertCarMarkersVisible(20000);
        MapTestUtil.assertCarMarkersCount(1);

        NavigationUtils.clearRideState(this);
    }


    private Ride rideWithoutDestination(Ride ride) {
        ride.setEndAddress(null);
        ride.setEndLocationLat(null);
        ride.setEndLocationLong(null);
        return ride;
    }

    private Ride changeRiderCancelled(Ride ride) {
        ride.setStatus(RideStatus.ADMIN_CANCELLED.toString());
        return ride;
    }

}
