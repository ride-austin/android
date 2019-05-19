package cases.s4_ride_request;

import android.os.RemoteException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.google.gson.reflect.TypeToken;
import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.ui.drawer.riderequest.RideRequestTypeFragment;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersCount;
import static com.rideaustin.utils.MapTestUtil.assertCarMarkersVisible;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Reference: RA-10194
 * Created on 30/10/2017
 *
 * @author sdelaysam
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RideRequestSuite extends BaseUITest {

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new RaActivityRule<>(SplashActivity.class, false, false);

    @Override
    public void setUp() {
        super.setUp();
        initMockResponseFactory(DriverMockResponseFactory.class);
        mockRequests(RequestType.GLOBAL_APP_INFO_200_GET,
                RequestType.CONFIG_DRIVER_200_GET,
                RequestType.LOGIN_SUCCESS_200_POST,
                RequestType.CURRENT_USER_200_GET,
                RequestType.CONFIG_DRIVER_200_GET,
                RequestType.TOKENS_200_POST,
                RequestType.QUEUES_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CURRENT_DRIVER_200_GET,
                RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET,
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET,
                RequestType.PENDING_EVENTS_200_POST,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.DRIVER_TYPES_200_GET);
    }


    /**
     * Reference: RA-13787
     */
    @Test
    @TestCases("C1929214")
    public void lookAndFeel() {
        login();
        openRideRequests();

        // check all categories are visible
        // check selected/active (based on ACTIVE_DRIVER_AVAILABLE_200_GET/CURRENT_DRIVER_200_GET)
        checkCategorySelected("STANDARD");
        checkCategorySelected("SUV");
        checkCategoryActive("PREMIUM");
        checkCategoryActive("LUXURY");
        checkCategoryInactive("TOMMY");
        checkCategoryInactive("Baby Driver");
        checkCategoryNotExist("HONDA");
        checkSimpleSummary("STANDARD + SUV");

        // check female option is hidden (CURRENT_DRIVER_200_GET has no womanOnly granted type)
//        onView(withId(R.id.woman_only_switch)).check(matches(not(isDisplayed())));
    }

    @Test
    @TestCases("C1929216")
    public void canSelectCategoriesUnderSettings() {
        login();
        openRideRequests();

        checkCategorySelected("STANDARD");
        checkCategorySelected("SUV");
        checkCategoryActive("PREMIUM");
        checkCategoryActive("LUXURY");
        checkSimpleSummary("STANDARD + SUV");

        tapOnCategory("PREMIUM");
        checkCategorySelected("STANDARD");
        checkCategorySelected("SUV");
        checkCategorySelected("PREMIUM");
        checkCategoryActive("LUXURY");
        checkSimpleSummary("STANDARD + SUV + PREMIUM");

        tapOnCategory("LUXURY");
        checkCategorySelected("STANDARD");
        checkCategorySelected("SUV");
        checkCategorySelected("PREMIUM");
        checkCategorySelected("LUXURY");
        checkSimpleSummaryWhenAllSelected("STANDARD + SUV + PREMIUM + LUXURY");

        tapOnCategory("STANDARD");
        tapOnCategory("SUV");
        tapOnCategory("PREMIUM");
        checkCategoryActive("STANDARD");
        checkCategoryActive("SUV");
        checkCategoryActive("PREMIUM");
        checkCategorySelected("LUXURY");
        checkSimpleSummary("LUXURY");

        tapOnCategory("LUXURY");
        waitForViewInWindow(onView(withText(R.string.car_type_save_warning)).inRoot(isToast()));
        checkSimpleSummary("LUXURY");
        checkCategorySelected("LUXURY");
    }

    /**
     * Reference: RA-13817
     */
    @Test
    @TestCases("C1929223")
    public void unableToChangeDuringRide() {
        removeRequests(RequestType.ACTIVE_DRIVER_AVAILABLE_200_GET);
        mockRequests(RequestType.ACTIVE_DRIVER_ASSIGNED_200_GET,
                RequestType.RIDE_DRIVER_ASSIGNED_200_GET);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "password");

        assertCarMarkersVisible(5000);
        assertCarMarkersCount(1);
        onView(allOf(withId(R.id.navigate), withText(R.string.navigate))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.cancel), withText(R.string.cancel))).check(matches(isDisplayed()));

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        // navigate to settings
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navRide));

        waitForViewInWindow(onView(withText(R.string.ride_request_change_error)).inRoot(isToast()));

        waitForDisplayed(R.id.mapContainer);
    }

    /**
     * Reference: RA-13820
     */
    @Test
    @TestCases({"C1929224", "C1929226", "C1929227"})
    public void categoriesChangedByAdmin() {
        login();
        openRideRequests();

        checkCategorySelected("STANDARD");
        checkCategorySelected("SUV");
        checkCategoryActive("PREMIUM");
        checkCategoryActive("LUXURY");
        checkCategoryInactive("TOMMY");
        checkCategoryInactive("Baby Driver");

        checkSimpleSummary("STANDARD + SUV");

        Driver driver = getResponse("CURRENT_DRIVER_200", Driver.class);
        Set<String> newCategories = new LinkedHashSet<>(Arrays.asList("LUXURY", "TOMMY"));
        driver.getSelectedCar().get().setCarCategories(newCategories);

        removeRequests(RequestType.CURRENT_DRIVER_200_GET);
        mockRequest(RequestType.CURRENT_DRIVER_200_GET, driver);
        mockEvent(RequestType.EVENT_CAR_CATEGORIES_CHANGED);

        checkChangedByAdminMessage("TOMMY", "STANDARD + SUV + PREMIUM");

        checkCategoryInactive("STANDARD");
        checkCategoryInactive("SUV");
        checkCategoryInactive("PREMIUM");
        checkCategorySelected("LUXURY");
        checkCategorySelected("TOMMY");
        checkCategoryInactive("Baby Driver");

        checkSimpleSummaryWhenAllSelected("LUXURY + TOMMY");
    }

    @Test
    @TestCases("C1929225")
    public void recovery() throws UiObjectNotFoundException, InterruptedException, RemoteException {
        login();
        openRideRequests();

        checkCategorySelected("STANDARD");
        checkCategorySelected("SUV");
        checkCategoryActive("PREMIUM");
        checkCategoryActive("LUXURY");
        checkCategoryInactive("TOMMY");
        checkCategoryInactive("Baby Driver");

        checkSimpleSummary("STANDARD + SUV");

        tapOnCategory("PREMIUM");
        checkCategorySelected("STANDARD");
        checkCategorySelected("SUV");
        checkCategorySelected("PREMIUM");
        checkCategoryActive("LUXURY");
        checkSimpleSummary("STANDARD + SUV + PREMIUM");

        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps();

        checkCategorySelected("STANDARD");
        checkCategorySelected("SUV");
        checkCategorySelected("PREMIUM");
        checkCategoryActive("LUXURY");
        checkSimpleSummary("STANDARD + SUV + PREMIUM");

        pressBack();
        openRideRequests();

        checkCategorySelected("STANDARD");
        checkCategorySelected("SUV");
        checkCategorySelected("PREMIUM");
        checkCategoryActive("LUXURY");
        checkSimpleSummary("STANDARD + SUV + PREMIUM");

        tapOnCategory("PREMIUM");
        tapOnCategory("SUV");
        checkCategorySelected("STANDARD");
        checkCategoryActive("SUV");
        checkCategoryActive("PREMIUM");
        checkCategoryActive("LUXURY");
        checkSimpleSummary("STANDARD");

        pressBack();
        openRideRequests();

        checkCategorySelected("STANDARD");
        checkCategoryActive("SUV");
        checkCategoryActive("PREMIUM");
        checkCategoryActive("LUXURY");
        checkSimpleSummary("STANDARD");
    }

    private void login() {
        mockAllCarTypes();

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "password");
    }

    private void openRideRequests() {
        assertCarMarkersVisible(5000);

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        // navigate to settings
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navRide));

        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText(R.string.ride_request)),
                "wait for ride request screen");
    }

    private void checkCategorySelected(String category) {
        getCategory(category)
                .check(matches(isDisplayed()))
                .check(matches(withTagValue(is((RideRequestTypeFragment.TAG_SELECTED)))));
    }

    private void checkCategoryActive(String category) {
        getCategory(category)
                .check(matches(isDisplayed()))
                .check(matches(withTagValue(is((RideRequestTypeFragment.TAG_ACTIVE)))));
    }

    private void checkCategoryInactive(String category) {
        getCategory(category)
                .check(matches(isDisplayed()))
                .check(matches(withTagValue(nullValue())));
    }

    private void checkCategoryNotExist(String category) {
        getCategory(category).check(doesNotExist());
    }

    private void checkSimpleSummary(String categories) {
        String extected = getString(R.string.ride_request_description)
                + categories
                + getString(R.string.ride_request_select_others);
        onView(withText(extected)).check(matches(isDisplayed()));
    }

    private void checkSimpleSummaryWhenAllSelected(String categories) {
        String extected = getString(R.string.ride_request_description)
                + categories
                + getString(R.string.ride_request_all_selected);
        onView(withText(extected)).check(matches(isDisplayed()));
    }

    private void checkChangedByAdminMessage(@Nullable String added, @Nullable String removed) {
        StringBuilder sb = new StringBuilder();
        if (added != null) {
            sb.append(getString(R.string.ride_request_added, added));
        }
        if (removed != null) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(getString(R.string.ride_request_removed, removed));
        }
        if (added != null) {
            sb.append(getString(R.string.ride_request_go_to_enable));
        }
        waitForViewInWindow(withText(R.string.ride_request_by_admin_title));
        waitForViewInWindow(withText(sb.toString()));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    private void tapOnCategory(String category) {
        getCategory(category).check(matches(isDisplayed())).perform(click());
    }

    private ViewInteraction getCategory(String category) {
        return onView(allOf(withId(R.id.item_bg),
                withChild(allOf(withId(R.id.tv_car_type), withText(category)))));
    }

    private void mockAllCarTypes() {
        Type type = new TypeToken<ArrayList<RequestedCarType>>() {
        }.getType();
        List<RequestedCarType> carTypes = getResponse("DRIVERS_CARTYPES_ALL_200", type);
        removeRequests(RequestType.DRIVERS_CARTYPES_200_GET);
        mockRequest(RequestType.DRIVERS_CARTYPES_200_GET, carTypes);
    }

}
