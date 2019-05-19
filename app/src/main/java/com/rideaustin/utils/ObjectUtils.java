package com.rideaustin.utils;

import android.os.Build;

import java.util.Objects;

/**
 * Created on 21/11/2017
 *
 * @author sdelaysam
 */

public class ObjectUtils {

    public static boolean equals(Object a, Object b) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.equals(a, b);
        }
        return (a == b) || (a != null && a.equals(b));
    }


}
