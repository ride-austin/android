package cases.s6_comment_field;

import android.os.RemoteException;
import android.support.test.espresso.DataInteraction;
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
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.RecentPlacesUtils;
import com.rideaustin.utils.ViewActionUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.swipeUp;
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
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertDestinationMarkersVisible;
import static com.rideaustin.utils.MapTestUtil.assertPickupMarkersVisible;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotEmptyText;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;

/**
 * Created by hatak on 26.05.2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CommentsSuite extends BaseUITest {

    public static final String COMMENT_1 = "The force is strong in this one";
    public static final String COMMENT_2 = "Luke, I'm your father";
    public static final String COMMENT_EMOJI = "This is very important comment \ud83d\ude04";
    public static final String COMMENT_1_EMOJI = "The force is strong in this one \ud83d\ude04";

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
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CONFIG_RIDER_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.CONFIG_ZIPCODES_200_GET,
                RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST,
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET,
                RequestType.RIDE_CANCEL_200_DELETE,
                RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                RequestType.RIDE_MAP_200_GET,
                RequestType.SPLIT_FARE_EMPTY_200_GET,
                RequestType.SPLIT_FARE_DELETE_200);
    }

    @Test
    @TestCases("C1930637")
    public void lookAndFeel() {
        startWithoutRide();

        setupPickupLocationAndValidate();

        // add comment and check
        onView(withId(R.id.comment)).perform(replaceText(COMMENT_EMOJI), pressImeActionButton());
        onView(allOf(withId(R.id.comment), withText(COMMENT_EMOJI))).check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1930639")
    public void addCommentBeforeRideRequest() {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        // add comment and check
        onView(withId(R.id.comment)).perform(typeText(COMMENT_1), pressImeActionButton());
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));


        //check for comment
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));

        //check request panel
        onView(withId(R.id.requestPanel)).check(matches(isDisplayed()));

    }

    @Test
    @TestCases("C1930640")
    public void addCommentAfterRideIsRequestedButDriverHasntAccepted() {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        mockRequests(RequestType.RIDE_REQUESTED_200_GET);

        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        waitForDisplayed(R.id.cancel_pending_request);

        tryToClosePrediction();

        // check empty comment
        onView(allOf(withId(R.id.comment), withHint(R.string.comment_hint), withText(""))).check(matches(isDisplayed()));

        mockRequests(RequestType.RIDE_UPDATE_200_PUT);

        // verify comment
        onView(withId(R.id.comment)).perform(typeText(COMMENT_1), pressImeActionButton());
        waitFor("request is fired", 2000);
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));

        // verify that we are sending request
        verifyRequest(RequestType.RIDE_UPDATE_200_PUT, hasQueryParam("comment", equalTo(COMMENT_1)));

        // cancel ride
        onView(withId(R.id.cancel_pending_request)).perform(click());
        waitForViewInWindow(onView(withText(R.string.text_cancel_ride_dialog)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        waitForViewInWindow(onView(withText(R.string.message_ride_cancelled_by_user)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    @Test
    @TestCases("C1930663")
    public void commentWhenDriverIsOnTheWay() {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        mockRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET);

        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        // check empty comment
        onView(allOf(withId(R.id.comment), withHint(R.string.comment_hint), withText(""))).check(matches(isDisplayed()));

        mockRequests(RequestType.RIDE_UPDATE_200_PUT);

        //verify is in ride
        waitForDisplayed(R.id.ride_details);

        // verify comment
        onView(withId(R.id.comment)).perform(typeText(COMMENT_1), pressImeActionButton());
        waitFor("request is fired", 2000);
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));

        // verify that we are sending request
        verifyRequest(RequestType.RIDE_UPDATE_200_PUT, hasQueryParam("comment", equalTo(COMMENT_1)));

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET);
        // cancel ride from driver side
        mockRequests(RequestType.RIDE_DRIVER_CANCELLED_200_GET);
        // check that it was cancelled
        waitForViewInWindow(onView(withText(R.string.message_ride_cancelled_by_driver)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    @Test
    @TestCases("C1930701")
    public void addCommentWhenDriverIsArrived() {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        mockRequests(RequestType.RIDE_DRIVER_REACHED_200_GET);

        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        // check empty comment
        onView(allOf(withId(R.id.comment), withHint(R.string.comment_hint), withText(""))).check(matches(isDisplayed()));

        mockRequests(RequestType.RIDE_UPDATE_200_PUT);

        //close driver arrived dialog
        //close driver arrived dialog
        waitForViewInWindow(onView(withText(R.string.driver_reached_notification_msg)));
        tryToCloseAnyVisibleDialog();

        //verify is in ride
        waitForDisplayed(R.id.ride_details);

        // verify comment
        onView(withId(R.id.comment)).perform(typeText(COMMENT_1), pressImeActionButton());
        waitFor("request is fired", 2000);
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));

        // verify that we are sending request
        verifyRequest(RequestType.RIDE_UPDATE_200_PUT, hasQueryParam("comment", equalTo(COMMENT_1)));

        removeRequests(RequestType.RIDE_DRIVER_REACHED_200_GET);
        // cancel ride from driver side
        mockRequests(RequestType.RIDE_DRIVER_CANCELLED_200_GET);
        // check that it was cancelled
        waitForViewInWindow(onView(withText(R.string.message_ride_cancelled_by_driver)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    @Test
    @TestCases("C1930641")
    public void updateCommentBeforeRideRequest() {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        // add comment and check
        onView(withId(R.id.comment)).perform(typeText(COMMENT_1), pressImeActionButton());
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));

        //check for comment
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));

        // set new comments
        onView(withId(R.id.comment)).perform(clearText());
        onView(withId(R.id.comment)).perform(typeText(COMMENT_2), pressImeActionButton());
        onView(allOf(withId(R.id.comment), withText(COMMENT_2))).check(matches(isDisplayed()));

        //check request panel
        onView(withId(R.id.requestPanel)).check(matches(isDisplayed()));

        assertPickupMarkersVisible();
        assertDestinationMarkersVisible();

    }

    @Test
    @TestCases("C1930642")
    public void updateCommentAfterRideIsRequestedButDriverHasntAccepted() {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        mockRequests(RequestType.RIDE_REQUESTED_200_GET);

        // verify comment
        onView(withId(R.id.comment)).perform(typeText(COMMENT_1), pressImeActionButton());
        waitFor("soft keyboard", 2000);

        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        // check comment value
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));

        mockRequests(RequestType.RIDE_UPDATE_200_PUT);

        // verify comment update
        onView(withId(R.id.comment)).perform(clearText());
        onView(withId(R.id.comment)).perform(typeText(COMMENT_2), pressImeActionButton());
        waitFor("request is fired", 2000);
        onView(allOf(withId(R.id.comment), withText(COMMENT_2))).check(matches(isDisplayed()));

        // verify that we are sending request
        verifyRequest(RequestType.RIDE_UPDATE_200_PUT, hasQueryParam("comment", equalTo(COMMENT_2)));

        // cancel ride
        onView(withId(R.id.cancel_pending_request)).perform(click());
        waitForViewInWindow(onView(withText(R.string.text_cancel_ride_dialog)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        waitForViewInWindow(onView(withText(R.string.message_ride_cancelled_by_user)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    @Test
    @TestCases("C1930664")
    public void updateCommentWhenDriverIsOnTheWay() {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        mockRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_200_GET);

        //add comment
        onView(withId(R.id.comment)).perform(typeText(COMMENT_1), pressImeActionButton());
        waitForDisplayed(R.id.btn_request_ride);

        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        mockRequests(RequestType.RIDE_UPDATE_200_PUT);

        //verify is in ride
        waitForDisplayed(R.id.ride_details);

        // verify that we are sending request
        verifyRequest(RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST, hasQueryParam("comment", equalTo(COMMENT_1)));

        // check comment
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));

        // verify comment
        onView(withId(R.id.comment)).perform(clearText());
        onView(withId(R.id.comment)).perform(typeText(COMMENT_2), pressImeActionButton());
        waitFor("request is fired", 2000);
        onView(allOf(withId(R.id.comment), withText(COMMENT_2))).check(matches(isDisplayed()));

        // verify that we are sending request
        verifyRequest(RequestType.RIDE_UPDATE_200_PUT, hasQueryParam("comment", equalTo(COMMENT_2)));

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_200_GET);
        // cancel ride from driver side
        mockRequests(RequestType.RIDE_DRIVER_CANCELLED_200_GET);
        // check that it was cancelled
        waitForViewInWindow(onView(withText(R.string.message_ride_cancelled_by_driver)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    @Test
    @TestCases("C1930702")
    public void updateCommentWhenDriverIsArrived() {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        mockRequests(RequestType.RIDE_DRIVER_REACHED_WITH_DESTINATION_200_GET);

        // add comment
        onView(withId(R.id.comment)).perform(typeText(COMMENT_1), pressImeActionButton());

        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        mockRequests(RequestType.RIDE_UPDATE_200_PUT);

        //close driver arrived dialog
        waitForViewInWindow(onView(withText(R.string.driver_reached_notification_msg)));
        tryToCloseAnyVisibleDialog();

        //verify is in ride
        waitForDisplayed(R.id.ride_details);

        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));

        // verify comment
        onView(withId(R.id.comment)).perform(clearText());
        onView(withId(R.id.comment)).perform(typeText(COMMENT_2), pressImeActionButton());
        waitFor("request is fired", 2000);
        onView(allOf(withId(R.id.comment), withText(COMMENT_2))).check(matches(isDisplayed()));

        // verify that we are sending request
        verifyRequest(RequestType.RIDE_UPDATE_200_PUT, hasQueryParam("comment", equalTo(COMMENT_2)));

        removeRequests(RequestType.RIDE_DRIVER_REACHED_WITH_DESTINATION_200_GET);
        // cancel ride from driver side
        mockRequests(RequestType.RIDE_DRIVER_CANCELLED_200_GET);
        // check that it was cancelled
        waitForViewInWindow(onView(withText(R.string.message_ride_cancelled_by_driver)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    @Test
    @TestCases("C1930741")
    public void commentFieldShouldBeHiddenOnceRideHasStarted() {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        mockRequests(RequestType.RIDE_ACTIVE_200_GET);

        // add comment
        onView(withId(R.id.comment)).perform(typeText(COMMENT_1), pressImeActionButton());

        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        //verify is in ride
        waitForDisplayed(R.id.ride_details);

        verifyRequest(RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST, hasQueryParam("comment", equalTo(COMMENT_1)));

        //check comment field is hidden
        onView(withId(R.id.comment)).check(matches(not(isDisplayed())));

        removeRequests(RequestType.RIDE_ACTIVE_200_GET);
        // cancel ride from driver side
        mockRequests(RequestType.RIDE_DRIVER_CANCELLED_200_GET);
        // check that it was cancelled
        waitForViewInWindow(onView(withText(R.string.message_ride_cancelled_by_driver)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    @Test
    @TestCases("C1930703")
    public void commentFieldMaintainsTheValueAsRideIsProgressing() {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        // add comment
        onView(withId(R.id.comment)).perform(typeText(COMMENT_1), pressImeActionButton());

        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        //verify is pending request
        waitForDisplayed(R.id.cancel_pending_request);

        // verify request send
        verifyRequest(RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST, hasQueryParam("comment", equalTo(COMMENT_1)));

        // verify comment value
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));
        mockRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_200_GET);

        //verify is in ride
        waitForDisplayed(R.id.ride_details);

        // verify comment value
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_200_GET);
        mockRequests(RequestType.RIDE_DRIVER_REACHED_WITH_DESTINATION_200_GET);

        waitForViewInWindow(allOf(withText(R.string.driver_reached_notification_msg), isDisplayed()));
        //close driver arrived dialog
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // verify comment value
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));


        removeRequests(RequestType.RIDE_DRIVER_REACHED_WITH_DESTINATION_200_GET);
        mockRequests(RequestType.RIDE_ACTIVE_200_GET);

        //check comment field is hidden
        ViewActionUtils.waitFor("new status", 3000);
        assertFalse(exists(withId(R.id.comment)));

        removeRequests(RequestType.RIDE_ACTIVE_200_GET);
        // cancel ride from driver side
        mockRequests(RequestType.RIDE_DRIVER_CANCELLED_200_GET);
        // check that it was cancelled
        waitForViewInWindow(onView(withText(R.string.message_ride_cancelled_by_driver)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    /**
     * No test case id in this suite https://testrail.devfactory.com//index.php?/suites/view/6450
     */
    @Test
    public void addCommentWithSpecialCharactersAndEmojis() {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        // type comment
        onView(withId(R.id.comment)).perform(replaceText(COMMENT_1_EMOJI), pressImeActionButton());

        Ride ride = getResponse("RIDE_DRIVER_REACHED_WITH_DESTINATION_200", Ride.class);
        ride.setComment(COMMENT_1_EMOJI);

        mockRequest(RequestType.RIDE_DRIVER_REACHED_WITH_DESTINATION_200_GET, ride);

        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        // verify comment
        onView(allOf(withId(R.id.comment), withText(COMMENT_1_EMOJI))).check(matches(isDisplayed()));

        removeRequests(RequestType.RIDE_DRIVER_REACHED_WITH_DESTINATION_200_GET);
        // cancel ride from driver side
        mockRequests(RequestType.RIDE_DRIVER_CANCELLED_200_GET);
        // check that it was cancelled
        waitForViewInWindow(onView(withText(R.string.message_ride_cancelled_by_driver)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    @Test
    @TestCases("C1930704")
    public void addUpdateCommentWhenDriverHasCancelledTheRideAfterArriving() throws InterruptedException {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        // type comment
        onView(withId(R.id.comment)).perform(replaceText(COMMENT_1), pressImeActionButton());

        mockRequests(RequestType.RIDE_DRIVER_REACHED_WITH_DESTINATION_200_GET);

        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        // verify comment
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));

        waitForViewInWindow(allOf(withText(R.string.driver_reached_notification_msg), isDisplayed()));
        //close driver arrived dialog
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        setNetworkError(true);
        DeviceTestUtils.setAirplaneMode(true);
        waitForDisplayed(R.id.error_panel);

        // change comment
        onView(withId(R.id.comment)).perform(clearText());
        onView(withId(R.id.comment)).perform(typeText(COMMENT_2), pressImeActionButton());

        removeRequests(RequestType.RIDE_DRIVER_REACHED_WITH_DESTINATION_200_GET);
        // cancel ride from driver side
        mockRequests(RequestType.RIDE_DRIVER_CANCELLED_200_GET, RequestType.CURRENT_RIDE_EMPTY_200_GET);
        // check that it was cancelled

        DeviceTestUtils.setAirplaneMode(false);
        Matchers.waitFor(condition("Yellow bar should disappear")
                .withMatcher(withId(R.id.error_panel))
                .withCheck(not(isDisplayed())));
        setNetworkError(false);

        waitForViewInWindow(onView(withText(R.string.message_ride_cancelled_by_driver)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    @Test
    @TestCases("C1930705")
    public void addUpdateCommentWhenDriverHasCompletedTheRide() throws InterruptedException {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        // type comment
        onView(withId(R.id.comment)).perform(replaceText(COMMENT_1), pressImeActionButton());

        mockRequests(RequestType.RIDE_ACTIVE_200_GET);

        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        // verify comment
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));

        //verify is in ride
        waitForDisplayed(R.id.ride_details);

        setNetworkError(true);
        DeviceTestUtils.setAirplaneMode(true);
        waitForDisplayed(R.id.error_panel);

        //verify comment is not visible
        onView(withId(R.id.comment)).check(matches(not(isDisplayed())));

        //complete ride
        Ride ride = getResponse("RIDE_ACTIVE_200", Ride.class);
        ride.setStatus("COMPLETED");
        removeRequests(RequestType.RIDE_ACTIVE_200_GET);
        mockRequest(RequestType.RIDE_ACTIVE_200_GET, ride);

        DeviceTestUtils.setAirplaneMode(false);
        Matchers.waitFor(condition("Yellow bar should disappear")
                .withMatcher(withId(R.id.error_panel))
                .withCheck(not(isDisplayed())));
        setNetworkError(false);

        waitForViewInWindow(onView(withId(R.id.rate_driver_container)));
    }

    @Test
    @TestCases("C1930652")
    public void commentFieldRecovery() throws UiObjectNotFoundException, InterruptedException, RemoteException {
        startWithoutRide();
        setupPickupLocationAndValidate();
        setupDestinationAndValidate();

        // add comment
        onView(withId(R.id.comment)).perform(typeText(COMMENT_1), pressImeActionButton());

        doRecoveryAndValidate();

        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());

        //verify is pending request
        waitForDisplayed(R.id.cancel_pending_request);

        // verify request send
        verifyRequest(RequestType.RIDE_REQUEST_WITH_DESTINATION_200_POST, hasQueryParam("comment", equalTo(COMMENT_1)));

        mockRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_200_GET);

        doRecoveryAndValidate();

        //verify is in ride
        waitForDisplayed(R.id.ride_details);

        doRecoveryAndValidate();

        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_200_GET);
        mockRequests(RequestType.RIDE_DRIVER_REACHED_WITH_DESTINATION_200_GET);

        waitForViewInWindow(allOf(withText(R.string.driver_reached_notification_msg), isDisplayed()));
        //close driver arrived dialog
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        doRecoveryAndValidate();

        removeRequests(RequestType.RIDE_DRIVER_REACHED_WITH_DESTINATION_200_GET);
        mockRequests(RequestType.RIDE_ACTIVE_200_GET);

        //check comment field is hidden
        ViewActionUtils.waitFor("ride status update", 3000);
        assertFalse(exists(withId(R.id.comment)));


        removeRequests(RequestType.RIDE_ACTIVE_200_GET);
        // cancel ride from driver side
        mockRequests(RequestType.RIDE_DRIVER_CANCELLED_200_GET);
        // check that it was cancelled
        waitForViewInWindow(onView(withText(R.string.message_ride_cancelled_by_driver)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    @Test
    @TestCases("C1930638")
    public void shouldNotAffectOtherElements() throws UiObjectNotFoundException, InterruptedException, RemoteException {
        startWithoutRide(true);

        setupPickupLocationAndValidate();

        // check comment field
        onView(withId(R.id.comment)).check(matches(allOf(isCompletelyDisplayed(), isClickable())));

        setupDestinationAndValidate();

        // check comment field
        onView(withId(R.id.comment)).check(matches(allOf(isCompletelyDisplayed(), isClickable())));

        onView(withId(R.id.destination_address)).perform(click());
        waitForDisplayed(R.id.addressInput);

        DataInteraction recent = onData(hasToString(containsString("name 10")))
                .inAdapterView(withId(R.id.listView));
        Matchers.waitFor(condition("Wait for recent place").withData(recent));
        recent.perform(click());

        // check comment field
        onView(withId(R.id.comment)).check(matches(allOf(isCompletelyDisplayed(), isClickable())));

        // simulate assigned state
        // Request Ride
        onView(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride))).perform(click());
        waitForDisplayed(R.id.cancel_pending_request);
        mockRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_200_GET);


        // tap on driver image should cause panel expand
        waitForDisplayed(R.id.ride_details);
        waitFor("animation", 1000);
        onView(withId(R.id.driver_container_small)).perform(swipeUp());
        waitForCompletelyDisplayed(R.id.ride_details);

        // TODO: FareSplit is moved to another screen
//        // request split fare
//        onView(allOf(withId(R.id.btn_fare_split), withText(R.string.btn_split_fare), isEnabled())).perform(click());
//        waitForDisplayed(R.id.et_phone_number);
//
//        // send fare split request
//        removeRequests(RequestType.SPLIT_FARE_EMPTY_200_GET);
//        mockRequests(RequestType.SPLIT_FARE_REQUEST_200_POST,
//                RequestType.SPLIT_FARE_REQUESTED_200_GET);
//
//        onView(withId(R.id.et_phone_number)).perform(typeText("+12345678900"), closeSoftKeyboard());
//        onView(withId(R.id.btn_send)).perform(click());
//        waitForDisplayed(R.id.tvsplit_fare_header_label);
//
//        // check comment field
//        onView(withId(R.id.comment)).check(matches(allOf(isCompletelyDisplayed(), isClickable())));
//
//        //expand split fare
//        onView(withId(R.id.tvsplit_fare_header_label)).perform(click());
//
//        // check comment field
//        onView(withId(R.id.comment)).check(matches(allOf(isCompletelyDisplayed(), isClickable())));
//
//        //remove split fare
//        onView(withId(R.id.im_delete_request)).perform(click());
//        waitForViewInWindow(onView(withText(getAppContext().getString(R.string.fare_split_delete_rider, "Serg Rider"))));
//
//        removeRequests(RequestType.SPLIT_FARE_REQUEST_200_POST,
//                RequestType.SPLIT_FARE_REQUESTED_200_GET);
//        mockRequests(RequestType.SPLIT_FARE_EMPTY_200_GET);
//
//        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // check comment field
        onView(withId(R.id.comment)).check(matches(allOf(isCompletelyDisplayed(), isClickable())));

        // cancel ride
        removeRequests(RequestType.RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_200_GET);
        // cancel ride from driver side
        mockRequests(RequestType.RIDE_DRIVER_CANCELLED_200_GET);
        // check that it was cancelled
        waitForViewInWindow(onView(withText(R.string.message_ride_cancelled_by_driver)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    private void doRecoveryAndValidate() throws InterruptedException, RemoteException, UiObjectNotFoundException {
        setNetworkError(true);
        DeviceTestUtils.setAirplaneMode(true);
        waitForDisplayed(R.id.error_panel);

        DeviceTestUtils.setAirplaneMode(false);
        Matchers.waitFor(condition("Yellow bar should disappear")
                .withMatcher(withId(R.id.error_panel))
                .withCheck(not(isDisplayed())));
        setNetworkError(false);

        // verify comment value
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        // verify comment value
        onView(allOf(withId(R.id.comment), withText(COMMENT_1))).check(matches(isDisplayed()));
    }

    private void setupDestinationAndValidate() {
        // set destination
        onView(withId(R.id.destination_address)).perform(click());
        waitForDisplayed(R.id.addressInput);
        onView(withId(R.id.addressInput)).perform(replaceText("11600 Research Blvd, Austin, Texas"), closeSoftKeyboard());

        DataInteraction prediction = onData(hasToString(containsString("11600 Research Blvd")))
                .atPosition(0)
                .inAdapterView(withId(R.id.listView));
        try {
            // TODO: better to throw here, but don't want to change signatures of all callers
            Matchers.waitFor(condition("Wait for prediction").withData(prediction));
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }

        prediction.perform(click());

        assertDestinationMarkersVisible(2000);

        //check request panel
        onView(withId(R.id.requestPanel)).check(matches(isDisplayed()));
    }

    private void startWithoutRide() {
        startWithoutRide(false);
    }

    private void startWithoutRide(boolean shouldMockRecentPlaces) {
        NavigationUtils.startActivity(activityRule);

        if (shouldMockRecentPlaces) {
            RecentPlacesUtils.mockRecentPlacesFullList(1443);
        }

        NavigationUtils.throughLogin("lord.vader@rider.com", "secret");

        tryToCloseAnyVisibleDialog();
        // Map screen is opened
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // Pick-up location is automatically set to the address that corresponds to the rider's GPS location
        waitForNotEmptyText(R.id.pickup_address);

        // "SET PICKUP LOCATION" pin is shown
        waitForDisplayed(R.id.set_pickup_location);

        // Driver's car is visible on the screen
        assertCarMarkersVisible(5000);

        // Car types slider is displayed in the bottom and 'REGULAR' is selected by default
        waitForDisplayed(R.id.car_types_slider);
        onView(allOf(withId(R.id.car_types_slider), isDisplayed())).check(matches(carTypeSelected(0)));
    }

    private void setupPickupLocationAndValidate() {
        // check fields
        onView(withId(R.id.pickup_address)).check(matches(isDisplayed()));
        onView(withId(R.id.destination_address)).check(matches(isDisplayed()));
        onView(withId(R.id.comment)).check(matches(not(isDisplayed())));

        // Tap pin to set pick up location
        onView(allOf(withId(R.id.pickup_location), isDisplayed())).perform(click());

        // Request panel appears
        waitForDisplayed(R.id.requestPanel);

        // check pickup marker
        assertPickupMarkersVisible();

        // check fields
        onView(withId(R.id.pickup_address)).check(matches(isDisplayed()));
        onView(withId(R.id.destination_address)).check(matches(isDisplayed()));
        waitFor(allOf(withId(R.id.comment), withHint(R.string.comment_hint), withText(""), isDisplayed()), "comments", 2000);
    }

    private void tryToCloseAnyVisibleDialog() {
        try {
            onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        } catch (Exception e) {
            // ignore
        }
    }

    private void tryToClosePrediction() {
        try {
            waitFor("predictions", 2000);
            onView(withId(R.id.app_title_logo)).perform(click());
        } catch (Exception e) {
            // ignore
        }
    }
}
