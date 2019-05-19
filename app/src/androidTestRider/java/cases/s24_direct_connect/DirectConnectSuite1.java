package cases.s24_direct_connect;

import android.os.RemoteException;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.ResponseModifier;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.api.config.DirectConnectConfig;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.driver.DirectConnectDriver;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.utils.UIUtils;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Nullable;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.NavigationViewActions.navigateTo;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static cases.s24_direct_connect.DirectConnectHelper.clearDirectConnectId;
import static cases.s24_direct_connect.DirectConnectHelper.findDriver;
import static cases.s24_direct_connect.DirectConnectHelper.findDriverError;
import static cases.s24_direct_connect.DirectConnectHelper.findDriverWithoutInternet;
import static cases.s24_direct_connect.DirectConnectHelper.gotoCarPicker;
import static cases.s24_direct_connect.DirectConnectHelper.gotoPaymentPicker;
import static cases.s24_direct_connect.DirectConnectHelper.mockConfigWithDirectConnect;
import static cases.s24_direct_connect.DirectConnectHelper.openDirectConnect;
import static cases.s24_direct_connect.DirectConnectHelper.typeDirectConnectId;
import static cases.s24_direct_connect.DirectConnectHelper.verifyInitialState;
import static com.rideaustin.RecyclerViewMatcher.withRecyclerView;
import static com.rideaustin.helpers.PaymentTestHelper.checkPaymentSelected;
import static com.rideaustin.helpers.PaymentTestHelper.selectPayment;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.hasNavigationItem;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.MatcherUtils.withEmptyText;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForToast;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Reference: RA-14190
 * Created on 06/12/2017
 *
 * @author sdelaysam
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DirectConnectSuite1 extends BaseUITest {

    @Rule
    public ActivityTestRule<NavigationDrawerActivity> activityRule = new RaActivityRule<>(NavigationDrawerActivity.class, false, false);

    @Override
    public void setUp() {
        super.setUp();
        initMockResponseFactory(RiderMockResponseFactory.class);
        mockRequests(RequestType.GLOBAL_APP_INFO_200_GET,
                RequestType.LOGIN_SUCCESS_200_POST,
                RequestType.TOKENS_200_POST,
                RequestType.RIDER_DATA_NO_RIDE_200_GET,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CONFIG_RIDER_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.CONFIG_ZIPCODES_200_GET,
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET,
                RequestType.ACDR_REGULAR_200_GET, // has drivers
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET);
    }

    @Test
    @TestCases("C2260723")
    public void shouldShowInMenu() throws UiObjectNotFoundException, InterruptedException, RemoteException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.navigationView)).check(matches(hasNavigationItem(R.id.navDirectConnect, false)));
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.close());

        DeviceTestUtils.pressHome();

        mockConfigWithDirectConnect(this);

        DeviceTestUtils.restoreFromRecentApps();

        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        waitFor(condition("Menu should contain Direct Connect")
                .withView(onView(withId(R.id.navigationView)))
                .withCheck(hasNavigationItem(R.id.navDirectConnect, true)));

        onView(withId(R.id.navigationView)).perform(navigateTo(R.id.navDirectConnect));

        waitFor(condition("Direct Connect should be opened")
                .withMatcher(allOf(withId(R.id.toolbarTitle), withText(R.string.connect_driver_title))));
    }

    /**
     * C2273224: Direct Connect: Submit button is inactive before entering DC id
     * C2273225: Direct Connect: Submit button is active after entering DC id
     * C2275454: Direct Connect: Back button should lead to the main screen
     */
    @Test
    @TestCases({"C2273224", "C2273225", "C2275454"})
    public void verifySubmitAndBack() throws InterruptedException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        //------------------------------------------------------------------------------------------
        // C2273224: Direct Connect: Submit button is inactive before entering DC id
        //------------------------------------------------------------------------------------------
        openDirectConnect();
        verifyInitialState();

        //------------------------------------------------------------------------------------------
        // C2273225: Direct Connect: Submit button is active after entering DC id
        //------------------------------------------------------------------------------------------
        typeDirectConnectId("12345");
        clearDirectConnectId();
        typeDirectConnectId("1");

        //------------------------------------------------------------------------------------------
        // C2275454: Direct Connect: Back button should lead to the main screen
        //------------------------------------------------------------------------------------------
        onView(navigationIcon()).perform(click());
        waitForDisplayed(R.id.mapContainer);

        openDirectConnect();
        verifyInitialState();
    }

    /**
     * C2273219: Direct Connect: Submit existing direct connect id
     * C2273220: Direct Connect: Submit expired direct connect id
     * C2275324: Direct Connect: Submit direct connect id when no internet connection
     */
    @Test
    @TestCases({"C2273219", "C2273220", "C2275324"})
    public void verifySearchDriver() throws InterruptedException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        //------------------------------------------------------------------------------------------
        // C2273219: Direct Connect: Submit existing direct connect id
        //------------------------------------------------------------------------------------------

        openDirectConnect();
        verifyInitialState();

        typeDirectConnectId("12345");
        findDriver(this, null);

        onView(navigationIcon()).perform(click());
        onView(withId(R.id.driver_id_text)).check(matches(withText("12345")));
        onView(withId(R.id.submit)).check(matches(isEnabled()));

        //------------------------------------------------------------------------------------------
        // C2273220: Direct Connect: Submit expired direct connect id
        //------------------------------------------------------------------------------------------

        findDriverError(this);

        onView(withId(R.id.driver_id_text)).check(matches(withText("12345")));
        onView(withId(R.id.submit)).check(matches(isEnabled()));

        //------------------------------------------------------------------------------------------
        // C2275324: Direct Connect: Submit direct connect id when no internet connection
        //------------------------------------------------------------------------------------------

        findDriverWithoutInternet(this);
    }

    @Test
    @TestCases({"C2276237", "C2276238", "C2276239"})
    public void shouldDisplayDriverInfo() throws InterruptedException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        openDirectConnect();
        typeDirectConnectId("12345");
        findDriver(this, response -> {
            response.setFirstName("First name");
            response.setLastName("Last name");
            response.setRating(1.23);
            return response;
        });

        onView(withId(R.id.driver_image))
                .check(matches(isDisplayed()))
                .check(matches(hasDrawable()));

        onView(withId(R.id.driver_name_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("First name Last name")));

        onView(withId(R.id.driver_rating_text))
                .check(matches(isDisplayed()))
                .check(matches(withText(UIUtils.formatRating(1.23))));
    }

    @Test
    @TestCases("C2276240")
    public void shouldShowCheapestCategory() throws InterruptedException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        openDirectConnect();
        typeDirectConnectId("12345");
        findDriver(this, null);

        // should show PREMIUM as its cheaper that REGULAR (has no surge)
        onView(withId(R.id.car_category_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("PREMIUM")));

        onView(withId(R.id.car_category_seats_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("4 seats")));

        onView(withId(R.id.priority_icon))
                .check(matches(not(isDisplayed())));

        // check PREMIUM is selected in car type selector
        gotoCarPicker();

        onView(withRecyclerView(R.id.list_car_types).atPosition(0))
                .check(matches(hasDescendant(allOf(withId(R.id.car_category_text), withText("STANDARD"), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.priority_icon), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.surge_factor_text), withText(UIUtils.formatSurgeFactor(2.25)), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.car_selected_icon), not(isDisplayed())))));

        onView(withRecyclerView(R.id.list_car_types).atPosition(1))
                .check(matches(hasDescendant(allOf(withId(R.id.car_category_text), withText("PREMIUM"), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.priority_icon), not(isDisplayed())))))
                .check(matches(hasDescendant(allOf(withId(R.id.car_selected_icon), isDisplayed()))));
    }


    @Test
    @TestCases({"C2276244", "C2276241"})
    public void shouldShowSurgeFactor() throws InterruptedException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        openDirectConnect();
        typeDirectConnectId("12345");
        findDriver(this, null);

        // should show PREMIUM as its cheaper that REGULAR (has no surge)
        onView(withId(R.id.car_category_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("PREMIUM")));

        onView(withId(R.id.priority_icon))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.surge_factor_text))
                .check(matches(not(isDisplayed())))
                .check(matches(withText(UIUtils.formatSurgeFactor(1.0))));

        // change selected car type
        gotoCarPicker();

        onView(withRecyclerView(R.id.list_car_types).atPosition(1))
                .check(matches(hasDescendant(allOf(withId(R.id.car_category_text), withText("PREMIUM"), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.priority_icon), not(isDisplayed())))))
                .check(matches(hasDescendant(allOf(withId(R.id.car_selected_icon), isDisplayed()))));

        onView(withRecyclerView(R.id.list_car_types).atPosition(0))
                .check(matches(hasDescendant(allOf(withId(R.id.car_category_text), withText("STANDARD"), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.priority_icon), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.surge_factor_text), withText(UIUtils.formatSurgeFactor(2.25)), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.car_selected_icon), not(isDisplayed())))))
                .perform(click());

        onView(withRecyclerView(R.id.list_car_types).atPosition(1))
                .check(matches(hasDescendant(allOf(withId(R.id.car_category_text), withText("PREMIUM"), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.priority_icon), not(isDisplayed())))))
                .check(matches(hasDescendant(allOf(withId(R.id.car_selected_icon), not(isDisplayed())))));

        onView(withRecyclerView(R.id.list_car_types).atPosition(0))
                .check(matches(hasDescendant(allOf(withId(R.id.car_category_text), withText("STANDARD"), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.priority_icon), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.surge_factor_text), withText(UIUtils.formatSurgeFactor(2.25)), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.car_selected_icon), isDisplayed()))));

        // go back and check car type changed
        onView(navigationIcon()).perform(click());

        onView(withId(R.id.car_category_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("STANDARD")));

        onView(withId(R.id.priority_icon))
                .check(matches(isDisplayed()));
        onView(withId(R.id.surge_factor_text))
                .check(matches(isDisplayed()))
                .check(matches(withText(UIUtils.formatSurgeFactor(2.25))));
    }

    @Test
    @TestCases({"C2276242", "C2276243"})
    public void shouldShowPaymentMethod() throws InterruptedException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        openDirectConnect();
        typeDirectConnectId("12345");
        findDriver(this, null);

        // should show selected payment method
        onView(withId(R.id.text_payment_card))
                .check(matches(isDisplayed()))
                .check(matches(withText(containsString("0000"))));

        // change selected payment method
        gotoPaymentPicker();
        checkPaymentSelected(2, "0000", true);
        selectPayment(this, 0, "2222");

        // go back and check payment method changed
        onView(navigationIcon()).perform(click());

        onView(withId(R.id.text_payment_card))
                .check(matches(isDisplayed()))
                .check(matches(withText(containsString("2222"))));
    }

    @Test
    @TestCases("C2276248")
    public void driverDetailsRecovery() throws InterruptedException, RemoteException, UiObjectNotFoundException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        openDirectConnect();
        typeDirectConnectId("12345");
        findDriver(this, response -> {
            response.setFirstName("First name");
            response.setLastName("Last name");
            response.setRating(1.23);
            return response;
        });

        gotoCarPicker();
        onView(withRecyclerView(R.id.list_car_types).atPosition(0))
                .check(matches(hasDescendant(allOf(withId(R.id.car_selected_icon), not(isDisplayed())))))
                .perform(click());
        onView(navigationIcon()).perform(click());

        gotoPaymentPicker();
        selectPayment(this, 0, "2222");
        onView(navigationIcon()).perform(click());

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        onView(withId(R.id.driver_image))
                .check(matches(isDisplayed()))
                .check(matches(hasDrawable()));

        onView(withId(R.id.driver_name_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("First name Last name")));

        onView(withId(R.id.driver_rating_text))
                .check(matches(isDisplayed()))
                .check(matches(withText(UIUtils.formatRating(1.23))));

        onView(withId(R.id.car_category_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("STANDARD")));

        onView(withId(R.id.car_category_seats_text))
                .check(matches(isDisplayed()))
                .check(matches(withText("4 seats")));

        onView(withId(R.id.priority_icon))
                .check(matches(isDisplayed()));

        onView(withId(R.id.surge_factor_text))
                .check(matches(isDisplayed()))
                .check(matches(withText(UIUtils.formatSurgeFactor(2.25))));

        onView(withId(R.id.text_payment_card))
                .check(matches(isDisplayed()))
                .check(matches(withText(containsString("2222"))));

    }

}
