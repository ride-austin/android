package cases.s6_settings.s4_update_documents.s1_tnc_card;

import android.graphics.drawable.BitmapDrawable;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
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
import com.rideaustin.api.config.DriverRegistrationWrapper;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.NavigationUtils;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Date;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.CommonNavigationUtils.takePhoto;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.ViewActionUtils.setDate;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

/**
 * Created by Sergey Petrov on 07/06/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class UpdateTncSuite extends BaseUITest {

    private static final int TNC_CARD_YEAR = 2020;
    private static final int TNC_CARD_MONTH = 11;
    private static final int TNC_CARD_DAY = 31;
    private static final Date TNC_CARD_DATE = getDate(TNC_CARD_YEAR, TNC_CARD_MONTH, TNC_CARD_DAY);

    // 2017-06-30 (according to DRIVER_DOCUMENTS_TNC_200.json)
    private static final Date TNC_ORIGINAL_DATE = getDate(2017, 5, 30);

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
                RequestType.QUEUES_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CURRENT_DRIVER_200_GET,
                RequestType.ACTIVE_DRIVER_EMPTY_200_GET,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET,
                RequestType.CONFIG_DRIVER_REGISTRATION_200_GET,
                RequestType.DRIVER_DOCUMENTS_TNC_200_GET,
                RequestType.DRIVER_DOCUMENTS_TNC_200_POST);
    }

    @Override
    public void tearDown() {
        super.tearDown();
        Intents.release();
    }

    /**
     * Manage TNC from registration
     * @throws IOException
     */
    @Test
    @TestCases("C1929518")
    public void manageTncFromRegistration() throws IOException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "password");

        waitFor("camera", 2000);

        // open settings
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        //------------------------------------------------------------------------------------------
        // TNC with back side enabled
        //------------------------------------------------------------------------------------------

        // open documents
        waitForDisplayed(R.id.textViewMyDocuments);
        onView(withId(R.id.textViewMyDocuments)).perform(click());
        onView(withId(R.id.title)).check(matches(withText(R.string.documents)));

        // open TNC
        waitFor(R.id.textTncCard, isEnabled());
        onView(withId(R.id.textTncCard)).perform(click());

        //------------------------------------------------------------------------------------------
        // Check TNC front
        //------------------------------------------------------------------------------------------

        checkTncFront(false, true);
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // Check TNC back
        //------------------------------------------------------------------------------------------

        checkTncBack(false);
        saveAndGoBack();

        //------------------------------------------------------------------------------------------
        // TNC with back side disabled
        //------------------------------------------------------------------------------------------

        Espresso.pressBack();

        DriverRegistrationWrapper config = getResponse("CONFIG_DRIVER_REGISTRATION_200", DriverRegistrationWrapper.class);
        config.getDriverRegistration().getTncCard().setBackPhotoEnabled(false);
        mockRequest(RequestType.CONFIG_DRIVER_REGISTRATION_200_GET, config);

        waitForDisplayed(R.id.textViewMyDocuments);
        onView(withId(R.id.textViewMyDocuments)).perform(click());
        onView(withId(R.id.title)).check(matches(withText(R.string.documents)));

        // open TNC
        waitFor(R.id.textTncCard, isEnabled());
        onView(withId(R.id.textTncCard)).perform(click());

        //------------------------------------------------------------------------------------------
        // Check TNC front
        //------------------------------------------------------------------------------------------

        checkTncFront(false, false);

        saveAndGoBack();
    }

    /**
     * Able to upload new TNC Card
     */
    @Test
    @TestCases("C1929519")
    public void uploadNewTncCard() throws IOException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "password");

        waitFor("camera", 2000);

        // open settings
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        //------------------------------------------------------------------------------------------
        // TNC with back side enabled
        //------------------------------------------------------------------------------------------

        mockRequest(RequestType.DRIVER_DOCUMENTS_TNC_200_GET, "[]");

        // open documents
        waitForDisplayed(R.id.textViewMyDocuments);
        onView(withId(R.id.textViewMyDocuments)).perform(click());
        onView(withId(R.id.title)).check(matches(withText(R.string.documents)));

        // open TNC
        waitFor(R.id.textTncCard, isEnabled());
        onView(withId(R.id.textTncCard)).perform(click());

        //------------------------------------------------------------------------------------------
        // Check TNC front
        //------------------------------------------------------------------------------------------

        checkTncFront(true, true);
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // Check TNC back
        //------------------------------------------------------------------------------------------

        checkTncBack(true);
        saveAndGoBack();

        //------------------------------------------------------------------------------------------
        // TNC with back side disabled
        //------------------------------------------------------------------------------------------

        Espresso.pressBack();

        DriverRegistrationWrapper config = getResponse("CONFIG_DRIVER_REGISTRATION_200", DriverRegistrationWrapper.class);
        config.getDriverRegistration().getTncCard().setBackPhotoEnabled(false);
        mockRequest(RequestType.CONFIG_DRIVER_REGISTRATION_200_GET, config);

        waitForDisplayed(R.id.textViewMyDocuments);
        onView(withId(R.id.textViewMyDocuments)).perform(click());
        onView(withId(R.id.title)).check(matches(withText(R.string.documents)));

        // open TNC
        waitFor(R.id.textTncCard, isEnabled());
        onView(withId(R.id.textTncCard)).perform(click());

        //------------------------------------------------------------------------------------------
        // Check TNC front
        //------------------------------------------------------------------------------------------

        checkTncFront(true, false);

        saveAndGoBack();
    }

    private void checkTncFront(boolean isEmpty, boolean isBackPhotoEnabled) throws IOException {
        // check title (according to CONFIG_DRIVER_REGISTRATION_200)
        onView(withId(R.id.toolbarTitle)).check(matches(withText("TNC Card Upload")));

        if (isEmpty) {
            // tnc image is hidden, and upload text is shown instead
            onView(withId(R.id.tnc_image)).check(matches(not(isDisplayed())));
            onView(allOf(withId(R.id.tnc_icn_upload), withText("If you have this, upload a picture"))).check(matches(isDisplayed()));
        } else {
            // check TNC front image is loaded
            waitFor(R.id.tnc_image, hasDrawable(instanceOf(BitmapDrawable.class)));
        }

        // check menu button is disabled by default and has proper title
        waitFor(R.id.next, allOf(withText(isBackPhotoEnabled ? R.string.next : R.string.save), not(isEnabled())));

        // check screen content (according to CONFIG_DRIVER_REGISTRATION_200)
        String title = "TNC Driver Card";
        onView(allOf(withId(R.id.tnc_card_title_1), withText(title))).check(matches(isDisplayed()));
        onView(withId(R.id.tnc_card_title_2)).check(matches(isDisplayed()));
        onView(withId(R.id.tnc_card_text_2)).check(matches(isDisplayed()));

        if (isBackPhotoEnabled) {
            // expiration date should not be displayed if there is back photo enabled
            onView(withId(R.id.expiration_date)).check(matches(not(isDisplayed())));
        } else {
            // otherwise expiration should be displayed
            onView(withId(R.id.expiration_date)).check(matches(isDisplayed()));
            // and date is set if its not supposed to be empty
            Matcher<View> matcher = isEmpty ? withText(R.string.select) : withText(DateHelper.dateToSimpleDateFormat(TNC_ORIGINAL_DATE));
            onView(allOf(withId(R.id.select_expiration_date_view), matcher)).check(matches(isDisplayed()));
        }

        // take photo
        takePhoto(R.id.openTakePhotoControl);

        // next should become enabled if there is back photo enabled
        if (isBackPhotoEnabled) {
            waitFor(R.id.next, allOf(withText(R.string.next), isEnabled()));
        } else if (isEmpty) {
            // check menu button is still disabled
            waitFor(R.id.next, allOf(withText(R.string.save), not(isEnabled())));
        }

        if (!isBackPhotoEnabled) {
            // select expiration date
            onView(withId(R.id.select_expiration_date_view)).perform(click());
            onView(isAssignableFrom(DayPickerView.class)).perform(setDate(TNC_CARD_YEAR, TNC_CARD_MONTH, TNC_CARD_DAY));
            onView(withId(R.id.mdtp_ok)).perform(click());

            // check date is set correctly
            waitForDisplayed(R.id.select_expiration_date_view);
            onView(withId(R.id.select_expiration_date_view)).check(matches(withText(DateHelper.dateToSimpleDateFormat(TNC_CARD_DATE))));
        }
    }

    private void checkTncBack(boolean isEmpty) throws IOException {

        // check title (according to CONFIG_DRIVER_REGISTRATION_200)
        onView(withId(R.id.toolbarTitle)).check(matches(withText("TNC Card Upload")));

        // tnc image is hidden, and upload text is shown instead
        onView(withId(R.id.tnc_image)).check(matches(not(isDisplayed())));
        onView(allOf(withId(R.id.tnc_icn_upload), withText("If you have this, upload a picture"))).check(matches(isDisplayed()));

        // check menu button is disabled by default and has "Save" title
        waitFor(R.id.next, allOf(withText(R.string.save), not(isEnabled())));

        String title = "TNC Driver Card back";
        onView(allOf(withId(R.id.tnc_card_title_1), withText(title))).check(matches(isDisplayed()));
        onView(withId(R.id.tnc_card_title_2)).check(matches(not(isDisplayed())));
        onView(withId(R.id.tnc_card_text_2)).check(matches(not(isDisplayed())));

        // two-sided TNC should have expiration date on back side
        onView(withId(R.id.expiration_date)).check(matches(isDisplayed()));
        Matcher<View> matcher = isEmpty ? withText(R.string.select) : withText(DateHelper.dateToSimpleDateFormat(TNC_ORIGINAL_DATE));
        onView(allOf(withId(R.id.select_expiration_date_view), matcher)).check(matches(isDisplayed()));

        // take photo
        takePhoto(R.id.openTakePhotoControl);

        if (isEmpty) {
            // check menu button is still disabled
            waitFor(R.id.next, allOf(withText(R.string.save), not(isEnabled())));
        }

        // select expiration date
        onView(withId(R.id.select_expiration_date_view)).perform(click());
        onView(isAssignableFrom(DayPickerView.class)).perform(setDate(TNC_CARD_YEAR, TNC_CARD_MONTH, TNC_CARD_DAY));
        onView(withId(R.id.mdtp_ok)).perform(click());

        // check date is set correctly
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).check(matches(withText(DateHelper.dateToSimpleDateFormat(TNC_CARD_DATE))));
    }

    private void saveAndGoBack() {
        // save
        waitFor(R.id.next, allOf(withText(R.string.save), isEnabled()));
        onView(withId(R.id.next)).perform(click());

        // RA-10485: check query has date
        verifyRequest(RequestType.DRIVER_DOCUMENTS_TNC_200_POST, hasQueryParam("validityDate", equalTo(DateHelper.dateToServerDateFormat(TNC_CARD_DATE))));

        // check success message
        waitForViewInWindow(withText(R.string.documents_updated));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // check we're back to documents
        onView(withId(R.id.title)).check(matches(withText(R.string.documents)));

        resetRequestStats(RequestType.DRIVER_DOCUMENTS_TNC_200_POST);
    }

    private static Date getDate(int year, int month, int day) {
        return DateHelper.getDate(year, month, day);
    }

}
