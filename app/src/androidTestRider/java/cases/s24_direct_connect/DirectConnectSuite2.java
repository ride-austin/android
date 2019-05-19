package cases.s24_direct_connect;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ImageView;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.NavigationUtils;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static cases.s24_direct_connect.DirectConnectHelper.directConnectMissed;
import static cases.s24_direct_connect.DirectConnectHelper.findDriver;
import static cases.s24_direct_connect.DirectConnectHelper.gotoCarPicker;
import static cases.s24_direct_connect.DirectConnectHelper.gotoPaymentPicker;
import static cases.s24_direct_connect.DirectConnectHelper.mockConfigWithDirectConnect;
import static cases.s24_direct_connect.DirectConnectHelper.openDirectConnect;
import static cases.s24_direct_connect.DirectConnectHelper.requestDirectConnect;
import static cases.s24_direct_connect.DirectConnectHelper.typeDirectConnectId;
import static com.rideaustin.RecyclerViewMatcher.withRecyclerView;
import static com.rideaustin.helpers.PaymentTestHelper.checkPaymentSelected;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created on 18/12/2017
 *
 * @author sdelaysam
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DirectConnectSuite2 extends BaseUITest {

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
    @TestCases("C2276246")
    public void driverAccepts() throws InterruptedException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        openDirectConnect();
        typeDirectConnectId("12345");
        findDriver(this, null);

        requestDirectConnect(this);
        NavigationUtils.toAssignedState(this);
    }

    @Test
    @TestCases({"C2666973", "C2666974"})
    public void driverDeclines() throws InterruptedException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        openDirectConnect();
        typeDirectConnectId("12345");
        findDriver(this, null);

        requestDirectConnect(this);
        directConnectMissed(this);
    }

    @Test
    @TestCases({"C2636935", "C2636936"})
    public void checkCarCategoryInRequest() throws InterruptedException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        openDirectConnect();
        typeDirectConnectId("12345");
        findDriver(this, null);

        // request with default car category
        requestDirectConnect(this);
        verifyRequestsCount(RequestType.DIRECT_CONNECT_REQUEST_200_POST, equalTo(1));
        verifyRequest(RequestType.DIRECT_CONNECT_REQUEST_200_POST,
                hasQueryParam("carCategory", equalTo("PREMIUM")));

        directConnectMissed(this);

        // select another car category
        gotoCarPicker();
        onView(withRecyclerView(R.id.list_car_types).atPosition(0))
                .check(matches(hasDescendant(allOf(withId(R.id.car_category_text), withText("STANDARD"), isDisplayed()))))
                .check(matches(hasDescendant(allOf(withId(R.id.car_selected_icon), not(isDisplayed())))))
                .perform(click());
        onView(navigationIcon()).perform(click());

        resetRequestStats(RequestType.DIRECT_CONNECT_REQUEST_200_POST);
        requestDirectConnect(this);
        verifyRequestsCount(RequestType.DIRECT_CONNECT_REQUEST_200_POST, equalTo(1));
        verifyRequest(RequestType.DIRECT_CONNECT_REQUEST_200_POST,
                hasQueryParam("carCategory", equalTo("REGULAR")));

        directConnectMissed(this);
    }

    @Test
    @TestCases("C2679826")
    public void addNewPayment() throws InterruptedException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        openDirectConnect();
        typeDirectConnectId("12345");
        findDriver(this, null);

        gotoPaymentPicker();

        onView(withId(R.id.add_payment))
                .check(matches(isDisplayed()))
                .perform(click());

        waitFor(condition("Add payment should open")
                .withMatcher(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment_add))));

        int DRAWABLE_UNKNOWN = R.drawable.unknown_cc;
        int DRAWABLE_VISA = R.drawable.visa;
        int VIEW_CARD_ID = R.id.cc_card;
        int VIEW_EXP_ID = R.id.cc_exp;
        int VIEW_CVV_ID = R.id.cc_ccv;

        waitForDisplayed(R.id.btn_add_payment);

        // check submit disabled
        onView(withId(R.id.btn_add_payment)).check(matches(isDisplayed())).check(matches(Matchers.not(isEnabled())));

        // credit card empty
        onView(Matchers.allOf(withId(VIEW_CARD_ID), withHint(R.string.hint_credit_card_number))).check(matches(isDisplayed()));

        // unknown card image shown
        onView(Matchers.allOf(instanceOf(ImageView.class), hasDrawable(DRAWABLE_UNKNOWN))).check(matches(isDisplayed()));

        // enter valid card
        onView(withId(VIEW_CARD_ID)).perform(typeText("4000056655665556"));

        // check card image
        onView(Matchers.allOf(instanceOf(ImageView.class), hasDrawable(DRAWABLE_VISA))).check(matches(isDisplayed()));

        // check submit disabled
        onView(withId(R.id.btn_add_payment)).check(matches(isDisplayed())).check(matches(Matchers.not(isEnabled())));

        waitForDisplayed(VIEW_EXP_ID);

        // enter expiration
        onView(Matchers.allOf(withId(VIEW_EXP_ID), withHint("MM/YY")))
                .check(matches(isDisplayed()))
                .perform(typeText("1220"));

        // check submit disabled
        onView(withId(R.id.btn_add_payment)).check(matches(isDisplayed())).check(matches(Matchers.not(isEnabled())));

        // enter CVV
        onView(Matchers.allOf(withId(VIEW_CVV_ID), withHint("CVV")))
                .check(matches(isDisplayed()))
                .perform(typeText("123"));

        // check submit enabled
        onView(withId(R.id.btn_add_payment)).check(matches(isDisplayed())).check(matches(isEnabled()));

        mockRequests(RequestType.RIDER_CARDS_200_GET, RequestType.RIDER_ADD_CARD_200_POST);

        onView(withId(R.id.btn_add_payment)).perform(click());

        checkPaymentSelected(3, "1234", false);
    }

    @Test
    @TestCases("C2276247")
    public void requestWithoutInternet() throws InterruptedException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        openDirectConnect();
        typeDirectConnectId("12345");
        findDriver(this, null);

        setNetworkError(true);

        onView(withId(R.id.request_driver))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withText(getString(R.string.network_error))).check(matches(isDisplayed()));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }

    @Test
    @TestCases("C2668929")
    public void cancelRequest() throws InterruptedException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        openDirectConnect();
        typeDirectConnectId("12345");
        findDriver(this, null);

        requestDirectConnect(this);
        mockRequests(RequestType.RIDE_CANCEL_200_DELETE);

        onView(withId(R.id.cancel))
                .check(matches(isDisplayed()))
                .perform(click());

        waitForViewInWindow(withText(R.string.direct_connect_cancel_request));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        onView(withId(R.id.request_driver))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));
        onView(withId(R.id.car_picker)).check(matches(isDisplayed()));
        onView(withId(R.id.payment_picker)).check(matches(isDisplayed()));
        onView(withId(R.id.cancel)).check(matches(not(isDisplayed())));
    }

    @Test
    @TestCases({"C2670895", "C2672344"})
    public void backDuringRequest() throws InterruptedException {
        mockConfigWithDirectConnect(this);
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        openDirectConnect();
        typeDirectConnectId("12345");
        findDriver(this, null);

        requestDirectConnect(this);

        // back and press No
        onView(navigationIcon()).perform(click());
        waitForViewInWindow(withText(R.string.direct_connect_cancel_request));
        onView(withId(R.id.md_buttonDefaultNegative)).perform(click());

        onView(withId(R.id.request_driver)).check(matches(not(isDisplayed())));
        onView(withId(R.id.car_picker)).check(matches(not(isDisplayed())));
        onView(withId(R.id.payment_picker)).check(matches(not(isDisplayed())));
        onView(withId(R.id.cancel)).check(matches(isDisplayed())).check(matches(isEnabled()));
        verifyRequestsCount(RequestType.RIDE_CANCEL_200_DELETE, equalTo(0));

        // back and press Yes
        onView(navigationIcon()).perform(click());
        mockRequests(RequestType.RIDE_CANCEL_200_DELETE);
        waitForViewInWindow(withText(R.string.direct_connect_cancel_request));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        verifyRequestsCount(RequestType.RIDE_CANCEL_200_DELETE, equalTo(1));
    }
}
