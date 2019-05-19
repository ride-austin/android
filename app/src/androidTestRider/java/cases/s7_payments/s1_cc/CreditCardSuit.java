package cases.s7_payments.s1_cc;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
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
import com.rideaustin.api.StripeManager;
import com.rideaustin.api.model.Payment;
import com.rideaustin.api.model.RiderData;
import com.rideaustin.helpers.PaymentTestHelper;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.ViewActionUtils;
import com.stripe.android.model.Token;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.helpers.PaymentTestHelper.checkPaymentSelected;
import static com.rideaustin.helpers.PaymentTestHelper.selectPayment;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.hasFieldInPost;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.MatcherUtils.withEmptyText;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-12273
 * Created by Sergey Petrov on 02/08/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CreditCardSuit extends BaseUITest {

    private static String VALID_VISA = "4000 0566 5566 5556";
    private static String VALID_MASTERCARD = "5105 1051 0510 5100";
    private static String VALID_AMEX = "3714 496353 98431";
    private static String VALID_DISCOVER = "6011 0009 9013 9424";
    private static String VALID_DINERS = "3852 0000 0232 37";
    private static String VALID_JSB = "3530 1113 3330 0000";
    private static String INVALID_CARD = "3782 10005 822463";
    private static String VALID_EXP = "12/20";
    private static String INVALID_EXP = "12/16";
    private static String CVV = "123";
    private static String CVV_AMEX = "1234";

    private static int VIEW_CARD_ID = com.devmarvel.creditcardentry.R.id.cc_card;
    private static int VIEW_EXP_ID = com.devmarvel.creditcardentry.R.id.cc_exp;
    private static int VIEW_CVV_ID = com.devmarvel.creditcardentry.R.id.cc_ccv;

    private static int DRAWABLE_VISA = com.devmarvel.creditcardentry.R.drawable.visa;
    private static int DRAWABLE_MASTERCARD = com.devmarvel.creditcardentry.R.drawable.master_card;
    private static int DRAWABLE_AMEX = com.devmarvel.creditcardentry.R.drawable.amex;
    private static int DRAWABLE_DISCOVER = com.devmarvel.creditcardentry.R.drawable.discover;
    private static int DRAWABLE_DINERS = com.devmarvel.creditcardentry.R.drawable.diners_club;
    private static int DRAWABLE_JSB = com.devmarvel.creditcardentry.R.drawable.jcb_payment_ico;
    private static int DRAWABLE_UNKNOWN = com.devmarvel.creditcardentry.R.drawable.unknown_cc;

    private static Token STRIPE_TOKEN = new Token("1", false, new Date(), false);

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
                RequestType.ACDR_EMPTY_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CONFIG_RIDER_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.CONFIG_ZIPCODES_200_GET);
    }

    /**
     * Adding first card
     * C1930910: Payments - Visa CC support
     * C1930911: Payments - MasterCard CC support
     * C1930912: Payments - AmericanExpress CC support
     * C1930913: Payments - Discover CC support
     * C1930914: Payments - Dinners Club CC support
     * C1930915: Payments - JCB CC support
     * C1930916: Payments - Invalid CC verification
     * C1930918: Payments - MM/YY Verification - Past MM/YY
     * C1930920: Payments - CVC Verification
     */
    @Test
    @TestCases({"C1930910", "C1930911", "C1930912",
            "C1930913", "C1930914", "C1930915",
            "C1930916", "C1930918", "C1930920"})
    public void addingNewCard() throws InterruptedException {
        removeRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);
        RiderData riderData = getResponse("RIDER_DATA_NO_RIDE_200", RiderData.class);
        riderData.getPayments().clear();
        mockRequest(RequestType.RIDER_DATA_NO_RIDE_200_GET, riderData);

        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        waitForDisplayed(R.id.unmet_requirement);

        // check open add payment
        onView(withId(R.id.unmet_requirement)).perform(click());

        waitForDisplayed(R.id.toolbarTitle);

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment_add))).check(matches(isDisplayed()));

        // go back
        Espresso.pressBack();

        // check map is visible
        onView(withId(R.id.mapContainer)).check(matches(isDisplayed()));

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        // navigate to payments
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navPayment));

        // check its add payment, not payments list
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment_add))).check(matches(isDisplayed()));

        //------------------------------------------------------------------------------------------
        // C1930910: Payments - Visa CC support
        //------------------------------------------------------------------------------------------

        enterValidCard(VALID_VISA, CVV, DRAWABLE_VISA);
        clearCard();

        //------------------------------------------------------------------------------------------
        // C1930911: Payments - MasterCard CC support
        //------------------------------------------------------------------------------------------

        enterValidCard(VALID_MASTERCARD, CVV, DRAWABLE_MASTERCARD);
        clearCard();

        //------------------------------------------------------------------------------------------
        // C1930912: Payments - AmericanExpress CC support
        //------------------------------------------------------------------------------------------

        enterValidCard(VALID_AMEX, CVV_AMEX, DRAWABLE_AMEX);
        clearCard();

        //------------------------------------------------------------------------------------------
        // C1930912: Payments - AmericanExpress CC support
        //------------------------------------------------------------------------------------------

        enterValidCard(VALID_AMEX, CVV_AMEX, DRAWABLE_AMEX);
        clearCard();

        //------------------------------------------------------------------------------------------
        // C1930913: Payments - Discover CC support
        //------------------------------------------------------------------------------------------

        enterValidCard(VALID_DISCOVER, CVV, DRAWABLE_DISCOVER);
        clearCard();

        //------------------------------------------------------------------------------------------
        // C1930914: Payments - Dinners Club CC support
        //------------------------------------------------------------------------------------------

        enterValidCard(VALID_DINERS, CVV, DRAWABLE_DINERS);
        clearCard();

        //------------------------------------------------------------------------------------------
        // C1930915: Payments - JCB CC support
        //------------------------------------------------------------------------------------------

        enterValidCard(VALID_JSB, CVV, DRAWABLE_JSB);
        clearCard();

        //------------------------------------------------------------------------------------------
        // C1930916: Payments - Invalid CC verification
        //------------------------------------------------------------------------------------------

        enterInvalidCard(INVALID_CARD);
        clearCard();

        //------------------------------------------------------------------------------------------
        // C1930918: Payments - MM/YY Verification - Past MM/YY
        //------------------------------------------------------------------------------------------

        enterInvalidExp(VALID_VISA);
        clearCard();

        //------------------------------------------------------------------------------------------
        // C1930920: Payments - CVC Verification
        //------------------------------------------------------------------------------------------

        mockRequest(RequestType.RIDER_CARDS_200_GET, "[]");
        mockRequests(RequestType.RIDER_ADD_CARD_NOT_APPROVED_400_POST);
        StripeManager.tokenProvider = (stripe, card) -> Observable.just(STRIPE_TOKEN);

        enterValidCard(VALID_VISA, CVV, DRAWABLE_VISA);
        onView(withId(R.id.btn_add_payment)).perform(click());

        waitForViewInWindow(onView(withText(RiderMockResponseFactory.CARD_NOT_APPROVED)).inRoot(isToast()));

        verifyRequest(RequestType.RIDER_ADD_CARD_NOT_APPROVED_400_POST, hasFieldInPost("token", equalTo(STRIPE_TOKEN.getId())));
    }

    /**
     * C1930921: Payments - Saved CC
     * C1930922: Payments - Multiple CC support
     */
    @Test
    @TestCases({"C1930921", "C1930922"})
    public void existingCardManagement() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        waitForDisplayed(R.id.mapContainer);

        onView(withId(R.id.unmet_requirement)).check(matches(not(isDisplayed())));

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        // navigate to payments
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navPayment));

        //------------------------------------------------------------------------------------------
        // C1930921: Payments - Saved CC
        // C1930922: Payments - multiple CC support
        //------------------------------------------------------------------------------------------

        // check it is payments list
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment))).check(matches(isDisplayed()));

        // check cards (according to RIDER_DATA_NO_RIDE_200)
        checkPaymentSelected(0, "2222", false);
        checkPaymentSelected(1, "1111", false);
        checkPaymentSelected(2, "0000", true);
    }

    @Test
    public void shouldSelectAnotherPayment() {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        waitForDisplayed(R.id.mapContainer);

        onView(withId(R.id.unmet_requirement)).check(matches(not(isDisplayed())));

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        // navigate to payments
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navPayment));

        checkPaymentSelected(0, "2222", false);
        checkPaymentSelected(2, "0000", true);

        selectPayment(this, 0, "2222");

        verifyRequestsCount(RequestType.RIDER_SELECT_CARD_200_PUT, is(1));
        verifyRequestsCount(RequestType.RIDER_CARDS_200_GET, is(1));

        checkPaymentSelected(0, "2222", true);
        checkPaymentSelected(2, "0000", false);
    }

    @Test
    public void editPayment() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        waitForDisplayed(R.id.mapContainer);

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        // navigate to payments
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navPayment));

        PaymentTestHelper.editPayment(0);

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment_edit))).check(matches(isDisplayed()));

        onView(withId(R.id.text_payment_card))
                .check(matches(withText(getString(R.string.payment_card_template, "2222"))))
                .check(matches(isDisplayed()));

        ViewActionUtils.waitFor("", 2000);

        onView(withId(R.id.expiration_input))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .check(matches(withEmptyText()));

        onView(withId(R.id.cvv_input))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())))
                .check(matches(withEmptyText()));

        onView(withId(R.id.btn_save))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())));

        onView(withId(R.id.expiration_input))
                .perform(typeText("0317"));

        onView(withId(R.id.btn_save))
                .check(matches(not(isEnabled())));

        onView(withId(R.id.expiration_input))
                .perform(typeText("8"));

        onView(withId(R.id.btn_save))
                .check(matches(isEnabled()))
                .perform(click());
    }

    @Test
    public void primaryCardExpired() {
        removeRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);
        RiderData riderData = getResponse("RIDER_DATA_NO_RIDE_200", RiderData.class);
        for (Payment payment : riderData.getPayments()) {
            if (payment.isPrimary()) {
                payment.setExpired(true);
            }
        }
        mockRequest(RequestType.RIDER_DATA_NO_RIDE_200_GET, riderData);

        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        waitForDisplayed(R.id.mapContainer);

        waitForDisplayed(R.id.unmet_requirement);

        onView(withId(R.id.unmet_requirement)).check(matches(withText(R.string.notification_primary_card_expired)));

        // check open payments
        onView(withId(R.id.unmet_requirement)).perform(click());

        waitForDisplayed(R.id.toolbarTitle);

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_payment))).check(matches(isDisplayed()));

    }

    private String getCardNumber(String formatted) {
        return formatted.replace(" ", "");
    }

    private String getExp(String formatted) {
        return formatted.replace("/", "");
    }

    private void enterValidCard(String card, String cvv, int drawable) {
        waitForDisplayed(R.id.btn_add_payment);

        // check submit disabled
        onView(withId(R.id.btn_add_payment)).check(matches(isDisplayed())).check(matches(not(isEnabled())));

        // credit card empty
        onView(allOf(withId(VIEW_CARD_ID), withHint(R.string.hint_credit_card_number))).check(matches(isDisplayed()));

        // unknown card image shown
        onView(allOf(instanceOf(ImageView.class), hasDrawable(DRAWABLE_UNKNOWN))).check(matches(isDisplayed()));

        // enter valid card
        onView(withId(VIEW_CARD_ID)).perform(typeText(getCardNumber(card)));

        // check card image
        onView(allOf(instanceOf(ImageView.class), hasDrawable(drawable))).check(matches(isDisplayed()));

        // check submit disabled
        onView(withId(R.id.btn_add_payment)).check(matches(isDisplayed())).check(matches(not(isEnabled())));

        waitForDisplayed(VIEW_EXP_ID);

        // enter expiration
        onView(allOf(withId(VIEW_EXP_ID), withHint("MM/YY")))
                .check(matches(isDisplayed()))
                .perform(typeText(getExp(VALID_EXP)));

        // check submit disabled
        onView(withId(R.id.btn_add_payment)).check(matches(isDisplayed())).check(matches(not(isEnabled())));

        // enter CVV
        onView(allOf(withId(VIEW_CVV_ID), withHint("CVV")))
                .check(matches(isDisplayed()))
                .perform(typeText(cvv));

        // check submit enabled
        onView(withId(R.id.btn_add_payment)).check(matches(isDisplayed())).check(matches(isEnabled()));
    }

    private void enterInvalidCard(String card) throws InterruptedException {
        waitForDisplayed(R.id.btn_add_payment);

        // check submit disabled
        onView(withId(R.id.btn_add_payment)).check(matches(isDisplayed())).check(matches(not(isEnabled())));

        // credit card empty
        onView(allOf(withId(VIEW_CARD_ID), withHint(R.string.hint_credit_card_number))).check(matches(isDisplayed()));

        // unknown card image shown
        onView(allOf(instanceOf(ImageView.class), hasDrawable(DRAWABLE_UNKNOWN))).check(matches(isDisplayed()));

        // enter invalid card
        onView(withId(VIEW_CARD_ID)).perform(typeText(getCardNumber(card)), closeSoftKeyboard());

        waitFor(condition("Expiration date should not be shown")
                .withMatcher(withId(VIEW_EXP_ID))
                .withCheck(not(isDisplayed()))
                .withDelay(1000));

        // check submit disabled
        onView(withId(R.id.btn_add_payment)).check(matches(isDisplayed())).check(matches(not(isEnabled())));
    }

    private void enterInvalidExp(String card) {
        waitForDisplayed(R.id.btn_add_payment);

        // check submit disabled
        onView(withId(R.id.btn_add_payment)).check(matches(isDisplayed())).check(matches(not(isEnabled())));

        // credit card empty
        onView(allOf(withId(VIEW_CARD_ID), withHint(R.string.hint_credit_card_number))).check(matches(isDisplayed()));

        // unknown card image shown
        onView(allOf(instanceOf(ImageView.class), hasDrawable(DRAWABLE_UNKNOWN))).check(matches(isDisplayed()));

        // enter card
        onView(withId(VIEW_CARD_ID)).perform(typeText(getCardNumber(card)));

        waitForCompletelyDisplayed(VIEW_EXP_ID);

        // enter invalid expiration
        onView(allOf(withId(VIEW_EXP_ID), withHint("MM/YY")))
                .check(matches(isDisplayed()))
                .perform(typeText(getExp(INVALID_EXP)));

        // check focus not moved
        onView(withId(VIEW_EXP_ID)).check(matches(hasFocus()));
        onView(withId(VIEW_CVV_ID)).check(matches(not(hasFocus())));

        // check submit disabled
        onView(withId(R.id.btn_add_payment)).check(matches(isDisplayed())).check(matches(not(isEnabled())));

        onView(withId(VIEW_EXP_ID)).perform(closeSoftKeyboard());
    }

    private void clearCard() {
        Espresso.pressBack();
        onView(withId(R.id.unmet_requirement)).perform(click());
        // something wrong with clearText() inside those custom inputs
//        onView(withId(VIEW_CVV_ID)).perform(clearText());
//        onView(withId(VIEW_EXP_ID)).perform(clearText());
//        onView(withId(VIEW_CARD_ID)).perform(clearText());
    }

}
