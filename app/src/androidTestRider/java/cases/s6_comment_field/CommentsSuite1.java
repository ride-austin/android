package cases.s6_comment_field;

import android.support.annotation.IdRes;
import android.support.test.espresso.DataInteraction;
import android.support.test.rule.ActivityTestRule;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.ViewActionUtils;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.withEmptyText;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.getText;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotEmptyText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

/**
 * Created by Sergey Petrov on 14/08/2017.
 */

public class CommentsSuite1 extends BaseUITest {

    private static final double ANOTHER_LAT = 30.417;
    private static final double ANOTHER_LNG = -97.751;

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
                RequestType.CONFIG_ZIPCODES_200_GET);
    }

    /**
     * Reference: RA-12541
     */
    @Test
    public void commentShouldBeSavedPerLocation() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // wait for map is loaded
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));
        waitForNotEmptyText(R.id.pickup_address);

        //------------------------------------------------------------------------------------------
        // Enter comment for current location
        //------------------------------------------------------------------------------------------

        // check comment empty and hidden
        onView(allOf(withId(R.id.comment), withEmptyText())).check(matches(not(isDisplayed())));

        // check comment for current is empty
        applyPredictionForInput(R.id.destination_address, "123");
        onView(allOf(withId(R.id.comment), withEmptyText())).check(matches(isDisplayed()));

        // type comment and request a ride
        String comment1 = "Comment 1";
        onView(withId(R.id.comment)).perform(typeText(comment1), closeSoftKeyboard());
        requestRideWithNoDrivers(comment1);

        // go back to initial
        onView(withId(R.id.btn_cancel_pickup)).perform(click());

        //------------------------------------------------------------------------------------------
        // Check comment for current location is restored
        //------------------------------------------------------------------------------------------

        // check comment empty and hidden
        onView(allOf(withId(R.id.comment), withEmptyText())).check(matches(not(isDisplayed())));

        // check comment for current is empty
        applyPredictionForInput(R.id.destination_address, "345");
        onView(allOf(withId(R.id.comment), withText(comment1))).check(matches(isDisplayed()));

        //------------------------------------------------------------------------------------------
        // Check comment is saved per location
        //------------------------------------------------------------------------------------------

        checkComment(ANOTHER_LAT, ANOTHER_LNG, null);
        checkComment(DEFAULT_LAT, DEFAULT_LNG, comment1);

        //------------------------------------------------------------------------------------------
        // Enter comment for another location
        //------------------------------------------------------------------------------------------

        String address1 = getText(withId(R.id.pickup_address));

        mockLocation(ANOTHER_LAT, ANOTHER_LNG);
        onView(withId(R.id.myLocationButton)).perform(click());
        waitFor(condition("Comment should be empty at new location")
                .withMatcher(withId(R.id.comment))
                .withCheck(withEmptyText()));

        String address2 = getText(withId(R.id.pickup_address));

        // type comment and request a ride
        String comment2 = "Comment 2";
        onView(withId(R.id.comment)).perform(typeText(comment2), closeSoftKeyboard());
        requestRideWithNoDrivers(comment2);

        // go back to initial
        onView(withId(R.id.btn_cancel_pickup)).perform(click());

        //------------------------------------------------------------------------------------------
        // Check comment for another location is restored
        //------------------------------------------------------------------------------------------

        // check comment empty and hidden
        onView(allOf(withId(R.id.comment), withEmptyText())).check(matches(not(isDisplayed())));

        // check comment for current is empty
        applyPredictionForInput(R.id.destination_address, "345");
        onView(allOf(withId(R.id.comment), withText(comment2))).check(matches(isDisplayed()));

        //------------------------------------------------------------------------------------------
        // Check comment is saved per location
        //------------------------------------------------------------------------------------------

        checkComment(DEFAULT_LAT, DEFAULT_LNG, comment1);
        checkComment(ANOTHER_LAT, ANOTHER_LNG, comment2);

        //------------------------------------------------------------------------------------------
        // Check user-input comment is not overwritten
        //------------------------------------------------------------------------------------------

        String commentChanged = "Comment changed";
        onView(withId(R.id.comment)).perform(replaceText(commentChanged), closeSoftKeyboard());
        checkComment(DEFAULT_LAT, DEFAULT_LNG, commentChanged);
        checkComment(ANOTHER_LAT, ANOTHER_LNG, commentChanged);

        // go to request ride
        onView(allOf(withId(R.id.set_pickup_location), isDisplayed())).perform(click());
        waitForDisplayed(R.id.requestPanel);
        waitFor(condition().withMatcher(withId(R.id.pickup_address)).withCheck(withText(address2)));
        onView(allOf(withId(R.id.comment), withText(commentChanged))).check(matches(isDisplayed()));

        //------------------------------------------------------------------------------------------
        // Check comment changing according to pickup address on request panel
        //------------------------------------------------------------------------------------------

        applyRecentItemForInput(R.id.pickup_address, address1);

        // still updated comment visible
        onView(allOf(withId(R.id.comment), withText(commentChanged))).check(matches(isDisplayed()));

        // enter comment for address 1
        onView(withId(R.id.comment)).perform(replaceText(comment1), closeSoftKeyboard());

        // change pickup address to address 2
        applyRecentItemForInput(R.id.pickup_address, address2);

        // comment should restore for address 2
        onView(allOf(withId(R.id.comment), withText(comment2))).check(matches(isDisplayed()));

        // change pickup address to address 1
        applyRecentItemForInput(R.id.pickup_address, address1);

        // comment should restore for address 1
        onView(allOf(withId(R.id.comment), withText(comment1))).check(matches(isDisplayed()));
    }

    private String applyPredictionForInput(@IdRes int inputId, String input) throws InterruptedException {
        onView(withId(inputId)).perform(click());
        waitForDisplayed(R.id.addressInput);
        onView(withId(R.id.addressInput)).perform(clearText(), typeText(input), closeSoftKeyboard());

        // wait prediction to appear
        DataInteraction prediction = onData(hasToString(containsString(input)))
                .atPosition(0)
                .inAdapterView(withId(R.id.listView));
        waitFor(condition("Prediction for '" + input + "' should be shown").withData(prediction));

        // save text and apply prediction
        String predictionText = getText(prediction.onChildView(withId(R.id.name)));
        prediction.perform(click());

        // check prediction is applied
        waitFor(condition("Prediction for '" + input + "' should be applied ")
                .withMatcher(withId(inputId))
                .withCheck(withText(predictionText)));
        return predictionText;
    }

    private void applyRecentItemForInput(@IdRes int inputId, String input) throws InterruptedException {
        onView(withId(inputId)).perform(click());
        waitForDisplayed(R.id.addressInput);
        DataInteraction recent = onData(hasToString(containsString(input)))
                .inAdapterView(withId(R.id.listView));
        waitFor(condition("Recent with text '" + input + "' should be shown").withData(recent));
        recent.perform(click());
    }


    private void requestRideWithNoDrivers(String comment) throws InterruptedException {
        NavigationUtils.throughRideRequest(this);
        onView(allOf(withId(R.id.comment), withText(comment))).check(matches(isDisplayed()));

        NavigationUtils.toNoDriversState(this);
        onView(allOf(withId(R.id.comment), withText(comment))).check(matches(isDisplayed()));
    }

    private void checkComment(double lat, double lng, String comment) throws InterruptedException {
        mockLocation(lat, lng);
        onView(withId(R.id.myLocationButton)).perform(click());
        ViewActionUtils.waitFor("animation", 1000);
        waitFor(condition("Comment should be restored on default location")
                .withMatcher(withId(R.id.comment))
                .withCheck(comment != null ? withText(comment) : withEmptyText()));
    }
}