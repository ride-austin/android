package com.rideaustin.ui.common;

import android.databinding.BaseObservable;
import android.support.annotation.CallSuper;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Viktor Kifer
 * On 24-Dec-2016.
 */

public class RxBaseObservable extends BaseObservable {

    private CompositeSubscription subscriptions = new CompositeSubscription();

    protected final void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
    }

    @CallSuper
    public void onStart() {

    }

    @CallSuper
    public void onStop() {
        subscriptions.clear();
    }

}
