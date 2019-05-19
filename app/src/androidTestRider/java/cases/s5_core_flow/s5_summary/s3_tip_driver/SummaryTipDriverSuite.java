package cases.s5_core_flow.s5_summary.s3_tip_driver;

import android.support.annotation.IdRes;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.App;
import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.CarTypeConfiguration;
import com.rideaustin.api.model.Ride;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.rate.RateDriverDialogViewModel;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.SerializationHelper;
import com.rideaustin.utils.TimeUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.MatcherUtils.withEmptyText;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.setRating;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;

/**
 * Created on 10/01/2018
 *
 * @author sdelaysam
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SummaryTipDriverSuite extends BaseUITest {

    private static final int USER_1_ID = 1443;
    private static final int MAX_TIP = 125;

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
                RequestType.ACDR_REGULAR_200_GET, // has drivers
                RequestType.RIDE_MAP_200_GET,
                RequestType.RIDE_RATING_200_PUT,
                RequestType.RIDE_CANCELLATION_SETTINGS_200_GET,
                RequestType.CHARITIES_200_GET);
    }

    @Test
    @TestCases({"C1930773", "C1930774", "C1930781"})
    public void lookAndFeel() {
        NavigationUtils.startActivity(activityRule);
        setRideToRate(true, 60);
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        waitForDisplayed(R.id.tv_ride_summary);
        onView(withId(R.id.radio_group_tips)).check(matches(isDisplayed()));
        onView(withId(R.id.custom_tip)).check(matches(not(isDisplayed())));
        onView(withId(R.id.tips_no))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()));
        onView(withId(R.id.tips_one))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()));
        onView(withId(R.id.tips_two))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()));
        onView(withId(R.id.tips_five))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()));
        onView(withId(R.id.tips_other))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()));
        onView(withId(R.id.custom_tip))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.tips_other))
                .perform(click())
                .check(matches(isChecked()));
        onView(withId(R.id.custom_tip))
                .check(matches(isDisplayed()));

        onView(withId(R.id.custom_tip)).perform(typeText("1000"), closeSoftKeyboard());
        onView(withId(R.id.custom_tip)).check(matches(withText(String.valueOf(MAX_TIP))));
    }

    @Test
    @TestCases("C1930775")
    public void enterOtherAmount() {
        NavigationUtils.startActivity(activityRule);
        setRideToRate(true, 60);
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        waitForDisplayed(R.id.tv_ride_summary);
        onView(withId(R.id.radio_group_tips)).check(matches(isDisplayed()));
        onView(withId(R.id.custom_tip)).check(matches(not(isDisplayed())));

        onView(withId(R.id.tips_other))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()))
                .perform(click())
                .check(matches(isChecked()));

        onView(withId(R.id.tips_no))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()));
        onView(withId(R.id.tips_one))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()));
        onView(withId(R.id.tips_two))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()));
        onView(withId(R.id.tips_five))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()));

        onView(withId(R.id.custom_tip))
                .check(matches(isDisplayed()))
                .check(matches(withEmptyText()))
                .perform(typeText("10"), closeSoftKeyboard())
                .check(matches(withText("10")));

        onView(withId(R.id.tips_no))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()))
                .perform(click())
                .check(matches(isChecked()));

        onView(withId(R.id.custom_tip))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.tips_one))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()));
        onView(withId(R.id.tips_two))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()));
        onView(withId(R.id.tips_five))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()));
        onView(withId(R.id.tips_other))
                .check(matches(isDisplayed()))
                .check(matches(isNotChecked()))
                .perform(click());

        onView(withId(R.id.custom_tip))
                .check(matches(isDisplayed()))
                .check(matches(withEmptyText()));
    }

    @Test
    @TestCases({"C1930776", "C1930778"})
    public void submitCorrectValue() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        setRideToRate(true, 60);
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        removeRequests(RequestType.RIDE_RATING_200_PUT);
        mockRequests(RequestType.RIDE_RATING_400_PUT);

        waitForDisplayed(R.id.tv_ride_summary);
        onView(withId(R.id.radio_group_tips)).check(matches(isDisplayed()));
        onView(withId(R.id.custom_tip)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())));

        onView(withId(R.id.rb_rate_driver)).perform(setRating(5.0f));

        checkTip(R.id.tips_no, 0.0);
        checkTip(R.id.tips_one, 1.0);
        checkTip(R.id.tips_two, 2.0);
        checkTip(R.id.tips_five, 5.0);
        checkTip(R.id.tips_other, 25.0);
        checkTip(R.id.tips_other, 123.0);
    }

    @Test
    @TestCases({"C1930779", "C1930780", "C1930782"})
    public void timeLimitExpired() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        setRideToRate(true, 20);
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        waitForDisplayed(R.id.tv_ride_summary);
        onView(withId(R.id.rb_rate_driver)).perform(setRating(5.0f));
        onView(withId(R.id.radio_group_tips)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())));

        waitFor(condition("Tip controls should disappear")
                .withMatcher(withId(R.id.radio_group_tips))
                .withCheck(not(isDisplayed())));

        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .perform(click());

        verifyRequestsCount(RequestType.RIDE_RATING_200_PUT, is(1));
        verifyRequest(RequestType.RIDE_RATING_200_PUT, hasQueryParam("tip", is("0.0")));
    }

    @Test
    @TestCases("C1930783")
    public void tippingDisabled() {
        NavigationUtils.startActivity(activityRule);
        setRideToRate(false, 60);
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        waitForDisplayed(R.id.tv_ride_summary);
        onView(withId(R.id.radio_group_tips)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())));

        onView(withId(R.id.rb_rate_driver)).perform(setRating(5.0f));
        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .perform(click());

        verifyRequestsCount(RequestType.RIDE_RATING_200_PUT, is(1));
        verifyRequest(RequestType.RIDE_RATING_200_PUT, hasQueryParam("tip", is("0.0")));
    }

    @Test
    @TestCases("C1930784")
    public void tippingDisabledInCarConfig() {
        NavigationUtils.startActivity(activityRule);
        setRideToRate(true, 60, false);
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        waitForDisplayed(R.id.tv_ride_summary);
        onView(withId(R.id.radio_group_tips)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())));

        onView(withId(R.id.rb_rate_driver)).perform(setRating(5.0f));
        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .perform(click());

        verifyRequestsCount(RequestType.RIDE_RATING_200_PUT, is(1));
        verifyRequest(RequestType.RIDE_RATING_200_PUT, hasQueryParam("tip", is("0.0")));
    }

    private void setRideToRate(boolean tippingEnabled, long tippingDelaySec) {
        setRideToRate(tippingEnabled, tippingDelaySec, true);
    }

    private void setRideToRate(boolean tippingEnabled, long tippingDelaySec, boolean enabledInCarType) {
        String rideDescription = "Custom ride description from server";
        removeRequests(RequestType.CONFIG_RIDER_200_GET);
        GlobalConfig config = getResponse("CONFIG_GLOBAL_200", GlobalConfig.class);
        config.getRides().setRideSummaryDescription(rideDescription);
        // NOTE: server doesn't sent tipping enabled flag
        // these tests were made according to test cases
        // which assume that system has that feature
        // also see Tipping class
        if (!tippingEnabled) {
            config.getTipping().setEnabled(tippingEnabled);
        }
        config.getTipping().setRideTipLimit(MAX_TIP);
        config.getTipping().setRidePaymentDelay(tippingDelaySec);
        mockRequest(RequestType.CONFIG_RIDER_200_GET, config);

        App.getPrefs().saveRideToRate(1L, USER_1_ID);
        Ride ride = getResponse("RIDE_COMPLETED_200", Ride.class);
        if (!enabledInCarType) {
            CarTypeConfiguration configuration = new CarTypeConfiguration();
            configuration.setDisableTipping(true);
            ride.getRequestedCarType().setConfiguration(SerializationHelper.serialize(configuration));
        }
        ride.setCompletedOn(TimeUtils.currentTimeMillis());
        mockRequest(RequestType.RIDE_COMPLETED_200_GET, ride);
    }

    private void checkTip(@IdRes int id, double value) throws InterruptedException {
        onView(withId(id)).perform(click());

        if (id == R.id.tips_other) {
            String text = String.valueOf((int)value);
            onView(withId(R.id.custom_tip))
                    .check(matches(isDisplayed()))
                    .perform(clearText(), typeText(text), closeSoftKeyboard())
                    .check(matches(withText(text)));
        }

        onView(withId(R.id.btn_submit))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .perform(click());

        if (value >= RateDriverDialogViewModel.CONFIRMATION_THRESHOLD) {
            waitFor(condition("Should show tipping confirmation")
                    .withMatcher(withText(getString(R.string.confirm_tipping_content, value))));
            onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
        }

        waitFor(condition("Should show rate error")
                .withView(onView(withText(RiderMockResponseFactory.RATING_ERROR)).inRoot(isToast())));

        verifyRequest(RequestType.RIDE_RATING_400_PUT, hasQueryParam("tip", is(String.valueOf(value))));
        resetRequestStats(RequestType.RIDE_RATING_400_PUT);

    }

}
