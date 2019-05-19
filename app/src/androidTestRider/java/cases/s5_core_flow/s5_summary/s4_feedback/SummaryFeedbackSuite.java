package cases.s5_core_flow.s5_summary.s4_feedback;

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
import com.rideaustin.api.model.Ride;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.NavigationUtils;
import com.rideaustin.utils.TimeUtils;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.MatcherUtils.noQueryParam;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.ViewActionUtils.setRating;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;

import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.MatcherUtils.withEmptyText;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static org.hamcrest.core.Is.is;


/**
 * Created on 24/01/2018
 *
 * @author sdelaysam
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SummaryFeedbackSuite extends BaseUITest {

    private static final int USER_1_ID = 1443;

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
    @TestCases("C1930785")
    public void sendLongText() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        setRideToRate(false, 60);
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        waitForDisplayed(R.id.tv_ride_summary);
        onView(withId(R.id.rb_rate_driver)).perform(setRating(5.0f));

        String str = "ABCD";
        int repeat = (int) Math.ceil((Constants.MAX_COMMENT_LENGTH + 1)/str.length());

        String longText = StringUtils.repeat(str, repeat);
        String fitText = longText.substring(0, Constants.MAX_COMMENT_LENGTH);

        onView(withId(R.id.edt_leave_comment))
                .check(matches(isDisplayed()))
                .perform(replaceText(longText));

        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .perform(click());

        verifyRequestsCount(RequestType.RIDE_RATING_200_PUT, is(1));
        verifyRequest(RequestType.RIDE_RATING_200_PUT, hasQueryParam("comment", is(fitText)));
    }

    @Test
    @TestCases("C1930786")
    public void commentIsOptional() {
        NavigationUtils.startActivity(activityRule);
        setRideToRate(false, 60);
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        waitForDisplayed(R.id.tv_ride_summary);
        onView(withId(R.id.rb_rate_driver)).perform(setRating(5.0f));

        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .perform(click());

        verifyRequestsCount(RequestType.RIDE_RATING_200_PUT, is(1));
        verifyRequest(RequestType.RIDE_RATING_200_PUT, noQueryParam("comment"));
    }

    private void setRideToRate(boolean tippingEnabled, long tippingDelaySec) {
        String rideDescription = "Custom ride description from server";
        removeRequests(RequestType.CONFIG_RIDER_200_GET);
        GlobalConfig config = getResponse("CONFIG_GLOBAL_200", GlobalConfig.class);
        config.getRides().setRideSummaryDescription(rideDescription);
        config.getTipping().setEnabled(tippingEnabled);
        config.getTipping().setRidePaymentDelay(tippingDelaySec);
        mockRequest(RequestType.CONFIG_RIDER_200_GET, config);

        App.getPrefs().saveRideToRate(1L, USER_1_ID);
        Ride ride = getResponse("RIDE_COMPLETED_200", Ride.class);
        ride.setCompletedOn(TimeUtils.currentTimeMillis());
        mockRequest(RequestType.RIDE_COMPLETED_200_GET, ride);
    }


}
