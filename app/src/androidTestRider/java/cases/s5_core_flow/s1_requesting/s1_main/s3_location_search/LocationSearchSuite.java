package cases.s5_core_flow.s1_requesting.s1_main.s3_location_search;

import android.support.test.espresso.DataInteraction;
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
import com.rideaustin.api.model.RiderData;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.MapTestUtil;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.RecentPlacesUtils;
import com.rideaustin.utils.ViewActionUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.CarSliderUtils.carTypeSelected;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersCount;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertPickupMarkersNotVisible;
import static com.rideaustin.utils.MapTestUtil.assertPickupMarkersVisible;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.MatcherUtils.withListSize;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotEmptyText;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsAnything.anything;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by hatak on 01.06.2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LocationSearchSuite extends BaseUITest {

    public static final String AUSTIN_AIRPORT_NAME = "Austin-Bergstrom International Airport";
    public static final int USER_1_ID = 1443;
    public static final int USER_2_ID = 5443;
    @Rule
    public ActivityTestRule<NavigationDrawerActivity> activityRule = new RaActivityRule<>(NavigationDrawerActivity.class, false, false);

    @Override
    public void setUp() {
        super.setUp();
        initMockResponseFactory(RiderMockResponseFactory.class);
        mockRequests(RequestType.GLOBAL_APP_INFO_200_GET,
                RequestType.LOGIN_SUCCESS_200_POST,
                RequestType.RIDER_DATA_NO_RIDE_200_GET,
                RequestType.TOKENS_200_POST,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.CAR_TYPES_200_GET,
                RequestType.CURRENT_RIDE_EMPTY_200_GET,
                RequestType.ACDR_REGULAR_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CONFIG_RIDER_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.CONFIG_ZIPCODES_200_GET,
                RequestType.LOGOUT_200_POST,
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET);
    }

    @Test
    @TestCases("C1930590")
    public void airportAndOtherAustinLocationsComesUpAtFirstPlaceAccordingToTheCurrentCity() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        RecentPlacesUtils.mockRecentPlaces(USER_1_ID);
        loginWithoutRide(USER_1_ID);

        validatePredictionFor(R.id.pickup_address, "Airp", AUSTIN_AIRPORT_NAME);
        validatePredictionFor(R.id.pickup_address, "Austin Airport", AUSTIN_AIRPORT_NAME);
        validatePredictionFor(R.id.pickup_address, "Airport", AUSTIN_AIRPORT_NAME);

        validatePredictionFor(R.id.destination_address, "Airp", AUSTIN_AIRPORT_NAME);
        validatePredictionFor(R.id.destination_address, "Austin Airport", AUSTIN_AIRPORT_NAME);
        validatePredictionFor(R.id.destination_address, "Airport", AUSTIN_AIRPORT_NAME);
    }

    @Test
    @TestCases("C1930591")
    public void selectingPlaceFromTheRecentPlacesMovesThePinToTheCorrectLocation() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        RecentPlacesUtils.mockRecentPlaces(USER_1_ID);
        loginWithoutRide(USER_1_ID);

        removeRequests(RequestType.ACDR_REGULAR_200_GET);
        List listOfCars = getResponse("ACDR_REGULAR_RECENT_PLACES_VALIDATION_200", List.class);
        mockRequest(RequestType.ACDR_REGULAR_200_GET, listOfCars);

        onView(withId(R.id.pickup_address)).perform(click());
        waitForDisplayed(R.id.addressInput);

        onData(anything()).atPosition(3) // 0 - home, 1 - work, 2 - delimiter
                .inAdapterView(withId(R.id.listView))
                .perform(click());

        tryConfirmFarAwayDialog();

        ViewActionUtils.waitFor("camera", 3000);
        onView(allOf(withId(R.id.pickup_address), withText(RecentPlacesUtils.PLACE_NAME_2))).check(matches(isDisplayed()));
        assertCarMarkersCount(1);

        onView(withId(R.id.pickup_address)).perform(click());
        onData(anything()).atPosition(4) // 0 - home, 1 - work, 2 - delimiter, 3 - PLACE_NAME_2
                .inAdapterView(withId(R.id.listView))
                .perform(click());

        tryConfirmFarAwayDialog();

        onView(allOf(withId(R.id.pickup_address), withText(RecentPlacesUtils.PLACE_NAME_1))).check(matches(isDisplayed()));
        assertCarMarkersCount(2);
    }

    @Test
    @TestCases("C1930592")
    public void correctLocationsSentToTheDriverWhenTheUserSelectedPlaceFromTheListOfRecentPlaces() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        RecentPlacesUtils.mockRecentPlaces(USER_1_ID);
        loginWithoutRide(USER_1_ID);

        onView(withId(R.id.pickup_address)).perform(click());
        waitForDisplayed(R.id.addressInput);

        onData(anything()).atPosition(3) // 0 - home, 1 - work, 2 - delimiter
                .inAdapterView(withId(R.id.listView))
                .perform(click());

        tryConfirmFarAwayDialog();
        ViewActionUtils.waitFor("camera", 3000);
        onView(allOf(withId(R.id.pickup_address), withText(RecentPlacesUtils.PLACE_NAME_2))).check(matches(isDisplayed()));

        onView(withId(R.id.destination_address)).perform(click());
        onData(anything()).atPosition(4) // 0 - home, 1 - work, 2 - delimiter, 3 - PLACE_NAME_2
                .inAdapterView(withId(R.id.listView))
                .perform(click());

        tryConfirmFarAwayDialog();

        onView(allOf(withId(R.id.destination_address), withText(RecentPlacesUtils.PLACE_NAME_1))).check(matches(isDisplayed()));

        // RA-13094: now need to tap on set pickup location even if destination is already set
        onView(allOf(withId(R.id.set_pickup_location), isDisplayed())).perform(click());

        // wait for request panel
        waitForDisplayed(R.id.requestPanel);

        waitFor(condition().withView(onView(withId(R.id.btn_request_ride))));

        mockRequests(RequestType.RIDE_REQUESTED_200_GET);
        mockRequests(RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST);
        mockRequests(RequestType.RIDE_CANCEL_200_DELETE);

        // Request Ride
        waitFor(condition("Ride request button should be shown")
                .withMatcher(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride)))
                .withCheck(allOf(isCompletelyDisplayed(), isEnabled(), isClickable())));
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        waitFor(condition("Cancel ride request should be shown")
                .withMatcher(withId(R.id.cancel_pending_request)));

        // validate send params
        verifyRequest(RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST, hasQueryParam("startLocationLat", equalTo("30.202435")));
        verifyRequest(RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST, hasQueryParam("startLocationLong", equalTo("-97.666405")));
        verifyRequest(RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST, hasQueryParam("startAddress", equalTo(RecentPlacesUtils.PLACE_NAME_2)));
        verifyRequest(RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST, hasQueryParam("startZipCode", equalTo("78719")));

        verifyRequest(RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST, hasQueryParam("endLocationLat", equalTo("30.277679")));
        verifyRequest(RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST, hasQueryParam("endLocationLong", equalTo("-97.741058")));
        verifyRequest(RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST, hasQueryParam("endAddress", equalTo(RecentPlacesUtils.PLACE_NAME_1)));
        verifyRequest(RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST, hasQueryParam("endZipCode", equalTo("78705")));


        // cancel ride
        onView(withId(R.id.cancel_pending_request)).perform(click());
        waitForViewInWindow(onView(withText(R.string.text_cancel_ride_dialog)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        mockEvent(RequestType.EVENT_RIDE_CANCELLED_BY_RIDER);

        waitFor(condition().withView(onView(withText(R.string.message_ride_cancelled_by_user))));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    @Test
    @TestCases("C1930593")
    public void newUserClickingPickUpOrDestinationNoRecentPlacesIsShown() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        loginWithoutRide(USER_1_ID);
        onView(withId(R.id.pickup_address)).perform(click());
        waitForDisplayed(R.id.addressInput);
        // 0 - home, 1 - work, 2 - delimiter, 3 - set on map
        onView(withId(R.id.listView)).check(matches(withListSize(4)));
        Espresso.pressBack();
        onView(withId(R.id.destination_address)).perform(click());
        waitForDisplayed(R.id.addressInput);
        // 0 - home, 1 - work, 2 - delimiter, 3 - set on map
        onView(withId(R.id.listView)).check(matches(withListSize(4)));
    }

    @Test
    @TestCases("C1930594")
    public void pickupLocationShouldBeAddedToRecentPlaces() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        loginWithoutRide(USER_1_ID);
        validatePredictionFor(R.id.pickup_address, "Airp", AUSTIN_AIRPORT_NAME);

        onView(withId(R.id.destination_address)).perform(click());
        waitForDisplayed(R.id.addressInput);

        // 0 - home, 1 - work, 2 - delimiter, 3 - Airport prediction,  4 - set on map
        onView(withId(R.id.listView)).check(matches(withListSize(5)));

        onData(anything())
                .inAdapterView(withId(R.id.listView))
                .onChildView(withText(AUSTIN_AIRPORT_NAME))
                .atPosition(3)
                .check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1930595")
    public void newPlacesShouldBeAddedOnTopOfRecentPlacesList() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        RecentPlacesUtils.mockRecentPlaces(USER_1_ID);
        loginWithoutRide(USER_1_ID);
        validatePredictionFor(R.id.pickup_address, "Airp", AUSTIN_AIRPORT_NAME);

        onView(withId(R.id.destination_address)).perform(click());
        waitForDisplayed(R.id.addressInput);

        // 0 - home, 1 - work, 2 - delimiter, 3 - Airport prediction, 4, 5 - other recents,  6 - set on map
        onView(withId(R.id.listView)).check(matches(withListSize(7)));

        onData(anything())
                .inAdapterView(withId(R.id.listView))
                .onChildView(withText(AUSTIN_AIRPORT_NAME))
                .atPosition(3)
                .check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1930596")
    public void showMaximumOfTenRecentPlaces() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        RecentPlacesUtils.mockRecentPlacesFullList(USER_1_ID);
        loginWithoutRide(USER_1_ID);
        onView(withId(R.id.pickup_address)).perform(click());
        waitForDisplayed(R.id.addressInput);

        // 2 - home/work, 1 - delimiter, 1 - set on map
        int maxCount = Constants.MAX_ELEMENTS_TO_SAVE_IN_HISTORY + 2 + 1 + 1;
        onView(withId(R.id.listView)).check(matches(withListSize(maxCount)));
        Espresso.pressBack();

        validatePredictionFor(R.id.pickup_address, "Airp", AUSTIN_AIRPORT_NAME);

        onView(withId(R.id.destination_address)).perform(click());
        waitForDisplayed(R.id.addressInput);
        onView(withId(R.id.listView)).check(matches(withListSize(maxCount)));

        onData(anything())
                .inAdapterView(withId(R.id.listView))
                .onChildView(withText(AUSTIN_AIRPORT_NAME))
                .atPosition(3) // 0 - home, 1 - work, 2 - delimiter
                .check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1930597")
    public void singleLocationIsNotRepeatedOnTheListOfRecentPlaces() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        loginWithoutRide(USER_1_ID);
        validatePredictionFor(R.id.pickup_address, "Airp", AUSTIN_AIRPORT_NAME);
        validatePredictionFor(R.id.destination_address, "Airp", AUSTIN_AIRPORT_NAME);

        onView(withId(R.id.pickup_address)).perform(click());
        waitForDisplayed(R.id.addressInput);

        // 0 - home, 1 - work, 2 - delimiter, 3 - Airport prediction,  4 - set on map
        onView(withId(R.id.listView)).check(matches(withListSize(5)));

        // check there is no multiple items error
        onData(hasToString(containsString(AUSTIN_AIRPORT_NAME)))
                .inAdapterView(withId(R.id.listView))
                .check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1930600")
    public void recentPlacesStillAvailableAfterLoggingOutAndLoggingBackIn() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login and check first user
        RecentPlacesUtils.mockRecentPlaces(USER_1_ID);
        loginWithoutRide(USER_1_ID);

        onView(withId(R.id.pickup_address)).perform(click());
        waitForDisplayed(R.id.addressInput);

        // 0 - home, 1 - work, 2 - delimiter, 3, 4 - other recents,  5 - set on map
        onView(withId(R.id.listView)).check(matches(withListSize(6)));

        // logout
        Espresso.pressBack();
        NavigationUtils.throughLogout();

        loginWithoutRide(USER_1_ID);

        onView(withId(R.id.pickup_address)).perform(click());
        waitForDisplayed(R.id.addressInput);

        // 0 - home, 1 - work, 2 - delimiter, 3, 4 - other recents,  5 - set on map
        onView(withId(R.id.listView)).check(matches(withListSize(6)));
    }

    @Test
    @TestCases("C1930601")
    public void selectingPlaceGreaterThan200m() throws UiObjectNotFoundException, InterruptedException {
        NavigationUtils.startActivity(activityRule);
        loginWithoutRide(USER_1_ID);

        onView(withId(R.id.pickup_address)).perform(click());
        waitForDisplayed(R.id.addressInput);
        onView(withId(R.id.clearButton)).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.addressInput)).perform(typeText("airp"), closeSoftKeyboard());

        DataInteraction dataInteraction = onData(hasToString(containsString(AUSTIN_AIRPORT_NAME)))
                .atPosition(0)
                .inAdapterView(withId(R.id.listView));
        waitFor(condition().withData(dataInteraction));

        dataInteraction
                .check(matches(isDisplayed()))
                .perform(click());

        // check distance dialog
        waitFor(condition().withView(onView(withText(getAppContext().getString(R.string.warning_location_distance, 200)))));
        onView(withId(R.id.md_buttonDefaultNegative)).perform(click());

        Espresso.pressBack(); // back to recents
        Espresso.pressBack(); // back to map

        assertPickupMarkersNotVisible();

        MapTestUtil.zoomOut(200);
        MapTestUtil.moveMapRight(1000);

        ViewActionUtils.waitFor("Swipe Animation", 2000);
        // Address has changed correspondingly
        ViewActionUtils.waitFor(R.id.pickup_address, withText(not(isEmptyString())), IDLE_TIMEOUT_MS);

        // Tap pin
        onView(withId(R.id.pickup_location)).perform(click());

        // check distance dialog
        waitFor(condition().withView(onView(withText(getAppContext().getString(R.string.warning_location_distance, 200)))));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        assertPickupMarkersVisible();
    }


    private void validatePredictionFor(final int fieldId, final String query, final String expectedResult) throws InterruptedException {
        onView(withId(fieldId)).perform(click());
        waitForDisplayed(R.id.addressInput);

        onView(withId(R.id.addressInput)).perform(clearText());
        onView(withId(R.id.addressInput)).perform(clearText());
        onView(withId(R.id.addressInput)).perform(typeText(query));

        DataInteraction dataInteraction = onData(hasToString(containsString(expectedResult)))
                .atPosition(0)
                .inAdapterView(withId(R.id.listView));
        waitFor(condition().withData(dataInteraction));

        dataInteraction
                .check(matches(isDisplayed()))
                .perform(click());

        tryToCloseDistanceDialog();
        waitForDisplayed(fieldId);
        onView(allOf(withId(fieldId), withText(containsString(expectedResult)))).check(matches(isDisplayed()));
    }

    private void tryToCloseDistanceDialog() {
        try {
            onView(withText(getAppContext().getString(R.string.warning_location_distance, 200))).check(matches(isDisplayed()));
            onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        } catch (Exception e) {
            // ignore
        }

    }

    private void loginWithoutRide(int userId) throws InterruptedException {
        removeRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);
        RiderData riderData = getResponse("RIDER_DATA_NO_RIDE_200", RiderData.class);
        riderData.getRider().getUser().setId(userId);
        riderData.getRider().getUser().setUuid(String.valueOf(userId));
        mockRequest(RequestType.RIDER_DATA_NO_RIDE_200_GET, riderData);

        NavigationUtils.throughLogin("han.solo@falcon.com", "secret");
        tryToCloseAnyVisibleDialog();

        // Map screen is opened
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // Pick-up location is automatically set to the address that corresponds to the rider's GPS location
        waitForNotEmptyText(R.id.pickup_address);

        // "SET PICKUP LOCATION" pin is shown
        waitFor(condition().withView(onView(withId(R.id.set_pickup_location))));

        // Driver's car is visible on the screen
        assertCarMarkersVisible();

        // Car types slider is displayed in the bottom and 'REGULAR' is selected by default
        waitFor(condition().withView(onView(withId(R.id.car_types_slider))));
        onView(allOf(withId(R.id.car_types_slider), isDisplayed())).check(matches(carTypeSelected(0)));

        // check fields
        onView(withId(R.id.pickup_address)).check(matches(isDisplayed()));
        onView(withId(R.id.destination_address)).check(matches(isDisplayed()));

        // check black pin
        onView(allOf(withId(R.id.pickup_location), isDisplayed())).check(matches(isDisplayed()));
    }

    private void tryConfirmFarAwayDialog() {
        NavigationUtils.tryCloseLocationWarning();
    }

    private void tryToCloseAnyVisibleDialog() {
        try {
            onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        } catch (Exception e) {
            // ignore
        }
    }
}
