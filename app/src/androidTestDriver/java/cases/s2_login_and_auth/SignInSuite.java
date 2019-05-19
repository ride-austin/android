package cases.s2_login_and_auth;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.facebook.FacebookActivity;
import com.facebook.login.LoginClientCreator;
import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.FacebookMocks;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.FacebookHelper;
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
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.DriverMockResponseFactory.INVALID_CREDENTIALS;
import static com.rideaustin.utils.MatcherUtils.hasNoErrorText;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.ViewActionUtils.allowPermissionsIfNeeded;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by hatak on 26.10.2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SignInSuite extends BaseUITest {

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new RaActivityRule<>(SplashActivity.class, false, false);

    @Override
    public void setUp() {
        super.setUp();
        initMockResponseFactory(DriverMockResponseFactory.class);
        mockRequests(RequestType.GLOBAL_APP_INFO_200_GET,
                RequestType.LOGIN_SUCCESS_200_POST,
                RequestType.CURRENT_USER_200_GET,
                RequestType.CONFIG_DRIVER_200_GET,
                RequestType.TOKENS_200_POST,
                RequestType.QUEUES_200_GET,
                RequestType.EVENTS_EMPTY_200_GET,
                RequestType.ACCEPT_RIDE_200_POST,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CURRENT_DRIVER_200_GET,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET,
                RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                RequestType.PENDING_EVENTS_200_POST,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.DRIVERS_CARTYPES_200_GET,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.CONFIG_DRIVER_200_GET);

        Intents.init();
    }

    @Override
    public void tearDown() {
        super.tearDown();
        // for tests using intent mocks
        Intents.release();
    }

    @Test
    @TestCases("C1929184")
    public void testSignInViaEmail() throws UiObjectNotFoundException, PackageManager.NameNotFoundException {
        NavigationUtils.startActivity(activityRule);
        waitForDisplayed(R.id.signIn);
        onView(withId(R.id.signIn)).check(matches(withText(R.string.sign_in))).check(matches(isDisplayed())).perform(click());

        waitForDisplayed(R.id.email);
        waitForDisplayed(R.id.password);
        waitForDisplayed(R.id.facebook);
        waitForDisplayed(R.id.done);
        waitForDisplayed(R.id.forgotPassword);

        // enter arbitrary data as we are mocking server
        onView(withId(R.id.email)).perform(typeText("anyvalid@email.com"));
        onView(withId(R.id.password)).perform(typeText("doesnotmatter"), closeSoftKeyboard());

        // perform login and wait
        onView(withId(R.id.done)).perform(click());

        // allow permission
        allowPermissionsIfNeeded();

        // check map is visible
        waitForDisplayed(R.id.mapContainer);
        waitForDisplayed(R.id.toolbarActionButton);
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).check(matches(isDisplayed()));

    }

    @Test
    @TestCases("C1929185")
    public void testSignInViaEmailValidations() {
        // mock invalid requests
        removeRequests(RequestType.LOGIN_SUCCESS_200_POST);
        mockRequests(RequestType.LOGIN_FAILED_401_POST);

        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(R.id.signIn);

        // go to login
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());

        // check title
        onView(withId(R.id.toolbarTitle)).check(matches(withText(R.string.title_sign_in)));

        // perform login
        onView(withId(R.id.done)).perform(click());
        onView(withId(R.id.email)).check(matches(hasErrorText(getString(R.string.email_error))));

        // enter arbitrary data as we are mocking server
        onView(withId(R.id.email)).perform(typeText("notregistered@email.com"));
        onView(withId(R.id.done)).perform(click());
        onView(withId(R.id.password)).check(matches(hasErrorText(getString(R.string.no_password_error))));


        // HACK: suddenly this test stopped working
        // No obvious reason, Espresso/UIAutomator were not updated
        // Maybe its because of support lib?
        // Anyway, removing this block breaks further test execution
        Espresso.pressBack();
        Espresso.pressBack();
        waitForDisplayed(R.id.signIn);
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());
        // END HACK

        onView(withId(R.id.email)).perform(clearText());
        onView(withId(R.id.password)).perform(clearText());

        // enter arbitrary data as we are mocking server
        onView(withId(R.id.email)).perform(typeText("notregistered@email.com"));
        onView(withId(R.id.password)).perform(typeText("doesnotmatter"), closeSoftKeyboard());

        // perform login
        onView(withId(R.id.done)).perform(click());

        // check login failed
        onView(withText(INVALID_CREDENTIALS)).check(matches(isDisplayed()));
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());
    }


    @Test
    @TestCases("C1929187")
    public void testSignInForgotPassword() {
        mockRequests(RequestType.RESET_PASSWORD_200_POST);

        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(R.id.signIn);

        // go to login
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());

        // go to forgot password
        onView(allOf(withId(R.id.forgotPassword), withText(R.string.forgot_password)))
                .check(matches(isDisplayed())).check(matches(isClickable()))
                .perform(click());

        // click on button
        onView(allOf(withId(R.id.reset), withText(R.string.reset_pass))).perform(click());

        // check error
        onView(withId(R.id.email)).check(matches(hasErrorText(getString(R.string.email_error))));

        // type invalid email
        onView(withId(R.id.email)).perform(typeText("invalidemail"), closeSoftKeyboard());

        // click on button
        onView(allOf(withId(R.id.reset), withText(R.string.reset_pass))).perform(click());

        // check error
        onView(withId(R.id.email)).check(matches(hasErrorText(getString(R.string.invalid_email_error))));

        // type valid email
        onView(withId(R.id.email)).perform(clearText(), typeText("registered@email.com"), closeSoftKeyboard());

        // click on button
        onView(allOf(withId(R.id.reset), withText(R.string.reset_pass))).perform(click());

        // check no error
        onView(withId(R.id.email)).check(matches(hasNoErrorText()));

        // check toast
        waitForViewInWindow(onView(withText(R.string.email_sent)).inRoot(isToast()));
    }

    @Test
    @TestCases("C1929186")
    public void signInViaFacebook() {
        mockRequests(RequestType.FACEBOOK_LOGIN_200_POST);
        // mock facebook intents
        final Intent resultData = new Intent();
        resultData.putExtra("com.facebook.LoginFragment:Result", LoginClientCreator.createSuccess());
        intending(hasComponent(FacebookActivity.class.getName())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData));

        // mock facebook requests
        FacebookHelper.meRequestCreator = FacebookMocks.getMe("FACEBOOK_ME_VALID");

        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(R.id.signIn);

        // go to login
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());

        // go to facebook login
        onView(allOf(withId(R.id.facebook), withText(R.string.fb_login_text))).perform(click());

        // allow permission
        allowPermissionsIfNeeded();

        // check map is visible
        waitForDisplayed(R.id.mapContainer);
        waitForDisplayed(R.id.toolbarActionButton);
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).check(matches(isDisplayed()));
    }
}
