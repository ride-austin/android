package com.rideaustin.ui.widgets;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.rideaustin.R;

/**
 * {@link RelativeLayout} with custom {@link ProgressBar}
 * Designed to use when indicating auto sign in process.
 *
 * Created by Sergey Petrov on 20/03/2017.
 */
public class LoadingWheel extends RelativeLayout {

    private static int DURATION_MOVE_MS = 200;

    private View progressView;
    private boolean isCentered;
    private boolean showBackground;
    private int maxMargin;
    private ValueAnimator animator;

    public LoadingWheel(Context context) {
        super(context);
        init(context);
    }

    public LoadingWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadingWheel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public LoadingWheel(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        progressView = findViewById(R.id.progressView);
        placeInCenter(isCentered, false);
    }

    private void init(Context context) {
        maxMargin = context.getResources().getDimensionPixelSize(R.dimen.loading_wheel_max_margin);
    }

    /**
     * Show self at the top of {@link Activity}'s content view.
     * @param activity host activity to show self
     */
    public void show(Activity activity) {
        FrameLayout root = (FrameLayout) activity.findViewById(android.R.id.content);
        root.addView(this);
    }

    /**
     * Remove self from parent, if any
     */
    public void hide() {
        if (getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).removeView(this);
        }
    }

    /**
     * Show self in center of screen
     * @param center if <code>true</code> show in center, otherwise shift down
     * @param animated use animation
     */
    public void placeInCenter(boolean center, boolean animated) {
        isCentered = center;
        if (progressView == null) {
            return;
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) progressView.getLayoutParams();
        boolean needChange = (layoutParams.topMargin == 0 && !isCentered) // is in center but shouldn't be
                || (layoutParams.topMargin > 0 && isCentered) // is not in center but it should
                || (animator != null && animator.isStarted()); // is in animation

        if (needChange) {
            // cancel animation if it's running
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }

            int endValue = isCentered ? 0 : maxMargin;
            if (animated) {
                // animate layout change
                animator = ValueAnimator.ofInt(layoutParams.topMargin, endValue);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.setDuration(DURATION_MOVE_MS);
                animator.addUpdateListener(animation -> {
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) progressView.getLayoutParams();
                    lp.setMargins(0, (Integer) animation.getAnimatedValue(), 0, 0);
                    progressView.setLayoutParams(lp);
                });
                animator.start();
            } else {
                // apply end value without animation
                // this may be requ
                layoutParams.setMargins(0, endValue, 0, 0);
                progressView.setLayoutParams(layoutParams);
            }
        }

    }

    /**
     * Show background under {@link ProgressBar}
     * @param show
     */
    public void showBackground(boolean show) {
        showBackground = show;
        if (progressView == null) {
            return;
        }
        if (showBackground) {
            progressView.setBackgroundResource(R.drawable.bg_loading_wheel);
        } else {
            progressView.setBackground(null);
        }
    }
}
