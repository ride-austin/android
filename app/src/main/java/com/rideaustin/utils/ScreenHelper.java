package com.rideaustin.utils;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

/**
 * Created by Viktor Kifer
 * On 28-Dec-2016.
 */

public class ScreenHelper {

    public static boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return pm.isInteractive();
        } else {
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }

}
