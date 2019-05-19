package cases_e2e.s2_login_authentication;

import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseE2ETest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.TestCases;
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
import static com.rideaustin.RiderMockResponseFactory.INVALID_CREDENTIALS;
import static com.rideaustin.TestConstants.ACTIVE_RIDER_EMAIL;
import static com.rideaustin.TestConstants.ACTIVE_RIDER_NAME;
import static com.rideaustin.TestConstants.ACTIVE_RIDER_PASSWORD;
import static com.rideaustin.TestConstants.DISABLED_RIDER_EMAIL;
import static com.rideaustin.TestConstants.DISABLED_RIDER_PASSWORD;
import static com.rideaustin.TestConstants.FACEBOOK_EMAIL;
import static com.rideaustin.TestConstants.FACEBOOK_NAME;
import static com.rideaustin.TestConstants.FACEBOOK_PASSWORD;
import static com.rideaustin.TestConstants.INACTIVE_RIDER_EMAIL;
import static com.rideaustin.TestConstants.INACTIVE_RIDER_NAME;
import static com.rideaustin.TestConstants.INACTIVE_RIDER_PASSWORD;
import static com.rideaustin.helpers.LoginTestHelper.checkName;
import static com.rideaustin.helpers.LoginTestHelper.logout;
import static com.rideaustin.helpers.LoginTestHelper.throughFacebookLogin;
import static com.rideaustin.helpers.LoginTestHelper.throughLogin;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by Sergey Petrov on 04/08/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginSuite extends BaseE2ETest {

    @Rule
    public ActivityTestRule<NavigationDrawerActivity> activityRule = new RaActivityRule<>(NavigationDrawerActivity.class, false, false);

    @Test
    @TestCases("C1273806")
    public void signInInactiveRider() {
        NavigationUtils.startActivity(activityRule);

        throughLogin(INACTIVE_RIDER_EMAIL, INACTIVE_RIDER_PASSWORD);

        closeRoundUpDialog();

        checkName(INACTIVE_RIDER_NAME);
    }

    @Test
    @TestCases("C1930339")
    public void signInViaEmail() {
        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(R.id.signIn);

        // go to login
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());

        // check title
        onView(withId(R.id.toolbarTitle)).check(matches(withText(R.string.title_sign_in)));

        // check toolbar navigate back
        onView(navigationIcon()).perform(click());

        throughLogin(ACTIVE_RIDER_EMAIL, ACTIVE_RIDER_PASSWORD);

        closeRoundUpDialog();

        checkName(ACTIVE_RIDER_NAME);

        logout();
    }

    @Test
    @TestCases("C1930340")
    public void signInInvalidCredentials() {
        NavigationUtils.startActivity(activityRule);

        throughLogin(ACTIVE_RIDER_EMAIL, "invalidpassword");

        // check login failed
        onView(withText(INVALID_CREDENTIALS)).check(matches(isDisplayed()));
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());
    }

    @Test
    @TestCases("C1273812")
    public void signInDisabledRider() {
        NavigationUtils.startActivity(activityRule);

        throughLogin(DISABLED_RIDER_EMAIL, DISABLED_RIDER_PASSWORD);

        // check login failed
        onView(withText(INVALID_CREDENTIALS)).check(matches(isDisplayed()));
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());

    }

    /**
     * Will work only on device with EN locale
     */
    @Test
    @TestCases("C1273808")
    public void signInViaFacebook() {
        NavigationUtils.startActivity(activityRule);

        throughFacebookLogin(FACEBOOK_EMAIL, FACEBOOK_PASSWORD);

        checkName(FACEBOOK_NAME);
    }

    private void closeRoundUpDialog() {
        // This dialog should always be shown
        // when test is running by script because script clears app data.
        // However, it may not be shown when running test in Android Studio.
        ViewInteraction dialog = onView(withText(R.string.choose_a_round_up));
        if (exists(dialog)) {
            onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.btn_no))).perform(click());
        }
    }


}
