package com.rideaustin.ui.map;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.manager.location.RALocation;
import com.rideaustin.utils.Constants;

import java8.util.Optional;

/**
 * Created by hatak on 22.02.2017.
 */

public class DriverCarMarker extends CarMarker {
    ValueAnimator alphaAnimation = ValueAnimator.ofFloat(0, 1).setDuration(1000);
    ValueAnimator accuracyAnimation = ValueAnimator.ofFloat(0, 1).setDuration(1000);
    private Circle circle;

    public DriverCarMarker(Context context, final GoogleMap googleMap, RALocation driverLocation, final float transparency) {
        super(context, googleMap, driverLocation, transparency, Optional.empty());
        circle = googleMap.addCircle(new CircleOptions()
                .center(driverLocation.getCoordinates())
                .strokeColor(Color.GRAY)
                .fillColor(Color.argb(128, 192, 192, 192))
                .strokeWidth(0.4f)
                .zIndex(2));
        setRadiusForAccuracyCircle(driverLocation.getLocation().getAccuracy());
    }

    private void setRadiusForAccuracyCircle(float accuracy) {
        if (accuracy >= 0) {
            circle.setRadius(accuracy / 2);
        }
    }

    @Override
    public void update(RALocation driverLocation, long dt) {
        float accuracy = driverLocation.getLocation().getAccuracy();
        boolean invalidData = accuracy < 0 || accuracy > Constants.LOCATION_HORIZONTAL_ACCURACY_FILTER;
        animateAccuracyCircle(accuracy / 2);
        if (!invalidData) {
            alphaAnimation.end();
            marker.setAlpha(Constants.NON_TRANSPARENT);
            super.update(driverLocation, dt);
        } else {
            animateBlinkingCar();
        }
    }

    @Override
    public void clear() {
        super.clear();
        circle.remove();
    }

    @Override
    public void setPosition(LatLng target) {
        super.setPosition(target);
        circle.setCenter(target);
    }

    private void animateAccuracyCircle(float targetRadius) {
        if (!accuracyAnimation.isStarted()) {
            accuracyAnimation.setFloatValues((float) circle.getRadius(), targetRadius);
            accuracyAnimation.addUpdateListener(animation -> setRadiusForAccuracyCircle((float) animation.getAnimatedValue()));
            accuracyAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    accuracyAnimation.removeAllUpdateListeners();
                    accuracyAnimation.removeAllListeners();
                    setRadiusForAccuracyCircle(targetRadius);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            accuracyAnimation.start();
        }
    }

    private void animateBlinkingCar() {
        if (!alphaAnimation.isStarted()) {
            alphaAnimation.addUpdateListener(animation -> marker.setAlpha((float) animation.getAnimatedValue()));
            alphaAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    alphaAnimation.removeAllUpdateListeners();
                    alphaAnimation.removeAllListeners();
                    marker.setAlpha(Constants.NON_TRANSPARENT);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            alphaAnimation.start();
        }
    }

}
