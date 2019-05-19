package cases.s2_login_authentication;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.facebook.FacebookActivity;
import com.facebook.login.LoginClientCreator;
import com.rideaustin.App;
import com.rideaustin.BaseUITest;
import com.rideaustin.FacebookMocks;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.api.model.RiderData;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.FacebookHelper;
import com.rideaustin.utils.NavigationUtils;

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
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.RiderMockResponseFactory.INVALID_CREDENTIALS;
import static com.rideaustin.utils.MatcherUtils.hasNoErrorText;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.ViewActionUtils.allowPermissionsIfNeeded;
import static com.rideaustin.utils.ViewActionUtils.swipeFromLeftEdge;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;

/**
 * Reference: RA-10187
 * Created by crossover on 05/05/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginSuite1 extends BaseUITest {

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

        Intents.init();
    }

    @Override
    public void tearDown() {
        super.tearDown();
        // for tests using intent mocks
        Intents.release();
    }

    /**
     * Sign-In - Via email
     * Verify that user is able to sign-in as a Rider
     */
    @Test
    @TestCases("C1930339")
    public void signInViaEmail() throws InstantiationException, IllegalAccessException {
        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(R.id.signIn);

        // go to login
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());

        // check title
        onView(withId(R.id.toolbarTitle)).check(matches(withText(R.string.title_sign_in)));

        // check toolbar navigate back
        onView(navigationIcon()).perform(click());

        // go to login again
        onView(withId(R.id.signIn)).perform(click());

        // check back action
        Espresso.pressBack();

        // go to login again
        onView(withId(R.id.signIn)).perform(click());

        // enter arbitrary data as we are mocking server
        onView(withId(R.id.email)).perform(clearText(), typeText("registered@email.com"));
        onView(withId(R.id.password)).perform(clearText(), typeText("doesnotmatter"), closeSoftKeyboard());

        // perform login
        onView(withId(R.id.done)).perform(click());

        // allow permission
        allowPermissionsIfNeeded();

        // check map is visible
        onView(withId(R.id.mapContainer)).check(matches(isDisplayed()));

        // open navigation drawer (by swipe)
        onView(withId(R.id.mapContainer)).perform(swipeFromLeftEdge());

        // check user name
        onView(allOf(withId(R.id.usersName), withText("FullName"))).check(matches(isDisplayed()));

        // navigate to settings
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        // sign out
        onView(withId(R.id.signOut)).perform(scrollTo(), click());
    }

    /**
     * Sign-In - Invalid Credentials
     * Verify that user is not allowed to login with invalid credentials and correct error message is shown
     */
    @Test
    @TestCases("C1930340")
    public void signInInvalidCredentials() {
        // mock invalid requests
        removeRequests(RequestType.LOGIN_SUCCESS_200_POST);
        mockRequests(RequestType.LOGIN_FAILED_401_POST);

        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(R.id.signIn);

        // go to login
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());

        // check title
        onView(withId(R.id.toolbarTitle)).check(matches(withText(R.string.title_sign_in)));

        // enter arbitrary data as we are mocking server
        onView(withId(R.id.email)).perform(typeText("notregistered@email.com"));
        onView(withId(R.id.password)).perform(typeText("doesnotmatter"), closeSoftKeyboard());

        // perform login
        onView(withId(R.id.done)).perform(click());

        // check login failed
        onView(withText(INVALID_CREDENTIALS)).check(matches(isDisplayed()));
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());

        // remove invalid login
        removeRequests(RequestType.LOGIN_FAILED_401_POST);
        mockRequests(RequestType.LOGIN_SUCCESS_200_POST);

        // enter arbitrary data as we are mocking server
        onView(withId(R.id.email)).perform(clearText(), typeText("registered@email.com"));
        onView(withId(R.id.password)).perform(clearText(), typeText("doesnotmatter"), closeSoftKeyboard());

        // perform login
        onView(withId(R.id.done)).perform(click());

        // allow permission
        allowPermissionsIfNeeded();

        // check map is visible
        onView(withId(R.id.mapContainer)).check(matches(isDisplayed()));

        // open navigation drawer (by swipe)
        onView(withId(R.id.mapContainer)).perform(swipeFromLeftEdge());

        // check user name
        onView(allOf(withId(R.id.usersName), withText("FullName"))).check(matches(isDisplayed()));

        // navigate to settings
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        // sign out
        onView(withId(R.id.signOut)).perform(scrollTo(), click());
    }

    /**
     * Sign-In - Via Facebook
     * Verify that user is able to sign in using FB account that is registered in Ride Austin system (User should be created first)
     */
    @Test
    @TestCases("C1930342")
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
        onView(withId(R.id.mapContainer)).check(matches(isDisplayed()));

        // open navigation drawer (by swipe)
        onView(withId(R.id.mapContainer)).perform(swipeFromLeftEdge());

        // check user name (taken from CURRENT_USER_200, not FACEBOOK_ME_VALID)
        onView(allOf(withId(R.id.usersName), withText("FullName"))).check(matches(isDisplayed()));

        // navigate to settings
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        // sign out
        onView(withId(R.id.signOut)).perform(scrollTo(), click());
    }

    /**
     * Sign-In - Forgot password
     */
    @Test
    @TestCases("C1930343")
    public void signInForgotPassword() {
        mockRequests(RequestType.RESET_PASSWORD_200_POST);

        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(R.id.signIn);

        // go to login
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());

        // go to forgot password
        onView(allOf(withId(R.id.forgotPassword), withText(R.string.forgot_password)))
                .check(matches(isDisplayed())).check(matches(isClickable()))
                .perform(click());

        // check title
        onView(withId(R.id.toolbarTitle)).check(matches(withText(R.string.title_forgot_pass)));

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

    /**
     * Sign-In - Via Facebook - Not existing account
     * Verify that when user is trying to login with FB account that is not registered in RA system, he is redirected to [Create Account] screen
     */
    @Test
    @TestCases("C1930344")
    public void signInViaFacebookNotExistingAccount() {
        mockRequests(RequestType.FACEBOOK_LOGIN_202_POST); // user not registered

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

        // check login failed
        String applicationName = App.getConfigurationManager().getLastConfiguration().getGeneralInformation().getApplicationName();
        onView(withText(getString(R.string.facebook_error, applicationName))).check(matches(isDisplayed()));
        onView(withId(com.afollestad.materialdialogs.R.id.md_buttonDefaultPositive)).perform(click());

        // check title
        onView(withId(R.id.toolbarTitle)).check(matches(withText(R.string.title_create_account)));
    }

    @Test
    @TestCases("C1930345")
    public void signInAsNotActiveRider() {
        removeRequests(RequestType.RIDER_DATA_NO_RIDE_200_GET);
        RiderData riderData = getResponse("RIDER_DATA_NO_RIDE_200", RiderData.class);
        riderData.getRider().getUser().setActive(false);
        mockRequest(RequestType.RIDER_DATA_NO_RIDE_200_GET, riderData);

        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(R.id.signIn);

        // go to login
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());

        // check title
        onView(withId(R.id.toolbarTitle)).check(matches(withText(R.string.title_sign_in)));

        // enter arbitrary data as we are mocking server
        onView(withId(R.id.email)).perform(clearText(), typeText("inactive@email.com"));
        onView(withId(R.id.password)).perform(clearText(), typeText("doesnotmatter"), closeSoftKeyboard());

        // perform login
        onView(withId(R.id.done)).perform(click());

        // allow permission
        allowPermissionsIfNeeded();

        // check map is visible
        onView(withId(R.id.mapContainer)).check(matches(isDisplayed()));

        // open navigation drawer (by swipe)
        onView(withId(R.id.mapContainer)).perform(swipeFromLeftEdge());

        // check user name
        onView(allOf(withId(R.id.usersName), withText("FullName"))).check(matches(isDisplayed()));

        // navigate to settings
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        // sign out
        onView(withId(R.id.signOut)).perform(scrollTo(), click());
    }

    /**
     * Sign-In - Loosing internet connection
     * when rider attempts login, we should show network error
     */
    @Test
    @TestCases("C1930346")
    public void signInLoosingInternetConnection() {
        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(R.id.signIn);

        // go to login
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());

        // check title
        onView(withId(R.id.toolbarTitle)).check(matches(withText(R.string.title_sign_in)));

        setNetworkError(true);

        // enter arbitrary data as we are mocking server
        onView(withId(R.id.email)).perform(clearText(), typeText("nointernet@email.com"));
        onView(withId(R.id.password)).perform(clearText(), typeText("doesnotmatter"), closeSoftKeyboard());

        // perform login
        onView(withId(R.id.done)).perform(click());

        // check popup
        onView(withText(getString(R.string.network_error))).check(matches(isDisplayed()));
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());
    }
}
