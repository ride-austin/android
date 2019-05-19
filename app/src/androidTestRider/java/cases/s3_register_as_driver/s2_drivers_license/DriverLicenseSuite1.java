package cases.s3_register_as_driver.s2_drivers_license;

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
import com.rideaustin.utils.DateHelper;
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
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.CommonNavigationUtils.takePhoto;
import static com.rideaustin.utils.DateHelper.getDate;
import static com.rideaustin.utils.DeviceTestUtils.search;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.ViewActionUtils.setDate;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Reference: RA-11931
 * Created by crossover on 05/05/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverLicenseSuite1 extends BaseUITest {

    private static final int LICENSE_YEAR = 1900;
    private static final int LICENSE_MONTH = 0;
    private static final int LICENSE_DAY = 1;
    private static final Date LICENSE_DATE = getDate(LICENSE_YEAR, LICENSE_MONTH, LICENSE_DAY);

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
     * C1930368: Cannot proceed before uploading license picture and setting license expiration date
     * C1930369: Can request help on License screen
     * C1930372: Can proceed after adding picture and license expiration date
     * C1930364: Loosing internet connection
     * C1930375: Back button goes to Your Driver Photo screen
     */
    @Test
    @TestCases({"C1930368", "C1930369", "C1930372", "C1930364", "C1930375"})
    public void test_DriverLicenseShouldBeRequired() throws IOException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // wait for map
        waitForDisplayed(R.id.mapContainer);

        // go to driver registration
        onView(navigationIcon()).perform(click());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navDriveWithRideApp));

        RegisterAsDriverUtils.throughInitial();
        RegisterAsDriverUtils.throughDriverPhoto(false);

        //------------------------------------------------------------------------------------------
        // C1930368: Cannot proceed before uploading license picture and setting license expiration date
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
        waitFor(allOf(withId(R.id.license), isDisplayed(), hasDrawable(instanceOf(BitmapDrawable.class))), "License should be set", 1000);

        // wait until "Next" is enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);

        //------------------------------------------------------------------------------------------
        // C1930369: Can request help on License screen
        // C1930364: Loosing internet connection
        //------------------------------------------------------------------------------------------

        RegisterAsDriverUtils.checkNeedHelp(this);

        // waitForDisplayed() does not work, probably because it starts waiting in dialog's root
        // this is alternative way of verifying UI elements on screen
        search().id(R.id.toolbarTitle).text(R.string.title_driver_photo).exists(3000);

        // check "Next" is enabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));
        onView(withId(R.id.license)).check(matches(hasDrawable(instanceOf(BitmapDrawable.class))));
        onView(withId(R.id.select_expiration_date_view)).check(matches(withText(DateHelper.dateToSimpleDateFormat(LICENSE_DATE))));

        //------------------------------------------------------------------------------------------
        // C1930372: Can proceed after adding picture and license expiration date
        //------------------------------------------------------------------------------------------

        // go next
        onView(withId(R.id.next)).perform(click());

        // check title (according to CONFIG_DRIVER_REGISTRATION_200)
        onView(allOf(withId(R.id.toolbarTitle), withText("TNC Card Upload"))).check(matches(isDisplayed()));

        // go back
        Espresso.pressBack();

        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(isEnabled()));
        onView(withId(R.id.license)).check(matches(hasDrawable(instanceOf(BitmapDrawable.class))));
        onView(withId(R.id.select_expiration_date_view)).check(matches(withText(DateHelper.dateToSimpleDateFormat(LICENSE_DATE))));

        //------------------------------------------------------------------------------------------
        // C1930375: Back button goes to Your Driver Photo screen
        //------------------------------------------------------------------------------------------

        waitForDisplayed(navigationIcon(), "Wait for toolbar");

        onView(navigationIcon()).perform(click());

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_photo))).check(matches(isDisplayed()));
        onView(withId(R.id.next)).check(matches(isEnabled()));
        onView(withId(R.id.photo)).check(matches(hasDrawable(instanceOf(BitmapDrawable.class))));

    }
}
