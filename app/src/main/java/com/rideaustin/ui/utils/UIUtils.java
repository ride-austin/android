package com.rideaustin.ui.utils;

import android.app.Activity;
import android.graphics.Point;

import java.text.DecimalFormat;

/**
 * Created by rost on 8/15/16.
 */
public final class UIUtils {

    private static final String DEFAULT_MONEY_FORMAT = "#.00";

    private UIUtils() {
    }

    public static Point getDisplaySize(Activity activity) {
        final Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(point);
        return point;
    }

    public static String formatRating(double rating) {
        return rating > 0 ? new DecimalFormat("#.00").format(rating) : "";
    }

    public static String formatSurgeFactor(double surgeFactor) {
        return surgeFactor > 1 ? new DecimalFormat("#.00").format(surgeFactor) + "X" : "";
    }

    public static String convertMoneyToString(double money){
        DecimalFormat df = new DecimalFormat(DEFAULT_MONEY_FORMAT);
        return df.format(money);
    }

}
