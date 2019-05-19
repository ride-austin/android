package com.rideaustin.utils;

/**
 * Created by crossover on 10/05/2017.
 */

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.test.espresso.DataInteraction;

import com.rideaustin.MockDelegate;
import com.rideaustin.R;
import com.rideaustin.RequestType;
import com.rideaustin.ResponseModifier;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.promocode.PromoCodeBalance;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.BaseUITest.getString;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.setRating;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotEmptyText;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;

/**
 * this is to used Navigation between screens.
 */
public class NavigationUtils extends CommonNavigationUtils {

    public static void toPromoCodesScreenByRequesting(MockDelegate mockDelegate, final double totalBalance) {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.mockRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET); // no current ride
        });

        // map is displayed
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // wait while pickup address is set
        waitForNotEmptyText(R.id.pickup_address);

        // wait for pin loaded
        waitForDisplayed(R.id.set_pickup_location);

        // go to request ride
        onView(allOf(withId(R.id.set_pickup_location), isDisplayed())).perform(click());

        // wait for request panel
        waitForDisplayed(R.id.requestPanel);

        // check pin view hidden
        onView(withId(R.id.set_pickup_location)).check(matches(not(isDisplayed())));

        // wait for request button
        waitForCompletelyDisplayed(R.id.btn_request_ride);

        // wait for request button
        waitForCompletelyDisplayed(R.id.btn_promo);

        mockDelegate.atomicOnRequests(() -> {
            // mock requested state
            PromoCodeBalance promoCodeBalance = new PromoCodeBalance();
            promoCodeBalance.setRemainder(totalBalance);
            mockDelegate.mockRequest(RequestType.PROMOCODE_REMAINDER_GET_200, promoCodeBalance);
        });

        // go to request ride
        onView(allOf(withId(R.id.btn_promo), isDisplayed())).perform(click());
    }


    public static void throughRideRequest(MockDelegate mockDelegate) throws InterruptedException {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            mockDelegate.mockRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET); // no current ride
        });

        // map is displayed
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // wait while pickup address is set
        waitForNotEmptyText(R.id.pickup_address);

        // wait for pin loaded
        waitForDisplayed(R.id.set_pickup_location);

        // go to request ride
        onView(allOf(withId(R.id.set_pickup_location), isDisplayed())).perform(click());

        // wait for request panel
        waitForDisplayed(R.id.requestPanel);

        // check pin view hidden
        onView(withId(R.id.set_pickup_location)).check(matches(not(isDisplayed())));

        mockDelegate.atomicOnRequests(() -> {
            // mock requested state
            mockDelegate.removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);
            mockDelegate.mockRequests(RequestType.RIDE_REQUESTED_200_GET,
                    RequestType.CURRENT_RIDE_REQUESTED_200_GET,
                    RequestType.RIDE_REQUEST_200_POST);
        });

        // wait for request button
        Matchers.waitFor(condition("Ride request button should be shown")
                .withMatcher(allOf(withId(R.id.btn_request_ride), withText(R.string.request_ride)))
                .withCheck(allOf(isCompletelyDisplayed(), isEnabled(), isClickable())));

        waitFor("Fucking espresso", 1000);

        // request a ride
        onView(allOf(withId(R.id.btn_request_ride), isEnabled())).perform(click());

        // wait for cancel button
        waitForDisplayed(R.id.cancel_pending_request);

        // check cancel button
        onView(withId(R.id.cancel_pending_request)).check(matches(allOf(isEnabled(), isClickable())));

        // check requesting label
        onView(allOf(withId(R.id.requesting), withText(R.string.requesting))).check(matches(isDisplayed()));
    }

    public static void throughRide(MockDelegate mockDelegate) {
        toAssignedState(mockDelegate);
        toReachedState(mockDelegate);
        toActiveState(mockDelegate);
    }

    public static void throughRideSummary(MockDelegate mockDelegate) {
        throughRideSummary(mockDelegate, null, null);
    }

    public static void throughRideSummary(MockDelegate mockDelegate, ResponseModifier<Ride> rideModifier) {
        throughRideSummary(mockDelegate, rideModifier, null);
    }

    public static void throughRideSummary(MockDelegate mockDelegate, Runnable action) {
        throughRideSummary(mockDelegate, null, action);
    }


    public static void throughRideSummary(MockDelegate mockDelegate, @Nullable ResponseModifier<Ride> rideModifier, @Nullable Runnable action) {
        mockDelegate.atomicOnRequests(() -> {
            clearRideState(mockDelegate);
            // mock complete state
            if (rideModifier != null) {
                mockRideWithModifier(mockDelegate, RequestType.RIDE_COMPLETED_200_GET, "RIDE_COMPLETED_200", rideModifier);
                mockRideWithModifier(mockDelegate, RequestType.CURRENT_RIDE_COMPLETED_200_GET, "RIDE_COMPLETED_200", rideModifier);
            } else {
                mockDelegate.mockRequests(RequestType.RIDE_COMPLETED_200_GET,
                        RequestType.CURRENT_RIDE_COMPLETED_200_GET);
            }
            mockDelegate.mockRequests(RequestType.RIDE_MAP_200_GET,
                    RequestType.RIDE_RATING_200_PUT);
        });


        // wait for map loaded
        waitForViewInWindow(allOf(withId(R.id.iv_ride_map), isDisplayed(), hasDrawable()));

        // [C1930765]: check map is loaded successfully
        onView(withId(R.id.tv_ride_map)).check(matches(not(isDisplayed())));
        onView(withId(R.id.pb_ride_map)).check(matches(not(isDisplayed())));

        // [C1930766]: driver name is correctly displayed, according to RIDE_COMPLETED_200_GET
        onView(withId(R.id.tv_comment_title)).check(matches(withText(getString(R.string.rating_feedback_message_text, "Ride15"))));

        // wait rate driver dialog
        waitForViewInWindow(withId(R.id.rb_rate_driver));

        // set rating
        onView(withId(R.id.rb_rate_driver))
                .check(matches(isEnabled()))
                .check(matches(isCompletelyDisplayed()))
                .perform(setRating(4.0f));

        // set comment
        onView(allOf(withId(R.id.edt_leave_comment), isDisplayed())).perform(typeText("Nice ride"), closeSoftKeyboard());

        if (exists(onView(allOf(withId(R.id.radio_group_tips), isDisplayed())))) {
            // set tip
            onView(withId(R.id.tips_one)).perform(scrollTo(), click());
        }

        if (action != null) {
            action.run();
        }

        // according to CONFIG_GLOBAL_200
        onView(withId(R.id.tv_ride_summary)).check(matches(withText("Eligible credits will be applied on your emailed receipt.")));


        // submit
        onView(allOf(withId(R.id.btn_submit), withText(R.string.btn_submit), isEnabled(), isClickable())).perform(scrollTo());
        onView(allOf(withId(R.id.btn_submit), withText(R.string.btn_submit), isEnabled(), isClickable(), isDisplayed())).perform(click());
    }

    public static void toRequestedState(MockDelegate mockDelegate) {
        mockDelegate.atomicOnRequests(() -> {
            // clear ride state
            clearRideState(mockDelegate);
            // simulate ride requested
            mockDelegate.removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);
            mockDelegate.mockRequests(RequestType.RIDE_REQUESTED_200_GET,
                    RequestType.CURRENT_RIDE_REQUESTED_200_GET,
                    RequestType.RIDE_REQUEST_200_POST);
        });

        // wait for cancel button
        waitForDisplayed(R.id.cancel_pending_request);

        // check cancel button
        onView(withId(R.id.cancel_pending_request)).check(matches(allOf(isDisplayed(), isEnabled(), isClickable())));

        // check requesting label
        onView(allOf(withId(R.id.requesting), withText(R.string.requesting))).check(matches(isDisplayed()));
    }

    public static void toAssignedState(MockDelegate mockDelegate) {
        mockDelegate.atomicOnRequests(() -> {
            // clear ride state
            clearRideState(mockDelegate);
            // simulate driver assigned
            mockDelegate.mockRequests(RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                    RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET,
                    RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                    RequestType.SPLIT_FARE_EMPTY_200_GET);
        });

        // wait for driver details collapsed
        waitForCompletelyDisplayed(R.id.driver_image_small);
    }

    public static void toAssignedState(MockDelegate mockDelegate, ResponseModifier<Ride> rideModifier) {
        mockDelegate.atomicOnRequests(() -> {
            // clear ride state
            clearRideState(mockDelegate);
            // simulate driver assigned
            mockRideWithModifier(mockDelegate, RequestType.RIDE_DRIVER_ASSIGNED_200_GET, "RIDE_DRIVER_ASSIGNED_200", rideModifier);
            mockRideWithModifier(mockDelegate, RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET, "RIDE_DRIVER_ASSIGNED_200", rideModifier);
            mockDelegate.mockRequests(RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                    RequestType.SPLIT_FARE_EMPTY_200_GET);
        });

        // wait for driver details collapsed
        waitForCompletelyDisplayed(R.id.driver_image_small);
    }


    public static void toReachedState(MockDelegate mockDelegate) {
        mockDelegate.atomicOnRequests(() -> {
            // clear ride state
            clearRideState(mockDelegate);
            // simulate driver reached
            mockDelegate.mockRequests(RequestType.RIDE_DRIVER_REACHED_200_GET,
                    RequestType.CURRENT_RIDE_DRIVER_REACHED_200_GET,
                    RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                    RequestType.SPLIT_FARE_EMPTY_200_GET);
        });

        // wait for state changed
        waitForViewInWindow(allOf(withText(R.string.driver_reached_notification_msg), isDisplayed()));
        // close dialog
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());
    }

    public static void toReachedState(MockDelegate mockDelegate, ResponseModifier<Ride> rideModifier) {
        mockDelegate.atomicOnRequests(() -> {
            // clear ride state
            clearRideState(mockDelegate);
            // simulate driver reached
            mockRideWithModifier(mockDelegate, RequestType.RIDE_DRIVER_REACHED_200_GET, "RIDE_DRIVER_REACHED_200", rideModifier);
            mockRideWithModifier(mockDelegate, RequestType.CURRENT_RIDE_DRIVER_REACHED_200_GET, "RIDE_DRIVER_REACHED_200", rideModifier);
            mockDelegate.mockRequests(RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                    RequestType.SPLIT_FARE_EMPTY_200_GET);
        });

        // wait for state changed
        waitForViewInWindow(allOf(withText(R.string.driver_reached_notification_msg), isDisplayed()));
        // close dialog
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());
    }


    public static void toActiveState(MockDelegate mockDelegate) {
        mockDelegate.atomicOnRequests(() -> {
            // clear ride state
            clearRideState(mockDelegate);
            // simulate ride active
            mockDelegate.mockRequests(RequestType.RIDE_ACTIVE_200_GET,
                    RequestType.CURRENT_RIDE_ACTIVE_200_GET,
                    RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                    RequestType.SPLIT_FARE_EMPTY_200_GET);
        });
    }

    public static void toActiveState(MockDelegate mockDelegate, ResponseModifier<Ride> rideModifier) {
        mockDelegate.atomicOnRequests(() -> {
            // clear ride state
            clearRideState(mockDelegate);
            // simulate driver reached
            mockRideWithModifier(mockDelegate, RequestType.RIDE_ACTIVE_200_GET, "RIDE_ACTIVE_200", rideModifier);
            mockRideWithModifier(mockDelegate, RequestType.CURRENT_RIDE_ACTIVE_200_GET, "RIDE_ACTIVE_200", rideModifier);
            mockDelegate.mockRequests(RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                    RequestType.SPLIT_FARE_EMPTY_200_GET);
        });
    }

    public static void toCancelledState(MockDelegate mockDelegate) {
        mockDelegate.atomicOnRequests(() -> {
            // clear ride state
            clearRideState(mockDelegate);
            // simulate ride cancelled
            mockDelegate.mockRequests(RequestType.RIDE_DRIVER_CANCELLED_200_GET,
                    RequestType.CURRENT_RIDE_EMPTY_200_GET);
        });
    }

    public static void toCancelledState(MockDelegate mockDelegate, ResponseModifier<Ride> rideModifier) {
        mockDelegate.atomicOnRequests(() -> {
            // clear ride state
            clearRideState(mockDelegate);
            // simulate ride cancelled
            mockRideWithModifier(mockDelegate, RequestType.RIDE_DRIVER_CANCELLED_200_GET, "RIDE_DRIVER_CANCELLED_200", rideModifier);
            mockDelegate.mockRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);
        });
    }

    public static void toNoDriversState(MockDelegate mockDelegate) {
        toNoDriversState(mockDelegate, R.string.message_no_driver_available);
    }

    public static void toNoDriversState(MockDelegate mockDelegate, @StringRes int messageId) {
        mockDelegate.atomicOnRequests(() -> {
            // clear ride state
            clearRideState(mockDelegate);
            // simulate ride cancelled
            mockDelegate.mockRequests(RequestType.RIDE_NO_AVAILABLE_DRIVER_200_GET,
                    RequestType.CURRENT_RIDE_EMPTY_200_GET);
        });
        // wait for state changed
        waitForViewInWindow(allOf(withText(messageId), isDisplayed()));

        // close dialog
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());
    }

    public static void clearRideState(MockDelegate mockDelegate) {
        mockDelegate.removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET,
                RequestType.RIDE_REQUESTED_200_GET,
                RequestType.CURRENT_RIDE_REQUESTED_200_GET,
                RequestType.RIDE_NO_AVAILABLE_DRIVER_200_GET,
                RequestType.CURRENT_RIDE_NO_AVAILABLE_DRIVER_200_GET,
                RequestType.RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.CURRENT_RIDE_DRIVER_ASSIGNED_200_GET,
                RequestType.RIDE_DRIVER_REACHED_200_GET,
                RequestType.CURRENT_RIDE_DRIVER_REACHED_200_GET,
                RequestType.RIDE_ACTIVE_200_GET,
                RequestType.CURRENT_RIDE_ACTIVE_200_GET,
                RequestType.RIDE_COMPLETED_200_GET,
                RequestType.CURRENT_RIDE_COMPLETED_200_GET,
                RequestType.RIDE_DRIVER_CANCELLED_200_GET,
                RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                RequestType.SPLIT_FARE_EMPTY_200_GET);
    }

    private static void mockRideWithModifier(MockDelegate mockDelegate, RequestType eventType, String resourceName, ResponseModifier<Ride> modifier) {
        Ride ride = mockDelegate.getResponse(resourceName, Ride.class);
        mockDelegate.mockRequest(eventType, modifier.modifyResponse(ride));
    }

    public static void applyAddressPrediction(final int fieldId, final String query, final String expectedResult) throws InterruptedException {
        applyAddressPrediction(fieldId, query, expectedResult, () -> {
            tryCloseLocationWarning();
            waitForDisplayed(fieldId);
            onView(allOf(withId(fieldId), withText(containsString(expectedResult)))).check(matches(isDisplayed()));
        });
    }

    public static void applyAddressPredictionDuringRide(final int fieldId, final String query, final String expectedResult) throws InterruptedException {
        applyAddressPrediction(fieldId, query, expectedResult, () -> {
            // confirmation should appear
            waitForViewInWindow(withText(R.string.update_ride_dialog_content));
            // confirm
            onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
            waitForDisplayed(fieldId);
            onView(allOf(withId(fieldId), withText(containsString(expectedResult)))).check(matches(isDisplayed()));
        });
    }

    public static void applyAddressPrediction(final int fieldId, final String query, final String expectedResult, Runnable action) throws InterruptedException {
        onView(withId(fieldId)).perform(click());
        waitForDisplayed(R.id.addressInput);

        onView(withId(R.id.addressInput)).perform(clearText());
        onView(withId(R.id.addressInput)).perform(clearText());
        onView(withId(R.id.addressInput)).perform(typeText(query));

        DataInteraction dataInteraction = onData(hasToString(containsString(expectedResult)))
                .atPosition(0)
                .inAdapterView(withId(R.id.listView));
        Matchers.waitFor(condition().withData(dataInteraction));

        dataInteraction
                .check(matches(isDisplayed()))
                .perform(click());

        action.run();
    }

    public static void tryCloseLocationWarning() {
        Context context = getInstrumentation().getTargetContext().getApplicationContext();
        String message = context.getString(R.string.warning_location_distance,
                PickupHelper.getDistantPickUpNotificationThreshold());
        if (exists(withText(message))) {
            onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        }
    }

}
