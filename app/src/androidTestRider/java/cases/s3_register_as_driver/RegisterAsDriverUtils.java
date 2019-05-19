package cases.s3_register_as_driver;

import android.support.annotation.Nullable;
import android.support.test.espresso.contrib.PickerActions;
import android.view.View;
import android.widget.DatePicker;

import com.rideaustin.MockDelegate;
import com.rideaustin.R;
import com.rideaustin.RequestType;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.DeviceTestUtils;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.io.IOException;
import java.util.Date;

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
import static com.rideaustin.BaseUITest.getString;
import static com.rideaustin.utils.CommonNavigationUtils.choosePhoto;
import static com.rideaustin.utils.CommonNavigationUtils.takePhoto;
import static com.rideaustin.utils.DateHelper.getDate;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.ViewActionUtils.setDate;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

/**
 * See {@link RegisterAsDriverSuit}
 * Created by Sergey Petrov on 05/07/2017.
 */

public class RegisterAsDriverUtils {

    public static void throughInitial() {
        // continue
        waitForDisplayed(R.id.registerDriver);
        onView(withId(R.id.registerDriver)).perform(click());

        // confirm license popup
        waitForViewInWindow(withText(R.string.license_confirm));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    public static void throughDriverPhoto(boolean fromGallery) throws IOException {
        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_photo))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        if (fromGallery) {
            choosePhoto(R.id.openTakePhotoControl);
        } else {
            takePhoto(R.id.openTakePhotoControl);
        }

        // wait until photo loaded
        waitFor(allOf(withId(R.id.photo), isDisplayed(), hasDrawable()), "Photo should be set", 1000);

        // wait until "Next" is enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

        // wait for confirmation
        waitForViewInWindow(withText(R.string.driver_clear_photo));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    public static void throughDriverLicense(boolean fromGallery) throws IOException {
        int year = 1900;
        int month = 0;
        int day = 1;
        Date date = getDate(year, month, day);


        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_license))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // wait for date picker
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).perform(click());

        // select expiration date
        onView(isAssignableFrom(DayPickerView.class)).perform(setDate(year, month, day));
        onView(withId(R.id.mdtp_ok)).perform(click());

        // check date is set correctly
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).check(matches(withText(DateHelper.dateToSimpleDateFormat(date))));

        // check "Next" is still disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        if (fromGallery) {
            choosePhoto(R.id.openTakePhotoControl);
        } else {
            takePhoto(R.id.openTakePhotoControl);
        }

        // wait until license photo loaded
        waitFor(allOf(withId(R.id.license), isDisplayed(), hasDrawable()), "License should be set", 1000);

        // wait until "Next" is enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());

    }

    public static void throughFrontPhoto(boolean fromGallery) throws IOException {
        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // check details text
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_front))).check(matches(isDisplayed()));

        if (fromGallery) {
            choosePhoto(R.id.openTakePhotoControl);
        } else {
            takePhoto(R.id.openTakePhotoControl);
        }

        // wait until car image loaded
        waitFor(allOf(withId(R.id.car_detail), isDisplayed(), hasDrawable()), "Car image should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());
    }

    public static void throughBackPhoto(boolean fromGallery) throws IOException {
        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // check details text
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_back))).check(matches(isDisplayed()));

        if (fromGallery) {
            choosePhoto(R.id.openTakePhotoControl);
        } else {
            takePhoto(R.id.openTakePhotoControl);
        }

        // wait until car image loaded
        waitFor(allOf(withId(R.id.car_detail), isDisplayed(), hasDrawable()), "Car image should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());
    }

    public static void throughInsidePhoto(boolean fromGallery) throws IOException {
        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // check details text
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_inside))).check(matches(isDisplayed()));

        if (fromGallery) {
            choosePhoto(R.id.openTakePhotoControl);
        } else {
            takePhoto(R.id.openTakePhotoControl);
        }

        // wait until car image loaded
        waitFor(allOf(withId(R.id.car_detail), isDisplayed(), hasDrawable()), "Car image should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());
    }

    public static void throughTrunkPhoto(boolean fromGallery) throws IOException {
        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // check details text
        onView(allOf(withId(R.id.text_detail), withText(R.string.car_photo_trunk))).check(matches(isDisplayed()));

        if (fromGallery) {
            choosePhoto(R.id.openTakePhotoControl);
        } else {
            takePhoto(R.id.openTakePhotoControl);
        }

        // wait until car image loaded
        waitFor(allOf(withId(R.id.car_detail), isDisplayed(), hasDrawable()), "Car image should be set", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());
    }

    public static void throughVehicleInfoInitial() {
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));
        waitForDisplayed(R.id.continue_button);
        onView(allOf(withId(R.id.continue_button), withText(R.string.driver_continue))).perform(click());
    }

    public static void throughVehicleYear(int position) {
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_year)))
                .check(matches(isDisplayed()));
        onView(withId(R.id.header)).check(matches(not(isDisplayed())));
        onData(anything()).atPosition(position)
                .inAdapterView(withId(R.id.list))
                .perform(click());
    }

    public static void throughVehicleMake(int position) {
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_make)))
                .check(matches(isDisplayed()));
        onData(anything()).atPosition(position)
                .inAdapterView(withId(R.id.list))
                .perform(click());
    }

    public static void throughVehicleModel(int position) {
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_model)))
                .check(matches(isDisplayed()));
        onData(anything()).atPosition(position)
                .inAdapterView(withId(R.id.list))
                .perform(click());
    }

    public static void throughVehicleColor(int position) {
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_color)))
                .check(matches(isDisplayed()));
        onData(anything()).atPosition(position)
                .inAdapterView(withId(R.id.list))
                .perform(click());
    }

    public static void throughVehicleLicensePlate() {
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_license_plate))).check(matches(isDisplayed()));
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));
        onView(allOf(withId(R.id.license_place_input), isDisplayed())).perform(typeText("T111TT"), closeSoftKeyboard());
        onView(withId(R.id.next)).check(matches(isEnabled()));
        onView(withId(R.id.next)).perform(click());
    }

    public static void skipTnc() {
        // check title (according to CONFIG_DRIVER_REGISTRATION_200)
        onView(allOf(withId(R.id.toolbarTitle), withText("TNC Card Upload"))).check(matches(isDisplayed()));

        // check missing front TNC warning
        onView(withId(R.id.next)).perform(click());
        String tncFront = "TNC Driver Card";
        waitForViewInWindow(withText(getString(R.string.missing_tnc_card_photo_warning, tncFront)));

        // skip
        onView(withId(R.id.md_buttonDefaultNegative)).perform(click());
    }

    public static void throughCarInfo() {
        throughVehicleInfoInitial();
        throughVehicleYear(0);
        throughVehicleMake(0);
        throughVehicleModel(0);
        throughVehicleColor(0);
        throughVehicleLicensePlate();

        // go next
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_vehicle_information))).check(matches(isDisplayed()));
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));
        onView(withId(R.id.next)).perform(click());
    }

    public static void throughInsurance() throws IOException {
        int year = 2018;
        int month = 11;
        int day = 31;
        Date date = getDate(year, month, day);

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_insurance))).check(matches(isDisplayed()));

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        // wait for date picker
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).perform(click());

        // select expiration date
        onView(isAssignableFrom(DayPickerView.class)).perform(setDate(year, month, day));
        onView(withId(R.id.mdtp_ok)).perform(click());

        // check date is set correctly
        waitForDisplayed(R.id.select_expiration_date_view);
        onView(withId(R.id.select_expiration_date_view)).check(matches(withText(DateHelper.dateToSimpleDateFormat(date))));

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

    }

    public static void throughDisclosure1() {
        throughDisclosure1("First name", "Last name", "1234567");
    }

    public static void throughDisclosure1(String firstName, String lastName, String driverLicense) {
        String middleName = "Middle name";
        String ssnNumber = "123456789";
        String zipCode = "12345";
        String state = "TX";
        String address = "Austin";
        int birthYear = 1979;
        int birthMonth = 11;
        int birthDay = 31;
        Date birthDate = getDate(birthYear, birthMonth, birthDay);


        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_fcra_disclosure))).check(matches(isDisplayed()));

        // input data
        onView(allOf(withId(R.id.firstNameInput), isDisplayed())).perform(typeText(firstName), pressImeActionButton());
        onView(allOf(withId(R.id.middleNameInput), isDisplayed())).perform(typeText(middleName), pressImeActionButton());
        onView(allOf(withId(R.id.lastNameInput), isDisplayed())).perform(typeText(lastName), closeSoftKeyboard());
        onView(allOf(withId(R.id.dateOfBirthInput), isDisplayed())).perform(click());

        // wait for date picker
        Matcher<View> datePicker = withClassName(Matchers.equalTo(DatePicker.class.getName()));
        waitForDisplayed(datePicker, "Date picker should be shown");

        // set birth date
        onView(datePicker).perform(PickerActions.setDate(birthYear, birthMonth + 1, birthDay));
        onView(withText(android.R.string.ok)).perform(click());

        // validate input
        waitForDisplayed(R.id.dateOfBirthInput);
        onView(withId(R.id.dateOfBirthInput)).check(matches(withText(DateHelper.dateToSimpleDateFormat(birthDate))));

        onView(allOf(withId(R.id.socialSecurityNumber), isDisplayed())).perform(typeText(ssnNumber), pressImeActionButton());
        onView(allOf(withId(R.id.currentZipcodeInput), isDisplayed())).perform(typeText(zipCode), pressImeActionButton());
        onView(allOf(withId(R.id.driverLicenseNumberInput), isDisplayed())).perform(typeText(driverLicense), pressImeActionButton());
        onView(allOf(withId(R.id.dlsInput), isDisplayed())).perform(typeText(state), pressImeActionButton());
        onView(allOf(withId(R.id.addressInput), isDisplayed())).perform(typeText(address), pressImeActionButton());

        // acknowledged
        onView(withId(R.id.acknowledgeReceipt)).perform(scrollTo(), click());

        // wait until "Next" is enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());
    }

    public static void throughDisclosure2() {
        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_fcra_disclosure))).check(matches(isDisplayed()));

        // acknowledged
        onView(withId(R.id.acknowledgeReceipt)).perform(scrollTo(), click());

        // go next
        onView(withId(R.id.next)).perform(click());
    }

    public static void throughDisclosure3() {
        throughDisclosure3("First name", "Last name");
    }

    public static void throughDisclosure3(String firstName, String lastName) {
        String middleName = "Middle name";

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_fcra_disclosure))).check(matches(isDisplayed()));

        // acknowledged
        onView(withId(R.id.fcra_authorization_full)).perform(scrollTo(), typeText(firstName + " " + middleName + " " + lastName), pressImeActionButton());

        // adjustPan in Manifest
        waitFor("Wait while keyboard hidden", 1000);

        // wait until "Next" is enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);

        // go next
        onView(withId(R.id.next)).perform(click());
    }

    public static void throughTerms() {
        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_terms_and_conditions))).check(matches(isDisplayed()));

        // agree to terms
        waitForDisplayed(R.id.checkBox);
        onView(withId(R.id.checkBox)).perform(click());

        // register
        onView(allOf(withId(R.id.continue_button), withText(R.string.driver_continue), isDisplayed(), isEnabled())).perform(click());
    }

    public static void checkNeedHelp() {
        checkNeedHelp(null);
    }

    public static void checkNeedHelp(@Nullable MockDelegate mockDelegate) {
        // turn internet off
        if (mockDelegate != null) {
            mockDelegate.setNetworkError(true);
        } else {
            DeviceTestUtils.setAirplaneMode(true);
        }

        // go to contact support
        onView(allOf(withId(R.id.need_help), withText(R.string.need_help), isDisplayed())).perform(click());

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.text_contact_support))).check(matches(isDisplayed()));

        // send message
        onView(allOf(withId(R.id.inputMessage), isDisplayed())).perform(typeText("Hey, RideAustin"), closeSoftKeyboard());

        onView(allOf(withId(R.id.buttonSubmit), isDisplayed(), isEnabled())).perform(click());
        waitForViewInWindow(onView(withText(R.string.network_error)).inRoot(isToast()));

        // turn internet on
        if (mockDelegate != null) {
            mockDelegate.setNetworkError(false);
        } else {
            DeviceTestUtils.setAirplaneMode(false);
            DeviceTestUtils.waitForInternet();
        }

        onView(allOf(withId(R.id.buttonSubmit), isDisplayed(), isEnabled())).perform(click());
        waitForViewInWindow(allOf(withText(R.string.trip_history_support_message_success_message), isDisplayed()));
    }
}
