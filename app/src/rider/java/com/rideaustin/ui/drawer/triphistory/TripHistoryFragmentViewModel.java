package com.rideaustin.ui.drawer.triphistory;

import android.content.Context;
import android.databinding.ObservableBoolean;

import com.rideaustin.App;
import com.rideaustin.api.model.paymenthistory.PaymentHistory;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.OnLoadMoreScrollListener;
import com.rideaustin.ui.common.RxBaseObservable;

import java.util.List;

/**
 * Created by ysych on 04.07.2016.
 */
public class TripHistoryFragmentViewModel extends RxBaseObservable implements OnLoadMoreScrollListener.Model {

    private final View view;

    private ObservableBoolean shouldDisplayEmptyText = new ObservableBoolean(false);

    public TripHistoryFragmentViewModel(View view) {
        this.view = view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!App.getDataManager().isLoggedIn()) {
            // app would be sent to login on resume
            return;
        }
        if (App.getDataManager().getTripHistoryModel() == null) {
            // unexpected state
            view.onUnexpectedState();
            return;
        }

        shouldDisplayEmptyText.set(false);
        addSubscription(App.getDataManager().getTripHistoryModel().getListObservable()
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnTripHistory));
        addSubscription(App.getDataManager().getTripHistoryModel().getHistorySelectedObservable()
                .filter(selected -> selected)
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnHistorySelected));
    }

    private void doOnTripHistory(List<PaymentHistory> list) {
        shouldDisplayEmptyText.set(list == null || list.isEmpty());
    }

    private void doOnHistorySelected(Boolean selected) {
        view.onHistorySelected();
    }

    public ObservableBoolean getShouldDisplayEmptyText() {
        return shouldDisplayEmptyText;
    }

    @Override
    public void loadMore() {
        // RA-9373: this method can be called after model is destroyed but view is alive yet.
        // We can safely ignore this request, view is also about to be destroyed.
        if (App.getDataManager().getTripHistoryModel() != null) {
            App.getDataManager().getTripHistoryModel().loadNextPage();
        }
    }

    @Override
    public boolean canLoadMore() {
        // RA-9373: this method can be called after model is destroyed but view is alive yet.
        // We can safely ignore this request, view is also about to be destroyed.
        return App.getDataManager().getTripHistoryModel() != null && App.getDataManager().getTripHistoryModel().canLoadMore();
    }

    @Override
    public boolean isLoadingMore() {
        // RA-9373: this method can be called after model is destroyed but view is alive yet.
        // We can safely ignore this request, view is also about to be destroyed.
        return App.getDataManager().getTripHistoryModel() != null && App.getDataManager().getTripHistoryModel().isLoadingMore();
    }

    public interface View {
        Context getContext();
        void onHistorySelected();
        void onUnexpectedState();
    }

}
