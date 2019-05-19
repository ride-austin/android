package cases.s4_register_as_rider;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.facebook.FacebookActivity;
import com.facebook.login.LoginClientCreator;
import com.rideaustin.App;
import com.rideaustin.BaseUITest;
import com.rideaustin.FacebookMocks;
import com.rideaustin.ImagePickerMock;
import com.rideaustin.R;
import com.rideaustin.RaActivityRule;
import com.rideaustin.RequestType;
import com.rideaustin.RiderMockResponseFactory;
import com.rideaustin.TestCases;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.FacebookHelper;
import com.rideaustin.utils.NavigationUtils;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.RiderMockResponseFactory.EMAIL_EXISTS;
import static com.rideaustin.RiderMockResponseFactory.PHONE_EXISTS;
import static com.rideaustin.utils.CommonNavigationUtils.takePhoto;
import static com.rideaustin.utils.MatcherUtils.exists;
import static com.rideaustin.utils.MatcherUtils.hasBodyContent;
import static com.rideaustin.utils.MatcherUtils.hasDrawable;
import static com.rideaustin.utils.MatcherUtils.hasFieldInPost;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.ViewActionUtils.allowPermissionsIfNeeded;
import static com.rideaustin.utils.ViewActionUtils.swipeFromLeftEdge;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForViewInWindow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class RegisterAsRiderSuite1 extends BaseUITest {

    @Rule
    public ActivityTestRule<NavigationDrawerActivity> activityRule = new RaActivityRule<>(NavigationDrawerActivity.class, false, false);

    @Override
    public void setUp() {
        super.setUp();
        Intents.init();
        initMockResponseFactory(RiderMockResponseFactory.class);
        mockRequests(RequestType.GLOBAL_APP_INFO_200_GET,
                RequestType.CURRENT_RIDER_WO_CHARITY_200_GET,
                RequestType.ACDR_EMPTY_200_GET,
                RequestType.CHARITIES_200_GET,
                RequestType.CONFIG_ZIPCODES_200_GET,
                RequestType.CONFIG_RIDER_200_GET,
                RequestType.SURGE_AREA_EMPTY_200_GET,
                RequestType.USERS_EXISTS_200_POST,
                RequestType.USERS_200_POST,
                RequestType.LOGIN_SUCCESS_200_POST,
                RequestType.RIDER_DATA_NO_RIDE_200_GET,
                RequestType.TOKENS_200_POST,
                RequestType.DRIVER_TYPES_200_GET,
                RequestType.PHONE_VERIFICATION_REQUEST_CODE_POST,
                RequestType.PHONE_VERIFICATION_SEND_CODE_POST,
                RequestType.RIDERS_PUT);
    }

    @Override
    public void tearDown() {
        super.tearDown();
        Intents.release();
    }

    private void tryToCloseRoundUpDialog() {
        ViewInteraction dialog = onView(withText(R.string.choose_a_round_up));
        if (exists(dialog)) {
            onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.btn_no))).perform(click());
        }
    }

    private void doCommonLogin() {
        onView(withText(R.string.title_sign_in)).check(matches(isDisplayed()));
        ViewInteraction emailLoginField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction passwordLoginField = onView(allOf(withId(R.id.password), isDisplayed()));
        ViewInteraction doneButton = onView(allOf(withId(R.id.done), withText(R.string.done), isDisplayed()));

        emailLoginField.perform(typeText("already.exists.email@test.com"), closeSoftKeyboard());
        passwordLoginField.perform(typeText("secret"), closeSoftKeyboard());

        doneButton.perform(click());

        allowPermissionsIfNeeded();
        tryToCloseRoundUpDialog();

        onView(withId(R.id.set_pickup_location)).check(matches(isDisplayed()));
    }

    private void doCommonRegister() {
        onView(withId(R.id.firstName)).perform(click());
        onView(withId(R.id.firstName)).perform(typeText("espresso"), closeSoftKeyboard());
        onView(withId(R.id.lastName)).perform(typeText("rider"), closeSoftKeyboard());
        onView(allOf(withId(R.id.done), withText(R.string.done))).perform(click());
        onView(withText(R.string.donate)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(withText(R.string.driver_info)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.skip), withText(R.string.skip))).perform(click());

        allowPermissionsIfNeeded();
        tryToCloseRoundUpDialog();
        onView(withId(R.id.set_pickup_location)).check(matches(isDisplayed()));
    }

    /**
     * Register - Create Account - via email
     */
    @Test
    @TestCases("C1930526")
    public void createAccountViaEmail() {
        mockRequests(RequestType.USERS_EXISTS_200_POST);

        NavigationUtils.startActivity(activityRule);

        closeSoftKeyboard();

        waitForDisplayed(R.id.signUp);

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));
        onView(withId(R.id.email)).perform(click());
        onView(withId(R.id.email)).perform(typeText("espresso.rider.2@xo.com"), closeSoftKeyboard());
        onView(withId(R.id.mobile)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.mobile)).perform(typeText("+48600200300"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("secret"), closeSoftKeyboard());
        onView(allOf(withId(R.id.password), withText("secret"))).perform(pressImeActionButton());
        onView(allOf(withId(R.id.next), withText("Next"))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.confirm_dialog))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_yes))).perform(click());

        doCommonRegister();
    }

    @Test
    @TestCases("C1930528")
    public void createAccountViaEmailValidations() {
        mockRequests(RequestType.USERS_EXISTS_200_POST);

        NavigationUtils.startActivity(activityRule);

        closeSoftKeyboard();

        waitForDisplayed(R.id.signUp);

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction nextButton = onView(allOf(withId(R.id.next), withText("Next"))).perform(click());

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        emailField.check(matches(hasErrorText(getAppContext().getString(R.string.email_error))));
        emailField.perform(typeText("valid.email@test.com"), closeSoftKeyboard());

        nextButton.perform(click());

        phoneField.check(matches(hasErrorText(getAppContext().getString(R.string.mobile_error))));
        phoneField.perform(clearText(), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());

        nextButton.perform(click());

        passwordField.check(matches(hasErrorText(getAppContext().getString(R.string.no_password_error))));
        passwordField.perform(typeText("sec"), closeSoftKeyboard());

        nextButton.perform(click());

        passwordField.check(matches(hasErrorText(getAppContext().getString(R.string.password_error))));
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        nextButton.perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.confirm_dialog))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.reenter))).perform(click());
    }

    @Test
    @TestCases("C1930529")
    public void createAccountAlreadyExistingEmail() {
        removeRequests(RequestType.USERS_EXISTS_200_POST);
        mockRequests(RequestType.USERS_EXISTS_EMAIL_400_POST);

        NavigationUtils.startActivity(activityRule);

        closeSoftKeyboard();

        waitForDisplayed(R.id.signUp);

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("already.exist.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText("Next"))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.confirm_dialog))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(withText(EMAIL_EXISTS)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.login))).perform(click());

        doCommonLogin();
    }

    @Test
    @TestCases("C1930530")
    public void createAccountAlreadyExistingEmailUseDifferentEmail() {
        removeRequests(RequestType.USERS_EXISTS_200_POST);
        mockRequests(RequestType.USERS_EXISTS_EMAIL_400_POST);

        NavigationUtils.startActivity(activityRule);

        closeSoftKeyboard();

        waitForDisplayed(R.id.signUp);

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("already.exist.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.confirm_dialog))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(withText(EMAIL_EXISTS)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.edit))).perform(click());

        emailField.perform(typeText("not.exist.email@test.com"), closeSoftKeyboard());

        removeRequests(RequestType.USERS_EXISTS_EMAIL_400_POST);
        mockRequests(RequestType.USERS_EXISTS_200_POST);

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.confirm_dialog))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_yes))).perform(click());

        doCommonRegister();
    }


    @Test
    @TestCases("C1930531")
    public void createAccountAlreadyExistingPhoneNumberLogin() {
        removeRequests(RequestType.USERS_EXISTS_200_POST);
        mockRequests(RequestType.USERS_EXISTS_PHONE_400_POST);

        NavigationUtils.startActivity(activityRule);

        closeSoftKeyboard();

        waitForDisplayed(R.id.signUp);

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("valid.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.confirm_dialog))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.btn_no))).perform(click());

        onView(withText(PHONE_EXISTS)).check(matches(isDisplayed()));

        removeRequests(RequestType.USERS_EXISTS_PHONE_400_POST);
        mockRequests(RequestType.USERS_EXISTS_200_POST);

        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.login))).perform(click());
        doCommonLogin();
    }

    @Test
    @TestCases("C1930532")
    public void createAccountAlreadyExistingPhoneNumberUseDifferent() {
        removeRequests(RequestType.USERS_EXISTS_200_POST);
        mockRequests(RequestType.USERS_EXISTS_PHONE_400_POST);

        NavigationUtils.startActivity(activityRule);

        closeSoftKeyboard();

        waitForDisplayed(R.id.signUp);

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("valid.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.confirm_dialog))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.btn_no))).perform(click());

        onView(withText(PHONE_EXISTS))
                .check(matches(isDisplayed()))
                .check(matches(withText(PHONE_EXISTS)));

        onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.edit))).perform(click());
        onView(allOf(withId(R.id.mobile), withText(startsWith("+")))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.mobile), withText(not(containsString("+48111222333"))))).check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1930533")
    public void createAccountRenterEmail() {
        NavigationUtils.startActivity(activityRule);

        closeSoftKeyboard();

        waitForDisplayed(R.id.signUp);

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("valid.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.confirm_dialog))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.reenter))).perform(click());

        emailField.check(matches(withText("valid.email@test.com")));
        passwordField.check(matches(withText("secret")));

        emailField.perform(clearText(), closeSoftKeyboard());
        emailField.perform(typeText("other.valid.email@test.com"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.confirm_dialog))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_yes))).perform(click());

        doCommonRegister();
    }

    @Test
    @TestCases("C1930527")
    public void createAccountViaFacebook() {
        mockRequests(RequestType.FACEBOOK_LOGIN_200_POST);
        // mock facebook intents
        final Intent resultData = new Intent();
        resultData.putExtra("com.facebook.LoginFragment:Result", LoginClientCreator.createSuccess());
        intending(hasComponent(FacebookActivity.class.getName())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData));

        // mock facebook requests
        FacebookHelper.meRequestCreator = FacebookMocks.getMe("FACEBOOK_ME_VALID");

        NavigationUtils.startActivity(activityRule);
        closeSoftKeyboard();

        waitForDisplayed(R.id.signUp);

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        onView(withId(R.id.email)).check(matches(isDisplayed()));
        onView(withId(R.id.mobile)).check(matches(isDisplayed()));
        onView(withId(R.id.password)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.next), withText("Next"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.image), hasDrawable())).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.facebook), withText(R.string.fb_login_text))).perform(click());

        allowPermissionsIfNeeded();

        tryToCloseRoundUpDialog();

        // open navigation drawer (by swipe)
        onView(withId(R.id.mapContainer)).perform(swipeFromLeftEdge());

        // check user name (taken from CURRENT_USER_200, not FACEBOOK_ME_VALID)
        onView(allOf(withId(R.id.usersName), withText("FullName"))).check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1930541")
    public void createProfileAddPhotoFromLibrary() throws IOException {
        NavigationUtils.startActivity(activityRule);
        closeSoftKeyboard();
        waitForDisplayed(R.id.signUp);

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("valid.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_yes))).perform(click());

        onView(withId(R.id.firstName)).perform(click());
        onView(withId(R.id.firstName)).perform(typeText("espresso"), closeSoftKeyboard());
        onView(withId(R.id.lastName)).perform(typeText("rider"), closeSoftKeyboard());

        onView(withId(R.id.profile)).perform(click());

        ImagePickerMock.mockImagePickFromGallery("gallery_picked_image.png");

        waitFor("animation", 500);

        onView(withId(R.id.choosePhoto)).perform(click());
        allowPermissionsIfNeeded();
        waitFor("animation", 500);
        onView(withId(R.id.profile)).check(matches(hasDrawable()));

        onView(allOf(withId(R.id.done), withText(R.string.done))).perform(click());
        onView(withText(R.string.donate)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(withText(R.string.driver_info)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.skip), withText(R.string.skip))).perform(click());

        allowPermissionsIfNeeded();

        tryToCloseRoundUpDialog();

        // open navigation drawer (by swipe)
        onView(withId(R.id.mapContainer)).perform(swipeFromLeftEdge());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        verifyRequest(RequestType.USERS_200_POST, hasFieldInPost("data"));
    }

    @Test
    @TestCases("C1930542")
    public void createProfileAddPhotoUsingCamera() throws IOException {
        NavigationUtils.startActivity(activityRule);
        closeSoftKeyboard();
        waitForDisplayed(R.id.signUp);

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("valid.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_yes))).perform(click());

        onView(withId(R.id.firstName)).perform(click());
        onView(withId(R.id.firstName)).perform(typeText("espresso"), closeSoftKeyboard());
        onView(withId(R.id.lastName)).perform(typeText("rider"), closeSoftKeyboard());

        takePhoto(R.id.profile);

        waitFor(allOf(withId(R.id.profile), isDisplayed(), hasDrawable()), "Image should be set", 1000);

        onView(allOf(withId(R.id.done), withText(R.string.done))).perform(click());
        onView(withText(R.string.donate)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(withText(R.string.driver_info)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.skip), withText(R.string.skip))).perform(click());

        allowPermissionsIfNeeded();

        tryToCloseRoundUpDialog();

        // open navigation drawer (by swipe)
        onView(withId(R.id.mapContainer)).perform(swipeFromLeftEdge());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));

        verifyRequest(RequestType.USERS_200_POST, hasFieldInPost("data"));
    }

    @Test
    @TestCases("C1930535")
    public void registerVerifyPhoneNumberReceiveSMSVerificationCode() {
        NavigationUtils.startActivity(activityRule);
        closeSoftKeyboard();

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("valid.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.confirm_dialog))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.btn_no))).perform(click());

        allowPermissionsIfNeeded();

        onView(withText(R.string.title_verify_phone)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.verifyText), withText(containsString("+48111222333")))).check(matches(isDisplayed()));

        waitFor("resend text to be enabled", 10000);

        onView(allOf(withId(R.id.resend), withText(R.string.resend)))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));
        onView(allOf(withId(R.id.changeMobile), withText(R.string.change_mobile)))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));

        onView(withTagValue(is(0))).check(matches(not(hasFocus())));
        onView(withTagValue(is(0))).perform(typeText("0"));
        onView(withTagValue(is(1))).check(matches(hasFocus()));
        onView(withTagValue(is(1))).perform(typeText("1"));
        onView(withTagValue(is(2))).check(matches(hasFocus()));
        onView(withTagValue(is(2))).perform(typeText("2"));
        onView(withTagValue(is(3))).check(matches(hasFocus()));
        onView(withTagValue(is(3))).perform(typeText("3"));

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());
        onView(withText(R.string.title_create_profile)).check(matches(isDisplayed()));

    }

    @Test
    @TestCases("C1930536")
    public void registerVerifyPhoneNumberResendSMSVerificationCode() {
        NavigationUtils.startActivity(activityRule);
        closeSoftKeyboard();

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("valid.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.confirm_dialog))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.btn_no))).perform(click());

        allowPermissionsIfNeeded();

        onView(withText(R.string.title_verify_phone)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.verifyText), withText(containsString("+48111222333")))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.changeMobile), withText(R.string.change_mobile)))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));

        onView(withTagValue(is(0))).check(matches(not(hasFocus())));

        Matcher<View> resendMatcher = allOf(withId(R.id.resend), withText(R.string.resend), isEnabled(), isClickable());
        // resend is disabled during 10 seconds in code
        waitFor(R.id.resend, resendMatcher, IDLE_TIMEOUT_MS);
        onView(resendMatcher).perform(click());

        verifyRequestsCount(RequestType.PHONE_VERIFICATION_REQUEST_CODE_POST, is(2));

        onView(withTagValue(is(0))).check(matches(not(hasFocus())));
        onView(withTagValue(is(0))).perform(typeText("0"));
        onView(withTagValue(is(1))).check(matches(hasFocus()));
        onView(withTagValue(is(1))).perform(typeText("1"));
        onView(withTagValue(is(2))).check(matches(hasFocus()));
        onView(withTagValue(is(2))).perform(typeText("2"));
        onView(withTagValue(is(3))).check(matches(hasFocus()));
        onView(withTagValue(is(3))).perform(typeText("3"));

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());
        onView(withText(R.string.title_create_profile)).check(matches(isDisplayed()));
    }

    @Test
    @TestCases("C1930537")
    public void registerVerifyPhoneNumberChangingMobileNumber() {
        NavigationUtils.startActivity(activityRule);
        closeSoftKeyboard();

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("valid.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.confirm_dialog))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.btn_no))).perform(click());

        allowPermissionsIfNeeded();

        onView(withText(R.string.title_verify_phone)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.verifyText), withText(containsString("+48111222333")))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.resend), withText(R.string.resend))).check(matches(isDisplayed()));

        onView(withTagValue(is(0))).check(matches(not(hasFocus())));
        onView(allOf(withTagValue(is(0)), withText(""))).check(matches(isDisplayed()));
        onView(allOf(withTagValue(is(1)), withText(""))).check(matches(isDisplayed()));
        onView(allOf(withTagValue(is(2)), withText(""))).check(matches(isDisplayed()));
        onView(allOf(withTagValue(is(3)), withText(""))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.changeMobile), withText(R.string.change_mobile))).perform(click());

        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.email), withText("valid.email@test.com"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.mobile), withText("+48 11 122 23 33"))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.password), withText("secret"))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.image), hasDrawable())).check(matches(isDisplayed()));

    }

    @Test
    @TestCases("C1930538")
    public void registerVerifyPhoneNumberInValidPhoneNumber() {
        removeRequests(RequestType.PHONE_VERIFICATION_SEND_CODE_POST);
        mockRequests(RequestType.PHONE_VERIFICATION_SEND_CODE_400_POST);

        NavigationUtils.startActivity(activityRule);
        closeSoftKeyboard();

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("valid.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.confirm_dialog))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultNegative), withText(R.string.btn_no))).perform(click());

        allowPermissionsIfNeeded();

        onView(withText(R.string.title_verify_phone)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.verifyText), withText(containsString("+48111222333")))).check(matches(isDisplayed()));

        onView(withTagValue(is(0))).check(matches(not(hasFocus())));
        onView(withTagValue(is(0))).perform(typeText("9"));
        onView(withTagValue(is(1))).check(matches(hasFocus()));
        onView(withTagValue(is(1))).perform(typeText("9"));
        onView(withTagValue(is(2))).check(matches(hasFocus()));
        onView(withTagValue(is(2))).perform(typeText("9"));
        onView(withTagValue(is(3))).check(matches(hasFocus()));
        onView(withTagValue(is(3))).perform(typeText("9"));

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        waitForViewInWindow(onView(withText(RiderMockResponseFactory.PHONE_VERIFICATION_ERROR)).inRoot(isToast()));
    }

    @Test
    @TestCases("C1930540")
    public void createProfileValidations() throws IOException {
        NavigationUtils.startActivity(activityRule);
        closeSoftKeyboard();

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("valid.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_yes))).perform(click());

        onView(allOf(withId(R.id.done), withText(R.string.done))).perform(click());
        onView(withId(R.id.firstName)).check(matches(hasErrorText(getAppContext().getString(R.string.first_name_error))));

        onView(withId(R.id.firstName)).perform(typeText("#NameTestData"), closeSoftKeyboard());
        onView(allOf(withId(R.id.done), withText(R.string.done))).perform(click());
        onView(withId(R.id.firstName)).check(matches(hasErrorText(getAppContext().getString(R.string.first_name_special_chars_error))));

        onView(withId(R.id.firstName)).perform(clearText());
        onView(withId(R.id.firstName)).perform(typeText("name"), closeSoftKeyboard());
        onView(allOf(withId(R.id.done), withText(R.string.done))).perform(click());

        onView(withId(R.id.lastName)).check(matches(hasErrorText(getAppContext().getString(R.string.last_name_error))));

        onView(withId(R.id.lastName)).perform(typeText("#NameTestData"), closeSoftKeyboard());
        onView(allOf(withId(R.id.done), withText(R.string.done))).perform(click());

        onView(withId(R.id.lastName)).check(matches(hasErrorText(getAppContext().getString(R.string.last_name_special_chars_error))));
    }

    @Test
    @TestCases("C1930543")
    public void createProfileTermsAndConditionsAustinHouston() throws IOException {
        NavigationUtils.startActivity(activityRule);
        closeSoftKeyboard();

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("valid.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_yes))).perform(click());

        onView(withText(R.string.title_create_profile)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.agreement), withText(getAppContext().getString(R.string.agreement, App.getCityName())))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.terms), withText(R.string.terms))).check(matches(isDisplayed()));

        Uri termsUri = Uri.parse(App.getConfigurationManager().getLastConfiguration().getGeneralInformation().getLegalRider());

        Intent resultData = new Intent();
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(allOf(hasAction(Intent.ACTION_VIEW), hasData(termsUri))).respondWith(result);

        onView(withId(R.id.terms)).perform(click());
        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(termsUri)));
    }

    @Test
    @TestCases("C1930545")
    public void registerRoundUpSelectCharityOption() {
        NavigationUtils.startActivity(activityRule);
        closeSoftKeyboard();

        onView(allOf(withId(R.id.signUp), withText(R.string.register))).perform(click());
        onView(withText(R.string.title_create_account)).check(matches(isDisplayed()));

        ViewInteraction emailField = onView(allOf(withId(R.id.email), isDisplayed()));
        ViewInteraction phoneField = onView(allOf(withId(R.id.mobile), isDisplayed()));
        ViewInteraction passwordField = onView(allOf(withId(R.id.password), isDisplayed()));

        phoneField.perform(clearText(), closeSoftKeyboard());

        emailField.perform(typeText("valid.email@test.com"), closeSoftKeyboard());
        phoneField.perform(typeText("+48111222333"), closeSoftKeyboard());
        passwordField.perform(typeText("secret"), closeSoftKeyboard());

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.correct))).perform(click());

        onView(allOf(withId(R.id.md_title), withText(R.string.bypass_pin_title))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.btn_yes))).perform(click());

        onView(withId(R.id.firstName)).perform(click());
        onView(withId(R.id.firstName)).perform(typeText("espresso"), closeSoftKeyboard());
        onView(withId(R.id.lastName)).perform(typeText("rider"), closeSoftKeyboard());
        onView(allOf(withId(R.id.done), withText(R.string.done))).perform(click());
        onView(withText(R.string.donate)).check(matches(isDisplayed()));

        onView(withId(R.id.switchToggle)).perform(click());
        onView(withText(R.string.choose_charity)).check(matches(isDisplayed()));
        onView(withText("Austin Pets Alive!")).perform(click());

        verifyRequestsCount(RequestType.RIDERS_PUT, is(1));
        verifyRequest(RequestType.RIDERS_PUT, hasBodyContent(containsString("Austin Pets Alive!")));

        //disable charity
        removeRequests(RequestType.RIDERS_PUT);
        mockRequests(RequestType.RIDERS_PUT_NO_CHARITY);

        onView(withId(R.id.switchToggle)).perform(click());
        verifyRequestsCount(RequestType.RIDERS_PUT_NO_CHARITY, is(1));

        onView(allOf(withId(R.id.next), withText(R.string.next))).perform(click());

        onView(withText(R.string.driver_info)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.skip), withText(R.string.skip))).perform(click());

        allowPermissionsIfNeeded();
        tryToCloseRoundUpDialog();

        // open navigation drawer (by swipe)
        onView(withId(R.id.mapContainer)).perform(swipeFromLeftEdge());
        waitForDisplayed(withText(R.string.donate), "wait for menu");
        onView(withText(R.string.donate)).perform(click());
        onView(withText(R.string.round_up)).check(matches(isDisplayed()));
        onView(withText(R.string.choose_charity)).check(matches(not(isDisplayed())));
        onView(withId(R.id.switchToggle)).check(matches(isDisplayed()));
    }

}
