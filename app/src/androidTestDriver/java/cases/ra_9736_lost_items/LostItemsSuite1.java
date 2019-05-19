package cases.ra_9736_lost_items;

import android.graphics.drawable.BitmapDrawable;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.driver.earnings.DriverEarningResponse;
import com.rideaustin.api.model.driver.earnings.DriverEarningResponseContent;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.TimeUtils;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.CommonNavigationUtils.choosePhoto;
import static com.rideaustin.utils.CommonNavigationUtils.takePhoto;
import static com.rideaustin.utils.MatcherUtils.childAtPosition;
import static com.rideaustin.utils.MatcherUtils.first;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.hasMultipartData;
import static com.rideaustin.utils.MatcherUtils.hasMultipartParam;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.MatcherUtils.matchesRegex;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by crossover on 26/05/2017.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LostItemsSuite1 extends BaseUITest {

    private static final String LOST_ITEM_FOUND = "Lost item found";
    private static final String FOUND_AN_ITEM = "I found an item";
    private static final String TELL_US_MORE = "Tell us more";
    private static final String IF_YOU_NOTICE = "If you notice";
    private static final String WHEN_DID_YOU_FIND = "When did you find this item?";
    private static final String DO_YOU_KNOW_WHICH_RIDE = "Do you know which ride this item belongs to?";
    private static final String PHOTO_OF_LOST_ITEM = "Photo of lost item";
    private static final String SHARE_NUMBER = "Can we share your number with the rider?";
    private static final String SHARE_DETAILS = "Share details";
    private static final String LOST_ITEM_SUCCESS = "Thank you. We’ve received your message and we will reach out to you as soon as possible.";
    private static final String LOREM_IPSUM = "Lorem ipsum";
    private static final String FORCE = "May the force be with you!!!";

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new RaActivityRule<>(SplashActivity.class, false, false);

    @Override
    public void setUp() {
        super.setUp();
        Intents.init();
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
                RequestType.DRIVER_ONLINE_TIME_200_GET,
                RequestType.RIDE_MAP_200_GET,
                RequestType.DRIVER_SUPPORT_TOPICS_200_GET,
                RequestType.DRIVER_SUPPORT_TOPICS_FOUND_ITEM_200_GET,
                RequestType.FOUND_ITEM_200_POST
        );
        DriverEarningResponse earningsResponse = getResponse("WEEKLY_EARNINGS_200", DriverEarningResponse.class);
        List<DriverEarningResponseContent> contents = earningsResponse.getContent();
        DriverEarningResponseContent last = contents.get(contents.size() - 1);
        long shift = TimeUtils.currentTimeMillis() - (last.completedOn > 0 ? last.completedOn : last.cancelledOn);
        for (DriverEarningResponseContent content : contents) {
            if (content.completedOn > 0) {
                content.completedOn += shift;
            }
            if (content.cancelledOn > 0) {
                content.cancelledOn += shift;
            }
            content.startedOn += shift;
        }
        mockRequest(RequestType.WEEKLY_EARNINGS_200_GET, earningsResponse);

        Ride ride = getResponse("RIDE_COMPLETED_200", Ride.class);
        if (ride.completedOn > 0) {
            ride.completedOn += shift;
        }
        if (ride.cancelledOn > 0) {
            ride.cancelledOn += shift;
        }
        if (ride.tippedOn > 0) {
            ride.tippedOn += shift;
        }
        if (ride.driverAcceptedOn > 0) {
            ride.driverAcceptedOn += shift;
        }
        mockRequest(RequestType.RIDE_COMPLETED_200_GET, ride);
    }

    @Override
    public void tearDown() {
        super.tearDown();
        Intents.release();
    }

    @Test
    @TestCases("C1929607")
    public void optionAccessibleFromRideDetails() {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("austindriver@xo.com", "test123");

        // open navigation drawer
        onView(allOf(withContentDescription(R.string.navigation_drawer_open), isDisplayed())).perform(click());

        // Open menu tap earning
        onView(allOf(withId(R.id.design_menu_item_text), withText(R.string.earnings), isDisplayed())).perform(click());

        //Weekly earnings opened
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.text_weekly_earnings))).check(matches(isDisplayed()));

        // Verify that week has a ride
        onView(allOf(withId(R.id.tv_trip_count), isDisplayed())).check(matches(not(withText("0"))));

        // Click on first day with a ride.
        onView(first(withChild(allOf(withId(R.id.tv_total_earnings), withText(not("$0.00")))))).perform(scrollTo());
        onView(first(withChild(allOf(withId(R.id.tv_total_earnings), withText(not("$0.00")))))).perform(click());

        // Daily earnings opened and previous rides loaded
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.text_daily_earnings))).check(matches(isDisplayed()));
        onView(allOf(first(withId(R.id.tv_trip_count)), isDisplayed())).check(matches(withText("2")));

        // Tap first trip to expand
        onData(anything()).inAdapterView(withId(R.id.trips_history)).atPosition(0).perform(scrollTo(), click());

        // Tap on support topics
        onView(withId(R.id.text_contact_support)).perform(scrollTo(), click());

        // Check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.contact_support))).check(matches(isDisplayed()));

        // Check if item found is visible
        onView(withText(LOST_ITEM_FOUND)).perform(scrollTo()).check(matches(isDisplayed()));

        // Tap found item
        onView(withText(LOST_ITEM_FOUND)).perform(click());
    }

    @Test
    @TestCases("C1929608")
    public void lookAndFeel() {
        optionAccessibleFromRideDetails();

        // Screen title reads: Tell us More
        onView(allOf(withId(R.id.toolbarTitle), withText(TELL_US_MORE))).check(matches(isDisplayed()));

        // Header text: I found an Item
        onView(allOf(withId(R.id.formTitle), withText(FOUND_AN_ITEM))).check(matches(isDisplayed()));

        // If you notice....
        onView(allOf(withId(R.id.formBody), withText(startsWith(IF_YOU_NOTICE)))).check(matches(isDisplayed()));

        // Date field for when the item was found
        onView(withChild(withChild(withText(WHEN_DID_YOU_FIND)))).perform(scrollTo());
        onView(allOf(childAtPosition(withChild(allOf(withText(WHEN_DID_YOU_FIND), isDisplayed())), 1), withId(R.id.formInput))).check(matches(isDisplayed()));

        // Text field for item owner
        onView(withChild(withChild(withText(DO_YOU_KNOW_WHICH_RIDE)))).perform(scrollTo());
        onView(allOf(childAtPosition(withChild(allOf(withText(DO_YOU_KNOW_WHICH_RIDE), isDisplayed())), 1), withId(R.id.formInput))).check(matches(isDisplayed()));

        // Photo field for the item
        onView(withChild(withChild(withText(PHOTO_OF_LOST_ITEM)))).perform(scrollTo());
        onView(allOf(childAtPosition(withChild(allOf(withText(PHOTO_OF_LOST_ITEM), isDisplayed())), 1), withId(R.id.formImage))).check(matches(isDisplayed()));

        // Yes/No share number
        onView(withChild(withChild(withText(SHARE_NUMBER)))).perform(scrollTo());
        onView(allOf(childAtPosition(withChild(allOf(withText(SHARE_NUMBER), isDisplayed())), 1), withChild(withId(R.id.radioButtonYes)))).check(matches(isDisplayed()));

        // Share details
        onView(withChild(withChild(withText(SHARE_DETAILS)))).perform(scrollTo());
        onView(allOf(childAtPosition(withChild(allOf(withText(SHARE_DETAILS), isDisplayed())), 1), withId(R.id.formInput))).check(matches(isDisplayed()));

        // Share details
        onView(withChild(withChild(withText(SHARE_DETAILS)))).perform(scrollTo());
        onView(allOf(childAtPosition(withChild(allOf(withText(SHARE_DETAILS), isDisplayed())), 1), withId(R.id.formInput))).check(matches(isDisplayed()));

        // Submit button
        onView(withId(R.id.formAction)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1929609")
    public void tellUsMoreFieldValidations() throws IOException {
        optionAccessibleFromRideDetails();

        // submit button disabled by default
        onView(withId(R.id.formAction)).perform(scrollTo()).check(matches(allOf(isDisplayed(), not(isEnabled()))));

        // Error message shown about blank fields
        onView(allOf(childAtPosition(withChild(allOf(withText(SHARE_DETAILS), isDisplayed())), 1), withId(R.id.formInput))).perform(typeText(FORCE), clearText(), closeSoftKeyboard());
        onView(withId(R.id.formAction)).check(matches(isEnabled())).perform(click());
        ViewInteraction formInput = onView(allOf(childAtPosition(withChild(withText(DO_YOU_KNOW_WHICH_RIDE)), 1), isEnabled(), withId(R.id.formInput)));
        formInput.perform(scrollTo());
        formInput.check(matches(allOf(isDisplayed(), hasErrorText(getString(R.string.field_required)))));

        // Error message about missing photo;
        ViewInteraction formInput2 = onView(allOf(childAtPosition(withChild(withText(DO_YOU_KNOW_WHICH_RIDE)), 1), isEnabled(), withId(R.id.formInput)));
        formInput2.perform(scrollTo());
        formInput2.check(matches(isDisplayed()));
        formInput2.perform(typeText(LOREM_IPSUM), closeSoftKeyboard());
        onView(withId(R.id.formAction)).perform(click());
        waitForViewInWindow(onView(withText(R.string.please_select_photo)).inRoot(isToast()));

        // choose photo
        choosePhoto(allOf(childAtPosition(withChild(allOf(withText(PHOTO_OF_LOST_ITEM), isDisplayed())), 1), withId(R.id.formImage)));

        // wait until photo loaded
        waitFor(allOf(allOf(childAtPosition(withChild(allOf(withText(PHOTO_OF_LOST_ITEM), isDisplayed())), 1), withId(R.id.formImage)), isDisplayed(), hasDrawable()), "Photo should be set", 1000);
        onView(withId(R.id.formAction)).perform(click());

        // Error message about missing share details;
        onView(allOf(childAtPosition(withChild(allOf(withText(SHARE_DETAILS), isDisplayed())), 1), withId(R.id.formInput)))
                .check(matches(hasErrorText(getString(R.string.field_required))));
    }

    @Test
    @TestCases("C1929611")
    public void tellUsMoreSubmitDetails() throws IOException {
        optionAccessibleFromRideDetails();

        // Fill the fields
        onView(allOf(childAtPosition(withChild(allOf(withText(DO_YOU_KNOW_WHICH_RIDE), isDisplayed())), 1), withId(R.id.formInput))).perform(typeText(LOREM_IPSUM), closeSoftKeyboard());

        Matcher<View> formImage = allOf(childAtPosition(withChild(withText(PHOTO_OF_LOST_ITEM)), 1), withId(R.id.formImage));
        onView(formImage).perform(scrollTo());

        // choose photo
        choosePhoto(formImage);

        waitFor(allOf(formImage, isDisplayed(), hasDrawable()), "Photo should be set", 1000);


        Matcher<View> shareNumber = allOf(childAtPosition(withChild(withText(SHARE_NUMBER)), 1), withChild(withId(R.id.radioButtonYes)));
        onView(shareNumber).perform(scrollTo(), click());
        ViewInteraction formInput = onView(allOf(childAtPosition(withChild(withText(SHARE_DETAILS)), 1), isEnabled(), withId(R.id.formInput)));
        formInput.perform(scrollTo());
        formInput.check(matches(isDisplayed()));
        formInput.perform(typeText(FORCE), closeSoftKeyboard());

        //Submit
        onView(withId(R.id.formAction)).perform(scrollTo(), click());

        // verify dialog
        waitForViewInWindow(withText(LOST_ITEM_SUCCESS));

        // Verify request
        verifyRequest(RequestType.FOUND_ITEM_200_POST, hasMultipartParam("item", "rideDescription", equalTo(LOREM_IPSUM)));
        verifyRequest(RequestType.FOUND_ITEM_200_POST, hasMultipartParam("item", "sharingContactsAllowed", is(true)));
        verifyRequest(RequestType.FOUND_ITEM_200_POST, hasMultipartParam("item", "details", equalTo(FORCE)));
        verifyRequest(RequestType.FOUND_ITEM_200_POST, hasMultipartParam("item", "foundOn", matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")));
    }

    @Test
    @TestCases("C1929610")
    public void tellUsMoreAddPhotoCameraLibrary() throws IOException {
        optionAccessibleFromRideDetails();

        Matcher<View> photoMatcher = allOf(childAtPosition(withChild(withText(PHOTO_OF_LOST_ITEM)), 1), withId(R.id.formImage));
        onView(photoMatcher).perform(scrollTo());

        // Add photo
        takePhoto(photoMatcher);

        waitFor("Need to wait on clean install 0_o", 1000);

        // Choose photo
        choosePhoto(photoMatcher);

        waitFor("Need to wait on clean install 0_o", 1000);

        //The final chosen photo is the photo set and the "Tell us more" screen opened again
        onView(photoMatcher).check(matches(hasDrawable(instanceOf(BitmapDrawable.class))));

        //Enter data in other fields and submit
        onView(allOf(childAtPosition(withChild(allOf(withText(DO_YOU_KNOW_WHICH_RIDE), isDisplayed())), 1), withId(R.id.formInput))).perform(typeText(LOREM_IPSUM), closeSoftKeyboard());
        ViewInteraction formInput = onView(allOf(childAtPosition(withChild(withText(SHARE_DETAILS)), 1), isEnabled(), withId(R.id.formInput)));

        formInput.perform(scrollTo());
        formInput.check(matches(isDisplayed()));
        formInput.perform(typeText(FORCE), closeSoftKeyboard());

        //Submit
        onView(withId(R.id.formAction)).perform(scrollTo(), click());

        //The correct photo is sent to the server
        verifyRequest(RequestType.FOUND_ITEM_200_POST, hasMultipartData("image"));
    }
}
