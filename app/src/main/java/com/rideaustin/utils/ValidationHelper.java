package com.rideaustin.utils;

import android.text.TextUtils;

/**
 * Created by kshumelchyk on 7/5/16.
 */
public class ValidationHelper {
    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
