package com.rideaustin.ui.drawer.promotions;

import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;

import com.rideaustin.App;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.manager.ConfigurationManager;
import com.rideaustin.schedulers.RxSchedulers;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by hatak on 23.11.16.
 */

public class FreeRidesViewModel extends BaseObservable {

    public final ObservableBoolean hasPromoCode = new ObservableBoolean(false);
    private final FreeRidesListener listener;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public FreeRidesViewModel(final FreeRidesListener listener) {
        this.listener = listener;
    }

    public void onStart() {
        subscriptions.add(
                getConfigurationManager()
                        .getConfigurationUpdates()
                        .startWith(getConfigurationManager().getLastConfiguration())
                        .filter(config -> config != null)
                        .observeOn(RxSchedulers.main())
                        .subscribe(listener::onGlobalConfigUpdate));
    }

    public void onStop() {
        subscriptions.clear();
    }

    protected ConfigurationManager getConfigurationManager() {
        return App.getConfigurationManager();
    }

    public interface FreeRidesListener {
        void onGlobalConfigUpdate(final GlobalConfig globalConfig);
    }
}
