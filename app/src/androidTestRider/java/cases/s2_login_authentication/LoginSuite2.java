package cases.s2_login_authentication;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;

/**
 * This suit is for cases not mentioned in TestRails
 * Created by Sergey Petrov on 30/05/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginSuite2 extends BaseUITest {

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
                RequestType.CONFIG_ZIPCODES_200_GET,
                RequestType.LOGOUT_200_POST);
    }

    /**
     * RA-10871: Unable to sign in previously received error from server for GET /rest/riders/{id}/cards request
     */
    @Test
    public void shouldLoginWithOtherCredentialsAfterCardsError() {
        // simulate 500 error
        removeRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);
        mockRequests(RequestType.RIDER_DATA_500_GET);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("bad_cards@xo.com", "password");

        // wait for error
        waitForViewInWindow(withText(RiderMockResponseFactory.INTERNAL_SERVER_ERROR));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // switch to normal response
        removeRequests(RequestType.RIDER_DATA_500_GET);
        mockRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);

        waitForDisplayed(navigationIcon(), "Wanna get back");
        onView(navigationIcon()).perform(click());

        // login again
        NavigationUtils.throughLogin("good_cards@xo.com", "password");

        // check map is visible
        onView(withId(R.id.mapContainer)).check(matches(isDisplayed()));
    }


}
