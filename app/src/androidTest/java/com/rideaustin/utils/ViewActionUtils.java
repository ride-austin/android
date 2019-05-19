package com.rideaustin.utils;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.ScrollToAction;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.util.HumanReadables;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.ui.widgets.CustomRatingBar;
import com.rideaustin.utils.location.LocationHelper;
import com.wdullaer.materialdatetimepicker.date.DatePickerController;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;

import org.hamcrest.Matcher;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import timber.log.Timber;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.actionWithAssertions;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.rideaustin.BaseUITest.IDLE_TIMEOUT_MS;
import static com.rideaustin.utils.DeviceTestUtils.search;
import static com.rideaustin.utils.MatcherUtils.isToast;
import static com.rideaustin.utils.Matchers.condition;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;

/**
 * Created by crossover on 18/05/2017.
 */

public class ViewActionUtils {

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitForDisplayed(@IdRes int viewId) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Matchers.waitFor(condition()
                    .withMatcher(withId(viewId))
                    .withTimeout(IDLE_TIMEOUT_MS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitForDisplayed(Matcher<View> matcher, String message) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Matchers.waitFor(condition(message)
                    .withMatcher(matcher)
                    .withTimeout(IDLE_TIMEOUT_MS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitForCompletelyDisplayed(@IdRes int viewId) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Matchers.waitFor(condition()
                    .withMatcher(allOf(withId(viewId), isCompletelyDisplayed()))
                    .withTimeout(IDLE_TIMEOUT_MS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitForCompletelyDisplayed(Matcher<View> matcher, String message) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Matchers.waitFor(condition(message)
                    .withMatcher(allOf(matcher, isCompletelyDisplayed()))
                    .withTimeout(IDLE_TIMEOUT_MS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitForNotDisplayed(@IdRes int viewId) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Matchers.waitFor(condition()
                    .withMatcher(withId(viewId))
                    .withCheck(not(isDisplayed()))
                    .withTimeout(IDLE_TIMEOUT_MS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitForNotDisplayed(Matcher<View> matcher, String message) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Matchers.waitFor(condition(message)
                    .withMatcher(allOf(matcher, not(isDisplayed())))
                    .withTimeout(IDLE_TIMEOUT_MS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitForNotEmptyText(@IdRes int viewId) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Matchers.waitFor(condition()
                    .withMatcher(allOf(withId(viewId), isDisplayed(), not(withText(""))))
                    .withTimeout(IDLE_TIMEOUT_MS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitFor(String detail, long timeInMillis) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Thread.sleep(timeInMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(detail);
        }
    }

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitFor(@IdRes int viewId, Matcher<View> matcher) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Matchers.waitFor(condition(App.getInstance().getResources().getResourceName(viewId))
                    .withMatcher(allOf(withId(viewId), matcher))
                    .withTimeout(IDLE_TIMEOUT_MS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitFor(@IdRes int viewId, Matcher<View> matcher, long timeInMillis) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Matchers.waitFor(condition(App.getInstance().getResources().getResourceName(viewId))
                    .withMatcher(allOf(withId(viewId), matcher))
                    .withTimeout(timeInMillis));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitFor(Matcher<View> matcher, String message, long timeInMillis) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Matchers.waitFor(condition(message)
                    .withMatcher(matcher)
                    .withTimeout(timeInMillis));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    public static void waitForToast(Matcher<View> matcher) throws InterruptedException {
        Matchers.waitFor(condition().withView(onView(matcher).inRoot(isToast())));
    }

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitForViewInWindow(Matcher<View> matcher) {
        waitForViewInWindow(onView(matcher));
    }

    /**
     * Method {@link #waitFor(Matcher, String, long)} does not work
     * for dialogs, because they seem not to be in root view...
     * Use this hack until found better solution
     * Seems it is a better solution :)
     */
    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitForViewInWindow(ViewInteraction interaction) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Matchers.waitFor(condition()
                    .withView(interaction)
                    .withTimeout(IDLE_TIMEOUT_MS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitForCondition(String message, MatcherUtils.Condition condition) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Matchers.waitFor(condition(message)
                    .withBool(condition::isSatisfied)
                    .withTimeout(IDLE_TIMEOUT_MS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    // Deprecated because planned to review and move most frequently used to Matchers
    @Deprecated
    public static void waitForData(DataInteraction dataInteraction) {
        // wrap this into try..catch to not to affect callers' code
        // but better to throw it to the test method directly
        try {
            Matchers.waitFor(condition()
                    .withData(dataInteraction)
                    .withTimeout(IDLE_TIMEOUT_MS));
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    public static void allowPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            UiObject allowPermissions = device.findObject(new UiSelector().className("android.widget.Button")
                    .resourceId("com.android.packageinstaller:id/permission_allow_button"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    Timber.e(e, "There is no permissions dialog to interact with ");
                }
            }
        }
    }

    public static void denyPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            UiObject denyPermissions = device.findObject(new UiSelector().className("android.widget.Button")
                    .resourceId("com.android.packageinstaller:id/permission_deny_button"));
            if (denyPermissions.exists()) {
                try {
                    denyPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    Timber.e(e, "There is no permissions dialog to interact with ");
                }
            }
        }
    }

    public static void allowPermissionsIfNeeded(long timeout) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            DeviceTestUtils.SearchBuilder sb = search().res("com.android.packageinstaller:id/permission_allow_button");
            boolean exists = sb.exists(timeout);
            if (exists) {
                sb.object().click();
            }
        }
    }

    public static ViewAction swipeFromLeftEdge() {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER_LEFT, GeneralLocation.CENTER_RIGHT, Press.FINGER);
    }

    public static ViewAction swipeToOpenBottomSheet() {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_CENTER, view -> new float[]{0f, -150f}, Press.FINGER);
    }

    public static ViewAction setSeekbarProgress(int progress) {
        return actionWithAssertions(new GeneralClickAction(Tap.SINGLE, new SeekBarThumbCoordinatesProvider(progress), Press.FINGER));
    }

    /**
     * http://stackoverflow.com/a/39827232
     */
    public static ViewAction swipeSeekbar(int fromProgress, int toProgress) {
        return actionWithAssertions(new GeneralSwipeAction(
                Swipe.FAST,
                new SeekBarThumbCoordinatesProvider(fromProgress),
                new SeekBarThumbCoordinatesProvider(toProgress),
                Press.FINGER));
    }

    private static class SeekBarThumbCoordinatesProvider implements CoordinatesProvider {
        int mProgress;

        public SeekBarThumbCoordinatesProvider(int progress) {
            mProgress = progress;
        }

        private static float[] getVisibleLeftTop(View view) {
            final int[] xy = new int[2];
            view.getLocationOnScreen(xy);
            return new float[]{(float) xy[0], (float) xy[1]};
        }

        @Override
        public float[] calculateCoordinates(View view) {
            if (!(view instanceof SeekBar)) {
                throw new PerformException.Builder()
                        .withViewDescription(HumanReadables.describe(view))
                        .withCause(new RuntimeException("SeekBar expected")).build();
            }
            SeekBar seekBar = (SeekBar) view;
            int width = seekBar.getWidth() - seekBar.getPaddingLeft() - seekBar.getPaddingRight();
            int xPosition = seekBar.getPaddingLeft() + width * mProgress / seekBar.getMax();
            float[] xy = getVisibleLeftTop(seekBar);
            return new float[]{xy[0] + xPosition, xy[1] + view.getHeight() / 2};
        }
    }

    public static ViewAction setRating(final float rating) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                ((CustomRatingBar) view).setScore(rating);
            }

            @Override
            public String getDescription() {
                return "set rating";
            }

            @SuppressWarnings("unchecked")
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isAssignableFrom(CustomRatingBar.class), isDisplayed());
            }
        };
    }

    public static ViewAction betterScrollTo() {
        return ViewActions.actionWithAssertions(new BetterScrollToAction());
    }

    private static class BetterScrollToAction implements ViewAction {

        private static final String TAG = ScrollToAction.class.getSimpleName();

        @SuppressWarnings("unchecked")
        @Override
        public Matcher<View> getConstraints() {
            return allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE), isDescendantOfA(anyOf(
                    isAssignableFrom(ScrollView.class), isAssignableFrom(HorizontalScrollView.class), isAssignableFrom(NestedScrollView.class))));
        }

        @Override
        public void perform(UiController uiController, View view) {
            if (isDisplayingAtLeast(90).matches(view)) {
                return;
            }
            Rect rect = new Rect();
            view.getDrawingRect(rect);
            if (!view.requestRectangleOnScreen(rect, true /* immediate */)) {
                Log.w(TAG, "Scrolling to view was requested, but none of the parents scrolled.");
            }
            uiController.loopMainThreadUntilIdle();
            if (!isDisplayingAtLeast(90).matches(view)) {
                throw new PerformException.Builder()
                        .withActionDescription(this.getDescription())
                        .withViewDescription(HumanReadables.describe(view))
                        .withCause(new RuntimeException(
                                "Scrolling to view was attempted, but the view is not displayed"))
                        .build();
            }
        }

        @Override
        public String getDescription() {
            return "scroll to";
        }
    }

    // https://codedump.io/share/NdvCB23kZCrf/1/how-to-set-time-to-materialdatetimepicker-with-espresso
    public static ViewAction setDate(final int year, final int monthOfYear, final int dayOfMonth) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                final DayPickerView dayPickerView = (DayPickerView) view;

                try {
                    Field f = DayPickerView.class.getDeclaredField("mController");
                    f.setAccessible(true);
                    DatePickerController controller = (DatePickerController) f.get(dayPickerView);
                    controller.onDayOfMonthSelected(year, monthOfYear, dayOfMonth);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    fail(e.getMessage());
                }
            }

            @Override
            public String getDescription() {
                return "set date";
            }

            @SuppressWarnings("unchecked")
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isAssignableFrom(DayPickerView.class), isDisplayed());
            }
        };
    }

    // http://stackoverflow.com/a/23467629
    public static String getText(final Matcher<View> matcher) {
        return getText(onView(matcher));
    }

    public static String getText(final ViewInteraction viewInteraction) {
        final String[] stringHolder = {null};
        viewInteraction.perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "getting text from a TextView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView tv = (TextView) view; //Save, because of check in getConstraints()
                stringHolder[0] = tv.getText().toString();
            }
        }, closeSoftKeyboard());
        return stringHolder[0];
    }

    public static String getText(final DataInteraction dataInteraction) {
        final String[] stringHolder = {null};
        dataInteraction.perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "getting text from a TextView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView tv = (TextView) view; //Save, because of check in getConstraints()
                stringHolder[0] = tv.getText().toString();
            }
        });
        return stringHolder[0];
    }


    public static ViewAction setStaticProgressBar() {
        return actionWithAssertions(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(ProgressBar.class);
            }

            @Override
            public String getDescription() {
                return "replace the ProgressBar drawable";
            }

            @Override
            public void perform(final UiController uiController, final View view) {
                ProgressBar progressBar = (ProgressBar) view;
                Drawable drawable = ContextCompat.getDrawable(view.getContext(), R.drawable.test_progress_bar);
                progressBar.setIndeterminateDrawable(drawable);
                uiController.loopMainThreadUntilIdle();
            }
        });
    }

    public static void applyPrediction(String id, String text, double lat, double lng) {
        LocationHelper.autocompletePredictionsProvider = (apiClient, query, bounds, filter) -> {
            List<AutocompletePrediction> predictions = new ArrayList<>();
            predictions.add(createPrediction(id, text));
            return Observable.just(predictions);
        };
        LocationHelper.placeByIdProvider = (apiClient, placeId) -> {
            if (placeId.equals(id)) {
                return Observable.just(new GeoPosition(lat, lng, text, text));
            }
            return Observable.error(new Throwable("Unexpected place id: " + placeId));
        };
    }

    private static AutocompletePrediction createPrediction(String id, String text) {
        return new AutocompletePrediction() {
            @Override
            public CharSequence getFullText(@Nullable CharacterStyle characterStyle) {
                return text;
            }

            @Override
            public CharSequence getPrimaryText(@Nullable CharacterStyle characterStyle) {
                return text;
            }

            @Override
            public CharSequence getSecondaryText(@Nullable CharacterStyle characterStyle) {
                return "";
            }

            @Nullable
            @Override
            public String getPlaceId() {
                return id;
            }

            @Nullable
            @Override
            public List<Integer> getPlaceTypes() {
                return new ArrayList<>();
            }

            @Override
            public AutocompletePrediction freeze() {
                return this;
            }

            @Override
            public boolean isDataValid() {
                return true;
            }

            @Override
            public String toString() {
                return text;
            }
        };
    }
}