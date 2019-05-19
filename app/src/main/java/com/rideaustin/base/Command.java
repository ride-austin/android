package com.rideaustin.base;

import android.view.View;

/**
 * Created by rideclientandroid on 10.10.2016.
 * Implements command pattern
 */
public interface Command<T> {
    void onExecute(View view, T item);
}
