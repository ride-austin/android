package cases.s5_core_flow.s2_receive_request;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.ResponseModifier;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideRequestParams;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.ui.utils.UIUtils;
import com.rideaustin.utils.LocalizeUtils;
import com.rideaustin.utils.MapTestUtil;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.SerializationHelper;
import com.rideaustin.utils.TimeUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.matchesRegex;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-10231, RA-11252
 * Created by Sergey Petrov on 12/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverReceiveRequestSuite1 extends BaseUITest {

    // according to SURGEAREA_200
    private static final double HIGHEST_SURGE_FACTOR = 2.5;

    // according to RIDE_REQUESTED_200 / EVENT_RIDE_REQUESTED
    private static final String ADDRESS = "11610 Jollyville Road, Austin, Texas";
    private static final String RIDER_NAME = "RideOneTwoNine";
    private static final double RATING = 5.0;
    private static final String CAR_CATEGORY = "STANDARD";
    private static final long ETA = 154;
    private static final double SURGE_FACTOR = 2.5;

    private static final String ADDRESS_1 = "Address 1";
    private static final String RIDER_NAME_1 = "Rider 1";
    private static final double RATING_1 = 1.23;
    private static final String CAR_CATEGORY_1 = "Category 1";
    private static final long ETA_1 = 1000;
    private static final double SURGE_FACTOR_1 = 1.0;

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new RaActivityRule<>(SplashActivity.class, false, false);

    @Override
    public void setUp() {
        super.setUp();
        initMockResponseFactory(DriverMockResponseFactory.class);
        mockRequests(RequestType.GLOBAL_APP_INFO_200_GET,
                RequestType.LOGIN_SUCCESS_200_POST,
                RequestType.CURRENT_USER_200_GET,
                RequestType.CONFIG_DRIVER_200_GET,
                RequestType.TOKENS_200_POST,
                RequestType.QUEUES_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.SURGE_AREA_200_GET,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.DRIVERS_CARTYPES_200_GET,
                RequestType.CURRENT_DRIVER_200_GET,
                RequestType.ACTIVE_DRIVER_EMPTY_200_GET,
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET,
                RequestType.DRIVER_GO_ONLINE_200_POST,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.PENDING_EVENTS_200_POST,
                RequestType.ACCEPT_RIDE_200_POST,
                RequestType.DECLINE_RIDE_200_DELETE,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE);
    }

    /**
     * C1929243: Can decline ride request
     * C1929247: Request disappears if rider cancels ride request
     * C1929246: Ride request disappears after 10 seconds
     * C1929248, C1929249: Request Details on Accept button, Pick-up location and Driver location on map
     * C1929250: Priority multiplier for PRIORITY request
     * C1929251: Request details and map updated on next ride request
     * C1929257: Surge area is shown on request screen map
     */
    @Test
    @TestCases({"C1929243", "C1929247", "C1929246", "C1929248", "C1929250", "C1929251", "C1929257"})
    public void requestPanelAndMap() {

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        //------------------------------------------------------------------------------------------
        // Go online
        //------------------------------------------------------------------------------------------

        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_online), isDisplayed(), isEnabled(), isClickable()));
        onView(withId(R.id.toolbarActionButton)).perform(click());
        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_offline), isDisplayed(), isEnabled(), isClickable()));

        MapTestUtil.assertSurgeMarkerVisible(UIUtils.formatSurgeFactor(HIGHEST_SURGE_FACTOR));

        //------------------------------------------------------------------------------------------
        // C1929243: Can decline ride request
        //------------------------------------------------------------------------------------------

        NavigationUtils.toRequestedState(this, null);

        // check ride info
        onView(allOf(withId(R.id.pickup_address), withText(ADDRESS))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.rider_name), withText(RIDER_NAME))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.rider_rate), withText(UIUtils.formatRating(RATING)))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.rider_car_type), withText(CAR_CATEGORY))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.eta), withText(LocalizeUtils.formatDriverEta(getAppContext(), ETA)))).check(matches(isDisplayed()));

        // C1929250: check surge factor
        onView(allOf(withId(R.id.surge_factor), withText(UIUtils.formatSurgeFactor(SURGE_FACTOR)))).check(matches(isDisplayed()));
        onView(withId(R.id.surge_panel)).check(matches(isDisplayed()));

        // check pending state UI
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline), isClickable())).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.button), withText(R.string.menu_decline), isClickable())).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.counter_text), withText(matchesRegex("\\d+")))).check(matches(isDisplayed()));
        onView(withId(R.id.accept_title)).check(matches(isDisplayed()));
        onView(withId(R.id.accept_subtitle)).check(matches(not(isDisplayed())));

        // C1929248: check markers on map
        MapTestUtil.assertCarMarkersCount(1);
        MapTestUtil.assertPickupMarkersVisible();

        NavigationUtils.toEmptyState(this);
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);

        // decline ride
        onView(allOf(withId(R.id.button), withText(R.string.menu_decline))).perform(click());

        //------------------------------------------------------------------------------------------
        // C1929247: Request disappears if rider cancels ride request
        //------------------------------------------------------------------------------------------

        // wait for state switch back to online
        waitFor(R.id.button, allOf(withText(R.string.menu_support), isDisplayed()));
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline), isClickable())).check(matches(isDisplayed()));
        assertFalse(exists(withId(R.id.pending_pickup)));

        // assert one car marker (no other drivers according to ACTIVE_DRIVERS_EMPTY_200)
        MapTestUtil.assertCarMarkersCount(1);
        // assert pickup marker removed
        MapTestUtil.assertPickupMarkersNotVisible();

        //------------------------------------------------------------------------------------------
        // C1929250: Priority multiplier for PRIORITY request (no surge factor)
        //------------------------------------------------------------------------------------------

        NavigationUtils.toRequestedState(this, changeRide(getResponse("RIDE_REQUESTED_200", Ride.class)));

        long startTime = TimeUtils.currentTimeMillis();

        // C1929251: Request details and map updated on next ride request
        onView(allOf(withId(R.id.pickup_address), withText(ADDRESS_1))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.rider_name), withText(RIDER_NAME_1))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.rider_rate), withText(UIUtils.formatRating(RATING_1)))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.rider_car_type), withText(CAR_CATEGORY_1))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.eta), withText(LocalizeUtils.formatDriverEta(getAppContext(), ETA_1)))).check(matches(isDisplayed()));

        // C1929250: check surge factor (no surge factor)
        onView(withId(R.id.surge_factor)).check(matches(not(isDisplayed())));
        onView(withId(R.id.surge_panel)).check(matches(not(isDisplayed())));

        //------------------------------------------------------------------------------------------
        // C1929246: Ride request disappears after 10 seconds (15 seconds)
        //------------------------------------------------------------------------------------------

        // wait for state switch back to online
        waitFor(R.id.button, allOf(withText(R.string.menu_support), isDisplayed()));

        int requestSeconds = (int) (TimeUtils.currentTimeMillis() - startTime) / 1000;
        // Here, countdown should be 15 seconds, according to CONFIG_GLOBAL_200 (see RideAcceptance.acceptancePeriod)
        // because we don't mock parameters in EVENT_RIDE_REQUESTED (see RideRequestParams)
        int minSeconds = 12;
        int maxSeconds = 16;
        String message = String.format(Locale.US, "Request seconds %d should be in range(%d, %d)", requestSeconds, minSeconds, maxSeconds);
        assertTrue(message, requestSeconds >= minSeconds && requestSeconds <= maxSeconds);

    }

    @Test
    public void shouldSendAcknowledgeRequest() {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        //------------------------------------------------------------------------------------------
        // Go online
        //------------------------------------------------------------------------------------------

        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_online), isDisplayed(), isEnabled(), isClickable()));
        onView(withId(R.id.toolbarActionButton)).perform(click());
        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_offline), isDisplayed(), isEnabled(), isClickable()));

        MapTestUtil.assertSurgeMarkerVisible(UIUtils.formatSurgeFactor(HIGHEST_SURGE_FACTOR));

        //------------------------------------------------------------------------------------------
        // Check ride request acknowledged and shown
        //------------------------------------------------------------------------------------------

        RideRequestParams params = new RideRequestParams();
        long now = TimeUtils.currentTimeMillis();
        params.setAcceptanceExpiration(now + 10000L);

        NavigationUtils.toRequestedState(this, null, params);
        verifyRequestsCount(RequestType.ACKNOWLEDGE_RIDE_200_POST, equalTo(1));
        resetRequestStats(RequestType.ACKNOWLEDGE_RIDE_200_POST);

        long startTime = TimeUtils.currentTimeMillis();

        // wait for state switch back to online
        waitFor(R.id.button, allOf(withText(R.string.menu_support), isDisplayed()));

        int requestSeconds = (int) (TimeUtils.currentTimeMillis() - startTime) / 1000;
        // Here, countdown should be 10 seconds, according to EVENT_RIDE_REQUESTED (see RideRequestParams)
        // but really test execution take some time... so check wider range
        int minSeconds = 4;
        int maxSeconds = 10;

        String message = String.format(Locale.US, "Request seconds %d should be in range(%d, %d)", requestSeconds, minSeconds, maxSeconds);
        assertTrue(message, requestSeconds >= minSeconds && requestSeconds <= maxSeconds);

        //------------------------------------------------------------------------------------------
        // Check staled ride request ignored
        //------------------------------------------------------------------------------------------

        RideRequestParams params1 = new RideRequestParams();
        now = TimeUtils.currentTimeMillis();
        params1.setAcceptanceExpiration(now);

        atomicOnRequests(() -> {
            NavigationUtils.clearRideState(this);
            mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET,
                    RequestType.ACKNOWLEDGE_RIDE_200_POST,
                    RequestType.RIDE_REQUESTED_200_GET);

            mockEvent(RequestType.EVENT_RIDE_REQUESTED, "EVENT_RIDE_REQUESTED", response -> {
                response.setParameters(SerializationHelper.serialize(params1));
                return response;
            });
        });

        waitFor("Ride request should be ignored", 5000);
        onView(withId(R.id.pending_pickup)).check(doesNotExist());
        verifyRequestsCount(RequestType.ACKNOWLEDGE_RIDE_200_POST, equalTo(0));
    }

    private Ride changeRide(Ride ride) {
        ride.setStartAddress(ADDRESS_1);
        ride.getRider().setFirstname(RIDER_NAME_1);
        ride.getRider().setRating(RATING_1);
        ride.getRequestedCarType().setTitle(CAR_CATEGORY_1);
        ride.setEstimatedTimeArrive(ETA_1);
        ride.setSurgeFactor(SURGE_FACTOR_1);
        return ride;
    }
}
