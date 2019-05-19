package com.rideaustin.ui.common;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import timber.log.Timber;

/**
 * Supposed to wrap bug <a href="https://issuetracker.google.com/37007605">37007605</a in Android
 * @see <a href="http://stackoverflow.com/questions/35653439/recycler-view-inconsistency-detected-invalid-view-holder-adapter-positionviewh">Topic 1</a>
 * @see <a href="http://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in">Topic 2</a>
 * This problem often occurs when {@link RecyclerView}'s content updated dynamically while scrolling,
 * so skipping one "bad" layout phase probably won't do many harm.
 * NOTE: Use this carefully, also consider data source and threading consistency.
 *
 * Created by Sergey Petrov on 14/04/2017.
 */

public class SafeLinearLayoutManager extends LinearLayoutManager {

    public SafeLinearLayoutManager(Context context) {
        super(context);
        // RA-11056: seems new pre-fetching feature in support library (25.1.+)
        // causes "Inconsistency detected "crashes when using adapter "notifyItemXXX" changes.
        // Issue mentioned in https://issuetracker.google.com/37007605
        // marked as fixed but still reproduced in 25.3.1
        setItemPrefetchEnabled(false);
    }

    public SafeLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        // RA-11056: seems new pre-fetching feature in support library (25.1.+)
        // causes "Inconsistency detected "crashes when using adapter "notifyItemXXX" changes.
        // Issue mentioned in https://issuetracker.google.com/37007605
        // marked as fixed but still reproduced in 25.3.1
        setItemPrefetchEnabled(false);
    }

    public SafeLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // RA-11056: seems new pre-fetching feature in support library (25.1.+)
        // causes "Inconsistency detected "crashes when using adapter "notifyItemXXX" changes.
        // Issue mentioned in https://issuetracker.google.com/37007605
        // marked as fixed but still reproduced in 25.3.1
        setItemPrefetchEnabled(false);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        // https://issuetracker.google.com/37007605
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Timber.e(e, e.getMessage());
        }
    }
}
