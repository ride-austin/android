package com.rideaustin.ui.widgets;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.rideaustin.R;

/**
 * Component can be used in different layouts to clip corners of its content.
 * <p>
 * Created by Sergey Petrov on 08/08/2017.
 */

public class RoundedCorners {

    private Path path = new Path();
    private float[] corners = new float[8];

    public void init(View view, AttributeSet attrs) {
        TypedArray attributes = view.getContext().obtainStyledAttributes(attrs, R.styleable.RoundedCorners);
        float cornerRadius = attributes.getDimension(R.styleable.RoundedCorners_cornerRadius, 0.0f);
        float topLeftCornerRadius = attributes.getDimension(R.styleable.RoundedCorners_topLeftCornerRadius, cornerRadius);
        float topRightCornerRadius = attributes.getDimension(R.styleable.RoundedCorners_topRightCornerRadius, cornerRadius);
        float bottomLeftCornerRadius = attributes.getDimension(R.styleable.RoundedCorners_bottomLeftCornerRadius, cornerRadius);
        float bottomRightCornerRadius = attributes.getDimension(R.styleable.RoundedCorners_bottomRightCornerRadius, cornerRadius);
        attributes.recycle();
        corners[0] = topLeftCornerRadius;
        corners[1] = topLeftCornerRadius;
        corners[2] = topRightCornerRadius;
        corners[3] = topRightCornerRadius;
        corners[4] = bottomRightCornerRadius;
        corners[5] = bottomRightCornerRadius;
        corners[6] = bottomLeftCornerRadius;
        corners[7] = bottomLeftCornerRadius;
    }

    public void updatePath(int w, int h) {
        path.reset();
        RectF rect = new RectF();
        rect.set(0, 0, w, h);
        path.addRoundRect(rect, corners, Path.Direction.CW);
        path.close();
    }

    public int clip(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(path);
        return save;
    }

}
