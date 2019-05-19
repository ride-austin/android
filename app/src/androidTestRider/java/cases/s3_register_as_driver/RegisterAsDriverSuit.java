package cases.s3_register_as_driver;

import android.support.annotation.NonNull;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestMatcher;
import com.rideaustin.RequestStats;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.NavigationUtils;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;

import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.CommonNavigationUtils.choosePhoto;
import static com.rideaustin.utils.CommonNavigationUtils.takePhoto;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.ViewActionUtils.setDate;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Reference: RA-10796, RA-10190
 * Created by Sergey Petrov on 29/05/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RegisterAsDriverSuit extends BaseUITest {

    private static final int LICENSE_YEAR = 1900;
    private static final int LICENSE_MONTH = 0;
    private static final int LICENSE_DAY = 1;
    private static final Date LICENSE_DATE = getDate(LICENSE_YEAR, LICENSE_MONTH, LICENSE_DAY);

    private static final int INSURANCE_YEAR = 2016;
    private static final int INSURANCE_MONTH = 0;
    private static final int INSURANCE_DAY = 1;
    private static final Date INSURANCE_DATE = getDate(INSURANCE_YEAR, INSURANCE_MONTH, INSURANCE_DAY);

    private static final int TNC_CARD_YEAR = 2020;
    private static final int TNC_CARD_MONTH = 11;
    private static final int TNC_CARD_DAY = 31;
    private static final Date TNC_CARD_DATE = getDate(TNC_CARD_YEAR, TNC_CARD_MONTH, TNC_CARD_DAY);

    private static final int BIRTH_YEAR = 1989;
    private static final int BIRTH_MONTH = 11;
    private static final int BIRTH_DAY = 31;
    private static final Date BIRTH_DATE = getDate(BIRTH_YEAR, BIRTH_MONTH, BIRTH_DAY);

    private static final String FIRST_NAME = "First name";
    private static final String MIDDLE_NAME = "Middle name";
    private static final String LAST_NAME = "Last name";
    private static final String SSN_NUMBER = "123456789";
    private static final String ZIP_CODE = "12345";
    private static final String DRIVER_LICENSE = "1234567";
    private static final String STATE = "TX";
    private static final String ADDRESS = "Austin";

    private static final String LICENSE_FILENAME = DRIVER_LICENSE + "-licensephoto.png";
    private static final String INSURANCE_FILENAME = DRIVER_LICENSE + "-insurance.png";

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
                RequestType.DRIVERS_TNC_CARD_POST_200);
    }

    @Override
    public void tearDown() {
        super.tearDown();
        Intents.release();
    }

    /**
     * C1930352: Driver has license popup - Yes - enter registration process
     * C1930351: Driver has license popup - No - exit registration process
     * C1930353: Register - Verify driver registration Screen Order
     *
     * @throws IOException
     */
    @Test
    @TestCases({"C1930352", "C1930351", "C1930353"})
    public void shouldGoThoughRegistration() throws IOException {
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

        // continue
        waitForDisplayed(R.id.registerDriver);
        onView(withId(R.id.registerDriver)).perform(click());

        // decline license popup
        waitForViewInWindow(withText(R.string.license_confirm));
        onView(withId(R.id.md_buttonDefaultNegative)).perform(click());

        // check back on map
        waitForDisplayed(R.id.mapContainer);

        // go to driver registration
        onView(navigationIcon()).perform(click());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navDriveWithRideApp));

        // continue
        waitForDisplayed(R.id.registerDriver);
        onView(withId(R.id.registerDriver)).perform(click());

        // confirm license popup
        waitForViewInWindow(withText(R.string.license_confirm));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        //------------------------------------------------------------------------------------------
        // 0. Driver photo
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_photo))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // choose photo
        choosePhoto(R.id.openTakePhotoControl);

        // wait until photo loaded
        waitFor(allOf(withId(R.id.photo), isDisplayed(), hasDrawable()), "Photo should be set", 1000);

        // wait until "Next" is enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        // wait for confirmation
        waitForViewInWindow(withText(R.string.driver_clear_photo));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());


        //------------------------------------------------------------------------------------------
        // 1. Driver license
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_license))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // wait for date picker
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).perform(click());

        // select expiration date
        onView(isAssignableFrom(DayPickerView.class)).perform(setDate(LICENSE_YEAR, LICENSE_MONTH, LICENSE_DAY));
        onView(withId(R.id.mdtp_ok)).perform(click());

        // check date is set correctly
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).check(matches(withText(DateHelper.dateToSimpleDateFormat(LICENSE_DATE))));

        // check "Next" is still disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // take photo
        takePhoto(R.id.openTakePhotoControl);

        // wait until license photo loaded
        waitFor(allOf(withId(R.id.license), isDisplayed(), hasDrawable()), "License should be set", 1000);

        // wait until "Next" is enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 2. TNC card (front)
        //------------------------------------------------------------------------------------------

        // check title (according to CONFIG_DRIVER_REGISTRATION_200)
        onView(allOf(withId(R.id.toolbarTitle), withText("TNC Card Upload"))).check(matches(isDisplayed()));

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // check missing front TNC warning
        onView(withId(R.id.next)).perform(click());
        String tncFront = "TNC Driver Card";
        waitForViewInWindow(withText(getString(R.string.missing_tnc_card_photo_warning, tncFront)));

        // don't skip
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // choose photo
        choosePhoto(R.id.openTakePhotoControl);

        // wait until tnc image loaded
        waitFor(allOf(withId(R.id.tnc_image), isDisplayed(), hasDrawable()), "License should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 3. TNC card (back)
        //------------------------------------------------------------------------------------------

        // check title (according to CONFIG_DRIVER_REGISTRATION_200)
        onView(allOf(withId(R.id.toolbarTitle), withText("TNC Card Upload"))).check(matches(isDisplayed()));

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

        //------------------------------------------------------------------------------------------
        // 4. Car photo (front)
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // check details text
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_front))).check(matches(isDisplayed()));

        //TODO: check tap on next

        // take photo
        takePhoto(R.id.openTakePhotoControl);

        // wait until car image loaded
        waitFor(allOf(withId(R.id.car_detail), isDisplayed(), hasDrawable()), "Car image should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 5. Car photo (back)
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // check details text
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_back))).check(matches(isDisplayed()));

        //TODO: check tap on next

        // choose photo
        choosePhoto(R.id.openTakePhotoControl);

        // wait until car image loaded
        waitFor(allOf(withId(R.id.car_detail), isDisplayed(), hasDrawable()), "Car image should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 6. Car photo (inside)
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // check details text
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_inside))).check(matches(isDisplayed()));

        //TODO: check tap on next

        // take photo
        takePhoto(R.id.openTakePhotoControl);

        // wait until car image loaded
        waitFor(allOf(withId(R.id.car_detail), isDisplayed(), hasDrawable()), "Car image should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 7. Car photo (trunk)
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // check details text
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_trunk))).check(matches(isDisplayed()));

        //TODO: check tap on next

        // choose photo
        choosePhoto(R.id.openTakePhotoControl);

        // wait until car image loaded
        waitFor(allOf(withId(R.id.car_detail), isDisplayed(), hasDrawable()), "Car image should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 8. Car information
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check requirements (according to CONFIG_DRIVER_REGISTRATION_200)
        onView(withText("2001 or Newer")).check(matches(isDisplayed()));
        onView(withText("4 Door")).check(matches(isDisplayed()));
        onView(withText("Not salvaged or Re-built Vehicles")).check(matches(isDisplayed()));

        // go next
        waitForDisplayed(R.id.continue_button);
        onView(allOf(withId(R.id.continue_button), withText(R.string.driver_continue))).perform(click());

        //------------------------------------------------------------------------------------------
        // 9. Car year information
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_year))).check(matches(isDisplayed()));

        // load list
        onData(hasToString("2018")).inAdapterView(withId(R.id.list)).perform(click());


        //------------------------------------------------------------------------------------------
        // 10. Car make information
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_make))).check(matches(isDisplayed()));

        // load list
        onData(hasToString("Tesla")).inAdapterView(withId(R.id.list)).perform(click());

        //------------------------------------------------------------------------------------------
        // 11. Car model information
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_model))).check(matches(isDisplayed()));

        // load list
        onData(hasToString("Model 3")).inAdapterView(withId(R.id.list)).perform(click());

        //------------------------------------------------------------------------------------------
        // 12. Car color information
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_color))).check(matches(isDisplayed()));

        // load list
        onData(hasToString("White")).inAdapterView(withId(R.id.list)).perform(click());

        //------------------------------------------------------------------------------------------
        // 13. TNC sticker
        //------------------------------------------------------------------------------------------

        // skipped (according to CONFIG_DRIVER_REGISTRATION_200)

        //------------------------------------------------------------------------------------------
        // 14. License plate
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_license_plate))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // type plate number
        onView(allOf(withId(R.id.license_place_input), isDisplayed())).perform(typeText("T111TT"), closeSoftKeyboard());

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 15. Car summary
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 16. Driver insurance
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_insurance))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // wait for date picker
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).perform(click());

        // select expiration date
        onView(isAssignableFrom(DayPickerView.class)).perform(setDate(INSURANCE_YEAR, INSURANCE_MONTH, INSURANCE_DAY));
        onView(withId(R.id.mdtp_ok)).perform(click());

        // check date is set correctly
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).check(matches(withText(DateHelper.dateToSimpleDateFormat(INSURANCE_DATE))));

        // check "Next" is still disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // take photo
        takePhoto(R.id.openTakePhotoControl);

        // wait until insurance photo loaded
        waitFor(allOf(withId(R.id.license), isDisplayed(), hasDrawable()), "License should be set", 1000);

        // wait until "Next" is enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 17. Fcra disclosure
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_fcra_disclosure))).check(matches(isDisplayed()));

        // input data
        onView(allOf(withId(R.id.firstNameInput), isDisplayed())).perform(typeText(FIRST_NAME), pressImeActionButton());
        onView(allOf(withId(R.id.middleNameInput), isDisplayed())).perform(typeText(MIDDLE_NAME), pressImeActionButton());
        onView(allOf(withId(R.id.lastNameInput), isDisplayed())).perform(typeText(LAST_NAME), closeSoftKeyboard());
        onView(allOf(withId(R.id.dateOfBirthInput), isDisplayed())).perform(click());

        // wait for date picker
        Matcher<View> datePicker = withClassName(Matchers.equalTo(DatePicker.class.getName()));
        waitForDisplayed(datePicker, "Date picker should be shown");

        // set birth date
        onView(datePicker).perform(PickerActions.setDate(BIRTH_YEAR, BIRTH_MONTH + 1, BIRTH_DAY));
        onView(withText(android.R.string.ok)).perform(click());

        // validate input
        waitForDisplayed(R.id.dateOfBirthInput);
        onView(withId(R.id.dateOfBirthInput)).check(matches(withText(DateHelper.dateToSimpleDateFormat(BIRTH_DATE))));

        onView(allOf(withId(R.id.socialSecurityNumber), isDisplayed())).perform(typeText(SSN_NUMBER), pressImeActionButton());
        onView(allOf(withId(R.id.currentZipcodeInput), isDisplayed())).perform(typeText(ZIP_CODE), pressImeActionButton());
        onView(allOf(withId(R.id.driverLicenseNumberInput), isDisplayed())).perform(typeText(DRIVER_LICENSE), pressImeActionButton());
        onView(allOf(withId(R.id.dlsInput), isDisplayed())).perform(typeText(STATE), pressImeActionButton());
        onView(allOf(withId(R.id.addressInput), isDisplayed())).perform(typeText(ADDRESS), pressImeActionButton());

        // acknowledged
        onView(withId(R.id.acknowledgeReceipt)).perform(scrollTo(), click());

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 18. Fcra check
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_fcra_disclosure))).check(matches(isDisplayed()));

        // acknowledged
        onView(withId(R.id.acknowledgeReceipt)).perform(scrollTo(), click());

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 19. Fcra authorization
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_fcra_disclosure))).check(matches(isDisplayed()));

        // acknowledged
        onView(withId(R.id.fcra_authorization_full)).perform(scrollTo(), typeText(FIRST_NAME + " " + MIDDLE_NAME + " " + LAST_NAME), pressImeActionButton());

        // adjustPan in Manifest
        waitFor("Wait while keyboard hidden", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 20. Terms and conditions
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_terms_and_conditions))).check(matches(isDisplayed()));

        // agree to terms
        waitForDisplayed(R.id.checkBox);
        onView(withId(R.id.checkBox)).perform(click());

        // register
        onView(allOf(withId(R.id.continue_button), withText(R.string.driver_continue), isDisplayed(), isEnabled())).perform(click());

        verifyRequestsCount(RequestType.DRIVERS_POST_200, is(1));

        // verify valid terms id sent (according to  CONFIG_GLOBAL_200)
        verifyRequest(RequestType.DRIVERS_POST_200, hasQueryParam("acceptedTermId", equalTo("1")));

        // RA-10544: verify valid birth date sent
        verifyRequest(RequestType.DRIVERS_POST_200, driverFileContains(DateHelper.dateToServerDateFormat(BIRTH_DATE)));

        // verify files are sent
        verifyRequest(RequestType.DRIVERS_POST_200, driverFileContainsFileName("licenseData", LICENSE_FILENAME));
        verifyRequest(RequestType.DRIVERS_POST_200, driverFileContainsFileName("insuranceData", INSURANCE_FILENAME));


        // verify TNC card uploaded with valid date
        verifyRequest(RequestType.DRIVERS_TNC_CARD_POST_200, hasQueryParam("validityDate", equalTo(DateHelper.dateToServerDateFormat(TNC_CARD_DATE))));

        // wait for success message
        waitForDisplayed(withText(containsString(getString(R.string.register_driver_sucess))), "Success message should be shown");
    }


    /**
     * Reference: RA-13790, RA-13807
     */
    @Test
    public void testScreenNavigation() throws IOException {
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

        // continue
        waitForDisplayed(R.id.registerDriver);
        onView(withId(R.id.registerDriver)).perform(click());

        // confirm license popup
        waitForViewInWindow(withText(R.string.license_confirm));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        //------------------------------------------------------------------------------------------
        // 0. Driver photo
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_photo))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // choose photo
        choosePhoto(R.id.openTakePhotoControl);

        // wait until photo loaded
        waitFor(allOf(withId(R.id.photo), isDisplayed(), hasDrawable()), "Photo should be set", 1000);

        // wait until "Next" is enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        // wait for confirmation
        waitForViewInWindow(withText(R.string.driver_clear_photo));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        //------------------------------------------------------------------------------------------
        // Go back/next
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_license))).check(matches(isDisplayed()));

        Espresso.pressBack();

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_photo))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // go next
        onView(withId(R.id.next)).perform(click());

        // wait for confirmation
        waitForViewInWindow(withText(R.string.driver_clear_photo));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        //------------------------------------------------------------------------------------------
        // Should show license
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_license))).check(matches(isDisplayed()));

        //------------------------------------------------------------------------------------------
        // 1. Driver license
        //------------------------------------------------------------------------------------------

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // wait for date picker
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).perform(click());

        // select expiration date
        onView(isAssignableFrom(DayPickerView.class)).perform(setDate(LICENSE_YEAR, LICENSE_MONTH, LICENSE_DAY));
        onView(withId(R.id.mdtp_ok)).perform(click());

        // check date is set correctly
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).check(matches(withText(DateHelper.dateToSimpleDateFormat(LICENSE_DATE))));

        // check "Next" is still disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // take photo
        takePhoto(R.id.openTakePhotoControl);

        // wait until license photo loaded
        waitFor(allOf(withId(R.id.license), isDisplayed(), hasDrawable()), "License should be set", 1000);

        // wait until "Next" is enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 2. Skip TNC card
        //------------------------------------------------------------------------------------------

        // check title (according to CONFIG_DRIVER_REGISTRATION_200)
        onView(allOf(withId(R.id.toolbarTitle), withText("TNC Card Upload"))).check(matches(isDisplayed()));

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // check missing front TNC warning
        onView(withId(R.id.next)).perform(click());
        waitForViewInWindow(withText(getString(R.string.missing_tnc_card_photo_warning, "TNC Driver Card")));

        // skip
        onView(withId(R.id.md_buttonDefaultNegative)).perform(click());

        //------------------------------------------------------------------------------------------
        // 4. Car photo (front)
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // check details text
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_front))).check(matches(isDisplayed()));

        // take photo
        takePhoto(R.id.openTakePhotoControl);

        // wait until car image loaded
        waitFor(allOf(withId(R.id.car_detail), isDisplayed(), hasDrawable()), "Car image should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 5. Car photo (back)
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // check details text
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_back))).check(matches(isDisplayed()));

        // choose photo
        choosePhoto(R.id.openTakePhotoControl);

        // wait until car image loaded
        waitFor(allOf(withId(R.id.car_detail), isDisplayed(), hasDrawable()), "Car image should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 6. Car photo (inside)
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // check details text
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_inside))).check(matches(isDisplayed()));

        // take photo
        takePhoto(R.id.openTakePhotoControl);

        // wait until car image loaded
        waitFor(allOf(withId(R.id.car_detail), isDisplayed(), hasDrawable()), "Car image should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 7. Car photo (trunk)
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // check details text
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_trunk))).check(matches(isDisplayed()));

        // choose photo
        choosePhoto(R.id.openTakePhotoControl);

        // wait until car image loaded
        waitFor(allOf(withId(R.id.car_detail), isDisplayed(), hasDrawable()), "Car image should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());


        //------------------------------------------------------------------------------------------
        // Go back to driver photo/next
        //------------------------------------------------------------------------------------------

        Espresso.pressBack();

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_trunk))).check(matches(isDisplayed()));

        Espresso.pressBack();

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_inside))).check(matches(isDisplayed()));

        Espresso.pressBack();

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_back))).check(matches(isDisplayed()));

        Espresso.pressBack();

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_front))).check(matches(isDisplayed()));

        Espresso.pressBack();

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText("TNC Card Upload"))).check(matches(isDisplayed()));

        Espresso.pressBack();

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_license))).check(matches(isDisplayed()));

        Espresso.pressBack();

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_photo))).check(matches(isDisplayed()));

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // go next
        onView(withId(R.id.next)).perform(click());

        // wait for confirmation
        waitForViewInWindow(withText(R.string.driver_clear_photo));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        //------------------------------------------------------------------------------------------
        // Should show license
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_license))).check(matches(isDisplayed()));

        //------------------------------------------------------------------------------------------
        // Go back to Drive Austin
        //------------------------------------------------------------------------------------------

        Espresso.pressBack();

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_photo))).check(matches(isDisplayed()));

        Espresso.pressBack();

        // continue
        waitForDisplayed(R.id.registerDriver);
        onView(withId(R.id.registerDriver)).perform(click());

        // confirm license popup
        waitForViewInWindow(withText(R.string.license_confirm));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        //------------------------------------------------------------------------------------------
        // 0. Driver photo
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_photo))).check(matches(isDisplayed()));

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // go next
        onView(withId(R.id.next)).perform(click());

        // wait for confirmation
        waitForViewInWindow(withText(R.string.driver_clear_photo));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        //------------------------------------------------------------------------------------------
        // 1. Driver license
        //------------------------------------------------------------------------------------------

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 2. TNC card (skip)
        //------------------------------------------------------------------------------------------

        // check title (according to CONFIG_DRIVER_REGISTRATION_200)
        onView(allOf(withId(R.id.toolbarTitle), withText("TNC Card Upload"))).check(matches(isDisplayed()));

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // check missing front TNC warning
        onView(withId(R.id.next)).perform(click());
        waitForViewInWindow(withText(getString(R.string.missing_tnc_card_photo_warning, "TNC Driver Card")));

        // skip
        onView(withId(R.id.md_buttonDefaultNegative)).perform(click());

        //------------------------------------------------------------------------------------------
        // 4. Car photo (front)
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_front))).check(matches(isDisplayed()));

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 5. Car photo (back)
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_back))).check(matches(isDisplayed()));

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 6. Car photo (inside)
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_inside))).check(matches(isDisplayed()));

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // go next
        onView(withId(R.id.next)).perform(click());

        //------------------------------------------------------------------------------------------
        // 7. Car photo (trunk)
        //------------------------------------------------------------------------------------------

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_trunk))).check(matches(isDisplayed()));

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));

        // go next
        onView(withId(R.id.next)).perform(click());

    }

    /**
     * Reference: RA-14045
     */
    @Test
    public void testUploadedFilesAreSaved() throws IOException {
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
        RegisterAsDriverUtils.skipTnc();
        RegisterAsDriverUtils.throughFrontPhoto(false);
        RegisterAsDriverUtils.throughBackPhoto(true);
        RegisterAsDriverUtils.throughInsidePhoto(false);
        RegisterAsDriverUtils.throughTrunkPhoto(true);
        RegisterAsDriverUtils.throughCarInfo();
        RegisterAsDriverUtils.throughInsurance();
        RegisterAsDriverUtils.throughDisclosure1();
        RegisterAsDriverUtils.throughDisclosure2();
        RegisterAsDriverUtils.throughDisclosure3();
        Espresso.pressBack();
        Espresso.pressBack();
        Espresso.pressBack();

        // disclosure1
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled())).perform(click());
        // disclosure2
        RegisterAsDriverUtils.throughDisclosure2();
        // disclosure3
        RegisterAsDriverUtils.throughDisclosure3();

        RegisterAsDriverUtils.throughTerms();
        verifyRequestsCount(RequestType.DRIVERS_POST_200, is(1));
        waitForDisplayed(withText(containsString(getString(R.string.register_driver_sucess))), "Success message should be shown");
    }

    /**
     * Reference: RA-14045
     */
    @Test
    public void testUploadedFilesResetAfterInfoChanged() throws IOException {
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
        RegisterAsDriverUtils.skipTnc();
        RegisterAsDriverUtils.throughFrontPhoto(false);
        RegisterAsDriverUtils.throughBackPhoto(true);
        RegisterAsDriverUtils.throughInsidePhoto(false);
        RegisterAsDriverUtils.throughTrunkPhoto(true);
        RegisterAsDriverUtils.throughCarInfo();
        RegisterAsDriverUtils.throughInsurance();
        RegisterAsDriverUtils.throughDisclosure1();
        RegisterAsDriverUtils.throughDisclosure2();
        RegisterAsDriverUtils.throughDisclosure3();
        Espresso.pressBack();
        Espresso.pressBack();
        Espresso.pressBack();
        Espresso.pressBack(); // this hides keyboard in disclosure 1
        Espresso.pressBack();

        // insurance
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled())).perform(click());

        // disclosures
        RegisterAsDriverUtils.throughDisclosure1("Another Name", "Last Name", "7654321");
        RegisterAsDriverUtils.throughDisclosure2();
        RegisterAsDriverUtils.throughDisclosure3("Another Name", "Last Name");

        RegisterAsDriverUtils.throughTerms();
        verifyRequestsCount(RequestType.DRIVERS_POST_200, is(1));
        waitForDisplayed(withText(containsString(getString(R.string.register_driver_sucess))), "Success message should be shown");
    }

    private static Date getDate(int year, int month, int day) {
        return DateHelper.getDate(year, month, day);
    }

    public static RequestMatcher driverFileContains(String substring) {
        return new RequestMatcher() {
            @Override
            public Result match(@NonNull RequestStats stats) {
                Request request = stats.getRequests().get(0);

                if (!(request.body() instanceof MultipartBody)) {
                    return fail("Not a MultiPartBody");
                }
                MultipartBody multiPartBody = (MultipartBody) request.body();
                int numParts = 3;
                if (multiPartBody.size() != numParts) {
                    return fail("MultiPartBody numParts differ, expected=" + numParts + ", actual=" + multiPartBody.size());
                }
                String driverFileContents = null;
                for (MultipartBody.Part part : multiPartBody.parts()) {
                    try {
                        if (isPartWithHeader(part, "driver")) {
                            driverFileContents = getFileContents(part);
                            break;
                        }
                    } catch (NoSuchFieldException | IllegalAccessException | IOException e) {
                        return fail(e.getMessage());
                    }
                }
                if (TextUtils.isEmpty(driverFileContents)) {
                    return fail("Unable to get driver file contents");
                }
                if (!driverFileContents.contains(substring)) {
                    return fail("Driver file does not contain=\"" + substring + "\", contents=\"" + driverFileContents + "\"");
                }
                return success();
            }
        };
    }

    public static RequestMatcher driverFileContainsFileName(String formName, String fileName) {
        return new RequestMatcher() {
            @Override
            public Result match(@NonNull RequestStats stats) {
                Request request = stats.getRequests().get(0);

                if (!(request.body() instanceof MultipartBody)) {
                    return fail("Not a MultiPartBody");
                }
                MultipartBody multiPartBody = (MultipartBody) request.body();
                int numParts = 3;
                if (multiPartBody.size() != numParts) {
                    return fail("MultiPartBody numParts differ, expected=" + numParts + ", actual=" + multiPartBody.size());
                }
                boolean found = false;
                for (MultipartBody.Part part : multiPartBody.parts()) {
                    try {
                        if (isPartWithHeader(part, formName) && isPartWithHeader(part, fileName)) {
                            found = true;
                            break;
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        return fail(e.getMessage());
                    }
                }
                if (!found) {
                    return fail("Request file does not contain=\"" + formName + "\", fileName=\"" + fileName + "\"");
                }
                return success();
            }
        };
    }


    private static boolean isPartWithHeader(MultipartBody.Part part, String formName) throws NoSuchFieldException, IllegalAccessException {
        Field f = part.getClass().getDeclaredField("headers");
        f.setAccessible(true);
        Headers headers = (Headers) f.get(part);
        return headers.get("Content-Disposition") != null && headers.get("Content-Disposition").contains(formName);
    }

    private static String getFileContents(MultipartBody.Part part) throws NoSuchFieldException, IllegalAccessException, IOException {
        Field f = part.getClass().getDeclaredField("body");
        f.setAccessible(true);
        RequestBody body = (RequestBody) f.get(part);
        final Buffer buffer = new Buffer();
        body.writeTo(buffer);
        return buffer.readUtf8();
    }

}
