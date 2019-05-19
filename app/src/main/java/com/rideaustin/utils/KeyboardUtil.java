package com.rideaustin.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by viacheslavokolitiy on 23.07.2016.
 */

public final class KeyboardUtil {

    private KeyboardUtil(){}

    public static void hideKeyBoard(final Context context,
                                                     final View view){
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyBoard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, 0);
    }

    /**
     * Reliable way to show keyboard. Flag is implicit so that it can be closed by activity softinputmode flags.
     *
     * @param context
     * @param view
     */
    public static void showKeyboardImplicit(final Context context, final View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null)
                    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }
}
