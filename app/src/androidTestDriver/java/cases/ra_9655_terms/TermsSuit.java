package cases.ra_9655_terms;

import android.os.RemoteException;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.rideaustin.BaseUITest;
import com.rideaustin.DriverMockResponseFactory;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.TestCases;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.NavigationUtils;

import org.junit.Rule;
import org.junit.Test;

import java.util.Date;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.utils.DeviceTestUtils.pressHome;
import static com.rideaustin.utils.DeviceTestUtils.restoreFromRecentApps;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.MatcherUtils.navigationIcon;
import static com.rideaustin.utils.ViewActionUtils.betterScrollTo;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Reference: RA-9655
 * Created by Sergey Petrov on 25/05/2017.
 */

public class TermsSuit extends BaseUITest {

    // 2017-06-15 23:59:59 GMT according to GLOBAL_CONFIG_200
    private static final Date TERMS_DATE = getDate(2017, 5, 15);

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
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.CURRENT_DRIVER_NO_TERMS_200_GET,
                RequestType.ACTIVE_DRIVER_EMPTY_200_GET,
                RequestType.DRIVER_GO_OFFLINE_200_DELETE,
                RequestType.ACTIVE_DRIVERS_EMPTY_200_GET,
                RequestType.ACCEPT_DRIVER_TERMS_200_PUT,
                RequestType.LOGOUT_200_POST);
    }

    /**
     * New Terms and Condition popup reminder - look and feel
     * 1. Verify New Terms and Condition displays correct text and provides correct button.
     * 2. Verify [Accept] button is disabled before Driver has scrolled until the last line of the text.
     */
    @Test
    @TestCases("C1929202")
    public void termsLookAndFeel() {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        checkTermsContent();

        //------------------------------------------------------------------------------------------
        // Scroll down to the bottom and check controls
        //------------------------------------------------------------------------------------------

        // scroll to the bottom
        onView(withId(R.id.accept_button)).perform(betterScrollTo());
        onView(withId(R.id.scrollView)).perform(swipeUp());

        // check checkbox is enabled
        onView(allOf(withId(R.id.accept_checkbox), withText(R.string.terms_check), isDisplayed())).check(matches(isEnabled()));

        // check button is disabled
        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(not(isEnabled())));

        //------------------------------------------------------------------------------------------
        // Verify checkbox and button
        //------------------------------------------------------------------------------------------

        // check agree
        onView(withId(R.id.accept_checkbox)).perform(click());

        // check button is enabled now
        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(isEnabled()));

        //------------------------------------------------------------------------------------------
        // Verify "Need help" works
        //------------------------------------------------------------------------------------------

        // check need help visible
        onView(allOf(withId(R.id.need_help), withText(R.string.need_help))).check(matches(isDisplayed()));

        // go to need help
        onView(withId(R.id.need_help)).perform(click());

        // wait for support fragment
        waitForDisplayed(R.id.inputMessage);

        // wait for back navigation
        waitForDisplayed(navigationIcon(), "Wait for navigation icon");

        // return back
        onView(navigationIcon()).perform(click());

        waitForDisplayed(R.id.accept_checkbox);

        // check controls are in the same states
        onView(allOf(withId(R.id.accept_checkbox), withText(R.string.terms_check), isDisplayed())).check(matches(allOf(isEnabled(), isChecked())));

        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(isEnabled()));

        //------------------------------------------------------------------------------------------
        // Verify back action works
        //------------------------------------------------------------------------------------------

        // wait for back navigation
        waitForDisplayed(navigationIcon(), "Wait for navigation icon");

        waitFor("WTF", 1000);

        // return back
        onView(navigationIcon()).perform(click());

        waitFor("WTF", 1000);

        // wait for button displayed
        waitForDisplayed(R.id.toolbarActionButton);

        // check button shows offline state
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).check(matches(isDisplayed()));
    }


    /**
     * New Terms and Condition popup reminder - recovery
     * Verify recovery scenario related to New Terms and Condition popup when:
     * - device is losing connection
     * - app is removed from history
     * - app is being put in the background
     */
    @Test
    @TestCases("C1929203")
    public void termsRecovery() throws UiObjectNotFoundException, InterruptedException, RemoteException {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // wait for terms
        waitForDisplayed(allOf(withId(R.id.terms_and_conditions), withText(not(""))), "Wait for terms on start");

        //------------------------------------------------------------------------------------------
        // Scenario 1 - Viewing popup reminder when losing internet connection
        //------------------------------------------------------------------------------------------

        // lose internet
        setNetworkError(true);

        // scroll to the bottom and verify text is there and controls working
        checkTermsControls();

        //------------------------------------------------------------------------------------------
        // Scenario 2 - Accept popup reminder when losing internet connection
        //------------------------------------------------------------------------------------------

        // lose internet
        setNetworkError(true);

        // scroll to bottom
        scrollTermsToTheBottom();

        // check checkbox is disabled
        onView(allOf(withId(R.id.accept_checkbox), withText(R.string.terms_check), isDisplayed())).check(matches(isEnabled()));

        // check button is disabled
        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(not(isEnabled())));

        // check agree
        onView(withId(R.id.accept_checkbox)).perform(click());

        // check button is enabled now
        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(isEnabled()));

        // try to accept
        onView(withId(R.id.accept_button)).perform(click());

        // check toast
        waitForViewInWindow(onView(withText(R.string.network_error)).inRoot(isToast()));

        // un-check agree
        onView(withId(R.id.accept_checkbox)).perform(click());

        // check button is disabled again
        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(not(isEnabled())));

        // scroll to top
        scrollTermsToTheTop();

        //------------------------------------------------------------------------------------------
        // Scenario 3 - Viewing popup reminder after putting app in the background
        //------------------------------------------------------------------------------------------

        // internet is back
        setNetworkError(false);

        // send app to background and restore
        pressHome();
        restoreFromRecentApps();

        waitForDisplayed(allOf(withId(R.id.terms_and_conditions), withText(not(""))), "Wait for terms on start");

        // scroll to the bottom and verify text is there and controls working
        checkTermsControls();

        //------------------------------------------------------------------------------------------
        // Scenario 4 - Remove app from history when popup reminder appears
        //------------------------------------------------------------------------------------------

        // same as Scenario 1 (if mocking responses)

        //------------------------------------------------------------------------------------------
        // Scenario 5 - Reopen app before [Accept] button is enabled
        // NOTE: accept button now is in scrollable area and is enabled/disabled based only on
        // agreement checkbox state
        //------------------------------------------------------------------------------------------

        // scroll to the bottom
        scrollTermsToTheBottom();

        // check checkbox is disabled
        onView(allOf(withId(R.id.accept_checkbox), withText(R.string.terms_check), isDisplayed())).check(matches(isEnabled()));

        // check button is disabled
        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(not(isEnabled())));

        // send app to background and restore
        pressHome();
        restoreFromRecentApps();

        waitForDisplayed(allOf(withId(R.id.terms_and_conditions), withText(not(""))), "Wait for terms on start");

        // check checkbox is disabled
        onView(allOf(withId(R.id.accept_checkbox), withText(R.string.terms_check), isDisplayed())).check(matches(isEnabled()));

        // check button is disabled
        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(not(isEnabled())));

        //------------------------------------------------------------------------------------------
        // Scenario 6 - Reopen app before [Accept] button is enabled
        // NOTE: accept button now is in scrollable area and is enabled/disabled based only on
        // agreement checkbox state
        //------------------------------------------------------------------------------------------

        // scroll to the bottom
        scrollTermsToTheBottom();

        // check checkbox is disabled
        onView(allOf(withId(R.id.accept_checkbox), withText(R.string.terms_check), isDisplayed())).check(matches(isEnabled()));

        // check button is disabled
        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(not(isEnabled())));

        // check agree
        onView(withId(R.id.accept_checkbox)).perform(click());

        // check button is enabled now
        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(isEnabled()));

        // send app to background and restore
        pressHome();
        restoreFromRecentApps();

        waitForDisplayed(allOf(withId(R.id.terms_and_conditions), withText(not(""))), "Wait for terms on start");

        // check checkbox is enabled
        onView(allOf(withId(R.id.accept_checkbox), withText(R.string.terms_check), isDisplayed())).check(matches(isEnabled()));

        // check button is enabled
        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(isEnabled()));
    }

    /**
     * Driver hasn't accepted new Terms and Condition - popup shows on login
     * Verify reminder popup appears correctly on login if Driver hasn't accepted new Terms and Conditions
     */
    @Test
    @TestCases("C1929204")
    public void termsOnLogin() {
        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        checkTermsContent();

        // check controls are ok
        checkTermsControls();

        // wait for back navigation
        waitForDisplayed(navigationIcon(), "Wait for navigation icon");

        // return to map
        onView(navigationIcon()).perform(click());

        // wait for button displayed
        waitForDisplayed(R.id.toolbarActionButton);

        // check button shows offline state
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).check(matches(isDisplayed()));
    }

    /**
     * Driver hasn't accepted new Terms and Condition - popup should not show when on a ride
     * Verify reminder popup should not appear on restart if Driver who hasn't accepted new Terms and Conditions is on a ride.
     */
    @Test
    @TestCases("C1929206")
    public void noTermsWhileInRide() {
        removeRequests(RequestType.ACTIVE_DRIVER_EMPTY_200_GET);
        mockRequests(RequestType.ACTIVE_DRIVER_ACTIVE_RIDE_200_GET,
                RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET,
                RequestType.PENDING_EVENTS_200_POST);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // check button shows in ride state
        onView(allOf(withId(R.id.sliderText), withText(R.string.slide_to_finish))).check(matches(isDisplayed()));

        waitFor("Wait for terms", 2000);

        // there is no terms
        assertFalse(exists(onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_terms_and_conditions)))));
    }

    /**
     * Driver has accepted new Terms and Condition - popup should not show on login
     * Verify reminder popup won't show on login if Driver has accepted new Terms and Conditions
     */
    @Test
    @TestCases("C1929207")
    public void noTermsOnLogin() {
        removeRequests(RequestType.CURRENT_DRIVER_NO_TERMS_200_GET);
        mockRequests(RequestType.CURRENT_DRIVER_200_GET);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // there is no terms
        assertFalse(exists(onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_terms_and_conditions)))));

        // wait for button displayed
        waitForDisplayed(R.id.toolbarActionButton);

        // check button shows in ride state
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).check(matches(isDisplayed()));

        waitFor("There should be no terms", 1000);

        // there is no terms
        assertFalse(exists(onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_terms_and_conditions)))));
    }

    /**
     * Offline Driver hasn't accepted new Terms and Condition - shouldn't be able to go Online
     * Verify Offline Driver is unable to Go Online if Driver hasn't accepted new Terms and Conditions
     */
    @Test
    @TestCases("C1929209")
    public void noOnlineWithoutTerms() {
        removeRequests(RequestType.CURRENT_DRIVER_NO_TERMS_200_GET);
        mockRequests(RequestType.CURRENT_DRIVER_200_GET);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // wait for button displayed
        waitForDisplayed(R.id.toolbarActionButton);

        // check button shows in ride state
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).check(matches(isDisplayed()));

        // simulate activate fail without terms accepted
        mockRequests(RequestType.DRIVER_GO_ONLINE_412_POST);

        // try to go online
        onView(withId(R.id.toolbarActionButton)).perform(click());

        // wait for error dialog
        waitForViewInWindow(withText(DriverMockResponseFactory.TERMS_NOT_ACCEPTED));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.read))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.cancel))).check(matches(isDisplayed()));

        // accept dialog
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        //------------------------------------------------------------------------------------------
        // Check terms
        //------------------------------------------------------------------------------------------

        checkTermsContent();
    }

    @Test
    public void shouldReceiveGoOfflineEvent() {
        removeRequests(RequestType.CURRENT_DRIVER_NO_TERMS_200_GET);
        mockRequests(RequestType.CURRENT_DRIVER_200_GET,
                RequestType.DRIVER_GO_ONLINE_200_POST,
                RequestType.DRIVER_UPDATE_LOCATION_200_PUT,
                RequestType.PENDING_EVENTS_200_POST);

        NavigationUtils.startActivity(activityRule);
        NavigationUtils.throughLogin("driver@email.com", "whatever");

        // wait for button displayed
        waitForDisplayed(R.id.toolbarActionButton);

        // go online
        onView(allOf(withId(R.id.toolbarActionButton), withText(R.string.action_go_online))).perform(click());

        // simulate go offline because of terms
        atomicOnRequests(() -> {
            removeRequests(RequestType.EVENTS_EMPTY_200_GET);
            mockRequests(RequestType.EVENT_GO_OFFLINE_TERMS);
        });

        // check dialog is shown
        waitForViewInWindow(withText(DriverMockResponseFactory.TERMS_NOT_ACCEPTED));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.read))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.cancel))).check(matches(isDisplayed()));

        atomicOnRequests(() -> {
            removeRequests(RequestType.EVENT_GO_OFFLINE_TERMS);
            mockRequests(RequestType.EVENTS_EMPTY_200_GET);
        });

        // accept dialog
        onView(withId(R.id.md_buttonDefaultPositive)).perform(click());

        //------------------------------------------------------------------------------------------
        // Check terms
        //------------------------------------------------------------------------------------------

        checkTermsContent();
    }

    private void scrollTermsToTheTop() {
        onView(withId(R.id.termsHeader)).perform(betterScrollTo());
        onView(withId(R.id.scrollView)).perform(swipeDown());
    }

    private void scrollTermsToTheBottom() {
        onView(withId(R.id.accept_button)).perform(betterScrollTo());
        onView(withId(R.id.scrollView)).perform(swipeUp());
    }

    private void checkTermsContent() {
        // wait for terms
        waitForDisplayed(allOf(withId(R.id.terms_and_conditions), withText(not(""))), "Wait for terms on start");

        // check title
        onView(allOf(withId(R.id.toolbarTitle), withText(R.string.title_terms_and_conditions))).check(matches(isDisplayed()));

        // check progress bar is not visible
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));

        String lastUpdated = getString(R.string.terms_last_updated, DateHelper.dateToUiShortDateFormat(TERMS_DATE).toUpperCase());
        onView(allOf(withId(R.id.terms_last_updated), isDisplayed())).check(matches(withText(lastUpdated)));
    }

    private void checkTermsControls() {
        // scroll to the bottom
        scrollTermsToTheBottom();

        // check checkbox is disabled
        onView(allOf(withId(R.id.accept_checkbox), withText(R.string.terms_check), isDisplayed())).check(matches(isEnabled()));

        // check button is disabled
        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(not(isEnabled())));

        // check agree
        onView(withId(R.id.accept_checkbox)).perform(click());

        // check button is enabled now
        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(isEnabled()));

        // uncheck agree
        onView(withId(R.id.accept_checkbox)).perform(click());

        // check button is disabled again
        onView(allOf(withId(R.id.accept_button), withText(R.string.terms_button), isDisplayed())).check(matches(not(isEnabled())));

        // scroll back to the top
        scrollTermsToTheTop();
    }

    private static Date getDate(int year, int month, int day) {
        return DateHelper.getDate(year, month, day);
    }
}
