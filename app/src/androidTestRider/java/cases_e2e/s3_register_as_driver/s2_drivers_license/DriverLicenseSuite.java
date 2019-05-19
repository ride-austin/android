package cases_e2e.s3_register_as_driver.s2_drivers_license;

import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseE2ETest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
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
import static com.rideaustin.TestConstants.ACTIVE_RIDER_EMAIL;
import static com.rideaustin.TestConstants.ACTIVE_RIDER_PASSWORD;
import static com.rideaustin.helpers.LoginTestHelper.throughLogin;
import static com.rideaustin.utils.CommonNavigationUtils.takePhoto;
import static com.rideaustin.utils.DateHelper.getDate;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.ViewActionUtils.setDate;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by Sergey Petrov on 17/08/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverLicenseSuite extends BaseE2ETest {

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
    }

    @Override
    public void tearDown() {
        super.tearDown();
        Intents.release();
    }

    @Test
    @TestCases("C1274483")
    public void shouldRequestHelpFromDriverLicense() throws IOException {
        NavigationUtils.startActivity(activityRule);
        throughLogin(ACTIVE_RIDER_EMAIL, ACTIVE_RIDER_PASSWORD);

        // wait for map
        waitForDisplayed(R.id.mapContainer);

        // go to driver registration
        onView(navigationIcon()).perform(click());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navDriveWithRideApp));

        RegisterAsDriverUtils.throughInitial();
        RegisterAsDriverUtils.throughDriverPhoto(false);

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

        // wait until "Next" is enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);

        RegisterAsDriverUtils.checkNeedHelp();

        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_license)),
                "Wait driver license title");

        // check expiration date still set
        onView(withId(R.id.select_expiration_date_view)).check(matches(withText(DateHelper.dateToSimpleDateFormat(LICENSE_DATE))));

        // check "Next" is still enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);

    }
}