package com.rideaustin.ui.common;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Listener which signals that {@link RecyclerView} was scrolled to the bottom.
 * Further action is considered by {@link Model}.
 * Created by Sergey Petrov on 07/04/2017.
 */

public class OnLoadMoreScrollListener extends RecyclerView.OnScrollListener {

    private static final int VISIBLE_THRESHOLD = 3;

    private final Model model;
    private final int visibleThreshold;
    private int firstVisibleItem, lastVisibleItem, visibleItemCount, totalItemCount;

    public OnLoadMoreScrollListener(Model model) {
        this(model, VISIBLE_THRESHOLD);
    }

    public OnLoadMoreScrollListener(Model model, int visibleThreshold) {
        this.model = model;
        this.visibleThreshold = visibleThreshold;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (dy < 0) return;

        final RecyclerView.LayoutManager layoutManager = getLayoutManager(recyclerView);
        if (layoutManager == null) return;

        visibleItemCount = layoutManager.getChildCount();
        totalItemCount = layoutManager.getItemCount();
        firstVisibleItem = getFirstVisibleItemPosition(layoutManager);
        lastVisibleItem = firstVisibleItem + visibleItemCount - 1;

        if (!model.isLoadingMore() // not loading yet
                && model.canLoadMore() // can load more
                && totalItemCount > visibleItemCount // has sense to scroll more
                && lastVisibleItem >= (totalItemCount - visibleThreshold)) { // bottom reached
            // yes, we can
            model.loadMore();
        }
    }

    private int getFirstVisibleItemPosition(RecyclerView.LayoutManager layoutManager) {
        int pos = 0;
        if (layoutManager instanceof LinearLayoutManager) {
            pos = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            pos = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null)[0];
        }
        return pos;
    }

    private RecyclerView.LayoutManager getLayoutManager(RecyclerView recyclerView) {
        if (recyclerView != null) {
            return recyclerView.getLayoutManager();
        }
        return null;
    }

    public interface Model {
        void loadMore();
        boolean canLoadMore();
        boolean isLoadingMore();
    }

}
