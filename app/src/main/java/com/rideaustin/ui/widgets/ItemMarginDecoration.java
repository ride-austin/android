package com.rideaustin.ui.widgets;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Sergey Petrov on 15/03/2017.
 */

public class ItemMarginDecoration extends RecyclerView.ItemDecoration {

    private final int margin;

    public ItemMarginDecoration(int margin) {
        this.margin = margin;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = margin;
        } else {
            outRect.top = 0;
        }
        outRect.left = margin;
        outRect.right = margin;
        outRect.bottom = margin;
    }
}
