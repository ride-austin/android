package com.rideaustin.utils;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import com.etiennelawlor.discreteslider.library.ui.DiscreteSeekBar;
import com.etiennelawlor.discreteslider.library.ui.DiscreteSlider;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static com.rideaustin.utils.ViewActionUtils.setSeekbarProgress;

/**
 * Created by Sergey Petrov on 23/05/2017.
 */

public class CarSliderUtils {

    public static Matcher<View> carTypesCount(int count) {
        return new BoundedMatcher<View, DiscreteSlider>(DiscreteSlider.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("car slider has " + count + " car types");
            }

            @Override
            protected boolean matchesSafely(DiscreteSlider item) {
                return item.getTickMarkCount() == count;
            }
        };
    }

    public static Matcher<View> carTypeSelected(int index) {
        return new BoundedMatcher<View, DiscreteSlider>(DiscreteSlider.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("car slider is on " + index + " position");
            }

            @Override
            protected boolean matchesSafely(DiscreteSlider item) {
                return item.getPosition() == index;
            }
        };
    }

    public static Matcher<View> carTypeTitle(String title) {
        return new BoundedMatcher<View, DiscreteSlider>(DiscreteSlider.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("car slider has \"" + title + "\" selected");
            }

            @Override
            protected boolean matchesSafely(DiscreteSlider item) {
                View seekBar = item.findViewById(com.etiennelawlor.discreteslider.library.R.id.discrete_seek_bar);
                return seekBar.getContentDescription().equals(title);
            }
        };
    }

    public static ViewAction selectCarType(int index) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                DiscreteSlider slider = ((DiscreteSlider) view);
                DiscreteSeekBar seekBar = (DiscreteSeekBar) slider.findViewById(com.etiennelawlor.discreteslider.library.R.id.discrete_seek_bar);
                float stepSize = seekBar.getMax() / (slider.getTickMarkCount() - 1);
                int progress = index * (int) stepSize;
                setSeekbarProgress(progress).perform(uiController, seekBar);
            }

            @Override
            public String getDescription() {
                return "set car slider to " + index + " position";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(DiscreteSlider.class);
            }
        };
    }
}
