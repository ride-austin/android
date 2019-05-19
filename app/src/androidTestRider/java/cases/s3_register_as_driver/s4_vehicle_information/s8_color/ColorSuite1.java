package cases.s3_register_as_driver.s4_vehicle_information.s8_color;

import android.support.test.espresso.Espresso;
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
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import cases.s3_register_as_driver.RegisterAsDriverUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.NavigationViewActions.navigateTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by crossover on 05/05/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ColorSuite1 extends BaseUITest {
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
     * C1930452: Back button moves back to the models selection screen
     * C1930453: Year, Model and make shown
     * C1930454: Select color
     */
    @Test
    @TestCases({"C1930452", "C1930453", "C1930454"})
    public void testVehicleColorSelection() throws IOException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // wait for map
        waitForDisplayed(R.id.mapContainer);

        // go to driver registration
        onView(navigationIcon()).perform(click());
        onView(withId(R.id.navigationView)).perform(navigateTo(R.id.navDriveWithRideApp));

        RegisterAsDriverUtils.throughInitial();
        RegisterAsDriverUtils.throughDriverPhoto(false);
        RegisterAsDriverUtils.throughDriverLicense(true);
        RegisterAsDriverUtils.skipTnc();
        RegisterAsDriverUtils.throughFrontPhoto(false);
        RegisterAsDriverUtils.throughBackPhoto(true);
        RegisterAsDriverUtils.throughInsidePhoto(false);
        RegisterAsDriverUtils.throughTrunkPhoto(true);
        RegisterAsDriverUtils.throughVehicleInfoInitial();
        RegisterAsDriverUtils.throughVehicleYear(1);
        RegisterAsDriverUtils.throughVehicleMake(0);
        RegisterAsDriverUtils.throughVehicleModel(0);
        RegisterAsDriverUtils.throughVehicleColor(0);

        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_license_plate)))
                .check(matches(isDisplayed()));

        Espresso.closeSoftKeyboard();
        Espresso.pressBack();

        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_color)))
                .check(matches(isDisplayed()));
        onView(withId(R.id.header))
                .check(matches(withText("2018 Aston Martin Vanquish S")))
                .check(matches(isDisplayed()));


        Espresso.pressBack();

        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_model)))
                .check(matches(isDisplayed()));
        onView(withId(R.id.header))
                .check(matches(withText("2018 Aston Martin Vanquish S")))
                .check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_make)))
                .check(matches(isDisplayed()));
        onView(withId(R.id.header))
                .check(matches(withText("2018 Aston Martin")))
                .check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_driver_year)))
                .check(matches(isDisplayed()));
        onView(withId(R.id.header))
                .check(matches(withText("2018")))
                .check(matches(isDisplayed()));

    }
}
