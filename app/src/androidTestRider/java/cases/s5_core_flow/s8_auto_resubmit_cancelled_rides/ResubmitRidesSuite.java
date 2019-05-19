package cases.s5_core_flow.s8_auto_resubmit_cancelled_rides;

import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.api.model.DriverLocation;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.NavigationUtils;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import java8.util.stream.StreamSupport;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.CarSliderUtils.carTypeSelected;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersCount;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertDestinationMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertEtaMarkerVisible;
import static com.rideaustin.utils.MapTestUtil.assertPickupMarkersVisible;
import static com.rideaustin.utils.MatcherUtils.isNotDecorView;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotEmptyText;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Created by hatak on 12.06.2017.
 */


@LargeTest
@RunWith(AndroidJUnit4.class)
public class ResubmitRidesSuite extends BaseUITest {

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
                RequestType.RIDE_CANCEL_200_DELETE,
                RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST,
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET);
    }


    @Test
    public void test_RideResubmit() throws InterruptedException {
        List<DriverLocation> driverLocations = new ArrayList<>();
        DriverLocation driverLocation1 = new DriverLocation();
        driverLocation1.setLatitude(30.416535);
        driverLocation1.setLongitude(-97.749177);
        driverLocation1.setCourse(-1);
        driverLocation1.setDrivingTimeToRider(128);
        Driver driver1 = new Driver();
        driver1.setId(1l);
        driverLocation1.setDriver(driver1);

        DriverLocation driverLocation2 = new DriverLocation();
        driverLocation2.setLatitude(30.416935);
        driverLocation2.setLongitude(-97.749177);
        driverLocation2.setCourse(-1);
        driverLocation2.setDrivingTimeToRider(128);
        Driver driver2 = new Driver();
        driver2.setId(2l);
        driverLocation2.setDriver(driver2);

        driverLocations.add(driverLocation1);
        driverLocations.add(driverLocation2);


        mockRequest(RequestType.ACDR_REGULAR_200_GET, driverLocations);

        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        // start in requesting state
        mockRequests(RequestType.RIDE_REQUESTED_200_GET);
        onView(Matchers.allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        // verify is requesting
        waitForDisplayed(R.id.cancel_pending_request);
        assertCarMarkersCount(2);

        // go to assigned state
        waitFor("animation", 1000);
        removeRequests(RequestType.RIDE_REQUESTED_200_GET);
        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET, prepareRide("Driver 1", "RX-8", "Honda"));

        // verify is assigned
        waitForDisplayed(R.id.ride_details);
        waitFor("animation", 1000);
        // assert driver details
        onView(allOf(withId(R.id.driver_name_small), withText("Driver 1"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.car_category_small), withText("STANDARD"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.car_make_small), withText(containsString("Honda RX-8")))).check(matches(isDisplayed()));
        assertCarMarkersCount(1);
        assertEtaMarkerVisible(getString(R.string.zero_mins));

        // go to requesting state
        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET);
        mockRequests(RequestType.RIDE_REQUESTED_200_GET);

        // verify is requesting
        waitForDisplayed(R.id.cancel_pending_request);
        assertCarMarkersCount(2);

        // prepare second ride object

        // go to assigned state with updated ride information
        waitFor("animation", 1000);
        removeRequests(RequestType.RIDE_REQUESTED_200_GET);
        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET, prepareRide("Driver 2", "Model-S", "Tesla"));

        //verify is assigned
        waitForDisplayed(R.id.ride_details);
        waitFor("animation", 1000);
        // assert driver details
        onView(allOf(withId(R.id.driver_name_small), withText("Driver 2"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.car_category_small), withText("STANDARD"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.car_make_small), withText(containsString("Tesla Model-S")))).check(matches(isDisplayed()));
        assertCarMarkersCount(1);
        assertEtaMarkerVisible(getString(R.string.zero_mins));

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET);
        cancelRide();
    }

    private Ride prepareRide(final String driverName, final String carModel, String carMake) {
        Ride updatedRide = getResponse("RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200", Ride.class);
        updatedRide.getActiveDriver().getDriver().setFirstname(driverName);
        List<Car> cars = updatedRide.getActiveDriver().getDriver().getCars();
        Car selectedCar = StreamSupport.stream(cars)
                .filter(Car::isSelected)
                .findFirst().get();

        selectedCar.setMake(carMake);
        selectedCar.setModel(carModel);
        updatedRide.getActiveDriver().setSelectedCar(selectedCar);
        return updatedRide;
    }


    private void cancelRide() {
        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET);
        mockRequests(RequestType.RIDE_DRIVER_CANCELLED_200_GET);
        waitForViewInWindow(onView(withText(R.string.message_ride_cancelled_by_driver)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }


    private void startWithoutRide() {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("lord.vader@rider.com", "secret");

        tryToCloseAnyVisibleDialog();
        // Map screen is opened
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // Pick-up location is automatically set to the address that corresponds to the rider's GPS location
        waitForNotEmptyText(R.id.pickup_address);

        // "SET PICKUP LOCATION" pin is shown
        waitForDisplayed(R.id.set_pickup_location);

        // Driver's car is visible on the screen
        assertCarMarkersVisible();

        // Car types slider is displayed in the bottom and 'REGULAR' is selected by default
        waitForDisplayed(R.id.car_types_slider);
        onView(Matchers.allOf(withId(R.id.car_types_slider), isDisplayed())).check(matches(carTypeSelected(0)));
    }

    private void tryToCloseAnyVisibleDialog() {
        try {
            onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        } catch (Exception e) {
            // ignore
        }
    }

    private void setupPickupLocationAndValidate() {
        // check fields
        onView(withId(R.id.pickup_address)).check(matches(isDisplayed()));
        onView(withId(R.id.destination_address)).check(matches(isDisplayed()));
        onView(withId(R.id.comment)).check(matches(not(isDisplayed())));

        // Tap pin to set pick up location
        onView(Matchers.allOf(withId(R.id.pickup_location), isDisplayed())).perform(click());

        // Request panel appears
        waitForDisplayed(R.id.requestPanel);

        // check pickup marker
        assertPickupMarkersVisible();

        // check fields
        onView(withId(R.id.pickup_address)).check(matches(isDisplayed()));
        onView(withId(R.id.destination_address)).check(matches(isDisplayed()));
        waitFor(Matchers.allOf(withId(R.id.comment), withHint(R.string.comment_hint), withText(""), isDisplayed()), "comments", 2000);
    }

    private void setupDestinationAndValidate() throws InterruptedException {
        // set destination
        NavigationUtils.applyAddressPrediction(R.id.destination_address, "11600 Research Blvd",  "11600 Research Blvd");
        assertDestinationMarkersVisible();
        //check request panel
        onView(withId(R.id.requestPanel)).check(matches(isDisplayed()));
    }

}
