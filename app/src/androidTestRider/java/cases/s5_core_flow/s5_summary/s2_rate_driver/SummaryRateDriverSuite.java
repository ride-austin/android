package cases.s5_core_flow.s5_summary.s2_rate_driver;

import android.support.test.espresso.Espresso;
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
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.hasQueryParam;
import static com.rideaustin.utils.MatcherUtils.isStarSelected;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.Matchers.condition;
import static com.rideaustin.utils.Matchers.waitFor;
import static com.rideaustin.utils.ViewActionUtils.setRating;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

/**
 * Created on 10/01/2018
 *
 * @author sdelaysam
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SummaryRateDriverSuite extends BaseUITest {

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
    @TestCases("C1930770")
    public void checkRatingBar() throws InterruptedException {
        NavigationUtils.startActivity(activityRule);
        setRideToRate();
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        removeRequests(RequestType.RIDE_RATING_200_PUT);
        mockRequests(RequestType.RIDE_RATING_400_PUT);

        waitForDisplayed(R.id.tv_ride_summary);
        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())));

        checkRating(1.0f, 0, "1.0");
        checkRating(2.0f, 1, "2.0");
        checkRating(3.0f, 2, "3.0");
        checkRating(4.0f, 3, "4.0");
        checkRating(5.0f, 4, "5.0");
    }

    @Test
    @TestCases({"C1930771", "C1930772"})
    public void cantCloseWithoutRating() {
        NavigationUtils.startActivity(activityRule);
        setRideToRate();
        NavigationUtils.throughLogin("valid@email.com", "whatever");

        waitForDisplayed(R.id.tv_ride_summary);
        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())))
                .perform(click());

        onView(withId(R.id.background)).perform(click());
        Espresso.pressBack();

        onView(withId(R.id.rb_rate_driver)).check(matches(isDisplayed()));

        onView(withId(R.id.edt_leave_comment)).perform(click(), typeText("Nice ride"), closeSoftKeyboard());

        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(not(isEnabled())))
                .perform(click());

        onView(withId(R.id.background)).perform(click());
        Espresso.pressBack();

        onView(withId(R.id.rb_rate_driver)).check(matches(isDisplayed()));
    }

    private void setRideToRate() {
        String rideDescription = "Custom ride description from server";
        removeRequests(RequestType.CONFIG_RIDER_200_GET);
        GlobalConfig config = getResponse("CONFIG_GLOBAL_200", GlobalConfig.class);
        config.getRides().setRideSummaryDescription(rideDescription);
        mockRequest(RequestType.CONFIG_RIDER_200_GET, config);

        App.getPrefs().saveRideToRate(1L, USER_1_ID);
        mockRequests(RequestType.RIDE_COMPLETED_200_GET);
    }

    private void checkRating(float rating, int selectedPosition, String paramValue) throws InterruptedException {

        onView(withId(R.id.rb_rate_driver))
                .check(matches(isDisplayed()))
                .perform(setRating(rating));

        onView(withId(R.id.rb_rate_driver))
                .check(matches(isStarSelected(0, selectedPosition >= 0)))
                .check(matches(isStarSelected(1, selectedPosition >= 1)))
                .check(matches(isStarSelected(2, selectedPosition >= 2)))
                .check(matches(isStarSelected(3, selectedPosition >= 3)))
                .check(matches(isStarSelected(4, selectedPosition >= 4)));

        onView(withId(R.id.btn_submit))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .perform(click());

        waitFor(condition("Should show rate error")
                .withView(onView(withText(RiderMockResponseFactory.RATING_ERROR)).inRoot(isToast())));

        verifyRequest(RequestType.RIDE_RATING_400_PUT, hasQueryParam("rating", is(paramValue)));
        resetRequestStats(RequestType.RIDE_RATING_400_PUT);
    }

}
