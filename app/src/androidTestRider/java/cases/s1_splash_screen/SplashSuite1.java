package cases.s1_splash_screen;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.BaseUITest;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.ViewActionUtils.allowPermissionsIfNeeded;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-10186
 * Created by crossover on 05/05/2017.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SplashSuite1 extends BaseUITest {

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

    @Rule
    public ActivityTestRule<NavigationDrawerActivity> activityRule = new RaActivityRule<>(NavigationDrawerActivity.class, false, false);

    /**
     * Splash screen: On fresh install.
     * On fresh install, all buttons and the version should be available
     */
    @Test
    @TestCases("C1930336")
    public void splashScreenOnFreshInstall() throws UiObjectNotFoundException {
        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(R.id.signIn);

        // logo is visible
        onView(withId(R.id.logo)).check(matches(isDisplayed()));

        // correct version name is displayed
        onView(withId(R.id.label_version)).check(matches(withText(getVersionName()))).check(matches(isDisplayed()));

        // sign-in/register buttons are displayed and clickable
        onView(withId(R.id.signIn)).check(matches(withText(R.string.sign_in))).check(matches(isDisplayed())).check(matches(isClickable()));
        onView(withId(R.id.signUp)).check(matches(withText(R.string.register))).check(matches(isDisplayed())).check(matches(isClickable()));
    }

    /**
     * Splash screen: On sign-out
     */
    @Test
    @TestCases("C1930337")
    public void signOut() {
        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(R.id.signIn);

        // go to login
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());

        // enter arbitrary data as we are mocking server
        onView(withId(R.id.email)).perform(typeText("anyvalid@email.com"));
        onView(withId(R.id.password)).perform(typeText("doesnotmatter"), closeSoftKeyboard());

        // perform login and wait
        onView(withId(R.id.done)).perform(click());

        // allow permission
        allowPermissionsIfNeeded();

        // check map is visible
        onView(withId(R.id.mapContainer)).check(matches(isDisplayed()));

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        // navigate to settings
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        // sign out
        onView(withId(R.id.signOut)).perform(scrollTo(), click());

        // check toast
        waitForViewInWindow(onView(withText(R.string.signed_out)).inRoot(isToast()));

        // logo is visible
        onView(withId(R.id.logo)).check(matches(isDisplayed()));

        // correct version name is displayed
        onView(withId(R.id.label_version)).check(matches(withText(getVersionName()))).check(matches(isDisplayed()));

        // sign-in/register buttons are displayed and clickable
        onView(withId(R.id.signIn)).check(matches(withText(R.string.sign_in))).check(matches(isDisplayed())).check(matches(isClickable()));
        onView(withId(R.id.signUp)).check(matches(withText(R.string.register))).check(matches(isDisplayed())).check(matches(isClickable()));
    }

    /**
     * Splash screen: On app restart when internet is down - error message is shown
     */
    @Test
    @TestCases("C1930338")
    public void appRestartWhenInternetIsDown() {
        // simulate network error
        setNetworkError(true);

        NavigationUtils.startActivity(activityRule);

        waitForDisplayed(withText(R.string.network_error), "Wait for error");

        // logo is not visible
        ViewInteraction logo = onView(withId(R.id.logo));
        if (exists(logo)) {
            logo.check(matches(not(isDisplayed())));
        }

        // click on retry shows popup again
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        // check network error message
        onView(withText(R.string.network_error)).check(matches(isDisplayed()));

        // click on cancel finishes activity
        onView(withId(R.id.md_buttonDefaultNegative)).perform(click());

        assertTrue(activityRule.getActivity().isFinishing());
    }

    private String getVersionName() {
        return AppInfoUtil.getAppVersionName();
    }
}
