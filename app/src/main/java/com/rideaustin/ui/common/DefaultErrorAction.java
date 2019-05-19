package com.rideaustin.ui.common;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by Viktor Kifer
 * On 28-Dec-2016.
 */

public class DefaultErrorAction implements Action1<Throwable> {

    @Nullable
    private String message;

    public DefaultErrorAction() {
    }

    public DefaultErrorAction(@NonNull String message) {
        this.message = message;
    }

    @Override
    public void call(Throwable throwable) {
        Timber.e(throwable, !TextUtils.isEmpty(message) ? message : throwable.getMessage());
    }
}
