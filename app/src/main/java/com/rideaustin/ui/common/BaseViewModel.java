package com.rideaustin.ui.common;

/**
 * Created by Viktor Kifer
 * On 26-Dec-2016.
 */

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import rx.functions.Action1;

/**
 * Created by Viktor Kifer
 * On 24-Dec-2016.
 */
public class BaseViewModel<T extends BaseView> extends RxBaseObservable {

    @NonNull
    private final Reference<T> viewRef;

    public BaseViewModel(@NonNull T view) {
        this.viewRef = new WeakReference<>(view);
    }

    public boolean isViewAttached() {
        return getView() != null;
    }

    @Nullable
    public T getView() {
        return viewRef.get();
    }

    protected final void performOnView(Action1<T> action) {
        T view = viewRef.get();
        if (view != null) {
            action.call(view);
        }
    }
}
