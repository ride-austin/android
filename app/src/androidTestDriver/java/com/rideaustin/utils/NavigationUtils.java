package com.rideaustin.utils;

import android.support.annotation.Nullable;

import com.rideaustin.MockDelegate;
import com.rideaustin.R;
import com.rideaustin.RequestType;
import com.rideaustin.RequestWrapper;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideRequestParams;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.helpers.MockHelper;

import org.hamcrest.core.AllOf;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.DeviceTestUtils.search;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.setRating;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by Sergey Petrov on 23/05/2017.
 */

public class NavigationUtils extends CommonNavigationUtils {

    public static void goOnline(MockDelegate mockDelegate) throws InterruptedException {
        waitFor(condition("Should be offline")
                .withMatcher(withId(R.id.toolbarActionButton))
                .withMatcher(withText(R.string.action_go_online)));

        mockDelegate.atomicOnRequests(() -> {
            clearOnlineOfflineState(mockDelegate);
            mockDelegate.mockRequests(RequestType.DRIVER_GO_ONLINE_200_POST,
                    RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET,
                    RequestType.DRIVER_UPDATE_LOCATION_200_PUT);
        });

        onView(withId(R.id.toolbarActionButton)).perform(click());

        waitFor(condition("Should become online")
                .withMatcher(withId(R.id.toolbarActionButton))
                .withMatcher(withText(R.string.action_go_offline)));
    }

    public static void goOffline(MockDelegate mockDelegate) throws InterruptedException {
        waitFor(condition("Should be online")
                .withMatcher(withId(R.id.toolbarActionButton))
                .withMatcher(withText(R.string.action_go_offline)));

        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            clearOnlineOfflineState(mockDelegate);
            mockDelegate.mockRequests(RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                    RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
        });

        onView(withId(R.id.toolbarActionButton)).perform(click());

        waitFor(condition("Should become offline")
                .withMatcher(withId(R.id.toolbarActionButton))
                .withMatcher(withText(R.string.action_go_online)));
    }

    public static void toEmptyState(MockDelegate mockDelegate) {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.mockRequests(RequestType.EVENTS_EMPTY_200_GET,
                    RequestType.RIDE_400_GET);
        });
    }

    public static void toRequestedState(MockDelegate mockDelegate, @Nullable Ride ride) {
        toRequestedState(mockDelegate, ride, null);
    }

    public static void toRequestedState(MockDelegate mockDelegate, @Nullable Ride ride, @Nullable RideRequestParams requestParams) {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET,
                    RequestType.ACKNOWLEDGE_RIDE_200_POST);

            if (ride == null) {
                mockDelegate.mockRequests(RequestType.RIDE_REQUESTED_200_GET);
            } else {
                ride.setStatus(RideStatus.REQUESTED.name());
                mockDelegate.mockRequest(RequestType.RIDE_REQUESTED_200_GET, ride);
            }

            mockDelegate.mockEvent(RequestType.EVENT_RIDE_REQUESTED, "EVENT_RIDE_REQUESTED", response -> {
                if (requestParams != null) {
                    response.setParameters(SerializationHelper.serialize(requestParams));
                }
                if (ride != null) {
                    response.setRide(ride);
                }
                return response;
            });
        });

        // Using "search()" instead of "waitForDisplayed"
        // to properly test requests arriving when app is in background
        search().id(R.id.pending_pickup).assertExist(10000L);
    }

    public static void toNextRideRequestedState(MockDelegate mockDelegate, @Nullable Ride currentRide, Ride nextRide) {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.mockRequests(RequestType.ACKNOWLEDGE_RIDE_200_POST);

            if (currentRide != null) {
                currentRide.setId(1L);
                currentRide.setStatus(RideStatus.ACTIVE.toString());
                mockDelegate.mockRequest(RequestType.RIDE_200_GET_ID_1, currentRide);

                ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, currentRide, mockDelegate);
                activeDriver.getRide().setNextRide(nextRide);
                mockDelegate.mockRequest(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, activeDriver);
            } else {
                mockDelegate.mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
            }

            nextRide.setId(2L);
            nextRide.setStatus(RideStatus.REQUESTED.toString());
            mockDelegate.mockRequest(RequestType.RIDE_200_GET_ID_2, nextRide);

            mockDelegate.mockEvent(RequestType.EVENT_RIDE_REQUESTED, "EVENT_RIDE_REQUESTED", response -> {
                RideRequestParams params = new RideRequestParams();
                long now = TimeUtils.currentTimeMillis();
                params.setAcceptanceExpiration(now + 10000L);
                response.setParameters(SerializationHelper.serialize(params));
                response.setRide(null);
                response.setNextRide(nextRide);
                return response;
            });
        });

        // Using "search()" instead of "waitForDisplayed"
        // to properly test requests arriving when app is in background
        search().id(R.id.pending_pickup).assertExist(10000L);
    }

    public static void acceptRideRequest(MockDelegate mockDelegate, @Nullable Ride ride) throws InterruptedException {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.mockRequests(RequestType.ACCEPT_RIDE_200_POST,
                    RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET);

            if (ride == null) {
                mockDelegate.mockRequests(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET,
                        RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                        RequestType.EVENTS_EMPTY_200_GET);
            } else {
                ride.setStatus(RideStatus.DRIVER_ASSIGNED.name());
                ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, ride, mockDelegate);
                mockDelegate.mockRequest(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, activeDriver);
                mockDelegate.mockRequest(RequestType.RIDE_DRIVER_ASSIGNED_200_GET, ride);
                mockDelegate.mockRequests(RequestType.EVENTS_EMPTY_200_GET);
            }
        });
        onView(withId(R.id.pending_pickup)).perform(click());

        waitFor(condition().withMatcher(withId(R.id.pickup_destination_card)));
        onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive))).check(matches(isDisplayed()));
    }

    public static void acceptNextRideRequest(MockDelegate mockDelegate, @Nullable Ride currentRide, Ride nextRide) throws InterruptedException {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.mockRequests(RequestType.ACCEPT_RIDE_200_POST,
                    RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                    RequestType.EVENTS_EMPTY_200_GET);

            if (currentRide != null) {
                currentRide.setId(1L);
                currentRide.setStatus(RideStatus.ACTIVE.toString());
                mockDelegate.mockRequest(RequestType.RIDE_200_GET_ID_1, currentRide);
                ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, currentRide, mockDelegate);
                activeDriver.getRide().setNextRide(nextRide);
                mockDelegate.mockRequest(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, activeDriver);

                nextRide.setId(2L);
                nextRide.setStatus(RideStatus.DRIVER_ASSIGNED.toString());
                mockDelegate.mockRequest(RequestType.RIDE_200_GET_ID_2, nextRide);

            } else {
                nextRide.setStatus(RideStatus.DRIVER_ASSIGNED.toString());
                ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, nextRide, mockDelegate);
                mockDelegate.mockRequest(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, activeDriver);
            }

        });
        onView(withId(R.id.pending_pickup)).perform(click());

        if (currentRide != null) {
            waitFor(condition("Should show pickup/destination").withMatcher(withId(R.id.pickup_destination_card)));
            onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).check(matches(isDisplayed()));

            waitFor(condition("Should show next ride button")
                    .withMatcher(withId(R.id.next))
                    .withMatcher(withText(R.string.stacked_ride_next_ride)));
        }
    }

    public static void arrive(MockDelegate mockDelegate, @Nullable Ride ride) throws InterruptedException {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.mockRequests(RequestType.REACH_RIDE_200_POST,
                    RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET);
            if (ride == null) {
                mockDelegate.mockRequests(RequestType.ACTIVE_DRIVER_REACHED_200_GET,
                        RequestType.RIDE_DRIVER_REACHED_200_GET,
                        RequestType.EVENTS_EMPTY_200_GET);
            } else {
                ride.setStatus(RideStatus.DRIVER_REACHED.name());
                ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_REACHED_200_GET, ride, mockDelegate);
                mockDelegate.mockRequest(RequestType.ACTIVE_DRIVER_REACHED_200_GET, activeDriver);
                mockDelegate.mockRequest(RequestType.RIDE_DRIVER_REACHED_200_GET, ride);
                mockDelegate.mockRequests(RequestType.EVENTS_EMPTY_200_GET);
            }
        });
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive))).perform(swipeRight());
        waitFor(condition("Wait for start trip button")
                .withMatcher(withId(R.id.sliderText))
                .withMatcher(withText(R.string.slide_to_start_trip)));
    }

    public static void startTrip(MockDelegate mockDelegate, @Nullable Ride ride) throws InterruptedException {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.mockRequests(RequestType.START_RIDE_200_POST,
                    RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                    RequestType.EVENTS_EMPTY_200_GET);
            mockDelegate.delayRequest(RequestType.EVENTS_EMPTY_200_GET, 2000L);
            if (ride == null) {
                mockDelegate.mockRequests(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET,
                        RequestType.RIDE_ACTIVE_200_GET);
            } else {
                ride.setStatus(RideStatus.ACTIVE.name());
                ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, ride, mockDelegate);
                mockDelegate.mockRequest(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET, activeDriver);
                mockDelegate.mockRequest(RequestType.RIDE_ACTIVE_200_GET, ride);
            }
        });
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_start_trip))).perform(swipeRight());
        waitForViewInWindow(withText(R.string.starting_trip_confirmation_dialog_msg));
        ViewActionUtils.waitFor("", 1000);
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        waitFor(condition("Wait for finish trip button")
                .withMatcher(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))));
    }

    public static void endTrip(MockDelegate mockDelegate, @Nullable Ride ride) throws InterruptedException {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.mockRequests(RequestType.EVENTS_EMPTY_200_GET,
                    RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET);
            mockDelegate.delayRequest(RequestType.EVENTS_EMPTY_200_GET, 2000L);

            if (ride == null) {
                mockDelegate.chainRequests(RequestType.END_RIDE_200_POST,
                        RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
            } else {
                ride.setStatus(RideStatus.COMPLETED.name());
                mockDelegate.chainRequests(RequestWrapper.wrap(RequestType.END_RIDE_200_POST, ride),
                        RequestWrapper.wrap(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET));
            }
        });
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).perform(swipeRight());
        waitForViewInWindow(withText(R.string.ending_trip_confirmation_dialog_msg));
        ViewActionUtils.waitFor("", 1000);
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        waitFor(condition("Wait for rate rider")
                .withMatcher(withId(R.id.rb_rate_driver)));
    }

    public static void endTripWithoutInternet(MockDelegate mockDelegate, @Nullable Ride ride) throws InterruptedException {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.mockRequests(RequestType.EVENTS_EMPTY_200_GET,
                    RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
            mockDelegate.delayRequest(RequestType.EVENTS_EMPTY_200_GET, 2000L);
            if (ride == null) {
                mockDelegate.mockRequests(RequestType.END_RIDE_200_POST);
            } else {
                ride.setStatus(RideStatus.COMPLETED.name());
                mockDelegate.mockRequest(RequestType.END_RIDE_200_POST, ride);
            }
        });
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).perform(swipeRight());
        waitForViewInWindow(withText(R.string.ending_trip_confirmation_dialog_msg));
        ViewActionUtils.waitFor("", 1000);
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        waitFor(condition("Should switch to offline")
                .withMatcher(withId(R.id.toolbarActionButton))
                .withMatcher(withText(R.string.action_go_online)));
    }

    public static void endTripWithNextRide(MockDelegate mockDelegate, Ride currentRide, Ride nextRide) throws InterruptedException {
        endTripWithNextRide(mockDelegate, currentRide, nextRide, () -> {
            waitFor(condition("Wait for rate rider")
                    .withMatcher(withId(R.id.rb_rate_driver)));
        });
    }

    public static void endTripWithNextRide(MockDelegate mockDelegate, Ride currentRide, Ride nextRide, Interruptable afterAction) throws InterruptedException {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);

            currentRide.setId(1L);
            currentRide.setStatus(RideStatus.COMPLETED.toString());
            mockDelegate.mockRequest(RequestType.END_RIDE_200_POST, currentRide);
            mockDelegate.mockRequest(RequestType.RIDE_200_GET_ID_1, currentRide);

            nextRide.setId(2L);
            nextRide.setStatus(RideStatus.DRIVER_ASSIGNED.toString());
            mockDelegate.mockRequest(RequestType.RIDE_200_GET_ID_2, nextRide);

            ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, nextRide, mockDelegate);
            mockDelegate.mockRequest(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, activeDriver);

        });
        onView(AllOf.allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).perform(swipeRight());
        waitForViewInWindow(withText(R.string.ending_trip_confirmation_dialog_msg));
        ViewActionUtils.waitFor("", 1000);
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        afterAction.run();
    }

    public static void rateRide(MockDelegate mockDelegate) throws InterruptedException {
        rateRide(mockDelegate, null);
    }

    public static void rateRide(MockDelegate mockDelegate, Ride ride) throws InterruptedException {
        rateRide(mockDelegate, ride, () -> {
            waitFor(condition("Should switch to online state")
                    .withMatcher(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_offline)))
                    .withCheck(allOf(isDisplayed(), isEnabled(), isClickable())));
        });
    }


    public static void rateRide(MockDelegate mockDelegate, @Nullable Ride ride, @Nullable Interruptable afterAction) throws InterruptedException {
        // wait rate driver dialog
        waitForViewInWindow(withId(R.id.rb_rate_driver));

        // set rating
        onView(withId(R.id.rb_rate_driver))
                .check(matches(isEnabled()))
                .check(matches(isCompletelyDisplayed()))
                .perform(setRating(4.0f));

        waitForDisplayed(R.id.btn_submit);

        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.mockRequests(RequestType.RIDE_RATING_200_PUT,
                    RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET,
                    RequestType.EVENTS_EMPTY_200_GET);
            if (ride != null) {
                mockDelegate.mockRequest(RequestType.RIDE_COMPLETED_200_GET, ride);
            } else {
                mockDelegate.mockRequests(RequestType.RIDE_COMPLETED_200_GET);
            }
        });

        // submit
        onView(AllOf.allOf(withId(R.id.btn_submit), isDisplayed())).perform(click());

        if (afterAction != null) {
            afterAction.run();
        }
    }

    public static void rateWithNextRide(MockDelegate mockDelegate, Ride currentRide, Ride nextRide) throws InterruptedException {
        // wait rate driver dialog
        waitForViewInWindow(withId(R.id.rb_rate_driver));

        // set rating
        onView(withId(R.id.rb_rate_driver))
                .check(matches(isEnabled()))
                .check(matches(isCompletelyDisplayed()))
                .perform(setRating(4.0f));

        waitForDisplayed(R.id.btn_submit);

        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);

            mockDelegate.mockRequests(RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                    RequestType.EVENTS_EMPTY_200_GET);

            currentRide.setId(1L);
            currentRide.setStatus(RideStatus.COMPLETED.toString());
            mockDelegate.mockRequest(RequestType.RIDE_RATING_200_PUT, currentRide);
            mockDelegate.mockRequest(RequestType.RIDE_200_GET_ID_1, currentRide);

            nextRide.setId(2L);
            nextRide.setStatus(RideStatus.DRIVER_ASSIGNED.toString());
            mockDelegate.mockRequest(RequestType.RIDE_200_GET_ID_2, nextRide);

            ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, nextRide, mockDelegate);
            mockDelegate.mockRequest(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, activeDriver);
        });

        // submit
        onView(AllOf.allOf(withId(R.id.btn_submit), isDisplayed())).perform(click());

        waitFor(condition("Should switch to driver assigned")
                .withMatcher(allOf(withId(R.id.sliderText), withText(R.string.slide_to_arrive))));
        onView(withId(R.id.pickup_destination_card)).check(matches(isDisplayed()));
    }

    public static void toRiderCancelled(MockDelegate mockDelegate, boolean isOnline) {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.chainRequests(RequestType.EVENT_RIDE_CANCELLED_BY_RIDER, RequestType.EVENTS_EMPTY_200_GET);
            Ride ride = mockDelegate.getResponse("RIDE_ADMIN_CANCELLED_200", Ride.class);
            ride.setStatus(RideStatus.RIDER_CANCELLED.toString());
            mockDelegate.mockRequest(RequestType.RIDE_ADMIN_CANCELLED_200_GET, ride);
            if (isOnline) {
                mockDelegate.mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
            } else {
                mockDelegate.mockRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
            }
        });
    }

    public static void toAdminCancelled(MockDelegate mockDelegate, boolean isOnline) {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.chainRequests(RequestType.EVENT_ADMIN_CANCELLED, RequestType.EVENTS_EMPTY_200_GET);
            mockDelegate.mockRequests(RequestType.RIDE_ADMIN_CANCELLED_200_GET);
            if (isOnline) {
                mockDelegate.mockRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
            } else {
                mockDelegate.mockRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
            }
        });
    }

    public static void toAdminCancelledWithNextRide(MockDelegate mockDelegate, Ride currentRide, Ride nextRide) {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);

            currentRide.setId(1L);
            currentRide.setStatus(RideStatus.ADMIN_CANCELLED.toString());
            mockDelegate.mockRequest(RequestType.RIDE_200_GET_ID_1, currentRide);

            nextRide.setId(2L);
            nextRide.setStatus(RideStatus.DRIVER_ASSIGNED.toString());
            mockDelegate.mockRequest(RequestType.RIDE_200_GET_ID_2, nextRide);

            ActiveDriver activeDriver = MockHelper.getActiveDriverWithRide(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, nextRide, mockDelegate);
            mockDelegate.mockRequest(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET, activeDriver);


            mockDelegate.mockEvent(RequestType.EVENT_ADMIN_CANCELLED, "EVENT_ADMIN_CANCELLED", response -> {
                response.setRide(currentRide);
                return response;
            });
        });
    }

    public static void clearOnlineOfflineState(MockDelegate mockDelegate) {
        mockDelegate.removeRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET,
                RequestType.ACTIVE_DRIVER_EMPTY_200_GET,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.DRIVER_GO_ONLINE_200_POST,
                RequestType.DRIVER_GO_ONLINE_400_POST,
                RequestType.DRIVER_GO_ONLINE_412_POST,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT);
    }

    public static void clearRideState(MockDelegate mockDelegate) {
        mockDelegate.removeRequests(RequestType.EVENTS_EMPTY_200_GET,
                RequestType.EVENT_RIDE_REQUESTED,
                RequestType.EVENT_RIDE_DRIVER_ASSIGNED,
                RequestType.EVENT_RIDE_DRIVER_REACHED,
                RequestType.EVENT_RIDE_ACTIVE,
                RequestType.EVENT_RIDE_COMPLETED,
                RequestType.EVENT_RIDE_CANCELLED_BY_RIDER,
                RequestType.EVENT_ADMIN_CANCELLED,
                RequestType.RIDE_400_GET,
                RequestType.RIDE_REQUESTED_200_GET,
                RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.RIDE_DRIVER_REACHED_200_GET,
                RequestType.RIDE_ACTIVE_200_GET,
                RequestType.RIDE_COMPLETED_200_GET,
                RequestType.ACKNOWLEDGE_RIDE_200_POST,
                RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                RequestType.ACCEPT_RIDE_200_POST,
                RequestType.REACH_RIDE_200_POST,
                RequestType.START_RIDE_200_POST,
                RequestType.END_RIDE_200_POST,
                RequestType.RIDE_200_GET_ID_1,
                RequestType.RIDE_200_GET_ID_2,
                RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET,
                RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET,
                RequestType.ACTIVE_DRIVER_REACHED_200_GET,
                RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET,
                RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
    }

    public interface Interruptable {
        void run() throws InterruptedException;
    }
}
