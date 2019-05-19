package com.rideaustin.ui.widgets;

/**
 * Created by poliveira on 07/08/2014.
 * Updated by guness on 06/11/2016
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rideaustin.R;
import com.rideaustin.utils.MathUtils;


/**
 * This is a temporary fix for:  https://code.google.com/p/android/issues/detail?id=218475
 * When native bug is solved, we can remove this class and use native one.
 * <p>
 * https://github.com/kanytu/custom-rating-bar
 * <p>
 * regular rating bar. it wraps the stars making its size fit the parent
 */
public class CustomRatingBar extends LinearLayout {

    private static final int DEFAULT_STARS = 5;

    private static final int MOVE_THRESHOLD = 50;
    private static final long ANIM_DURATION = 100;
    private static final float ANIM_SCALE = 1.2f;

    private static final String STAR_OFF_TAG = "StarOff";
    private static final String STAR_HALF_TAG = "StarHalf";
    private static final String STAR_ON_TAG = "StarOn";

    public IRatingBarCallbacks getOnScoreChanged() {
        return onScoreChanged;
    }

    public void setOnScoreChanged(IRatingBarCallbacks onScoreChanged) {
        this.onScoreChanged = onScoreChanged;
    }

    public interface IRatingBarCallbacks {
        void scoreChanged(float score);
    }

    private int maxStars = DEFAULT_STARS;
    private float currentScore = 0f;
    @DrawableRes
    private int starOnResource = R.drawable.ic_star_blue;
    @DrawableRes
    private int starOffResource = R.drawable.ic_star_gray;
    @DrawableRes
    private int startHalfResource = R.drawable.ic_star_blue;

    private ImageView[] starViews;
    private float starPadding;
    private IRatingBarCallbacks onScoreChanged;
    private int lastStarIndex;
    private boolean isDisplayOnly;
    private double lastX;
    private boolean halfStars = false;

    public CustomRatingBar(Context context) {
        super(context);
        init();
    }

    public float getScore() {
        return currentScore;
    }

    public void setScore(float score) {
        score = Math.round(score * 2) / 2.0f;
        if (!halfStars)
            score = Math.round(score);
        currentScore = score;
        refreshStars();
        if (onScoreChanged != null)
            onScoreChanged.scoreChanged(currentScore);
    }

    public void setScrollToSelect(boolean enabled) {
        isDisplayOnly = !enabled;
    }

    public CustomRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeAttributes(attrs, context);
        init();
    }

    private void initializeAttributes(AttributeSet attrs, Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomRatingBar);
        final int N = typedArray.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.CustomRatingBar_maxStars)
                maxStars = typedArray.getInt(attr, DEFAULT_STARS);
            else if (attr == R.styleable.CustomRatingBar_stars)
                currentScore = typedArray.getFloat(attr, 0f);
            else if (attr == R.styleable.CustomRatingBar_starHalf)
                startHalfResource = typedArray.getResourceId(attr, android.R.drawable.star_on);
            else if (attr == R.styleable.CustomRatingBar_starOn)
                starOnResource = typedArray.getResourceId(attr, android.R.drawable.star_on);
            else if (attr == R.styleable.CustomRatingBar_starOff)
                starOffResource = typedArray.getResourceId(attr, android.R.drawable.star_off);
            else if (attr == R.styleable.CustomRatingBar_starPadding)
                starPadding = typedArray.getDimension(attr, 0);
            else if (attr == R.styleable.CustomRatingBar_onlyForDisplay)
                isDisplayOnly = typedArray.getBoolean(attr, false);
            else if (attr == R.styleable.CustomRatingBar_halfStars)
                halfStars = typedArray.getBoolean(attr, true);
        }
        typedArray.recycle();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public CustomRatingBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeAttributes(attrs, context);
        init();
    }

    void init() {
        starViews = new ImageView[maxStars];
        for (int i = 0; i < maxStars; i++) {
            final int numStars = i + 1;
            ImageView starView = createStar();
            starView.setContentDescription(getContext().getResources().getQuantityString(R.plurals.stars, numStars, numStars));
            starView.setFocusableInTouchMode(true);
            addView(starView);
            starViews[i] = starView;
        }
        refreshStars();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    /**
     * hardcore math over here
     *
     * @param posX position of touch
     * @return score
     */
    private float getScoreForPosition(float posX) {
        if (halfStars)
            return (float) Math.round(((posX / ((float) getWidth() / (maxStars * 3f))) / 3f) * 2f) / 2;
        float value = (float) Math.ceil((posX / ((float) getWidth() / (maxStars))));
        value = value <= 0 ? 1 : value;
        value = value > maxStars ? maxStars : value;
        return value;
    }

    private int getImageForScore(float score) {
        if (score > 0)
            return Math.round(score) - 1;
        else return -1;
    }

    private void refreshStars() {
        boolean flagHalf = ((int)(currentScore) != 0 && ((int)(currentScore % 0.5) == 0)) && halfStars;
        for (int i = 1; i <= maxStars; i++) {
            ImageView starView = starViews[i - 1];
            if (i <= currentScore) {
                starView.setTag(STAR_ON_TAG);
                starView.setImageResource(starOnResource);
            } else {
                if (flagHalf && i - 0.5 <= currentScore) {
                    starView.setTag(STAR_HALF_TAG);
                    starView.setImageResource(startHalfResource);
                } else {
                    starView.setTag(STAR_OFF_TAG);
                    starView.setImageResource(starOffResource);
                }
            }
        }
    }

    private ImageView createStar() {
        ImageView v = new ImageView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        v.setPadding((int) starPadding, 0, (int) starPadding, 0);
        v.setAdjustViewBounds(true);
        v.setScaleType(ImageView.ScaleType.FIT_CENTER);
        v.setLayoutParams(params);
        v.setImageResource(starOffResource);
        return v;
    }

    @Nullable
    private ImageView getImageView(int position) {
        if (position >= 0 && position < starViews.length) {
            return starViews[position];
        } else {
            return null;
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (isDisplayOnly)
            return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                animateStarRelease(getImageView(lastStarIndex));
                lastStarIndex = -1;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - lastX) > MOVE_THRESHOLD)
                    requestDisallowInterceptTouchEvent(true);
                float lastScore = currentScore;
                currentScore = getScoreForPosition(event.getX());
                if (!MathUtils.almostEqual(lastScore, currentScore, 0.1)) {
                    animateStarRelease(getImageView(lastStarIndex));
                    animateStarPressed(getImageView(getImageForScore(currentScore)));
                    lastStarIndex = getImageForScore(currentScore);
                    refreshStars();
                    if (onScoreChanged != null)
                        onScoreChanged.scoreChanged(currentScore);
                }
                break;
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastScore = currentScore;
                currentScore = getScoreForPosition(event.getX());
                animateStarPressed(getImageView(getImageForScore(currentScore)));
                lastStarIndex = getImageForScore(currentScore);
                if (!MathUtils.almostEqual(lastScore, currentScore, 0.1)) {
                    refreshStars();
                    if (onScoreChanged != null)
                        onScoreChanged.scoreChanged(currentScore);
                }
        }
        return true;
    }

    private void animateStarPressed(@Nullable ImageView star) {
        if (star != null)
            ViewCompat.animate(star).scaleX(ANIM_SCALE).scaleY(ANIM_SCALE).setDuration(ANIM_DURATION).start();
    }

    private void animateStarRelease(@Nullable ImageView star) {
        if (star != null)
            ViewCompat.animate(star).scaleX(1f).scaleY(1f).setDuration(ANIM_DURATION).start();
    }

    public boolean isHalfStars() {
        return halfStars;
    }

    public void setHalfStars(boolean halfStars) {
        this.halfStars = halfStars;
    }

    @VisibleForTesting
    public boolean isStarOff(int index) {
        ImageView view = getImageView(index);
        if (view != null) {
            return STAR_OFF_TAG.equals(view.getTag());
        }
        return false;
    }

    @VisibleForTesting
    public boolean isStarHalf(int index) {
        ImageView view = getImageView(index);
        if (view != null) {
            return STAR_HALF_TAG.equals(view.getTag());
        }
        return false;
    }

    @VisibleForTesting
    public boolean isStarOn(int index) {
        ImageView view = getImageView(index);
        if (view != null) {
            return STAR_ON_TAG.equals(view.getTag());
        }
        return false;
    }

}