package com.rideaustin.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.NoMatchingRootException;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

/**
 * Created by Sergey Petrov on 22/06/2017.
 */

public class Matchers {

    public static Condition condition() {
        return condition("Condition");
    }

    public static Condition condition(@NonNull String message) {
        return new Condition(message);
    }

    public static void waitFor(Condition condition) throws InterruptedException {
        if (condition.delay > 0L) {
            Thread.sleep(condition.delay);
        }

        long startTime = System.currentTimeMillis();
        long endTime = startTime + condition.timeout;
        do {
            if (condition.isMet()) {
                return;
            }
            Thread.sleep(condition.interval);

        } while (System.currentTimeMillis() < endTime);

        condition.fail();
    }

    public static class Condition {

        private String message;
        private long timeout = 30000L;
        private long delay = 0L;
        private long interval = 200L;

        @Nullable
        private Matcher<View> viewMatcher;

        @Nullable
        private ViewInteraction viewInteraction;

        @Nullable
        private DataInteraction dataInteraction;

        @Nullable
        private BoolCondition boolCondition;

        private ViewAssertion viewAssertion;

        Condition(@NonNull String message) {
            this.message = message;
        }

        boolean isMet() {
            checkPreconditions();
            try {
                if (viewInteraction != null) {
                    viewInteraction.check(viewAssertion);
                }
                if (dataInteraction != null) {
                    dataInteraction.check(viewAssertion);
                }
                if (boolCondition != null) {
                    return boolCondition.isMet();
                }
                return true;
            } catch (AssertionFailedError | RuntimeException e) {
                return false;
            }
        }

        void fail() {
            Assert.fail(message);
        }

        public Condition withMatcher(Matcher<View> matcher) {
            if (viewMatcher != null) {
                viewMatcher = AllOf.allOf(viewMatcher, matcher);
            } else {
                viewMatcher = matcher;
            }
            return this;
        }

        public Condition withView(ViewInteraction view) {
            viewInteraction = view;
            return this;
        }

        public Condition withData(DataInteraction data) {
            dataInteraction = data;
            return this;
        }

        public Condition withCheck(Matcher<View> matcher) {
            if (viewAssertion == null) {
                return withAssertion(matches(matcher));
            } else {
                return withMatcher(matcher);
            }
        }

        public Condition withAssertion(ViewAssertion assertion) {
            viewAssertion = assertion;
            return this;
        }

        public Condition withBool(BoolCondition bool) {
            boolCondition = bool;
            return this;
        }

        public Condition withTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Condition withInterval(long interval) {
            this.interval = interval;
            return this;
        }

        public Condition withDelay(long delay) {
            this.delay = delay;
            return this;
        }

        private void checkPreconditions() {
            if (viewMatcher != null) {
                viewInteraction = onView(viewMatcher);
            }
            if (viewInteraction == null && dataInteraction == null && boolCondition == null) {
                throw new IllegalStateException("Need at least one condition to perform check on");
            }
            if (viewAssertion == null && (viewInteraction != null || dataInteraction != null)) {
                viewAssertion = matches(isDisplayed());
            }
        }
    }

    public interface BoolCondition {
        boolean isMet();
    }

}
