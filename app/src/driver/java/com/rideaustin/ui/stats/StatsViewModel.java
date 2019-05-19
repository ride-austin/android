package com.rideaustin.ui.stats;

import android.databinding.ObservableBoolean;

import com.rideaustin.App;
import com.rideaustin.api.model.DriverStat;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.base.ProgressCallback;
import com.rideaustin.schedulers.RxSchedulers;

import java.util.List;

import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by Sergey Petrov on 26/07/2017.
 */

public class StatsViewModel {

    public final ObservableBoolean showEmptyText = new ObservableBoolean(false);
    private Subscription subscription = Subscriptions.empty();

    StatsViewModel(StatsAdapter adapter, ProgressCallback callback) {
        if (!App.getDataManager().isLoggedIn()) {
            return;
        }
        showEmptyText.set(false);
        subscription = App.getDataManager().getDriverStats()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<List<DriverStat>>(callback) {
                    @Override
                    public void onNext(List<DriverStat> driverStats) {
                        super.onNext(driverStats);
                        adapter.setStats(driverStats);
                        showEmptyText.set(driverStats == null || driverStats.isEmpty());
                    }

                    @Override
                    public void onAnyError(BaseApiException e) {
                        super.onAnyError(e);
                        adapter.setStats(null);
                        showEmptyText.set(true);
                    }
                });
    }

    void onDestroy() {
        subscription.unsubscribe();
    }
}
