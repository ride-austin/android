package cases.s12_promocode;

import android.support.annotation.NonNull;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.promocode.PromoCode;
import com.rideaustin.api.model.promocode.PromoCodeBalance;
import com.rideaustin.api.model.promocode.PromoCodeResponse;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.MoneyUtils;
import com.rideaustin.utils.NavigationUtils;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.RecyclerViewMatcher.withRecyclerView;
import static com.rideaustin.RiderMockResponseFactory.INVALID_PROMOCODE_ERROR_MSG;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForNotEmptyText;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class PromoCodesSuite extends BaseUITest {

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
                RequestType.ACDR_REGULAR_200_GET); // has drivers
    }

    @Test
    @TestCases({"C1931055", "C1931056", "C1931057", "C1931058", "C1931072", "C1931071"})
    public void testPromoCodes() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // open promocodes screen
        NavigationUtils.toPromoCodesScreenByRequesting(this, 20);

        // check balance value
        // wait for request button
        waitForCompletelyDisplayed(R.id.balance_value);

        // check value
        onView(allOf(withId(R.id.balance_value), withText(getString(R.string.money, MoneyUtils.format(20))))).check(matches(isDisplayed()));

        // press back
        pressBack();

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        // navigate to promocodes
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navPromotions));

        waitForCompletelyDisplayed(R.id.balance_value);

        // check value
        onView(allOf(withId(R.id.balance_value), withText(getString(R.string.money, MoneyUtils.format(20))))).check(matches(isDisplayed()));

        // check apply button is disabled
        onView(withId(R.id.apply)).check(matches(not(isEnabled())));

        PromoCodeResponse response = getPromoCodeSubmitResponse();
        mockRequest(RequestType.PROMOCODE_POST_200, response);

        // apply new promocode
        onView(withId(R.id.promoCode)).perform(ViewActions.typeText(response.getCodeLiteral()));
        onView(withId(R.id.apply)).perform(click());

        // check success popup message
        waitFor(condition().withView(onView(allOf(withId(com.afollestad.materialdialogs.R.id.md_content), withText(getString(R.string.text_promo_success, response.getCodeLiteral(), 1f))))));
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());

        mockRequest(RequestType.PROMOCODE_REDEMPTIONS_GET_200, getPromoCodesList());

        // open promocodes details
        onView(withId(R.id.credits_balance)).perform(click());

        // check total value
        onView(allOf(withId(R.id.total_balance), withText(getString(R.string.money, MoneyUtils.format(20))))).check(matches(isDisplayed()));

        // check promocodes list
        onView(withRecyclerView(R.id.credits_list)
                .atPosition(0))
                .check(matches(hasDescendant(allOf(withId(R.id.credit_balance), withText(getString(R.string.money, MoneyUtils.format(10)))))))
                .check(matches(hasDescendant(allOf(withId(R.id.credit_code), withText("xxxyyy")))))
                .check(matches(hasDescendant(allOf(withId(R.id.credit_expiration), withText("Expires in 1 day")))))
                .check(matches(hasDescendant(allOf(withId(R.id.credit_ride_qualifier), withText("For your next 9 rides")))));

        // check promocodes list
        onView(withRecyclerView(R.id.credits_list)
                .atPosition(1))
                .check(matches(hasDescendant(allOf(withId(R.id.credit_balance), withText(getString(R.string.money, MoneyUtils.format(1)))))))
                .check(matches(hasDescendant(allOf(withId(R.id.credit_code), withText("qwerty")))))
                .check(matches(hasDescendant(allOf(withId(R.id.credit_expiration), withText("")))))
                .check(matches(hasDescendant(allOf(withId(R.id.credit_ride_qualifier), withText("For your next ride")))));

    }

    @Test
    @TestCases({"C1931062", "C1931063"})
    public void testApplyPromocodeFiled() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // open promocodes screen
        NavigationUtils.toPromoCodesScreenByRequesting(this, 20);

        // check balance value
        // wait for request button
        waitForCompletelyDisplayed(R.id.balance_value);

        mockRequests(RequestType.PROMOCODE_POST_400);

        // apply new promocode
        onView(withId(R.id.promoCode)).perform(ViewActions.typeText("bzzzzzzz"));
        onView(withId(R.id.apply)).perform(click());

        // check success popup message
        waitFor(condition().withView(onView(allOf(withId(com.afollestad.materialdialogs.R.id.md_content), withText(INVALID_PROMOCODE_ERROR_MSG)))));
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());
    }


    @Test
    @TestCases({"C1931078", "C1931079"})
    public void testNoInternet() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);

        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        // map is displayed
        onView(withContentDescription(getString(R.string.google_map))).check(matches(isDisplayed()));

        // wait while pickup address is set
        waitForNotEmptyText(R.id.pickup_address);

        // wait for pin loaded
        waitForDisplayed(R.id.set_pickup_location);

        // go to request ride
        onView(allOf(withId(R.id.set_pickup_location), isDisplayed())).perform(click());

        // wait for request panel
        waitForDisplayed(R.id.requestPanel);

        // check pin view hidden
        onView(withId(R.id.set_pickup_location)).check(matches(not(isDisplayed())));

        // wait for request button
        waitForCompletelyDisplayed(R.id.btn_request_ride);

        // wait for request button
        waitForCompletelyDisplayed(R.id.btn_promo);

        // switch off internet
        setNetworkError(true);

        atomicOnRequests(() -> {
            // mock requested state
            PromoCodeBalance promoCodeBalance = new PromoCodeBalance();
            promoCodeBalance.setRemainder(20);
            mockRequest(RequestType.PROMOCODE_REMAINDER_GET_200, promoCodeBalance);
        });

        // go to request ride
        onView(allOf(withId(R.id.btn_promo), isDisplayed())).perform(click());

        // check network error
        waitForViewInWindow(onView(withText(R.string.network_error)).inRoot(isToast()));

        setNetworkError(false);

        pressBack();

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        // navigate to promocodes
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navPromotions));

        // check balance value
        // wait for request button
        waitForCompletelyDisplayed(R.id.balance_value);

        setNetworkError(true);

        // open promocodes details
        onView(withId(R.id.credits_balance)).perform(click());

        // check network error
        waitForViewInWindow(onView(withText(R.string.network_error)).inRoot(isToast()));
    }

    @Test
    @TestCases("C1931082")
    public void testPromotionsShouldNotBeVisibleDuringTheRide() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        // login
        NavigationUtils.throughLogin("valid@email.com", "whatever");
        // request ride
        NavigationUtils.throughRideRequest(this);
        NavigationUtils.toAssignedState(this);

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        // check promocodes menu is not visible
        onView(withText(R.string.promotions)).check(doesNotExist());

        NavigationUtils.clearRideState(this);
    }

    private List<PromoCode> getPromoCodesList() {
        List<PromoCode> promoCodes = new ArrayList<>();

        PromoCode promoCode1 = new PromoCode();
        DateTime expirationDate = DateTime.now().plusDays(2);
        promoCode1.setCodeLiteral("xxxyyy");
        promoCode1.setRemainingValue(10);
        promoCode1.setTimesUsed(1);
        promoCode1.setMaximumUses(10);
        promoCode1.setExpiresOn(expirationDate.getMillis());

        PromoCode promoCode2 = new PromoCode();
        promoCode2.setCodeLiteral("qwerty");
        promoCode2.setRemainingValue(1);
        promoCode2.setTimesUsed(1);
        promoCode2.setMaximumUses(1);

        promoCodes.add(promoCode1);
        promoCodes.add(promoCode2);
        return promoCodes;
    }

    @NonNull
    private PromoCodeResponse getPromoCodeSubmitResponse() {
        PromoCodeResponse response = new PromoCodeResponse();
        response.setCodeLiteral("promo");
        response.setCodeValue(1);
        return response;
    }

}
