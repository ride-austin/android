package cases.s5_core_flow.s7_location_updates;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.Ride;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.TimeUtils;
import com.rideaustin.utils.ViewActionUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static junit.framework.Assert.fail;

/**
 * Created on 03/01/2018
 *
 * @author sdelaysam
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LocationUpdatesSuite extends BaseUITest {

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
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CURRENT_DRIVER_200_GET,
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET,
                RequestType.ACCEPT_DRIVER_TERMS_200_PUT,
                RequestType.PENDING_EVENTS_200_POST,
                RequestType.ACTIVE_DRIVER_EMPTY_200_GET,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE);
    }


    @Test
    @TestCases({"C1929357", "C1929358", "C1929359", "C1929360"})
    public void checkLocationUpdates() throws InterruptedException {
        long onlineInterval = 5000L;
        long onlineMovingInterval = 3000L;
        long tripInterval = 2000L;

        GlobalConfig config = getResponse("CONFIG_GLOBAL_200", GlobalConfig.class);
        config.getLocationUpdateIntervals().setWhenOnlineAndNotMoving(onlineInterval / 1000);
        config.getLocationUpdateIntervals().setWhenOnlineAndMoving(onlineMovingInterval / 1000);
        config.getLocationUpdateIntervals().setWhenOnTrip(tripInterval / 1000);
        removeRequests(RequestType.CONFIG_DRIVER_200_GET);
        mockRequest(RequestType.CONFIG_DRIVER_200_GET, config);

        Ride ride = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
        ride.setStartLocationLat(30.418641);
        ride.setStartLocationLong(-97.748798);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // offline
        checkNoLocationUpdates(10000);

        // online not moving
        NavigationUtils.goOnline(this);
        checkLocationUpdates(onlineInterval, 3, "being online");

        // online moving
        NavigationUtils.startMoving(this, DEFAULT_ROUTE, 1000);
        checkLocationUpdates(onlineMovingInterval, 3, "being online and moving");
        NavigationUtils.stopMoving();

        // get request ride
        NavigationUtils.toRequestedState(this, ride);
        ViewActionUtils.waitFor("Animation", 2000);

        // accept ride and moving
        NavigationUtils.acceptRideRequest(this, ride);

        NavigationUtils.startMoving(this, DEFAULT_ROUTE, 1000);
        checkLocationUpdates(tripInterval, 3, "when accepted");
        NavigationUtils.stopMoving();

        // arrive and not moving
        NavigationUtils.arrive(this, ride);
        checkNoLocationUpdates(5000);

        // start ride and moving
        NavigationUtils.startTrip(this, ride);
        NavigationUtils.startMoving(this, DEFAULT_ROUTE, 1000);
        checkLocationUpdates(tripInterval, 3, "during trip");
        NavigationUtils.stopMoving();

        // end trip
        NavigationUtils.endTrip(this, null);
        checkLocationUpdates(onlineInterval, 3, "when trip ended");

        // online
        NavigationUtils.rateRide(this);
        checkLocationUpdates(onlineInterval, 3, "being online");

        // offline
        NavigationUtils.goOffline(this);
        checkNoLocationUpdates(10000);
    }

    private void checkLocationUpdates(long interval, int count, String message) throws InterruptedException {
        long deviation = 250L;

        AtomicInteger validCount = new AtomicInteger(0);
        AtomicLong lastReceived = new AtomicLong(TimeUtils.currentTimeMillis());

        setOnRequestAction(RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                () -> {
                    long now = TimeUtils.currentTimeMillis();
                    long diff = now - lastReceived.getAndSet(now);
                    if (diff > interval - deviation && diff < interval + deviation) {
                        validCount.incrementAndGet();
                    }
                });

        waitFor(condition("Take " + count + " location updates with correct interval " + message)
                .withBool(() -> validCount.get() == count)
                .withInterval(deviation)
                .withTimeout(interval * Math.max(count * 2, 10))); // 10 intervals (or double count)
    }

    private void checkNoLocationUpdates(long period) {
        setOnRequestAction(RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                () -> fail("Should not send location updates"));

        ViewActionUtils.waitFor("", period);
    }
}
