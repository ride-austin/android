package cases.s5_core_flow.s_bevo_bucks;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
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
import com.rideaustin.api.model.PaymentProvider;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.TimeUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.rideaustin.helpers.PaymentTestHelper.selectPayment;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static org.hamcrest.Matchers.is;

/**
 * Created by Sergey Petrov on 16/08/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class BevoBucksSuite extends BaseUITest {

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
                RequestType.RIDE_MAP_200_GET,
                RequestType.RIDE_RATING_200_PUT,
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET);
    }

    /**
     * Reference: RA-12555
     */
    @Test
    public void shouldSendBevoBucksPaymentMethodInSummary() throws InterruptedException {
        shouldSelectAndSendPaymentMethodInSummary(PaymentProvider.BEVO_BUCKS);
    }

    /**
     * Reference: RA-12555
     */
    @Test
    public void shouldSendCreditCardPaymentMethodInSummary() throws InterruptedException {
        shouldSelectAndSendPaymentMethodInSummary(PaymentProvider.CREDIT_CARD);
    }

    private void shouldSelectAndSendPaymentMethodInSummary(PaymentProvider paymentProvider) throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("valid@email.com", "password");

        // select BB as primary payment
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navPayment));
        selectPayment(this, 3, "Bevo Bucks");
        Espresso.pressBack();

        // go through ride
        NavigationUtils.throughRideRequest(this);
        NavigationUtils.throughRide(this);
        NavigationUtils.throughRideSummary(this,
                response -> {
                    // ride completed now - bevo bucks has expire period
                    response.setCompletedOn(TimeUtils.currentTimeMillis());
                    return response;
                },
                () -> {
                    // bevo container should be visible (according to CONFIG_RIDER_BEVO_BUCKS_200_GET)
                    onView(withId(R.id.bevo_container)).check(matches(isDisplayed()));
                    ViewInteraction selector = onView(withId(R.id.bevo_switch)).check(matches(isChecked()));
                    if (paymentProvider != PaymentProvider.BEVO_BUCKS) {
                        // uncheck switch
                        selector.perform(click());
                    }
                });

        verifyRequest(RequestType.RIDE_RATING_200_PUT, hasQueryParam("paymentProvider", is(paymentProvider.name())));
    }
}
