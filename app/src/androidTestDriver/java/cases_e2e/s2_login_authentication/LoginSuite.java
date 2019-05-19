package cases_e2e.s2_login_authentication;

import android.support.test.espresso.Espresso;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rideaustin.BaseE2ETest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.TestCases;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.DriverMockResponseFactory.INVALID_CREDENTIALS;
import static com.rideaustin.helpers.LoginTestHelper.checkName;
import static com.rideaustin.helpers.LoginTestHelper.logout;
import static com.rideaustin.helpers.LoginTestHelper.throughFacebookLogin;
import static com.rideaustin.helpers.LoginTestHelper.throughLogin;
import static com.rideaustin.utils.MatcherUtils.hasNoErrorText;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.ViewActionUtils.allowPermissionsIfNeeded;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;

/**
 * Created by Sergey Petrov on 08/08/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginSuite extends BaseE2ETest {

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new RaActivityRule<>(SplashActivity.class, false, false);

    @Test
    @TestCases("C1929184")
    public void signInViaEmail() {
        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(R.id.signIn);

        // go to login
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());

        // check title
        onView(withId(R.id.toolbarTitle)).check(matches(withText(R.string.title_sign_in)));

        // check toolbar navigate back
        onView(navigationIcon()).perform(click());

        throughLogin("austindriver@xo.com", "test123");

        // allow permission
        allowPermissionsIfNeeded();

        checkName("Austin Driver");

        logout();
    }

    @Test
    @TestCases("C1929185")
    public void signInValidations() {
        NavigationUtils.startActivity(activityRule);

        throughLogin("austindriver@xo.com", "invalidpassword");

        // check login failed
        onView(withText(INVALID_CREDENTIALS)).check(matches(isDisplayed()));
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());

        // check empty email
        onView(withId(R.id.email)).perform(clearText());
        onView(withId(R.id.done)).perform(click());
        onView(withId(R.id.email)).check(matches(hasErrorText(getString(R.string.email_error))));

        // check invalid email
        onView(withId(R.id.email)).perform(typeText("invalidemail.com"));
        onView(withId(R.id.done)).perform(click());
        onView(withId(R.id.email)).check(matches(hasErrorText(getString(R.string.invalid_email_error))));

        // check empty password
        onView(withId(R.id.email)).perform(clearText(), typeText("austindriver@xo.com"));
        onView(withId(R.id.password)).perform(clearText());
        onView(withId(R.id.done)).perform(click());
        onView(withId(R.id.email)).check(matches(hasNoErrorText()));
        onView(withId(R.id.password)).check(matches(hasErrorText(getString(R.string.no_password_error))));

        // check short password
        onView(withId(R.id.password)).perform(typeText("12345"), closeSoftKeyboard());
        onView(withId(R.id.done)).perform(click());
        onView(withId(R.id.password)).check(matches(hasErrorText(getString(R.string.password_error))));

        // check valid input
        onView(withId(R.id.password)).perform(clearText(), typeText("test123"), closeSoftKeyboard());
        onView(withId(R.id.done)).perform(click());

        allowPermissionsIfNeeded();

        checkName("Austin Driver");
    }

    @Test
    @TestCases("C1929186")
    public void signInViaFacebook() {
        NavigationUtils.startActivity(activityRule);

        throughFacebookLogin("rideaustinqa@gmail.com", "test123456");

        checkName("Ride Austin");
    }
}
