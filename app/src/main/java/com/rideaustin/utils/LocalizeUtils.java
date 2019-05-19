package com.rideaustin.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rideaustin.R;

/**
 * Created by Sergey Petrov on 27/02/2017.
 */

public class LocalizeUtils {

    /**
     * Returns driver arrival time representation in minutes
     * @param context context to use for localization
     * @param seconds amount of seconds
     * @return time in minutes representation string
     */
    @NonNull
    public static String formatDriverEta(Context context, @Nullable Long seconds) {
        if (seconds == null) {
            return context.getString(R.string.no_mins);
        }
        int mins = (int) (seconds / 60);
        if (mins <= 0) {
            // http://stackoverflow.com/a/17261327
            return context.getString(R.string.zero_mins);
        }
        return context.getResources().getQuantityString(R.plurals.minutes_plural, mins, mins);
    }
}
