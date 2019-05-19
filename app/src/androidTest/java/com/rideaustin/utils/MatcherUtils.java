package com.rideaustin.utils;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.NoMatchingRootException;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.Root;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rideaustin.FormBasedRequest;
import com.rideaustin.RequestMatcher;
import com.rideaustin.RequestStats;
import com.rideaustin.ui.widgets.CustomRatingBar;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Created by hatak on 16.05.2017.
 */

public class MatcherUtils {

    public static BoundedMatcher<View, ImageView> hasDrawable() {
        return new BoundedMatcher<View, ImageView>(ImageView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has drawable");
            }

            @Override
            public boolean matchesSafely(ImageView imageView) {
                return imageView.getDrawable() != null;
            }
        };
    }

    public static BoundedMatcher<View, ImageView> hasDrawable(Matcher<Drawable> matcher) {
        return new BoundedMatcher<View, ImageView>(ImageView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has drawable");
            }

            @Override
            public boolean matchesSafely(ImageView imageView) {
                return matcher.matches(imageView.getDrawable());
            }
        };
    }

    public static BoundedMatcher<View, ImageView> hasDrawable(int resourceId) {
        return new BoundedMatcher<View, ImageView>(ImageView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has drawable");
            }

            @Override
            public boolean matchesSafely(ImageView imageView) {
                return resourceId == getResourceId(imageView);
            }

            private Integer getResourceId(ImageView imageView) {
                try {
                    Class<?> clazz = imageView.getClass();
                    while (clazz != ImageView.class) {
                        clazz = clazz.getSuperclass();
                    }

                    Field f = clazz.getDeclaredField("mResource");
                    f.setAccessible(true);
                    return (Integer) f.get(imageView);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                }
            }
        };
    }



    public static Matcher<View> hasNoErrorText() {
        return new BoundedMatcher<View, EditText>(EditText.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("has no error text");
            }

            @Override
            protected boolean matchesSafely(EditText view) {
                return view.getError() == null;
            }
        };
    }

    public static Matcher<View> navigationIcon() {
        return allOf(
                isAssignableFrom(ImageButton.class),
                withParent(isAssignableFrom(Toolbar.class)));
    }

    public static Matcher<View> hasRotation(int degree) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("rotated at " + degree + " degrees");
            }

            @Override
            public boolean matchesSafely(View view) {
                return view.getRotation() == degree;
            }
        };
    }

    public static boolean exists(Matcher<View> matcher) {
        return exists(onView(matcher));
    }

    public static boolean exists(ViewInteraction viewInteraction) {
        try {
            viewInteraction.check(matches(isDisplayed()));
            return true;
        } catch (NoMatchingViewException | NoMatchingRootException | AssertionFailedError e) {
            return false;
        }
    }

    public static boolean exists(DataInteraction dataInteraction) {
        try {
            dataInteraction.check(matches(isDisplayed()));
            return true;
        } catch (NoMatchingViewException | NoMatchingRootException e) {
            return false;
        }
    }

    public static RequestMatcher hasFieldInPost(String fieldName) {
        return new RequestMatcher() {
            @Override
            public Result match(@NonNull RequestStats requestStats) {
                for (Request request : requestStats.getRequests()) {
                    if (request.body() instanceof FormBody) {
                        FormBasedRequest formBasedRequest = new FormBasedRequest((FormBody) request.body());
                        if (TextUtils.isEmpty(formBasedRequest.getValueForField(fieldName))) {
                            return fail("Field \"" + fieldName + "\" is empty in POST");
                        }
                    } else {
                        return fail("Request body is not a FormBody");
                    }
                }
                return success();
            }
        };
    }

    public static RequestMatcher hasFieldInPost(String fieldName, Matcher<String> matcher) {
        return new RequestMatcher() {
            @Override
            public Result match(@NonNull RequestStats requestStats) {
                for (Request request : requestStats.getRequests()) {
                    if (request.body() instanceof FormBody) {
                        FormBasedRequest formBasedRequest = new FormBasedRequest((FormBody) request.body());
                        String field = formBasedRequest.getValueForField(fieldName);
                        if (!matcher.matches(field)) {
                            return fail("Field \"" + fieldName + "\" with content \"" + field + "\" does not match " + matcher);
                        }
                    } else {
                        return fail("Request body is not a FormBody");
                    }
                }
                return success();
            }
        };
    }


    public static RequestMatcher hasBodyContent(Matcher<String> data) {
        return new RequestMatcher() {
            @Override
            public Result match(@NonNull RequestStats requestStats) {
                for (Request request : requestStats.getRequests()) {
                    if (request.body() != null) {
                        String content = bodyToString(request);
                        if (!data.matches(content)) {
                            return fail("String: [" + data + "] not found in request body");
                        }
                    } else {
                        fail("body not found");
                    }
                }
                return success();
            }
        };
    }

    public static RequestMatcher hasMultipartParam(@NonNull String partName, @NonNull String param, Matcher<?> paramValue) {
        return new RequestMatcher() {
            @Override
            public Result match(@NonNull RequestStats requestStats) {
                for (Request request : requestStats.getRequests()) {
                    MultipartBody body = (MultipartBody) request.body();

                    List<MultipartBody.Part> parts = body.parts();
                    boolean found = false;
                    for (MultipartBody.Part value : parts) {
                        try {
                            Headers headers = (Headers) FieldUtils.readField(value, "headers", true);
                            String nameHeader = headers.get("Content-Disposition");
                            String calculatedHeader = "form-data; name=\"" + partName + "\"";
                            String calculatedCleanHeader = "form-data; name=" + partName;
                            if (calculatedCleanHeader.equals(nameHeader) || calculatedHeader.equals(nameHeader)) {

                                RequestBody requestBody = (RequestBody) FieldUtils.readField(value, "body", true);
                                JSONObject object = new JSONObject(bodyToString(new Request.Builder()
                                        .url("https://api-rc.rideaustin.com/")
                                        .post(requestBody)
                                        .build()));
                                if (!paramValue.matches(object.get(param))) {
                                    return fail("expected value: " + paramValue + " for parameter: " + param + " is different than found: " + object.get(partName));
                                } else {
                                    found = true;
                                    break;
                                }
                            }
                        } catch (IllegalAccessException | JSONException e) {
                            return fail(e.getMessage());
                        }
                    }
                    if (!found) {
                        return fail("expected value: " + paramValue + " for parameter: " + param + " not found");
                    }
                }
                return success();
            }
        };
    }

    public static RequestMatcher hasMultipartData(@NonNull String partName) {
        return new RequestMatcher() {
            @Override
            public Result match(@NonNull RequestStats requestStats) {
                for (Request request : requestStats.getRequests()) {
                    MultipartBody body = (MultipartBody) request.body();

                    List<MultipartBody.Part> parts = body.parts();
                    boolean found = false;
                    for (MultipartBody.Part value : parts) {
                        try {
                            Headers headers = (Headers) FieldUtils.readField(value, "headers", true);
                            String nameHeader = headers.get("Content-Disposition");
                            String calculatedHeader = "form-data; name=\"" + partName + "\"";
                            String calculatedCleanHeader = "form-data; name=" + partName;
                            if (nameHeader.startsWith(calculatedHeader) || nameHeader.startsWith(calculatedCleanHeader)) {
                                found = true;
                                break;
                            }
                        } catch (IllegalAccessException e) {
                            return fail(e.getMessage());
                        }
                    }
                    if (!found) {
                        return fail("expected name: " + partName + " not found");
                    }
                }
                return success();
            }
        };
    }

    public static RequestMatcher hasQueryParam(String param, Matcher<String> paramValue) {
        return new RequestMatcher() {
            @Override
            public Result match(@NonNull RequestStats requestStats) {
                for (Request request : requestStats.getRequests()) {
                    HttpUrl url = request.url();

                    List<String> values = url.queryParameterValues(param);
                    boolean found = false;
                    for (String value : values) {
                        if (paramValue.matches(value)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return fail("expected value: " + paramValue + " for parameter: " + param + " not found");
                    }
                }
                return success();
            }
        };
    }

    public static RequestMatcher noQueryParam(String param) {
        return new RequestMatcher() {
            @Override
            public Result match(@NonNull RequestStats requestStats) {
                for (Request request : requestStats.getRequests()) {
                    HttpUrl url = request.url();
                    String value = url.queryParameter(param);
                    if (value != null) {
                        return fail("parameter: " + param + " not expected but found with value: " + value);
                    }
                }
                return success();
            }
        };
    }

    private static String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            throw new RuntimeException("can't read body content");
        }
    }

    public static Matcher<Root> isToast() {
        return new TypeSafeMatcher<Root>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("is toast");
            }

            @Override
            public boolean matchesSafely(Root root) {
                int type = root.getWindowLayoutParams().get().type;
                if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                    IBinder windowToken = root.getDecorView().getWindowToken();
                    IBinder appToken = root.getDecorView().getApplicationWindowToken();
                    if (windowToken == appToken) {
                        // windowToken == appToken means this window isn't contained by any other windows.
                        // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    public static Matcher<Root> isNotDecorView(ActivityTestRule activityTestRule) {
        return withDecorView(not(activityTestRule.getActivity().getWindow().getDecorView()));
    }

    public static <T> Matcher<T> first(final Matcher<T> matcher) {
        return new BaseMatcher<T>() {
            boolean isFirst = true;

            @Override
            public boolean matches(final Object item) {
                if (isFirst && matcher.matches(item)) {
                    isFirst = false;
                    return true;
                }

                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("should return first matching item");
            }
        };
    }

    public static Matcher<String> matchesRegex(final String regex) {
        return new TypeSafeMatcher<String>() {

            @Override
            protected boolean matchesSafely(String item) {
                return item.matches(regex);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("should match expression=`" + regex + "`");
            }
        };
    }

    public static Matcher<View> withEmptyText() {
        return new BoundedMatcher<View, TextView>(TextView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("with empty text");
            }

            @Override
            public boolean matchesSafely(TextView textView) {
                return TextUtils.isEmpty(textView.getText().toString());
            }
        };
    }

    public static Matcher<View> withListSize(final int size) {
        return new BoundedMatcher<View, ListView>(ListView.class) {
            @Override public boolean matchesSafely (final ListView listView) {
                return listView.getCount () == size;
            }

            @Override public void describeTo (final Description description) {
                description.appendText ("ListView should have " + size + " items");
            }
        };
    }

    public static Matcher<View> withAlpha(float alpha) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("alpha is " + alpha);
            }

            @Override
            public boolean matchesSafely(View view) {
                return Float.compare(view.getAlpha(), alpha) == 0;
            }
        };
    }

    public static Matcher<View> hasNavigationItem(@IdRes int itemId, boolean visible) {
        return new BoundedMatcher<View, NavigationView>(NavigationView.class) {
            Resources resources = null;
            @Override
            public void describeTo(Description description) {
                String idDescription = Integer.toString(itemId);
                if (resources != null) {
                    try {
                        idDescription = resources.getResourceName(itemId);
                    } catch (Resources.NotFoundException e) {
                        // No big deal, will just use the int value.
                        idDescription = String.format("%s (resource name not found)", itemId);
                    }
                }
                description.appendText("has navigation item with id: " + idDescription + ", with visible: " + visible);
            }

            @Override
            public boolean matchesSafely(NavigationView view) {
                resources = view.getResources();
                MenuItem item = view.getMenu().findItem(itemId);
                if (item != null) {
                    return item.isVisible() == visible;
                }
                return false;
            }
        };
    }

    public static Matcher<View> isStarSelected(int index, boolean selected) {
        return new BoundedMatcher<View, CustomRatingBar>(CustomRatingBar.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("has no error text");
            }

            @Override
            protected boolean matchesSafely(CustomRatingBar view) {
                if (selected) {
                    return view.isStarOn(index);
                } else {
                    return view.isStarOff(index);
                }
            }
        };
    }


    @Deprecated
    public interface Condition {
        boolean isSatisfied();
    }
}
