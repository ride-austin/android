package com.rideaustin.utils;

import android.content.Context;
import android.content.Intent;

/**
 * Created by supreethks on 27/10/16.
 */

public class ShareUtils {

    private static final String TYPE_TEXT_PLAIN = "text/plain";

    public static void shareText(Context context, String shareText) {
        Intent sendIntent = new Intent();
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType(TYPE_TEXT_PLAIN);
        context.startActivity(sendIntent);
    }
}
