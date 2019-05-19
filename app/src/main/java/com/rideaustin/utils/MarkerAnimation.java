package com.rideaustin.utils;

/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */

import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.rideaustin.ui.map.AnimateObject;

public class MarkerAnimation {

    public static Job animateMarkerToGB(final long durationInMs,
                                        final Marker marker,
                                        final LatLng finalPosition,
                                        final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            float elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = (float) SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        };
        handler.post(runnable);
        return new Job(handler, runnable);
    }

    public static Job animateMapObjectToPosition(final long durationInMs,
                                                 final AnimateObject object,
                                                 final LatLng finalPosition,
                                                 final float finalCourse,
                                                 final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = object.getPosition();
        final float startCourse = object.getRotation();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();

        final boolean animateCourse = Math.abs(startCourse - finalCourse) < 90;
        if (!animateCourse) {
            object.setRotation(finalCourse);
        }

        Runnable runnable = new Runnable() {
            float elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = (float) SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);
                LatLng target = latLngInterpolator.interpolate(v, startPosition, finalPosition);
                object.setPosition(target);

                if (animateCourse) {
                    object.setRotation((finalCourse - startCourse) * v + startCourse);
                }

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        };
        handler.post(runnable);
        return new Job(handler, runnable);
    }


    public static class Job {

        private final Handler handler;
        private final Runnable runnable;

        private Job(Handler handler, Runnable runnable) {
            this.handler = handler;
            this.runnable = runnable;
        }

        public void cancel() {
            handler.removeCallbacks(runnable);
        }
    }

}
