package com.rideaustin.utils;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.rule.ActivityTestRule;
import android.view.View;

import com.rideaustin.ImagePickerMock;
import com.rideaustin.MockDelegate;
import com.rideaustin.R;
import com.rideaustin.api.model.Coordinates;

import org.hamcrest.Matcher;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.BaseUITest.IDLE_TIMEOUT_MS;
import static com.rideaustin.utils.ViewActionUtils.allowPermissionsIfNeeded;
import static com.rideaustin.utils.ViewActionUtils.denyPermissionsIfNeeded;
import static com.rideaustin.utils.ViewActionUtils.swipeFromLeftEdge;
import static com.rideaustin.utils.ViewActionUtils.waitFor;
import static com.rideaustin.utils.ViewActionUtils.waitForCompletelyDisplayed;
import static com.rideaustin.utils.ViewActionUtils.waitForDisplayed;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by Sergey Petrov on 23/05/2017.
 */

public class CommonNavigationUtils {

    private static Subscription movementSubscription = Subscriptions.empty();

    public static void throughLogin(String username, String password) {
        throughLogin(username, password, true);
    }

    public static void throughLogin(String username, String password, boolean allowPermissions) {
        waitForDisplayed(R.id.signIn);

        //Click Sign in button
        onView(allOf(withText(R.string.sign_in), isDisplayed())).perform(click());

        //Enter credentials
        onView(allOf(withId(R.id.email), isDisplayed())).perform(typeText(username), closeSoftKeyboard());
        onView(allOf(withId(R.id.password), isDisplayed())).perform(typeText(password), closeSoftKeyboard());

        //Click Done button
        onView(allOf(withId(R.id.done), withText(R.string.done), isDisplayed())).perform(click());

        if (allowPermissions) {
            allowPermissionsIfNeeded();
        } else {
            denyPermissionsIfNeeded();
        }
    }

    public static void throughLogout() {
        onView(withId(R.id.mapContainer)).perform(swipeFromLeftEdge());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(R.id.navSettings));
        onView(withId(R.id.signOut)).perform(scrollTo(), click());
    }

    public static void takePhoto(@IdRes int viewToOpenBottomSheet) throws IOException {
        takePhoto(withId(viewToOpenBottomSheet));
    }

    public static void takePhoto(Matcher<View> viewToOpenBottomSheet) throws IOException {
        // open bottom sheet
        waitFor(viewToOpenBottomSheet, "wait for view to open bottom sheet", IDLE_TIMEOUT_MS);
        onView(viewToOpenBottomSheet).perform(click());

        waitForCompletelyDisplayed(R.id.takePhotoContainer);

        // mock camera intent
        ImagePickerMock.mockImagePickFromCamera("camera_picked_image.png");

        // click take photo
        onView(withId(R.id.takePhoto)).perform(click());

        allowPermissionsIfNeeded();
        allowPermissionsIfNeeded();
    }

    public static void cancelTakePhoto(@IdRes int viewToOpenBottomSheet) throws IOException {
        cancelTakePhoto(withId(viewToOpenBottomSheet));
    }

    public static void cancelTakePhoto(Matcher<View> viewToOpenBottomSheet) throws IOException {
        // open bottom sheet
        waitFor(viewToOpenBottomSheet, "wait for view to open bottom sheet", IDLE_TIMEOUT_MS);
        onView(viewToOpenBottomSheet).perform(click());

        waitForCompletelyDisplayed(R.id.takePhotoContainer);

        // mock camera intent
        ImagePickerMock.mockImagePickFromCameraCancel();

        // click take photo
        onView(withId(R.id.takePhoto)).perform(click());

        allowPermissionsIfNeeded();
        allowPermissionsIfNeeded();
    }

    public static void choosePhoto(@IdRes int viewToOpenBottomSheet) throws IOException {
        choosePhoto(withId(viewToOpenBottomSheet));
    }

    public static void choosePhoto(Matcher<View> viewToOpenBottomSheet) throws IOException {
        // open bottom sheet
        waitFor(viewToOpenBottomSheet, "wait for view to open bottom sheet", IDLE_TIMEOUT_MS);
        onView(viewToOpenBottomSheet).perform(click());

        waitForCompletelyDisplayed(R.id.takePhotoContainer);

        // mock gallery intent
        ImagePickerMock.mockImagePickFromGallery("gallery_picked_image.png");

        // click choose photo
        onView(withId(R.id.choosePhoto)).perform(click());

        allowPermissionsIfNeeded();
    }

    public static void cancelChoosePhoto(@IdRes int viewToOpenBottomSheet) throws IOException {
        cancelChoosePhoto(withId(viewToOpenBottomSheet));
    }

    public static void cancelChoosePhoto(Matcher<View> viewToOpenBottomSheet) throws IOException {
        // open bottom sheet
        waitFor(viewToOpenBottomSheet, "wait for view to open bottom sheet", IDLE_TIMEOUT_MS);
        onView(viewToOpenBottomSheet).perform(click());

        waitForCompletelyDisplayed(R.id.takePhotoContainer);

        // mock gallery intent
        ImagePickerMock.mockImagePickFromGalleryCancel();

        // click choose photo
        onView(withId(R.id.choosePhoto)).perform(click());

        allowPermissionsIfNeeded();
    }

    public static void startActivity(ActivityTestRule activityTestRule) {
        activityTestRule.launchActivity(new Intent());
        assertNotNull(activityTestRule.getActivity());
    }

    public static void startMoving(MockDelegate delegate, Coordinates[] coordinates, long intervalMs) {
        assertTrue(coordinates.length > 1);
        movementSubscription = Observable.from(coordinates).repeat()
                .concatMap(c -> Observable.just(c).delay(intervalMs, TimeUnit.MILLISECONDS))
                .subscribe(c -> delegate.mockLocation(c.getLat(), c.getLng()));
    }

    public static void stopMoving() {
        movementSubscription.unsubscribe();
    }
}
