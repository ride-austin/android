package com.rideaustin.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.FrameLayout;

/**
 * Created by yshloma on 21.07.2016.
 */
public class TouchWrapper extends FrameLayout {

    public static final int DELTA_TIME = 50;
    public static final int NO_SPAN = -1;
    public static final int ZOOM_FACTOR = 7;

    @Nullable
    private TouchWrapperListener listener;
    private boolean isShowPickupMarker = true;

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private int fingers = 0;
    private long lastZoomTime = 0;
    private float lastSpan = NO_SPAN;

    public TouchWrapper(@NonNull Context context) {
        super(context);
        init();
    }

    public TouchWrapper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TouchWrapper(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
        init();
    }

    private void init() {
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (lastSpan == NO_SPAN) {
                    lastSpan = detector.getCurrentSpan();
                } else if (detector.getEventTime() - lastZoomTime >= DELTA_TIME) {
                    lastZoomTime = detector.getEventTime();
                    if (listener != null) {
                        listener.onMapZoom(getZoomValue(detector.getCurrentSpan(), lastSpan));
                    }
                    lastSpan = detector.getCurrentSpan();
                }
                return false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                lastSpan = NO_SPAN;
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                lastSpan = NO_SPAN;
            }
        });

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                if (listener != null) {
                    listener.onMapScrollingEnabled(false);
                    listener.onMapZoomIn();
                }
                return true;
            }
        });
    }

    private float getZoomValue(float currentSpan, float lastSpan) {
        double value = (Math.log(currentSpan / lastSpan) * ZOOM_FACTOR);
        return (float) value;
    }

    public void setShowPickupMarker(boolean isShowPickupMarker) {
        this.isShowPickupMarker = isShowPickupMarker;
    }

    public void setListener(@NonNull TouchWrapperListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                fingers = fingers + 1;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                fingers = fingers - 1;
                break;
            case MotionEvent.ACTION_UP:
                fingers = 0;
                break;
            case MotionEvent.ACTION_DOWN:
                if (listener != null) {
                    listener.onTouched();
                }
                fingers = 1;
                break;
            case MotionEvent.ACTION_MOVE:
                if (listener != null) {
                    listener.onTouchMoved();
                }
                break;
        }

        if (listener != null) {
            if (fingers > 1) {
                listener.onMapScrollingEnabled(false);
            } else if (fingers < 1) {
                listener.onMapScrollingEnabled(true);
                listener.onTouchReleased(isShowPickupMarker);
            }
        }
        if (fingers > 1) {
            return scaleGestureDetector.onTouchEvent(ev);
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    public interface TouchWrapperListener {
        void onTouchReleased(final boolean showMarker);

        void onTouchMoved();

        void onTouched();

        void onMapZoom(float zoomValue);

        void onMapZoomIn();

        void onMapScrollingEnabled(boolean enabled);
    }
}
