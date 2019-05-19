package cases.ra_9534_ride_comments;

import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;

import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Reference: RA-10830
 * Created by Sergey Petrov on 31/05/2017.
 */

public class RideCommentSuite1 extends BaseUITest {

    private static String COMMENT = "Ride comment";
    private static String COMMENT_UPDATED = "Ride comment updated";

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
                RequestType.DRIVER_TYPES_200_GET);
    }

    /**
     * When Ride has comment, show Comment after Driver accepted
     * Verify when requested ride contains Rider's comment, Driver is able to see it on screen.
     * Reference: RA-10833
     */
    @Test
    @TestCases("C1929269")
    public void commentIsShownInAcceptedState() throws InterruptedException {
        //------------------------------------------------------------------------------------------
        // Receive and accept ride with a comment
        //------------------------------------------------------------------------------------------

        receiveRideRequest(COMMENT);
        acceptRideRequest(COMMENT);

        //------------------------------------------------------------------------------------------
        // Check comment
        //------------------------------------------------------------------------------------------

        waitForDisplayed(R.id.comments_container);
        onView(allOf(withId(R.id.comment), isDisplayed())).check(matches(withText(COMMENT)));
    }

    /**
     * Receive new comment when Driver is on the way
     * Verify when Rider add a comment when Driver is on the way, relevant notification is received and Driver is able to see Rider's comment correctly.
     * Reference: RA-10834
     */
    @Test
    @TestCases("C1929306")
    public void commentReceivedInAcceptedState() throws InterruptedException {

        //------------------------------------------------------------------------------------------
        // Receive and accept ride without a comment
        //------------------------------------------------------------------------------------------

        receiveRideRequest(null);
        acceptRideRequest(null);

        // check there is no comment
        waitFor("Should have no comment", 5000);
        onView(withId(R.id.comments_container)).check(matches(not(isDisplayed())));

        //------------------------------------------------------------------------------------------
        // Rider updated comment
        //------------------------------------------------------------------------------------------

        mockUpdateCommentEvent(RequestType.EVENT_RIDE_DRIVER_ASSIGNED, "EVENT_RIDE_DRIVER_ASSIGNED", COMMENT);

        // check comment
        waitForDisplayed(R.id.comments_container);
        onView(allOf(withId(R.id.comment), isDisplayed())).check(matches(withText(COMMENT)));
    }

    /**
     * Receive new comment when Driver is arrived
     * Verify when Rider add a comment when Driver has arrived, relevant notification is received and Driver is able to see Rider's comment correctly.
     * Reference: RA-10835
     */
    @Test
    @TestCases("C1929331")
    public void commentReceivedInArrivedState() throws InterruptedException {

        //------------------------------------------------------------------------------------------
        // Receive, accept ride without a comment, arrive
        //------------------------------------------------------------------------------------------

        receiveRideRequest(null);
        acceptRideRequest(null);
        arrive(null);

        // check there is no comment
        waitFor("Should have no comment", 5000);
        onView(withId(R.id.comments_container)).check(matches(not(isDisplayed())));

        //------------------------------------------------------------------------------------------
        // Rider updated comment
        //------------------------------------------------------------------------------------------

        mockUpdateCommentEvent(RequestType.EVENT_RIDE_DRIVER_REACHED, "EVENT_RIDE_DRIVER_REACHED", COMMENT);

        // check comment
        waitForDisplayed(R.id.comments_container);
        onView(allOf(withId(R.id.comment), isDisplayed())).check(matches(withText(COMMENT)));
    }

    /**
     * Comment should be hidden once ride has started
     * Verify Comment should be hidden once ride has started.
     * Reference: RA-10836
     */
    @Test
    @TestCases("C1929350")
    public void commentHiddenInStartedState() throws InterruptedException {

        //------------------------------------------------------------------------------------------
        // Accept ride with comment
        //------------------------------------------------------------------------------------------

        commentIsShownInAcceptedState();

        //------------------------------------------------------------------------------------------
        // Arrive, check comment
        //------------------------------------------------------------------------------------------

        arrive(COMMENT);
        waitForDisplayed(R.id.comments_container);
        onView(allOf(withId(R.id.comment), isDisplayed())).check(matches(withText(COMMENT)));

        //------------------------------------------------------------------------------------------
        // Start trip, check comment is hidden and not updated
        //------------------------------------------------------------------------------------------

        startTrip(COMMENT);
        // check there is no comment
        onView(withId(R.id.comments_container)).check(matches(not(isDisplayed())));

        mockUpdateCommentEvent(RequestType.EVENT_RIDE_ACTIVE, "EVENT_RIDE_ACTIVE", COMMENT_UPDATED);

        // check there is no comment
        waitFor("Should have no comment", 5000);
        onView(withId(R.id.comments_container)).check(matches(not(isDisplayed())));
    }

    /**
     * Should not show comment when Rider sent new comment after Driver has started the ride
     * Verify when Rider add a comment after Driver has started the ride, Driver app should not show comment.
     * Reference: RA-10837
     */
    @Test
    @TestCases("C1929351")
    public void commentNotReceivedInStartedState() throws InterruptedException {

        //------------------------------------------------------------------------------------------
        // Start trip without comment
        //------------------------------------------------------------------------------------------

        receiveRideRequest(null);
        acceptRideRequest(null);
        arrive(null);
        startTrip(null);

        //------------------------------------------------------------------------------------------
        // Rider updated comment
        //------------------------------------------------------------------------------------------

        mockUpdateCommentEvent(RequestType.EVENT_RIDE_DRIVER_REACHED, "EVENT_RIDE_DRIVER_REACHED", COMMENT);

        // check there is no comment
        waitFor("Should have no comment", 5000);
        onView(withId(R.id.comments_container)).check(matches(not(isDisplayed())));
    }

    /**
     * Receive updated comment when Driver is on the way
     * Verify when Rider updates comment when Driver is on the way, relevant notification is received and Driver is able to see Rider's updated comment correctly.
     * Reference: RA-10838
     */
    @Test
    @TestCases("C1929307")
    public void commentUpdatedInAcceptedState() throws InterruptedException {

        //------------------------------------------------------------------------------------------
        // Receive and accept ride with comment
        //------------------------------------------------------------------------------------------

        receiveRideRequest(COMMENT);
        acceptRideRequest(COMMENT);

        // check comment
        waitForDisplayed(R.id.comments_container);
        onView(allOf(withId(R.id.comment), isDisplayed())).check(matches(withText(COMMENT)));


//        waitFor("", 5000);

        //------------------------------------------------------------------------------------------
        // Rider updated comment
        //------------------------------------------------------------------------------------------

        mockUpdateCommentEvent(RequestType.EVENT_RIDE_DRIVER_ASSIGNED, "EVENT_RIDE_DRIVER_ASSIGNED", COMMENT_UPDATED);

        // comment should update
        waitForDisplayed(allOf(withId(R.id.comment), withText(COMMENT_UPDATED)), "Comment updated");
    }

    /**
     * Receive updated comment when Driver is arrived
     * Verify when Rider updates comment when Driver has arrived, relevant notification is received and Driver is able to see Rider's updated comment correctly.
     * Reference: RA-10839
     */
    @Test
    @TestCases("C1929332")
    public void commentUpdatedInArrivedState() throws InterruptedException {

        //------------------------------------------------------------------------------------------
        // Receive, accept ride with comment, arrive
        //------------------------------------------------------------------------------------------

        receiveRideRequest(COMMENT);
        acceptRideRequest(COMMENT);
        arrive(COMMENT);

        // check comment
        waitForDisplayed(R.id.comments_container);
        onView(allOf(withId(R.id.comment), isDisplayed())).check(matches(withText(COMMENT)));

        //------------------------------------------------------------------------------------------
        // Rider updated comment
        //------------------------------------------------------------------------------------------

        mockUpdateCommentEvent(RequestType.EVENT_RIDE_DRIVER_REACHED, "EVENT_RIDE_DRIVER_REACHED", COMMENT_UPDATED);

        // comment should update
        waitForDisplayed(allOf(withId(R.id.comment), withText(COMMENT_UPDATED)), "Comment updated");

    }

    /**
     * Comment is still displayed after Rider has added/updated destination
     * Verify when destination is added/updated, comment is still displayed correctly.
     */
    @Test
    @TestCases("C1929308")
    public void commentNotAffectedByDestinationChange() throws InterruptedException {

        //------------------------------------------------------------------------------------------
        // Receive and accept ride without a comment
        //------------------------------------------------------------------------------------------

        receiveRideRequest(null);
        acceptRideRequest(null);

        // check there is no comment
        waitFor("Should have no comment", 5000);
        onView(withId(R.id.comments_container)).check(matches(not(isDisplayed())));

        //------------------------------------------------------------------------------------------
        // Rider updated comment
        //------------------------------------------------------------------------------------------

        mockUpdateCommentEvent(RequestType.EVENT_RIDE_DRIVER_ASSIGNED, "EVENT_RIDE_DRIVER_ASSIGNED", COMMENT);

        // check comment
        waitForDisplayed(R.id.comments_container);
        onView(allOf(withId(R.id.comment), isDisplayed())).check(matches(withText(COMMENT)));

        //------------------------------------------------------------------------------------------
        // Rider updated destination
        //------------------------------------------------------------------------------------------

        // check destination not visible
        onView(withId(R.id.finish_address)).check(matches(not(isDisplayed())));

        mockUpdateCommentEvent(RequestType.EVENT_RIDE_DRIVER_ASSIGNED, "EVENT_RIDE_ACTIVE", COMMENT);

        waitFor("Should have no destination", 5000);

        // check destination still not visible
        onView(withId(R.id.finish_address)).check(matches(not(isDisplayed())));

        // comment is the same
        onView(allOf(withId(R.id.comment), isDisplayed())).check(matches(withText(COMMENT)));

        //------------------------------------------------------------------------------------------
        // Arrive
        //------------------------------------------------------------------------------------------

        arrive(COMMENT);

        mockEvent(RequestType.EVENT_RIDE_DRIVER_REACHED, "EVENT_RIDE_DRIVER_REACHED", response -> {
            Ride ride = getResponse("RIDE_DRIVER_REACHED_200", Ride.class);
            ride.setComment(COMMENT);
            response.setRide(ride);
            return response;
        });

        // check comment
        waitForDisplayed(R.id.comments_container);
        onView(allOf(withId(R.id.comment), isDisplayed())).check(matches(withText(COMMENT)));

        // check destination is empty (EVENT_RIDE_DRIVER_REACHED has no destination)
        waitForDisplayed(allOf(withId(R.id.finish_address), withText("")), "Should have no destination");

        //------------------------------------------------------------------------------------------
        // Rider updated destination
        //------------------------------------------------------------------------------------------

        atomicOnRequests(() -> {
            removeRequests(RequestType.RIDE_DRIVER_REACHED_200_GET,
                    RequestType.EVENT_RIDE_DRIVER_REACHED);
            // generate destination updated event (RIDE_ACTIVE has destination)
            mockRide(RequestType.RIDE_DRIVER_REACHED_200_GET, "RIDE_ACTIVE_200", RideStatus.DRIVER_REACHED, COMMENT);
            mockUpdateLocationEvent(RequestType.EVENT_RIDE_DRIVER_REACHED, "EVENT_RIDE_ACTIVE", COMMENT);

        });

        waitForDisplayed(allOf(withId(R.id.finish_address), withText("Austin-Bergstrom International Airport")), "Should have destination");

        // comment is the same
        onView(allOf(withId(R.id.comment), isDisplayed())).check(matches(withText(COMMENT)));

    }

    private void receiveRideRequest(@Nullable String comment) {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "password");

        waitFor("camera", 2000);

        //------------------------------------------------------------------------------------------
        // Go online
        //------------------------------------------------------------------------------------------

        removeRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
        mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.DRIVERS_CARTYPES_200_GET);

        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).perform(click());
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline))).check(matches(isDisplayed()));

        //------------------------------------------------------------------------------------------
        // Receive ride request
        //------------------------------------------------------------------------------------------

        Ride ride = null;
        if (comment != null) {
            ride = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
            ride.setComment(comment);
        }
        NavigationUtils.toRequestedState(this, ride);
    }

    public void acceptRideRequest(@Nullable String comment) throws InterruptedException {
        Ride ride = getResponse("RIDE_DRIVER_ASSIGNED_200", Ride.class);
        ride.setComment(comment);
        NavigationUtils.acceptRideRequest(this, ride);
    }

    private void arrive(@Nullable String comment) throws InterruptedException {
        Ride ride = getResponse("RIDE_DRIVER_REACHED_200", Ride.class);
        ride.setComment(comment);
        NavigationUtils.arrive(this, ride);
    }

    private void startTrip(@Nullable String comment) throws InterruptedException {
        Ride ride = getResponse("RIDE_ACTIVE_200", Ride.class);
        ride.setComment(comment);
        NavigationUtils.startTrip(this, ride);
    }

    private void mockUpdateCommentEvent(RequestType eventType, String resourceName, String comment) {
        mockEvent(eventType, resourceName, response -> {
            response.setEventType(RideStatus.RIDER_COMMENT_UPDATED);
            response.getRide().setComment(comment);
            return response;
        });
    }

    private void mockUpdateLocationEvent(RequestType eventType, String resourceName, String comment) {
        mockEvent(eventType, resourceName, response -> {
            response.setEventType(RideStatus.END_LOCATION_UPDATED);
            response.getRide().setComment(comment);
            return response;
        });
    }

    private void mockRide(RequestType requestType, String resourceName, RideStatus status, String comment) {
        Ride ride = getResponse(resourceName, Ride.class);
        ride.setStatus(status.name());
        ride.setComment(comment);
        mockRequest(requestType, ride);
    }
}
