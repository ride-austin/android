package cases_e2e.s3_register_as_driver.s1_driver_photo;

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
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import cases.s3_register_as_driver.RegisterAsDriverUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static cases.s3_register_as_driver.RegisterAsDriverUtils.checkNeedHelp;
import static com.rideaustin.TestConstants.ACTIVE_RIDER_EMAIL;
import static com.rideaustin.TestConstants.ACTIVE_RIDER_PASSWORD;
import static com.rideaustin.helpers.LoginTestHelper.throughLogin;
import static com.rideaustin.utils.CommonNavigationUtils.takePhoto;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by Sergey Petrov on 16/08/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverPhotoSuite extends BaseE2ETest {

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
    @TestCases("C1274453")
    public void shouldRequestHelpFromDriverPhotos() throws IOException {
        NavigationUtils.startActivity(activityRule);
        throughLogin(ACTIVE_RIDER_EMAIL, ACTIVE_RIDER_PASSWORD);

        // wait for map
        waitForDisplayed(R.id.mapContainer);

        // go to driver registration
        onView(navigationIcon()).perform(click());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navDriveWithRideApp));

        RegisterAsDriverUtils.throughInitial();

        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_photo)),
                "Wait driver photo title");

        // check "Next" is disabled
        waitForDisplayed(R.id.next);
        onView(withId(R.id.next)).check(matches(not(isEnabled())));

        takePhoto(R.id.openTakePhotoControl);

        // wait until photo loaded
        waitFor(allOf(withId(R.id.photo), isDisplayed(), hasDrawable()), "Photo should be set", 1000);

        // wait until "Next" is enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);

        checkNeedHelp();

        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_photo)),
                "Wait driver photo title");

        // check "Next" is still enabled
        waitFor(allOf(withId(R.id.next), isEnabled()), "Next should be enabled", 1000);
    }
}
