package cases.s9_fare_split;

import android.os.RemoteException;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.google.gson.reflect.TypeToken;
import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.api.model.faresplit.FareSplitResponse;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by Sergey Petrov on 27/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FareSplitSuite1 extends BaseUITest {

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

    @Test
    public void shouldRestoreFareSplit() throws UiObjectNotFoundException, InterruptedException, RemoteException {
        fail("Should be fixed");

        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);

        // simulate assigned state
        NavigationUtils.toAssignedState(this);

        // tap on driver image should cause panel expand
        onView(withId(R.id.driver_image_small)).perform(click());
        waitForCompletelyDisplayed(R.id.ride_details);

        //------------------------------------------------------------------------------------------
        // request fare split
        //------------------------------------------------------------------------------------------

        onView(allOf(withId(R.id.btn_fare_split), withText(R.string.btn_split_fare), isEnabled())).perform(click());
        waitForDisplayed(R.id.et_phone_number);

        // send fare split request
        removeRequests(RequestType.SPLIT_FARE_EMPTY_200_GET);
        mockRequests(RequestType.SPLIT_FARE_REQUEST_200_POST,
                RequestType.SPLIT_FARE_REQUESTED_200_GET);

        onView(withId(R.id.et_phone_number)).perform(typeText("+12345678900"), closeSoftKeyboard());
        onView(withId(R.id.btn_send)).perform(click());
//        waitForDisplayed(R.id.tvsplit_fare_header_label);

//        // check request
//        String header = getAppContext().getResources().getQuantityString(R.plurals.fare_split_detail_header, 1, 1);
//        onView(allOf(withId(R.id.tvsplit_fare_header_label), withText(header))).check(matches(isDisplayed()));
//        onView(withId(R.id.tvsplit_fare_header_label)).perform(click());
//        onView(withId(R.id.split_fare_rider_container)).check(matches(isDisplayed()));
//        // according to SPLIT_FARE_REQUESTED_200_GET
//        onView(allOf(withId(R.id.tv_split_fare_name), withText("Serg Rider"))).check(matches(isDisplayed()));
//        onView(allOf(withId(R.id.tv_split_fare_status), withText(RideStatus.REQUESTED.toString()))).check(matches(isDisplayed()));

        //------------------------------------------------------------------------------------------
        // send app to background and restore
        //------------------------------------------------------------------------------------------

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

//        // check info is still here
//        onView(allOf(withId(R.id.tvsplit_fare_header_label), withText(header))).check(matches(isDisplayed()));
//        onView(allOf(withId(R.id.tv_split_fare_name), withText("Serg Rider"))).check(matches(isDisplayed()));
//        onView(allOf(withId(R.id.tv_split_fare_status), withText(RideStatus.REQUESTED.toString()))).check(matches(isDisplayed()));

        //------------------------------------------------------------------------------------------
        // lose internet, send app to background and restore
        //------------------------------------------------------------------------------------------

        DeviceTestUtils.setAirplaneMode(true);

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        waitForDisplayed(R.id.error_panel);

//        // check info is still here
//        onView(allOf(withId(R.id.tvsplit_fare_header_label), withText(header))).check(matches(isDisplayed()));
//        onView(allOf(withId(R.id.tv_split_fare_name), withText("Serg Rider"))).check(matches(isDisplayed()));
//        onView(allOf(withId(R.id.tv_split_fare_status), withText(FareSplitResponse.SplitFareState.REQUESTED.toString()))).check(matches(isDisplayed()));

        //------------------------------------------------------------------------------------------
        // fare split updated while app had no connection
        //------------------------------------------------------------------------------------------

        List<FareSplitResponse> fareSplits = getFareSplitsResponse("SPLIT_FARE_REQUESTED_200");
        String status = FareSplitResponse.SplitFareState.ACCEPTED.toString();
        fareSplits.get(0).setStatus(status);
        removeRequests(RequestType.SPLIT_FARE_REQUESTED_200_GET);
        mockRequest(RequestType.SPLIT_FARE_REQUESTED_200_GET, fareSplits);

        // restore internet
        DeviceTestUtils.setAirplaneMode(false);
//        Matchers.waitFor(condition().withMatcher(withId(R.id.error_panel)).withCheck(not(isDisplayed())));
//        Matchers.waitFor(condition().withMatcher(allOf(withId(R.id.tv_split_fare_status), withText(status))));

        //------------------------------------------------------------------------------------------
        // lose internet, send app to background and restore
        //------------------------------------------------------------------------------------------

        DeviceTestUtils.pressHome();
        DeviceTestUtils.setAirplaneMode(true);
        DeviceTestUtils.restoreFromRecentApps();

        waitForDisplayed(R.id.error_panel);

        //------------------------------------------------------------------------------------------
        // fare split returns empty list and then internet restored
        //------------------------------------------------------------------------------------------

        removeRequests(RequestType.SPLIT_FARE_REQUESTED_200_GET);
        mockRequest(RequestType.SPLIT_FARE_REQUESTED_200_GET, "[]");

        // restore internet
        DeviceTestUtils.setAirplaneMode(false);
//        Matchers.waitFor(condition().withMatcher(withId(R.id.error_panel)).withCheck(not(isDisplayed())));
//        Matchers.waitFor(condition()
//                .withMatcher(withId(R.id.tvsplit_fare_header_label))
//                .withAssertion(doesNotExist()));

    }

    private List<FareSplitResponse> getFareSplitsResponse(String resourceName) {
        Type type = new TypeToken<ArrayList<FareSplitResponse>>() {}.getType();
        return getResponse(resourceName, type);
    }



}
