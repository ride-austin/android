package com.rideaustin.ui.earnings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

/**
 * Created by vokol on 22.08.2016.
 */
public class NonScrollableExpandableList extends ExpandableListView {
    public NonScrollableExpandableList(Context context) {
        super(context);
    }

    public NonScrollableExpandableList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonScrollableExpandableList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}
