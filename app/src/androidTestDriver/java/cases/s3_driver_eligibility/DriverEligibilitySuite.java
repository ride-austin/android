package cases.s3_driver_eligibility;

import android.content.pm.PackageManager;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by hatak on 31.10.2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DriverEligibilitySuite extends BaseUITest {

    @Rule
    public RaActivityRule<SplashActivity> activityRule = new RaActivityRule<>(SplashActivity.class, true, false);

    @Override
    public void setUp() {
        super.setUp();
        initMockResponseFactory(DriverMockResponseFactory.class);
        mockRequests(RequestType.GLOBAL_APP_INFO_200_GET,
                RequestType.LOGIN_SUCCESS_200_POST,
                RequestType.CURRENT_USER_200_GET,
                RequestType.CONFIG_DRIVER_200_GET,
                RequestType.TOKENS_200_POST,
                RequestType.QUEUES_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.ACCEPT_RIDE_200_POST,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CURRENT_DRIVER_200_GET,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET,
                RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                RequestType.PENDING_EVENTS_200_POST,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.DRIVERS_CARTYPES_200_GET,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.CONFIG_DRIVER_200_GET,
                RequestType.CONFIG_DRIVER_REGISTRATION_200_GET);
    }

    @Test
    @TestCases({"C1929194", "C1929195", "C1929196", "C1929198"})
    public void testCantGoOnline() throws UiObjectNotFoundException, PackageManager.NameNotFoundException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@xo.com", "qwerty");

        waitForDisplayed(R.id.mapContainer);
        waitForDisplayed(R.id.toolbarActionButton);

        mockRequest(RequestType.DRIVER_GO_ONLINE_400_POST, (Object)"server specific error");


        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).check(matches(isDisplayed())).perform(click());

        waitForViewInWindow(onView(withText("server specific error")).inRoot(isToast()));

        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1929197")
    public void testCantSelectUnapprovedCar() throws UiObjectNotFoundException, PackageManager.NameNotFoundException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@xo.com", "qwerty");

        waitForDisplayed(R.id.mapContainer);
        waitForDisplayed(R.id.toolbarActionButton);
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).check(matches(isDisplayed()));

        // got to my cars
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));
        waitForCompletelyDisplayed(R.id.usersPhoto);

        List<Car> allCars = new ArrayList<>();
        Car car = createCar();
        allCars.add(car);
        mockRequest(RequestType.DRIVER_ALL_CARS_200_GET, allCars);
        mockRequest(RequestType.DRIVER_SELECT_CAR_400_PUT, "server specific error");

        onView(withId(R.id.textViewMyCars)).perform(click());
        onView(withText(car.getMake() + " " + car.getModel())).perform(click());

        waitForViewInWindow(onView(withText("server specific error")).inRoot(isToast()));
    }

    @Test
    @TestCases("C1929200")
    public void testDriverMustHaveAtLeastOneCategorySelected() throws UiObjectNotFoundException, PackageManager.NameNotFoundException {
        removeRequests(RequestType.DRIVERS_CARTYPES_200_GET);
        mockRequest(RequestType.DRIVERS_CARTYPES_200_GET, getSingleCar());

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@xo.com", "qwerty");

        waitForDisplayed(R.id.mapContainer);
        waitForDisplayed(R.id.toolbarActionButton);
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).check(matches(isDisplayed()));

        // got to my cars
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navRide));
        waitForDisplayed(R.id.tv_car_type);
        onView(allOf(withId(R.id.tv_car_type), withText("STANDARD"))).perform(click());
        waitForViewInWindow(onView(withText(R.string.car_type_save_warning)).inRoot(isToast()));
    }

    private List<RequestedCarType> getSingleCar() {
        List<RequestedCarType> carTypes = new ArrayList<>();

        RequestedCarType carType = new RequestedCarType("REGULAR", "STANDARD");
        carTypes.add(carType);

        return carTypes;
    }

    private Car createCar() {
        Car car = new Car();
        car.setId(1234L);
        car.setColor("White");
        car.setMake("Aston Marin");
        car.setModel("DB9");
        car.setYear("2017");
        car.setInsuranceExpiryDate("2018-03-31");
        car.setSelected(false);
        car.setInspectionStatus("NOT_INSPECTED");
        car.setRemoved(false);
        Set<String> categories = new HashSet<>();
        categories.add("REGULAR");
        car.setCarCategories(categories);
        return car;
    }

}
