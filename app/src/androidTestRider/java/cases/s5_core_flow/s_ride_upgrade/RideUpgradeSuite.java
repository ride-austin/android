package cases.s5_core_flow.s_ride_upgrade;

import android.os.RemoteException;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.google.firebase.messaging.RemoteMessage;
import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.UpgradeRequest;
import com.rideaustin.api.model.UpgradeRequestStatus;
import com.rideaustin.manager.notification.firebase.RiderFirebaseMessagingService;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.utils.UIUtils;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.ViewActionUtils;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.CarSliderUtils.carTypeSelected;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertDestinationMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertPickupMarkersVisible;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.swipeFromLeftEdge;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotEmptyText;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Created by hatak on 05.06.2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RideUpgradeSuite extends BaseUITest {

    public static final String RIDE_UPGRADE_SUCCESS = "Your ride was successfully upgraded to SUV";
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
                RequestType.ACDR_REGULAR_200_GET,
                RequestType.CURRENT_RIDE_EMPTY_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CONFIG_RIDER_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.CONFIG_ZIPCODES_200_GET,
                RequestType.RIDE_CANCEL_200_DELETE,
                RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST,
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET,
                RequestType.RIDES_ESTIMATE);
    }


    /**
     * C1930845 - Receive SUV Upgrade Request
     * C1930846 - Accept SUV Upgrade request
     * C1930859 - Fare Estimate should be correct after SUV Upgrade is accepted
     */
    @Test
    @TestCases({"C1930845", "C1930846", "C1930859"})
    public void receiveAndAccept() throws InterruptedException {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();
        startRide();
        sendRideUpgradeNotification();

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET);
        removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);

        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));
        mockRequests(RequestType.RIDES_UPGRADE_ACCEPT);

        waitFor(condition().withView(onView(withText(getAppContext().getString(R.string.upgrade_ride_title, "SUV")))));
        onView(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV"))))
                .check(matches(isDisplayed()));

        ViewActionUtils.waitFor("animation", 2000);
        onView(allOf(withId(R.id.decline_button), withText(R.string.btn_no))).check(matches(isDisplayed()));
        onView(withId(R.id.cancel_button)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.accept_button), withText(R.string.btn_yes))).perform(click());

        waitFor(condition().withView(onView(withText(RIDE_UPGRADE_SUCCESS))));

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET);
        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.ACCEPTED, 1));
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.ACCEPTED, 1));

        ViewActionUtils.waitFor("animation", 3000);
        onView(allOf(withId(R.id.car_category_small), withText("SUV"))).check(matches(isDisplayed()));
        onView(withId(R.id.driver_container_small)).perform(swipeUp());
        ViewActionUtils.waitFor("animation", 1000);
        onView(withId(R.id.fare_estimate_button)).perform(click());
        ViewActionUtils.waitFor("request", 1000);
        verifyRequest(RequestType.RIDES_ESTIMATE, hasQueryParam("carCategory", is("SUV")));
        Espresso.pressBack();

        cancelRide();
    }


    @Test
    @TestCases("C1930847")
    public void declineSUVUpgradeRequest() throws InterruptedException {
        mockRequests(RequestType.RIDES_UPGRADE_DECLINE);

        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();
        startRide();
        sendRideUpgradeNotification();

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET);
        removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);

        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));

        waitFor(condition().withView(onView(withText(getAppContext().getString(R.string.upgrade_ride_title, "SUV")))));
        onView(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV"))))
                .check(matches(isDisplayed()));

        ViewActionUtils.waitFor("animation", 2000);
        onView(allOf(withId(R.id.decline_button))).perform(click());
        ViewActionUtils.waitFor("request fired", 2000);
        verifyRequestsCount(RequestType.RIDES_UPGRADE_DECLINE, is(1));

        cancelRide();
    }


    @Test
    @TestCases("C1930848")
    public void receiveSUVUpgradeRequestWhenInOtherScreen() throws InterruptedException {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();
        startRide();

        //navigate to settings
        onView(withId(R.id.mapContainer)).perform(swipeFromLeftEdge());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        sendRideUpgradeNotification();

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET);
        removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);

        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));
        mockRequests(RequestType.RIDES_UPGRADE_ACCEPT);

        waitFor(condition().withView(onView(withText(getAppContext().getString(R.string.upgrade_ride_title, "SUV")))));
        onView(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV"))))
                .check(matches(isDisplayed()));

        cancelRide();
    }

    @Test
    @TestCases("C1930849")
    public void receiveSUVUpgradeCancellationFromDriverBeforeAccepting() throws InterruptedException {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();
        startRide();

        sendRideUpgradeNotification();

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET);
        removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);

        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));

        waitFor(condition().withView(onView(withText(getAppContext().getString(R.string.upgrade_ride_title, "SUV")))));
        onView(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV"))))
                .check(matches(isDisplayed()));

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET);
        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.CANCELLED, 1));
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.CANCELLED, 1));

        ViewActionUtils.waitFor("ride status update", 3000);
        waitFor(condition().withView(onView(withText(R.string.upgrade_ride_cancelled_msg)).inRoot(isToast())));
        assertFalse(exists(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV")))));

        cancelRide();
    }

    @Test
    @TestCases("C1930851")
    public void receiveSUVUpgradeCancellationFromServerAfterExpired() throws InterruptedException {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();
        startRide();

        sendRideUpgradeNotification();

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET);
        removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);

        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));

        waitFor(condition().withView(onView(withText(getAppContext().getString(R.string.upgrade_ride_title, "SUV")))));
        onView(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV"))))
                .check(matches(isDisplayed()));

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET);
        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.EXPIRED, 1));
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.EXPIRED, 1));

        ViewActionUtils.waitFor("ride status update", 3000);

        // assert category is REGULAR and dialog not visible
        waitFor(condition().withView(onView(withText(R.string.upgrade_ride_expired_msg)).inRoot(isToast())));
        assertFalse(exists(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV")))));
        onView(allOf(withId(R.id.car_category_small), withText("STANDARD"))).check(matches(isDisplayed()));

        cancelRide();
    }

    @Test
    @TestCases("C1930852")
    public void acceptSUVUpgradeWhileLosingInternetConnection() throws InterruptedException {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();
        startRide();

        sendRideUpgradeNotification();

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET);
        removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);

        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));
        mockRequests(RequestType.RIDES_UPGRADE_ACCEPT);

        waitFor(condition().withView(onView(withText(getAppContext().getString(R.string.upgrade_ride_title, "SUV")))));
        onView(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV"))))
                .check(matches(isDisplayed()));

        ViewActionUtils.waitFor("animation", 2000);

        // switch off internet
        setNetworkError(true);

        // schedule internet switch on in 5s
        RxSchedulers.schedule(() -> setNetworkError(false), 5, TimeUnit.SECONDS);

        // accept ride upgrade
        onView(allOf(withId(R.id.accept_button), withText(R.string.btn_yes))).perform(click());

        waitFor(condition().withView(onView(withText(RIDE_UPGRADE_SUCCESS))));
        assertFalse(exists(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV")))));

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET);
        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.ACCEPTED, 1));
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.ACCEPTED, 1));

        ViewActionUtils.waitFor("new status arrived", 3000);

        // assert category is SUV and dialog not visible
        assertFalse(exists(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV")))));
        onView(allOf(withId(R.id.car_category_small), withText("SUV"))).check(matches(isDisplayed()));

        cancelRide();
    }

    @Test
    @TestCases({"C1930854", "C1930855", "C1930856"})
    public void test_UpgradeFromStandardToSUVWithPriorityFare() throws InterruptedException {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();
        startRide();

        sendRideUpgradeNotification();

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET);
        removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);

        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 2));
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 2));
        mockRequests(RequestType.RIDES_UPGRADE_ACCEPT);

        // validate ride upgrade dialog
        waitFor(condition().withView(onView(withText(getAppContext().getString(R.string.upgrade_ride_title, "SUV")))));
        onView(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV"))))
                .check(matches(isDisplayed()));
        onView(withId(R.id.surge_icon)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.surge_factor_text), withText(getAppContext().getString(R.string.priority_fare_text, UIUtils.formatSurgeFactor(2)))))
                .check(matches(isDisplayed()));

        ViewActionUtils.waitFor("animation", 2000);

        // accept ride upgrade
        onView(allOf(withId(R.id.accept_button), withText(R.string.btn_yes))).perform(click());

        // wait for success dialog
        waitFor(condition().withView(onView(withText(RIDE_UPGRADE_SUCCESS))));
        assertFalse(exists(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV")))));

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET);
        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.ACCEPTED, 2));
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.ACCEPTED, 2));

        // related to RideStatusService 2s period
        ViewActionUtils.waitFor("new status arrived", 3000);

        // assert category is SUV and dialog not visible
        assertFalse(exists(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV")))));
        onView(allOf(withId(R.id.car_category_small), withText("SUV"))).check(matches(isDisplayed()));

        completeRide();
    }

    @Test
    @TestCases("C1930866")
    public void recovery() throws InterruptedException, RemoteException, UiObjectNotFoundException {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();
        startRide();

        sendRideUpgradeNotification();

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET);
        removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);

        mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));
        mockRequest(RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, preparePendingRideUpgradeRequest(UpgradeRequestStatus.REQUESTED, 1));
        mockRequests(RequestType.RIDES_UPGRADE_ACCEPT);

        waitFor(condition().withView(onView(withText(getAppContext().getString(R.string.upgrade_ride_title, "SUV")))));
        onView(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV"))))
                .check(matches(isDisplayed()));

        ViewActionUtils.waitFor("animation", 2000);

        // switch off internet
        setNetworkError(true);
        ViewActionUtils.waitFor("animation", 5000);
        // switch on internet
        setNetworkError(false);

        onView(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV"))))
                .check(matches(isDisplayed()));

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        waitFor(condition().withView(onView(withText(getAppContext().getString(R.string.upgrade_ride_title, "SUV")))));
        onView(allOf(withId(R.id.content_text), withText(getAppContext().getString(R.string.upgrade_ride_content, "REGULAR", "SUV"))))
                .check(matches(isDisplayed()));

        cancelRide();
    }

    private void completeRide() throws InterruptedException {
        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET);
        mockRequests(RequestType.RIDE_COMPLETED_200_GET, RequestType.RIDE_MAP_200_GET);
        waitFor(condition().withView(onView(allOf(withId(R.id.rate_driver_container), isDisplayed()))));
    }

    private void cancelRide() throws InterruptedException {
        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET);
        mockRequests(RequestType.RIDE_DRIVER_CANCELLED_200_GET);
        waitFor(condition().withView(onView(withText(R.string.message_ride_cancelled_by_driver))));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    private void startRide() {
        mockRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET);

        // Request Ride
        onView(Matchers.allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        //verify is in ride
        waitForDisplayed(R.id.ride_details);
    }

    private void sendRideUpgradeNotification() {
        String messageBody = "{\"rideId\":777, \"source\":\"REGULAR\", \"target\":\"SUV\", \"sound\":\"Update.caf\"}";

        RemoteMessage remoteMessage = new RemoteMessage.Builder("abc")
                .addData("eventKey", "RIDE_UPGRADE")
                .addData("body", messageBody)
                .build();

        RiderFirebaseMessagingService.injectPushNotification(remoteMessage);
    }

    private Ride prepareRideUpgradeRequest() {
        Ride ride = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
        UpgradeRequest rideUpgrade = new UpgradeRequest();
        rideUpgrade.setSource("REGULAR");
        rideUpgrade.setTarget("SUV");
        rideUpgrade.setSurgeFactor(2.5);
        rideUpgrade.setStatus(UpgradeRequestStatus.REQUESTED.name());
        ride.setUpgradeRequest(rideUpgrade);
        ride.getRequestedCarType().setTitle("SUV");
        return ride;
    }

    private Ride preparePendingRideUpgradeRequest(UpgradeRequestStatus upgradeRequestStatus, double surgeFactor) {
        Ride ride = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
        UpgradeRequest rideUpgrade = new UpgradeRequest();
        rideUpgrade.setSource("REGULAR");
        rideUpgrade.setTarget("SUV");
        rideUpgrade.setSurgeFactor(surgeFactor);
        rideUpgrade.setStatus(upgradeRequestStatus.name());
        ride.setUpgradeRequest(rideUpgrade);
        if (upgradeRequestStatus == UpgradeRequestStatus.ACCEPTED) {
            ride.getRequestedCarType().setTitle("SUV");
            ride.getRequestedCarType().setCarCategory("SUV");
        }
        return ride;
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
    }

    private void setupDestinationAndValidate() throws InterruptedException {
        // set destination
        NavigationUtils.applyAddressPrediction(R.id.destination_address, "11600 Research Blvd",  "11600 Research Blvd");
        assertDestinationMarkersVisible();
        //check request panel
        onView(withId(R.id.requestPanel)).check(matches(isDisplayed()));
    }
}
