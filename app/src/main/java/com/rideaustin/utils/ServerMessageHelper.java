package com.rideaustin.utils;

import android.content.Context;
import android.text.TextUtils;

import com.rideaustin.R;
import com.rideaustin.base.BaseApiException;

/**
 * Created by Sergey Petrov on 10/04/2017.
 */

public class ServerMessageHelper {

    /**
     * Returns message based on server error or {@link R.string#technical_issues}
     *
     * @param context   context to get string of
     * @param exception server error
     * @return message to display
     */

    public static String getErrorMessage(Context context, BaseApiException exception) {
        if (!TextUtils.isEmpty(exception.getBody())) {
            return stripQuotes(exception.getBody()).trim();
        }
        return context.getString(R.string.error_unknown);
    }

    /**
     * Strip double quotes from the beginning and the end og the <code>string</code>
     * @see <a href="http://stackoverflow.com/a/2608682">answer on StackOverflow</a>
     *
     * @param string string to strip quotes
     * @return string without double quotes
     */
    //
    private static String stripQuotes(String string) {
        return string.replaceAll("^\"|\"$", "");
    }

}
