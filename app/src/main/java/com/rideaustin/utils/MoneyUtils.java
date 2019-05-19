package com.rideaustin.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Created by hatak on 06.09.2017.
 */

public class MoneyUtils {

    public static String format(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        return decimalFormat.format(value).replace(DecimalFormatSymbols.getInstance().getDecimalSeparator() + "00", "");
    }
}
