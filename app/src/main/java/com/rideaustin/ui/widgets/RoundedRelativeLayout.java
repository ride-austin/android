package com.rideaustin.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Sergey Petrov on 08/08/2017.
 */

public class RoundedRelativeLayout extends RelativeLayout {

    private RoundedCorners roundedCorners = new RoundedCorners();

    public RoundedRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        roundedCorners.init(this, attrs);
    }

    public RoundedRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        roundedCorners.init(this, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        roundedCorners.updatePath(w, h);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = roundedCorners.clip(canvas);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }
}
