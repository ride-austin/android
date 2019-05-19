package com.rideaustin.ui.common;

import android.support.annotation.CallSuper;
import android.support.annotation.StringRes;

import com.rideaustin.App;
import com.rideaustin.base.ProgressCallback;
import com.rideaustin.utils.SingleSubject;

import rx.Observable;
import rx.Subscription;

/**
 * Created by hatak on 23.10.2017.
 */

public class RxBaseViewModel implements ProgressCallback {

    private SingleSubject<ProgressEvent> progressSubject = SingleSubject.create();
    private LifecycleSubscriptions subscriptions = new LifecycleSubscriptions();

    @CallSuper
    public void onStart() {
    }

    @CallSuper
    public void onResume() {
    }

    @CallSuper
    public void onPause() {
        subscriptions.onPause();
    }

    @CallSuper
    public void onStop() {
        subscriptions.onStop();
    }

    @CallSuper
    public void onDestroy() {
        subscriptions.onDestroy();
    }

    protected final void untilPause(Subscription subscription) {
        subscriptions.untilPause(subscription);
    }

    protected final void untilStop(Subscription subscription) {
        subscriptions.untilStop(subscription);
    }

    protected final void untilDestroy(Subscription subscription) {
        subscriptions.untilDestroy(subscription);
    }

    public final Observable<ProgressEvent> getProgressEvents() {
        return progressSubject.asObservable().onBackpressureLatest();
    }

    @Override
    public void showProgress() {
        progressSubject.onNext(ProgressEvent.showProgress());
    }

    @Override
    public void showProgress(String message) {
        progressSubject.onNext(ProgressEvent.showProgress(message));
    }

    @Override
    public void showLoadingWheel() {
        progressSubject.onNext(ProgressEvent.showLoadingWheel());
    }

    @Override
    public void hideProgress() {
        progressSubject.onNext(ProgressEvent.hideProgress());
    }

    protected final String getString(@StringRes int resId) {
        return App.getInstance().getString(resId);
    }

    protected final String getString(@StringRes int resId, Object... formatArgs) {
        return App.getInstance().getString(resId, formatArgs);
    }
}

