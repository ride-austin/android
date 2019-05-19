package cases.s3_register_as_driver.s3_tnc_card_upload;

import android.graphics.drawable.BitmapDrawable;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
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
import com.rideaustin.api.config.DriverRegistrationWrapper;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.NavigationUtils;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Date;

import cases.s3_register_as_driver.RegisterAsDriverUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.CommonNavigationUtils.choosePhoto;
import static com.rideaustin.utils.CommonNavigationUtils.takePhoto;
import static com.rideaustin.utils.DeviceTestUtils.search;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.ViewActionUtils.setDate;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Reference: RA-12271
 * Created by crossover on 05/05/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TncUploadSuite1 extends BaseUITest {

    private static final int TNC_CARD_YEAR = 2020;
    private static final int TNC_CARD_MONTH = 11;
    private static final int TNC_CARD_DAY = 31;
    private static final Date TNC_CARD_DATE = DateHelper.getDate(TNC_CARD_YEAR, TNC_CARD_MONTH, TNC_CARD_DAY);

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
                RequestType.SUPPORT_MESSAGE_200_POST);
    }

    @Override
    public void tearDown() {
        super.tearDown();
        Intents.release();
    }

    /**
     * C1930380: Correct screen text and description
     * C1930381: Can view instructions on how to obtain TNC card
     * C1930383: Can continue without uploading TNC card
     * C1930384: Can request help on driver sign up
     * C1930385: Can upload an TNC card, FRONT and BACK, Expiration date
     * C1930388: Second TNC card upload screen -only if first TNC card is uploaded
     */
    @Test
    @TestCases({"C1930380", "C1930381", "C1930383", "C1930384", "C1930385", "C1930388"})
    public void tncCardShouldBeOptional() throws IOException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // wait for map
        waitForDisplayed(R.id.mapContainer);

        // go to driver registration
        onView(navigationIcon()).perform(click());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navDriveWithRideApp));

        RegisterAsDriverUtils.throughInitial();
        RegisterAsDriverUtils.throughDriverPhoto(false);
        RegisterAsDriverUtils.throughDriverLicense(true);

        //------------------------------------------------------------------------------------------
        // C1930380: Correct screen text and description
        //------------------------------------------------------------------------------------------

        // according to CONFIG_DRIVER_REGISTRATION_200
        String header = "TNC Card Upload";
        String title1 = "TNC Driver Card";
        String text1 = "You will need a TNC Card from the City Transportation Office. If you have this, upload a picture here:";
        String title2 = "Don't have one?";
        String text2 = "View new driver instructions";
        String linkText = "here";

        onView(allOf(withId(R.id.toolbarTitle), withText(header))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.tnc_card_title_1), withText(title1))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.tnc_card_text_1), withText(text1))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.tnc_card_title_2), withText(title2))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.tnc_card_text_2), withText(startsWith(text2)))).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.expiration_date)).check(matches(not(isDisplayed())));

        //------------------------------------------------------------------------------------------
        // C1930381: Can view instructions on how to obtain TNC card
        //------------------------------------------------------------------------------------------

        onView(withId(R.id.tnc_card_text_2)).perform(ViewActions.openLinkWithText(linkText));

        search("Should open browser with TNC instructions")
                .res("com.android.chrome:id/url_bar")
                .text("www.rideaustin.com/drivers")
                .assertExist(5000);

        DeviceTestUtils.pressBack();

        //------------------------------------------------------------------------------------------
        // C1930383: Can continue without uploading TNC card
        //------------------------------------------------------------------------------------------

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // check missing front TNC warning
        onView(withId(R.id.next)).perform(click());
        String tncFront = "TNC Driver Card";
        waitForViewInWindow(withText(getString(R.string.missing_tnc_card_photo_warning, tncFront)));

        // skip
        onView(withId(R.id.md_buttonDefaultNegative)).perform(click());

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        Espresso.pressBack();

        // check returned to tnc front
        onView(allOf(withId(R.id.toolbarTitle), withText(header))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.tnc_card_title_1), withText(title1))).check(matches(isDisplayed()));

        //------------------------------------------------------------------------------------------
        // C1930384: Can request help on driver sign up
        //------------------------------------------------------------------------------------------

        RegisterAsDriverUtils.checkNeedHelp(this);

        // waitForDisplayed() does not work, probably because it starts waiting in dialog's root
        // this is alternative way of verifying UI elements on screen
        search().id(R.id.toolbarTitle).text(header).exists(3000);
        onView(allOf(withId(R.id.tnc_card_title_1), withText(title1))).check(matches(isDisplayed()));

        //------------------------------------------------------------------------------------------
        // C1930385: Can upload an TNC card, FRONT and BACK, Expiration date
        // C1930388: Second TNC card upload screen -only if first TNC card is uploaded
        //------------------------------------------------------------------------------------------

        // choose photo
        choosePhoto(R.id.openTakePhotoControl);

        // wait until tnc image loaded
        waitFor(allOf(withId(R.id.tnc_image), isDisplayed(), hasDrawable(instanceOf(BitmapDrawable.class))), "License should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        // check back side
        onView(withId(R.id.tnc_card_title_2)).check(matches(not(isDisplayed())));
        onView(withId(R.id.tnc_card_text_2)).check(matches(not(isDisplayed())));
        onView(withId(R.id.expiration_date)).check(matches(isDisplayed()));

        // check missing back TNC warning
        onView(withId(R.id.next)).perform(click());
        String tncBack = "TNC Driver Card back";
        waitForViewInWindow(withText(getString(R.string.missing_tnc_card_photo_warning, tncBack)));

        // don't skip
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // take photo
        takePhoto(R.id.openTakePhotoControl);

        // wait until tnc image loaded
        waitFor(allOf(withId(R.id.tnc_image), isDisplayed(), hasDrawable()), "License should be set", 1000);

        // check missing TNC expiration date warning
        onView(withId(R.id.next)).perform(click());
        waitForViewInWindow(withText(R.string.missing_tnc_card_expiration_date_warning));

        // don't skip
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // wait for date picker
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).perform(click());

        // select expiration date
        onView(isAssignableFrom(DayPickerView.class)).perform(setDate(TNC_CARD_YEAR, TNC_CARD_MONTH, TNC_CARD_DAY));
        onView(withId(R.id.mdtp_ok)).perform(click());

        // check date is set correctly
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).check(matches(withText(DateHelper.dateToSimpleDateFormat(TNC_CARD_DATE))));

        // go next
        onView(withId(R.id.next)).perform(click());

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));
    }

    /**
     * C1930387: Only one side is required for all Drivers registered in All cities besides Austin
     */
    @Test
    @TestCases("C1930387")
    public void checkOneSideTnc() throws IOException {
        removeRequests(RequestType.CONFIG_DRIVER_REGISTRATION_200_GET);
        DriverRegistrationWrapper registration = getResponse("CONFIG_DRIVER_REGISTRATION_200", DriverRegistrationWrapper.class);
        registration.getDriverRegistration().getTncCard().setBackPhotoEnabled(false);
        mockRequest(RequestType.CONFIG_DRIVER_REGISTRATION_200_GET, registration);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // wait for map
        waitForDisplayed(R.id.mapContainer);

        // go to driver registration
        onView(navigationIcon()).perform(click());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navDriveWithRideApp));

        RegisterAsDriverUtils.throughInitial();
        RegisterAsDriverUtils.throughDriverPhoto(false);
        RegisterAsDriverUtils.throughDriverLicense(true);

        // according to CONFIG_DRIVER_REGISTRATION_200
        String header = "TNC Card Upload";
        String title1 = "TNC Driver Card";
        String text1 = "You will need a TNC Card from the City Transportation Office. If you have this, upload a picture here:";
        String title2 = "Don't have one?";
        String text2 = "View new driver instructions";

        onView(allOf(withId(R.id.toolbarTitle), withText(header))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.tnc_card_title_1), withText(title1))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.tnc_card_text_1), withText(text1))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.tnc_card_title_2), withText(title2))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.tnc_card_text_2), withText(startsWith(text2)))).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.expiration_date)).check(matches(isDisplayed()));

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // check missing front TNC warning
        onView(withId(R.id.next)).perform(click());
        String tncFront = "TNC Driver Card";
        waitForViewInWindow(withText(getString(R.string.missing_tnc_card_photo_warning, tncFront)));

        // don't skip
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // take photo
        takePhoto(R.id.openTakePhotoControl);

        // wait until tnc image loaded
        waitFor(allOf(withId(R.id.tnc_image), isDisplayed(), hasDrawable()), "License should be set", 1000);

        // check missing TNC expiration date warning
        onView(withId(R.id.next)).perform(click());
        waitForViewInWindow(withText(R.string.missing_tnc_card_expiration_date_warning));

        // don't skip
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // wait for date picker
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).perform(click());

        // select expiration date
        onView(isAssignableFrom(DayPickerView.class)).perform(setDate(TNC_CARD_YEAR, TNC_CARD_MONTH, TNC_CARD_DAY));
        onView(withId(R.id.mdtp_ok)).perform(click());

        // check date is set correctly
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).check(matches(withText(DateHelper.dateToSimpleDateFormat(TNC_CARD_DATE))));

        // go next
        onView(withId(R.id.next)).perform(click());

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

    }

}
