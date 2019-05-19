package com.rideaustin.ui.common;

import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Probably needs to be removed. Adapter should handle this by design.
 */
@Deprecated
public class RecycleViewOnClickListener extends RecyclerView.SimpleOnItemTouchListener {

    private final GestureDetector gestureDetector;

    public RecycleViewOnClickListener(RecyclerView recyclerView, RecycleViewAction onClickAction, RecycleViewAction onLongClickAction) {
        this.gestureDetector = new GestureDetector(recyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null) {
                    onClickAction.run(recyclerView.getChildAdapterPosition(child));
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null) {
                    onLongClickAction.run(recyclerView.getChildAdapterPosition(child));
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return gestureDetector.onTouchEvent(e);
    }
}
