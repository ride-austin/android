package cases.s5_core_flow.s3_driver_arrived.s4_location_updates;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseUITest;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.MatcherUtils.noQueryParam;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCondition;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.is;

/**
 * Reference: RA-11493
 * Created by Sergey Petrov on 21/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverArrivedLocationUpdatesSuite extends BaseUITest {

    private static final double AIRPORT_LAT = 30.2021489;
    private static final double AIRPORT_LNG = -97.666829;
    private static final float BAD_ACCURACY = 100;

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

    /**
     * C1930723: rider location == pickup location and rider is not moving
     * C1930724: rider location != pickup location and rider is not moving
     * C1930725: rider location != pickup location and rider is moving
     * C1930726: Locations with low accuracy are not sent
     */
    @Test
    @TestCases({"C1930723", "C1930724", "C1930725", "C1930726"})
    public void test_UpdateLocation() throws InterruptedException {

        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // request ride
        NavigationUtils.throughRideRequest(this);

        // simulate reached state
        NavigationUtils.toAssignedState(this);
        NavigationUtils.toReachedState(this);

        //------------------------------------------------------------------------------------------
        // Check that after driver accepted the ride and arrived, Rider app sends location updates
        //------------------------------------------------------------------------------------------

        resetRequestStats(RequestType.RIDE_DRIVER_REACHED_200_GET);

        // check default location sent
        waitForCondition("App should send default location", () -> {
            String lat = String.valueOf(DEFAULT_LAT);
            String lng = String.valueOf(DEFAULT_LNG);
            return hasRequests(RequestType.RIDE_DRIVER_REACHED_200_GET, hasQueryParam("lat", is(lat)))
                    && hasRequests(RequestType.RIDE_DRIVER_REACHED_200_GET, hasQueryParam("lng", is(lng)));
        });

        //------------------------------------------------------------------------------------------
        // Switch off internet
        //------------------------------------------------------------------------------------------

        setNetworkError(true);
        resetRequestStats(RequestType.RIDE_DRIVER_REACHED_200_GET);

        // simulate driver moved to another location
        mockLocation(AIRPORT_LAT, AIRPORT_LNG);

        // wait to check ride is not sent
        waitFor("Checking", 2500L);

        assertFalse(hasRequest(RequestType.RIDE_DRIVER_REACHED_200_GET));

        //------------------------------------------------------------------------------------------
        // Switch on internet
        //------------------------------------------------------------------------------------------

        setNetworkError(false);

        waitForCondition("App should send updated location", () -> {
            String lat = String.valueOf(AIRPORT_LAT);
            String lng = String.valueOf(AIRPORT_LNG);
            return hasRequests(RequestType.RIDE_DRIVER_REACHED_200_GET, hasQueryParam("lat", is(lat)))
                    && hasRequests(RequestType.RIDE_DRIVER_REACHED_200_GET, hasQueryParam("lng", is(lng)));
        });

        //------------------------------------------------------------------------------------------
        // Locations with low accuracy are not sent
        //------------------------------------------------------------------------------------------

        resetRequestStats(RequestType.RIDE_DRIVER_REACHED_200_GET);
        mockLocation(DEFAULT_LAT, DEFAULT_LNG, BAD_ACCURACY);

        // wait to check ride is sent but without location
        waitFor("Checking", 2500L);

        waitForCondition("App should send ride without location", () -> hasRequests(RequestType.RIDE_DRIVER_REACHED_200_GET, noQueryParam("lat"))
                && hasRequests(RequestType.RIDE_DRIVER_REACHED_200_GET, noQueryParam("lng")));

    }

}
