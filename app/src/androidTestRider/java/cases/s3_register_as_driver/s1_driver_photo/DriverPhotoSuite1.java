package cases.s3_register_as_driver.s1_driver_photo;

import android.graphics.drawable.BitmapDrawable;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.Matchers;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import cases.s3_register_as_driver.RegisterAsDriverUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.CommonNavigationUtils.cancelTakePhoto;
import static com.rideaustin.utils.CommonNavigationUtils.choosePhoto;
import static com.rideaustin.utils.CommonNavigationUtils.takePhoto;
import static com.rideaustin.utils.DeviceTestUtils.search;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Reference: RA-11203
 * Created by crossover on 05/05/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverPhotoSuite1 extends BaseUITest {

    @Rule
    public ActivityTestRule<NavigationDrawerActivity> activityRule = new RaActivityRule<>(NavigationDrawerActivity.class, false, false);

    @Override
    public void setUp() {
        super.setUp();
        Intents.init();
        initMockResponseFactory(RiderMockResponseFactory.class);
        mockRequests(RequestType.GLOBAL_APP_INFO_200_GET,
                RequestType.LOGIN_SUCCESS_200_POST,
                RequestType.TOKENS_200_POST,
                RequestType.RIDER_DATA_NO_RIDE_200_GET,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.ACDR_EMPTY_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CONFIG_RIDER_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.CONFIG_ZIPCODES_200_GET,
                RequestType.CONFIG_DRIVER_REGISTRATION_200_GET,
                RequestType.DRIVERS_POST_200,
                RequestType.DRIVERS_PHOTO_POST_200,
                RequestType.DRIVERS_CAR_POST_200,
                RequestType.DRIVERS_CAR_PHOTO_POST_200,
                RequestType.SUPPORT_MESSAGE_200_POST);
    }

    @Override
    public void tearDown() {
        super.tearDown();
        Intents.release();
    }

    /**
     * C1930355: Cannot proceed without adding a photo of driver
     * C1930356: Can request help from Driver Photo screen
     * C1930357: Can go back From Driver Photo to Splash screen
     * C1930358: Add driver photo from library
     * C1930359: Add driver photo from camera
     * C1930360: Add driver photo from library - cancel
     * C1930361: Add driver photo from camera - cancel
     * C1930362: Tap [Take photo] - cancel
     * C1930365: Tap [Next] - tap [No] on confirmation - change photo
     * C1930366: Tap [Next] - confirmation appears
     * C1930367: Tap [Next] - tap [Yes] on confirmation
     *
     * @throws IOException
     */
    @Test
    @TestCases({"C1930355", "C1930356", "C1930357",
            "C1930358", "C1930359", "C1930360",
            "C1930361", "C1930362", "C1930365",
            "C1930366", "C1930367"})
    public void test_DriverPhotoShouldBeRequired() throws IOException, InterruptedException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // wait for map
        waitForDisplayed(R.id.mapContainer);

        // go to driver registration
        onView(navigationIcon()).perform(click());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navDriveWithRideApp));

        //------------------------------------------------------------------------------------------
        // Drive with RideAustin
        //------------------------------------------------------------------------------------------

        RegisterAsDriverUtils.throughInitial();

        //------------------------------------------------------------------------------------------
        // C1930355: Cannot proceed without adding a photo of driver
        //------------------------------------------------------------------------------------------

        // 1. Driver tap next without adding photo

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_photo))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // 2. Driver tap take a photo, do not chose any, tap next

        // choose photo and cancel
        cancelTakePhoto(R.id.openTakePhotoControl);

        // check "Next" is still disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // 3. Driver click contact support, move back and tap next

        // go to contact support
        onView(allOf(withId(R.id.need_help), withText(R.string.need_help), isDisplayed())).perform(click());

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.text_contact_support))).check(matches(isDisplayed()));

        // go back
        Espresso.pressBack();

        // check "Next" is still disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // 4. Driver move back and return

        // go back
        Espresso.pressBack();

        // continue
        waitForDisplayed(R.id.registerDriver);
        onView(withId(R.id.registerDriver)).perform(click());

        // confirm license popup
        waitForViewInWindow(withText(R.string.license_confirm));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_photo))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        //------------------------------------------------------------------------------------------
        // C1930356: Can request help from Driver Photo screen
        //------------------------------------------------------------------------------------------

        RegisterAsDriverUtils.checkNeedHelp(this);

        // waitForDisplayed() does not work, probably because it starts waiting in dialog's root
        // this is alternative way of verifying UI elements on screen
        search().id(R.id.toolbarTitle).text(R.string.title_driver_photo).exists(3000);

        takePhoto(R.id.openTakePhotoControl);

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // go to contact support
        onView(allOf(withId(R.id.need_help), withText(R.string.need_help), isDisplayed())).perform(click());

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.text_contact_support))).check(matches(isDisplayed()));

        Espresso.pressBack();

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));
        Matchers.waitFor(condition("Photo should be displayed")
                .withMatcher(withId(R.id.photo))
                .withCheck(hasDrawable(instanceOf(BitmapDrawable.class))));

        //------------------------------------------------------------------------------------------
        // C1930357: Can go back From Driver Photo to Splash screen
        //------------------------------------------------------------------------------------------

        Espresso.pressBack();

        // continue
        waitForDisplayed(R.id.registerDriver);
        onView(withId(R.id.registerDriver)).perform(click());

        // confirm license popup
        waitForViewInWindow(withText(R.string.license_confirm));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_photo))).check(matches(isDisplayed()));

        // check "Next" is enable
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));
        Matchers.waitFor(condition("Photo should be displayed")
                .withMatcher(withId(R.id.photo))
                .withCheck(hasDrawable(instanceOf(BitmapDrawable.class))));

        //------------------------------------------------------------------------------------------
        // C1930362: Tap [Take photo] - cancel
        //------------------------------------------------------------------------------------------

        waitForDisplayed(R.id.openTakePhotoControl);
        onView(withId(R.id.openTakePhotoControl)).perform(click());
        waitForCompletelyDisplayed(R.id.takePhotoContainer);
        waitForDisplayed(R.id.cancel);
        onView(withId(R.id.cancel)).perform(click());
        waitFor(R.id.takePhotoContainer, not(isCompletelyDisplayed()));

        //------------------------------------------------------------------------------------------
        // C1930366: Tap [Next] - confirmation appears
        //------------------------------------------------------------------------------------------

        onView(withId(R.id.next)).perform(click());
        waitForViewInWindow(withText(R.string.driver_clear_photo));

        //------------------------------------------------------------------------------------------
        // C1930365: Tap [Next] - tap [No] on confirmation - change photo
        //------------------------------------------------------------------------------------------

        onView(withId(R.id.md_buttonDefaultNegative)).perform(click());

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));
        Matchers.waitFor(condition("Photo should be displayed")
                .withMatcher(withId(R.id.photo))
                .withCheck(hasDrawable(instanceOf(BitmapDrawable.class))));

        //------------------------------------------------------------------------------------------
        // C1930358: Add driver photo from library
        //------------------------------------------------------------------------------------------

        choosePhoto(R.id.openTakePhotoControl);

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));
        Matchers.waitFor(condition("Photo should be displayed")
                .withMatcher(withId(R.id.photo))
                .withCheck(hasDrawable(instanceOf(BitmapDrawable.class))));

        onView(withId(R.id.next)).perform(click());
        waitForViewInWindow(withText(R.string.driver_clear_photo));

        //------------------------------------------------------------------------------------------
        // C1930367: Tap [Next] - tap [Yes] on confirmation
        //------------------------------------------------------------------------------------------

        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_license))).check(matches(isDisplayed()));

        // go back and check photo is displayed
        Espresso.pressBack();

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));
        Matchers.waitFor(condition("Photo should be displayed")
                .withMatcher(withId(R.id.photo))
                .withCheck(hasDrawable(instanceOf(BitmapDrawable.class))));
    }

}
