package com.rideaustin.ui.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.rideaustin.R;

/**
 * Inspired by <a href="https://github.com/glomadrian/loading-balls">https://github.com/glomadrian/loading-balls</a>
 * Created by Sergey Petrov on 10/03/2017.
 */

public class EtaLoadedView extends View {

    private Ball ball;
    private Ring ring;
    private PathAnimator pathAnimator;

    private float ringThickness = 1.0f;
    private int ringColor = 0;
    private float ballRadius = 10.0f;
    private int ballColor = 0;
    private float ballStroke = 1.0f;
    private int ballStrokeColor = 0;
    private int cycleTime = 400;

    public EtaLoadedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EtaLoadedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.EtaLoadedView);
        ringThickness = attributes.getDimension(R.styleable.EtaLoadedView_ringThickness, ringThickness);
        ringColor = attributes.getColor(R.styleable.EtaLoadedView_ringColor, ringColor);
        ballRadius = attributes.getDimension(R.styleable.EtaLoadedView_ballRadius, ballRadius);
        ballColor = attributes.getColor(R.styleable.EtaLoadedView_ballColor, ballColor);
        ballStroke = attributes.getDimension(R.styleable.EtaLoadedView_ballStroke, ballStroke);
        ballStrokeColor = attributes.getColor(R.styleable.EtaLoadedView_ballStrokeColor, ballStrokeColor);
        cycleTime = attributes.getInt(R.styleable.EtaLoadedView_cycleTime, cycleTime);
        ball = new Ball(ballRadius, ballColor, ballStroke, ballStrokeColor);
        ring = new Ring(ringThickness, ringColor, ball.getFullRadius());
        attributes.recycle();
    }

    public void start() {
        if (pathAnimator != null && isShown()) {
            pathAnimator.start();
        }
    }

    public void stop() {
        if (pathAnimator != null) {
            pathAnimator.stop();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            start();
        } else {
            stop();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pathAnimator != null) {
            pathAnimator.stop();
            pathAnimator.destroy();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (pathAnimator != null) {
            pathAnimator.stop();
            pathAnimator.destroy();
        }
        ring.setSize(w, h);
        pathAnimator = new PathAnimator(ball, w, h, cycleTime);
        pathAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ring.render(canvas);
        ball.render(canvas);
    }

    private static class Ring {

        private Paint paint;
        private float padding;
        private RectF rect;
        private float radius;

        Ring(float thickness, int color, float ballRadius) {
            paint = new Paint();
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(thickness);
            paint.setAntiAlias(true);
            padding = Math.max(ballRadius, thickness);
            rect = new RectF();
        }

        public void setSize(int width, int height) {
            radius = (float) Math.min(width, height) / 2 - padding;
            rect.set((float) width / 2 - radius, (float) height / 2 - radius, (float) width / 2 + radius, (float) height / 2 + radius);
        }

        public void render(Canvas canvas) {
            canvas.drawArc(rect, 0, 360, false, paint);
        }
    }


    private static class Ball {

        private Paint paint;
        private float stroke;
        private Paint strokePaint;
        private float radius;
        private PointF position;

        Ball(float radius, int color, float stroke, int strokeColor) {
            this.radius = radius;
            this.stroke = stroke;
            this.position = new PointF();
            paint = new Paint();
            paint.setColor(color);
            paint.setAntiAlias(true);
            strokePaint = new Paint();
            strokePaint.setColor(strokeColor);
            strokePaint.setAntiAlias(true);
        }

        public void setPosition(float x, float y) {
            position.set(x, y);
        }

        public void render(Canvas canvas) {
            canvas.drawCircle(position.x, position.y, getFullRadius(), strokePaint);
            canvas.drawCircle(position.x, position.y, radius, paint);
        }

        public float getFullRadius() {
            return radius + stroke;
        }
    }


    private class PathAnimator implements ValueAnimator.AnimatorUpdateListener {

        private Ball ball;
        private Path path;
        private ValueAnimator valueAnimator;

        PathAnimator(Ball ball, int width, int height, int duration) {
            this.ball = ball;
            path = new Path();
            path.addCircle((float) width / 2, (float) height / 2, (float) Math.min(width, height) / 2 - ball.getFullRadius(), Path.Direction.CW);
            valueAnimator = new ValueAnimator();
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.setDuration(duration);
            valueAnimator.setFloatValues(0, 1f);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(this);
        }

        public void start() {
            if (!valueAnimator.isStarted()) {
                valueAnimator.start();
            }
        }

        public void stop(){
            if (valueAnimator.isRunning()) {
                valueAnimator.cancel();
            }
        }

        public void destroy() {
            valueAnimator.removeAllUpdateListeners();
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float fraction = animation.getAnimatedFraction();
            if (fraction > 1) {
                fraction = fraction - 1;
            }
            float[] coordinates = getPathCoordinates(path, fraction);
            ball.setPosition(coordinates[0], coordinates[1]);

            EtaLoadedView.this.invalidate();
        }

        private float[] getPathCoordinates(Path path, float fraction) {
            float aCoordinates[] = { 0f, 0f };
            PathMeasure pm = new PathMeasure(path, false);
            pm.getPosTan(pm.getLength() * fraction, aCoordinates, null);
            return aCoordinates;
        }
    }

}
