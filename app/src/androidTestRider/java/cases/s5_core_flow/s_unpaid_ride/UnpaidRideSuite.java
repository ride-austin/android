package cases.s5_core_flow.s_unpaid_ride;

import android.os.RemoteException;
import android.support.test.espresso.Espresso;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.App;
import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.api.model.RiderData;
import com.rideaustin.api.model.UnpaidBalance;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.DeviceTestUtils;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.TimeUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.helpers.PaymentTestHelper.checkPaymentSelected;
import static com.rideaustin.helpers.PaymentTestHelper.selectPayment;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotEmptyText;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Created by Sergey Petrov on 18/08/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class UnpaidRideSuite extends BaseUITest {

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
                RequestType.CONFIG_RIDER_BEVO_BUCKS_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.CONFIG_ZIPCODES_200_GET,
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET,
                RequestType.ACDR_REGULAR_200_GET, // has drivers
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET);
    }

    /**
     * Reference: RA-12592
     */
    @Test
    public void shouldShowReminderOnStartAndDisappear() throws InterruptedException {
        long willChargeAfter = 10000; // 10 sec

        removeRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);
        UnpaidBalance unpaid = new UnpaidBalance();
        unpaid.setWillChargeOn(TimeUtils.currentTimeMillis() + willChargeAfter);
        RiderData riderData = getResponse("RIDER_DATA_NO_RIDE_200", RiderData.class);
        riderData.setUnpaid(unpaid);
        mockRequest(RequestType.RIDER_DATA_NO_RIDE_200_GET, riderData);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // check reminder is visible
        onView(withId(R.id.unmet_requirement)).check(matches(isDisplayed()));

        // mock empty unpaid response
        mockRequests(RequestType.UNPAID_EMPTY_GET_200);

        // reminder should disappear after 10 seconds (start-up time included)
        waitFor(condition("Unpaid reminder should disappear")
                .withMatcher(withId(R.id.unmet_requirement))
                .withCheck(not(isDisplayed()))
                .withTimeout(willChargeAfter));

        verifyRequestsCount(RequestType.UNPAID_EMPTY_GET_200, is(1));
    }

    /**
     * Reference: RA-12592
     */
    @Test
    public void shouldShowReminderAndToast() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // no reminder according to RIDER_DATA_NO_RIDE_200_GET
        onView(withId(R.id.unmet_requirement)).check(matches(not(isDisplayed())));

        // go to request ride
        waitForNotEmptyText(R.id.pickup_address);
        waitForDisplayed(R.id.set_pickup_location);
        onView(allOf(withId(R.id.set_pickup_location), isDisplayed())).perform(click());
        waitForDisplayed(R.id.requestPanel);
        waitForCompletelyDisplayed(R.id.btn_request_ride);

        // request a ride with unpaid balance
        mockRequests(RequestType.RIDE_REQUEST_402_POST,
                RequestType.CURRENT_RIDE_EMPTY_200_GET,
                RequestType.UNPAID_BEVO_BUCKS_GET_200);

        onView(withId(R.id.btn_request_ride)).perform(click());

        waitFor(condition().withMatcher(withText(RiderMockResponseFactory.UNPAID_RIDE)));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // wait for reminder
        waitForDisplayed(R.id.unmet_requirement);
    }

    /**
     * Reference: RA-12668
     */
    @Test
    public void shouldShowUnpaidBalanceInPayments() throws InterruptedException {
        long willChargeAfter = 10000; // 10 sec
        String amount = "100";
        String money = getString(R.string.money, amount);

        removeRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);
        UnpaidBalance unpaid = new UnpaidBalance();
        unpaid.setAmount(amount);
        unpaid.setWillChargeOn(TimeUtils.currentTimeMillis() + willChargeAfter);
        RiderData riderData = getResponse("RIDER_DATA_NO_RIDE_200", RiderData.class);
        riderData.setUnpaid(unpaid);
        mockRequest(RequestType.RIDER_DATA_NO_RIDE_200_GET, riderData);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // tap on reminder
        waitFor(condition("Should show unpaid rides reminder")
                .withMatcher(allOf(withId(R.id.unmet_requirement), withText("You have unpaid rides!"))));
        onView(withId(R.id.unmet_requirement)).perform(click());

        // check payments screen opened
        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment)),
                "Wait for payments screen");

        // check unpaid is shown
        onView(withId(R.id.unpaid_title))
                // according to config
                .check(matches(withText("Unpaid balance!")))
                .check(matches(isDisplayed()));

        onView(withId(R.id.unpaid_sub_title))
                // according to config
                .check(matches(withText("You need to pay your balance to be able to take another trip!")))
                .check(matches(isDisplayed()));

        onView(withId(R.id.unpaid_amount))
                .check(matches(withText(money)))
                .check(matches(isDisplayed()));

        // mock empty unpaid response
        mockRequests(RequestType.UNPAID_EMPTY_GET_200);

        // balance should disappear after 10 seconds (start-up time included)
        waitFor(condition("Unpaid balance should disappear")
                .withMatcher(withId(R.id.unpaid_title))
                .withCheck(not(isDisplayed()))
                .withTimeout(willChargeAfter));

        onView(withId(R.id.unpaid_sub_title)).check(matches(not(isDisplayed())));
        onView(withId(R.id.unpaid_amount)).check(matches(not(isDisplayed())));

        verifyRequestsCount(RequestType.UNPAID_EMPTY_GET_200, greaterThan(0));
    }

    /**
     * Reference: RA-12692
     */
    @Test
    public void shouldShowUnpaidUi() throws InterruptedException {
        long willChargeAfter = 10000; // 10 sec
        String amount = "100";
        String money = getString(R.string.money, amount);

        removeRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);
        UnpaidBalance unpaid = new UnpaidBalance();
        unpaid.setAmount(amount);
        unpaid.setWillChargeOn(TimeUtils.currentTimeMillis() + willChargeAfter);
        RiderData riderData = getResponse("RIDER_DATA_NO_RIDE_200", RiderData.class);
        riderData.setUnpaid(unpaid);
        mockRequest(RequestType.RIDER_DATA_NO_RIDE_200_GET, riderData);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // tap on reminder
        waitFor(condition("Should show unpaid rides reminder")
                .withMatcher(allOf(withId(R.id.unmet_requirement), withText("You have unpaid rides!"))));
        onView(withId(R.id.unmet_requirement)).perform(click());

        // check payments screen opened
        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment)),
                "Wait for payments screen");

        // check unpaid is shown
        waitForDisplayed(R.id.unpaid_item);
        onView(withId(R.id.unpaid_item)).perform(click());

        // check unpaid screen opened
        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText("Unpaid balance!")),
                "Wait for unpaid screen");

        onView(withId(R.id.unpaid_sub_title))
                // according to config
                .check(matches(withText("You need to pay your balance to be able to take another trip!")))
                .check(matches(isDisplayed()));

        onView(withId(R.id.unpaid_amount))
                .check(matches(withText(money)))
                .check(matches(isDisplayed()));

        onView(withId(R.id.text_payment_card))
                // according to RIDER_DATA_NO_RIDE_200_GET
                .check(matches(withText(containsString("0000"))))
                .check(matches(isDisplayed()));

        onView(withId(R.id.icon_payment_type))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btn_submit))
                .check(matches(isEnabled()))
                .check(matches(isDisplayed()));

        // mock empty unpaid response
        mockRequests(RequestType.UNPAID_EMPTY_GET_200);

        // unpaid should disappear after 10 seconds (start-up time included)
        waitFor(condition("Should show payments screen")
                .withMatcher(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment)))
                .withTimeout(willChargeAfter));

        // no unpaid on payments screen
        onView(withId(R.id.unpaid_sub_title)).check(matches(not(isDisplayed())));
        onView(withId(R.id.unpaid_amount)).check(matches(not(isDisplayed())));

        verifyRequestsCount(RequestType.UNPAID_EMPTY_GET_200, greaterThan(0));
    }

    /**
     * Reference: RA-12692
     */
    @Test
    public void shouldShowSelectedPaymentMethod() throws InterruptedException {
        long willChargeAfter = 1000000;
        String amount = "100";

        removeRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);
        UnpaidBalance unpaid = new UnpaidBalance();
        unpaid.setAmount(amount);
        unpaid.setWillChargeOn(TimeUtils.currentTimeMillis() + willChargeAfter);
        RiderData riderData = getResponse("RIDER_DATA_NO_RIDE_200", RiderData.class);
        riderData.setUnpaid(unpaid);
        mockRequest(RequestType.RIDER_DATA_NO_RIDE_200_GET, riderData);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // tap on reminder
        waitFor(condition("Should show unpaid rides reminder")
                .withMatcher(allOf(withId(R.id.unmet_requirement), withText("You have unpaid rides!"))));
        onView(withId(R.id.unmet_requirement)).perform(click());

        // check payments screen opened
        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment)),
                "Wait for payments screen");

        // check unpaid is shown
        waitForDisplayed(R.id.unpaid_item);

        // check selected payment
        checkPaymentSelected(2, "0000", true);
        verifyPrimaryPaymentUsed("0000");

        // select another card
        selectPayment(this, 1, "1111");
        verifyPrimaryPaymentUsed("1111");

        // select BB
        selectPayment(this, 3, "Bevo Bucks");
        verifyPrimaryPaymentUsed("Bevo Bucks");
    }

    /**
     * Reference: RA-12692
     */
    @Test
    public void shouldPayWithRegularPayment() throws InterruptedException {
        long willChargeAfter = 1000000;
        String amount = "100";

        removeRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);
        UnpaidBalance unpaid = new UnpaidBalance();
        unpaid.setAmount(amount);
        unpaid.setWillChargeOn(TimeUtils.currentTimeMillis() + willChargeAfter);
        RiderData riderData = getResponse("RIDER_DATA_NO_RIDE_200", RiderData.class);
        riderData.setUnpaid(unpaid);
        mockRequest(RequestType.RIDER_DATA_NO_RIDE_200_GET, riderData);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // tap on reminder
        waitFor(condition("Should show unpaid rides reminder")
                .withMatcher(allOf(withId(R.id.unmet_requirement), withText("You have unpaid rides!"))));
        onView(withId(R.id.unmet_requirement)).perform(click());

        // check payments screen opened
        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment)),
                "Wait for payments screen");

        // check unpaid is shown
        waitForDisplayed(R.id.unpaid_item);
        onView(withId(R.id.unpaid_item)).perform(click());

        // check unpaid screen opened
        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText("Unpaid balance!")),
                "Wait for unpaid screen");

        onView(withId(R.id.btn_submit))
                .check(matches(isEnabled()))
                .check(matches(isDisplayed()));

        mockRequests(RequestType.UNPAID_POST_200, RequestType.UNPAID_EMPTY_GET_200);

        onView(withId(R.id.btn_submit)).perform(click());

        // should show success
        waitFor(condition("Should show success")
                .withMatcher(withText(R.string.send_message_title)));

        // unpaid should disappear
        waitFor(condition("Should show payments screen")
                .withMatcher(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment))));

        // no unpaid on payments screen
        onView(withId(R.id.unpaid_sub_title)).check(matches(not(isDisplayed())));
        onView(withId(R.id.unpaid_amount)).check(matches(not(isDisplayed())));


        verifyRequestsCount(RequestType.UNPAID_POST_200, is(1));
        verifyRequestsCount(RequestType.UNPAID_EMPTY_GET_200, greaterThan(0));
    }

    /**
     * Reference: RA-12692
     */
    @Test
    public void shouldReturnToPaymentsAfterPayInBackground() throws InterruptedException, RemoteException, UiObjectNotFoundException {
        long willChargeAfter = 1000000;
        String amount = "100";

        removeRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);
        UnpaidBalance unpaid = new UnpaidBalance();
        unpaid.setAmount(amount);
        unpaid.setWillChargeOn(TimeUtils.currentTimeMillis() + willChargeAfter);
        RiderData riderData = getResponse("RIDER_DATA_NO_RIDE_200", RiderData.class);
        riderData.setUnpaid(unpaid);
        mockRequest(RequestType.RIDER_DATA_NO_RIDE_200_GET, riderData);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // tap on reminder
        waitFor(condition("Should show unpaid rides reminder")
                .withMatcher(allOf(withId(R.id.unmet_requirement), withText("You have unpaid rides!"))));
        onView(withId(R.id.unmet_requirement)).perform(click());

        // check payments screen opened
        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment)),
                "Wait for payments screen");

        // check unpaid is shown
        waitForDisplayed(R.id.unpaid_item);
        onView(withId(R.id.unpaid_item)).perform(click());

        // check unpaid screen opened
        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText("Unpaid balance!")),
                "Wait for unpaid screen");

        onView(withId(R.id.btn_submit))
                .check(matches(isEnabled()))
                .check(matches(isDisplayed()));

        mockRequests(RequestType.UNPAID_POST_200, RequestType.UNPAID_EMPTY_GET_200);

        onView(withId(R.id.btn_submit)).perform(click());

        // send to background and restore after some time
        DeviceTestUtils.pressHome();
        DeviceTestUtils.restoreFromRecentApps(3000);

        // unpaid should disappear
        waitFor(condition("Should show payments screen")
                .withMatcher(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment))));

        // no unpaid on payments screen
        onView(withId(R.id.unpaid_sub_title)).check(matches(not(isDisplayed())));
        onView(withId(R.id.unpaid_amount)).check(matches(not(isDisplayed())));


        verifyRequestsCount(RequestType.UNPAID_POST_200, is(1));
        verifyRequestsCount(RequestType.UNPAID_EMPTY_GET_200, greaterThan(0));
    }

    @Test
    public void shouldPayWithBevoBucks() throws InterruptedException {
        long willChargeAfter = 1000000;
        String amount = "100";
        String url = "http://google.com";

        removeRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);
        UnpaidBalance unpaid = new UnpaidBalance();
        unpaid.setAmount(amount);
        unpaid.setWillChargeOn(TimeUtils.currentTimeMillis() + willChargeAfter);
        unpaid.setBevoBucksUrl(url);
        RiderData riderData = getResponse("RIDER_DATA_NO_RIDE_200", RiderData.class);
        riderData.setUnpaid(unpaid);
        mockRequest(RequestType.RIDER_DATA_NO_RIDE_200_GET, riderData);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // tap on reminder
        waitFor(condition("Should show unpaid rides reminder")
                .withMatcher(allOf(withId(R.id.unmet_requirement), withText("You have unpaid rides!"))));
        onView(withId(R.id.unmet_requirement)).perform(click());

        // check payments screen opened
        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment)),
                "Wait for payments screen");

        // check unpaid is shown
        waitForDisplayed(R.id.unpaid_item);

        // select BB
        selectPayment(this, 3, "Bevo Bucks");

        onView(withId(R.id.unpaid_item)).perform(click());

        // check unpaid screen opened
        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText("Unpaid balance!")),
                "Wait for unpaid screen");

        onView(withId(R.id.btn_submit))
                .check(matches(isEnabled()))
                .check(matches(isDisplayed()))
                .perform(click());

        // check unpaid screen opened
        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText(R.string.pay_with_bevobucks)),
                "Wait for bevo bucks screen");

        // simulate pay success
        mockRequests(RequestType.UNPAID_EMPTY_GET_200);

        Espresso.pressBack();

        // check payments screen opened
        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment)),
                "Wait for payments screen");

        // no unpaid on payments screen
        onView(withId(R.id.unpaid_sub_title)).check(matches(not(isDisplayed())));
        onView(withId(R.id.unpaid_amount)).check(matches(not(isDisplayed())));

        verifyRequestsCount(RequestType.UNPAID_EMPTY_GET_200, greaterThan(0));

    }

    private void verifyPrimaryPaymentUsed(String name) {
        // go to unpaid
        onView(withId(R.id.unpaid_item)).perform(click());

        // check unpaid screen opened
        waitForDisplayed(allOf(withId(R.id.toolbarTitle), withText("Unpaid balance!")),
                "Wait for unpaid screen");

        // verify primary card selected
        onView(withId(R.id.text_payment_card))
                .check(matches(withText(containsString(name))))
                .check(matches(isDisplayed()));

        Espresso.pressBack();
    }
}
