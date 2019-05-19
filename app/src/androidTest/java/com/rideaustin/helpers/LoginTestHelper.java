package com.rideaustin.helpers;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;

import com.rideaustin.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.DeviceTestUtils.search;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.ViewActionUtils.allowPermissionsIfNeeded;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by Sergey Petrov on 08/08/2017.
 */

public class LoginTestHelper {

    private static final long FB_TIMEOUT = 20000;

    public static void throughLogin(String email, String password) {
        waitForDisplayed(R.id.signIn);

        // go to login
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());

        // check title
        onView(withId(R.id.toolbarTitle)).check(matches(withText(R.string.title_sign_in)));

        // enter invalid credentials
        onView(withId(R.id.email)).perform(typeText(email));
        onView(withId(R.id.password)).perform(typeText(password), closeSoftKeyboard());

        // perform login
        onView(withId(R.id.done)).perform(click());

        allowPermissionsIfNeeded();
    }

    /**
     * Login via Facebook using WebView
     * Will work only on device with EN locale
     */
    public static void throughFacebookLogin(String email, String password) {
        waitForDisplayed(R.id.signIn);

        // go to login
        onView(withId(R.id.signIn)).check(matches(isDisplayed())).perform(click());

        // go to facebook login
        onView(allOf(withId(R.id.facebook), withText(R.string.fb_login_text))).perform(click());

        // enter FB credentials
        search().text("Email address or phone number").assertExist(FB_TIMEOUT);
        search().text("Email address or phone number").object().setText(email);
        search().text("Facebook password").object().setText(password);
        search().descContains("Log In").object().click();

        // check user is logged in and has RideAustin permissions
        search().descContains("Continue").assertExist(FB_TIMEOUT);
        search().descContains("Continue").object().click();

        allowPermissionsIfNeeded(FB_TIMEOUT);

        waitForDisplayed(R.id.mapContainer);
    }

    public static void checkName(String name) {
        // check map is visible
        onView(withId(R.id.mapContainer)).check(matches(isDisplayed()));

        // open navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());

        // check user name
        onView(allOf(withId(R.id.usersName), withText(name))).check(matches(isDisplayed()));

        // close navigation drawer
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.close());
    }

    public static void logout() {
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

        waitForDisplayed(R.id.signIn);
    }
}
