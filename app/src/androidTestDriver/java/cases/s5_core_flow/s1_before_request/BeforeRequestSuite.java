package cases.s5_core_flow.s1_before_request;

import android.os.RemoteException;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.DriverLocation;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersNotVisible;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.getCarMarkersCount;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCondition;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by hatak on 13.06.2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class BeforeRequestSuite extends BaseUITest {

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
                RequestType.CURRENT_DRIVER_200_GET,
                RequestType.QUEUES_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.ACTIVE_DRIVER_EMPTY_200_GET,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.LOGOUT_200_POST,
                RequestType.DRIVER_GO_ONLINE_200_POST,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET);
    }


    /**
     * C1929229 - CanGoOnline
     * C1929230 - CanGoOffline
     * C1929231 - Map Verification - driver's car position, locate me button
     * C1929232 - Map Verification - zoom in, zoom out and move
     * C1929235 - New registered Driver that is eligible to Driver should be able to go Online
     */
    @Test
    @TestCases({"C1929229", "C1929230", "C1929231", "C1929232", "C1929235"})
    public void test1() throws UiObjectNotFoundException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "password");
        waitFor("camera", 2000);
        goOnline();
        goOffline();
        goOnline();

        assertCarMarkersVisible();
        moveMap(150);
        waitFor("camera", 2000);
        assertCarMarkersNotVisible();

        waitForDisplayed(R.id.myLocation);
        onView(withId(R.id.myLocation)).perform(click());

        waitFor("camera", 2000);
        assertCarMarkersVisible();
        mapZoomIn();
        waitFor("camera", 2000);
        assertCarMarkersVisible();
        mapZoomOut();
        waitFor("camera", 2000);
        assertCarMarkersVisible();
    }

    @Test
    @TestCases("C1929233")
    public void mapVerificationOtherDriversCars() throws UiObjectNotFoundException {
        // prepare other drivers
        List<DriverLocation> driverLocations = new ArrayList<>();
        DriverLocation driverLocation1 = new DriverLocation();
        driverLocation1.setLatitude(30.2013196);
        driverLocation1.setLongitude(-97.6671468);
        driverLocation1.setCourse(-1);
        Driver driver1 = new Driver();
        driver1.setId(666L);
        driverLocation1.setDriver(driver1);

        DriverLocation driverLocation2 = new DriverLocation();
        driverLocation2.setLatitude(30.2017196);
        driverLocation2.setLongitude(-97.6671468);
        driverLocation2.setCourse(-1);
        Driver driver2 = new Driver();
        driver2.setId(999L);
        driverLocation2.setDriver(driver2);

        driverLocations.add(driverLocation1);
        driverLocations.add(driverLocation2);

        removeRequests(RequestType.ACTIVE_DRIVERS_EMPTY_200_GET);
        mockRequest(RequestType.ACDR_REGULAR_200_GET, driverLocations);

        // send location
        mockLocation(30.2015196, -97.6671468);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "password");

        // go online
        waitFor("camera", 2000);
        goOnline();

        waitForCondition("fetch other drivers", () -> getCarMarkersCount() == 3);
    }

    @Test
    @TestCases("C1929234")
    public void recovery() throws UiObjectNotFoundException, RemoteException, InterruptedException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "password");
        waitFor("camera", 2000);
        goOnline();
        assertCarMarkersVisible();

        // switch network off
        setNetworkError(true);
        waitForViewInWindow(onView(withText(R.string.network_error)).inRoot(isToast()));

        // switch network on
        setNetworkError(false);

        // simulate server go offline event due to missing connection
        mockEvent(RequestType.EVENT_GO_OFFLINE_INACTIVE);

        // according to EVENT_GO_OFFLINE_INACTIVE
        waitForViewInWindow(withText(containsString("You have been marked as offline due to inactivity")));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // wait for offline button
        waitFor(R.id.toolbarActionButton, allOf(withText(R.string.action_go_online), isDisplayed()));
        assertCarMarkersVisible();

        goOnline();
        assertCarMarkersVisible();

        // send background and restore
        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        assertCarMarkersVisible();
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline))).check(matches(isDisplayed()));

    }

    /**
     * Reference: RA-11385
     */
    @Test
    public void shouldBeSentOfflineOn409() throws InterruptedException {
        removeRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET,
                RequestType.DRIVER_UPDATE_LOCATION_409_PUT);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "password");

        Matchers.waitFor(condition("Driver should be sent offline")
                .withMatcher(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))));
    }

    private void mapZoomOut() throws UiObjectNotFoundException {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject map = device.findObject(new UiSelector().descriptionContains(getString(R.string.google_map)));
        map.pinchIn(30, 60);
    }

    private void mapZoomIn() throws UiObjectNotFoundException {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject map = device.findObject(new UiSelector().descriptionContains(getString(R.string.google_map)));
        map.pinchOut(30, 60);
    }

    private void moveMap(int value) throws UiObjectNotFoundException {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        UiObject map = device.findObject(new UiSelector().descriptionContains(getString(R.string.google_map)));
        map.swipeLeft(value);
    }

    private void goOffline() {
        // go offline
        removeRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.DRIVERS_CARTYPES_200_GET);
        mockRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET, RequestType.DRIVER_GO_OFFLINE_200_DELETE);

        // check offline button
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline))).perform(click());
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).check(matches(isDisplayed()));
    }

    private void goOnline() {
        // go online
        removeRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET, RequestType.DRIVER_GO_OFFLINE_200_DELETE);
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.DRIVERS_CARTYPES_200_GET);

        // check offline button
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).perform(click());
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline))).check(matches(isDisplayed()));
    }
}
