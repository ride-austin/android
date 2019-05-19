package cases.s24_direct_connect;

import android.support.test.espresso.contrib.DrawerActions;

import com.rideaustin.MockDelegate;
import com.rideaustin.R;
import com.rideaustin.RequestType;
import com.rideaustin.ResponseModifier;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.api.config.DirectConnectConfig;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.driver.DirectConnectDriver;
import com.rideaustin.utils.NavigationUtils;

import org.hamcrest.Matchers;

import javax.annotation.Nullable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.NavigationViewActions.navigateTo;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.withEmptyText;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForToast;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created on 18/12/2017
 *
 * @author sdelaysam
 */

public class DirectConnectHelper {

    private static String CONFIG_TITLE = "Direct Connect";
    private static String CONFIG_DESCRIPTION = "Direct Connect enables you to pair with your driver by simply entering in their Driver ID !!!";

    public static void mockConfigWithDirectConnect(MockDelegate mockDelegate) {
        GlobalConfig config = mockDelegate.getResponse("CONFIG_GLOBAL_200", GlobalConfig.class);
        DirectConnectConfig directConnect = new DirectConnectConfig();
        directConnect.setEnabled(true);
        directConnect.setTitle(CONFIG_TITLE);
        directConnect.setDescription(CONFIG_DESCRIPTION);
        config.setDirectConnectConfig(directConnect);
        mockDelegate.removeRequests(RequestType.CONFIG_RIDER_200_GET);
        mockDelegate.mockRequest(RequestType.CONFIG_RIDER_200_GET, config);
    }

    public static void openDirectConnect() throws InterruptedException {
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.navigationView)).perform(navigateTo(R.id.navDirectConnect));
        waitFor(condition("Direct Connect should be opened")
                .withMatcher(allOf(withId(R.id.toolbarTitle), withText(R.string.connect_driver_title))));
    }

    public static void verifyInitialState() {
        onView(withId(R.id.driver_id_text))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .check(matches(withEmptyText()));
        onView(withId(R.id.description))
                .check(matches(isDisplayed()))
                .check(matches(withText(CONFIG_DESCRIPTION)));
        onView(withId(R.id.submit))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())));
    }

    public static void typeDirectConnectId(String id) {
        onView(withId(R.id.driver_id_text))
                .perform(typeText(id), closeSoftKeyboard());
        onView(withId(R.id.submit)).check(matches(isEnabled()));
    }

    public static void clearDirectConnectId() {
        onView(withId(R.id.driver_id_text)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.submit)).check(matches(not(isEnabled())));
    }

    public static void findDriver(MockDelegate mockDelegate, @Nullable ResponseModifier<DirectConnectDriver> modifier) throws InterruptedException {
        mockDelegate.removeRequests(RequestType.DIRECT_CONNECT_GET_DRIVER_200,
                RequestType.DIRECT_CONNECT_GET_DRIVER_404);
        if (modifier != null) {
            DirectConnectDriver driver = mockDelegate.getResponse("DIRECT_CONNECT_DRIVER_200", DirectConnectDriver.class);
            mockDelegate.mockRequest(RequestType.DIRECT_CONNECT_GET_DRIVER_200, modifier.modifyResponse(driver));
        } else {
            mockDelegate.mockRequests(RequestType.DIRECT_CONNECT_GET_DRIVER_200);
        }
        onView(withId(R.id.submit)).perform(click());
        waitFor(condition("Driver summary should be opened")
                .withMatcher(allOf(withId(R.id.toolbarTitle), withText(R.string.connect_driver_title))));
    }

    public static void findDriverError(MockDelegate mockDelegate) throws InterruptedException {
        mockDelegate.removeRequests(RequestType.DIRECT_CONNECT_GET_DRIVER_200,
                RequestType.DIRECT_CONNECT_GET_DRIVER_404);
        mockDelegate.mockRequests(RequestType.DIRECT_CONNECT_GET_DRIVER_404);
        onView(withId(R.id.submit)).perform(click());
        waitForToast(withText(RiderMockResponseFactory.DIRECT_CONNECT_DRIVER_NOT_FOUND));
    }

    public static void findDriverWithoutInternet(MockDelegate mockDelegate) throws InterruptedException {
        mockDelegate.removeRequests(RequestType.DIRECT_CONNECT_GET_DRIVER_200,
                RequestType.DIRECT_CONNECT_GET_DRIVER_404);
        mockDelegate.mockRequests(RequestType.DIRECT_CONNECT_GET_DRIVER_200);

        mockDelegate.setNetworkError(true);

        onView(withId(R.id.submit)).perform(click());
        waitForToast(withText(R.string.network_error));

        mockDelegate.setNetworkError(false);
    }

    public static void gotoCarPicker() throws InterruptedException {
        onView(withId(R.id.car_picker))
                .check(matches(isDisplayed()))
                .perform(click());

        waitFor(condition("Car picker should be opened")
                .withMatcher(allOf(withId(R.id.toolbarTitle), withText(R.string.category_title))));
        onView(withText(R.string.direct_connect_car_categories))
                .check(matches(isDisplayed()));
    }

    public static void gotoPaymentPicker() throws InterruptedException {
        onView(withId(R.id.payment_picker))
                .check(matches(isDisplayed()))
                .perform(click());

        waitFor(condition("Payment picker should be opened")
                .withMatcher(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment))));
        onView(withId(R.id.list_payments))
                .check(matches(isDisplayed()));
    }

    public static void requestDirectConnect(MockDelegate mockDelegate) throws InterruptedException {
        mockDelegate.atomicOnRequests(() -> {
            // clear ride state
            NavigationUtils.clearRideState(mockDelegate);
            // simulate ride requested
            mockDelegate.removeRequests(RequestType.CURRENT_RIDE_EMPTY_200_GET);
            mockDelegate.mockRequests(RequestType.RIDE_REQUESTED_200_GET,
                    RequestType.CURRENT_RIDE_REQUESTED_200_GET,
                    RequestType.DIRECT_CONNECT_REQUEST_200_POST);
        });
        onView(withId(R.id.request_driver))
                .check(matches(isDisplayed()))
                .perform(click());

        waitFor(condition("Should show requesting state")
                .withMatcher(withText(R.string.direct_connect_requesting)));

        onView(withId(R.id.request_driver)).check(matches(not(isDisplayed())));
        onView(withId(R.id.car_picker)).check(matches(not(isDisplayed())));
        onView(withId(R.id.payment_picker)).check(matches(not(isDisplayed())));
        onView(withId(R.id.cancel)).check(matches(isDisplayed())).check(matches(isEnabled()));
    }

    public static void directConnectMissed(MockDelegate mockDelegate) throws InterruptedException {
        NavigationUtils.toNoDriversState(mockDelegate, R.string.direct_connect_no_driver);

        waitFor(condition("Should show requesting state")
                .withMatcher(withText(R.string.direct_connect_requesting))
                .withCheck(not(isDisplayed())));

        onView(withId(R.id.request_driver))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));
        onView(withId(R.id.car_picker)).check(matches(isDisplayed()));
        onView(withId(R.id.payment_picker)).check(matches(isDisplayed()));
        onView(withId(R.id.cancel)).check(matches(not(isDisplayed())));
    }

}
