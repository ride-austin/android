package com.rideaustin.ui.utils;

import android.text.InputFilter;

/**
 * Created by crossover on 16/02/2017.
 */

/**
 * check this for Char types
 * http://www.fileformat.info/info/unicode/category/index.htm
 */
public class TextFilters {


    public static final InputFilter EMOJI_FILTER = (source, start, end, dest, dstart, dend) -> {
        for (int index = start; index < end; index++) {

            int type = Character.getType(source.charAt(index));

            if (type == Character.SURROGATE) {
                return "";
            }
        }
        return null;
    };

    public static final InputFilter OTHER_SYMBOL_FILTER = (source, start, end, dest, dstart, dend) -> {
        for (int index = start; index < end; index++) {

            int type = Character.getType(source.charAt(index));

            if (type == Character.OTHER_SYMBOL) {
                return "";
            }
        }
        return null;
    };

    public static final InputFilter MATH_SYMBOL_FILTER = (source, start, end, dest, dstart, dend) -> {
        for (int index = start; index < end; index++) {

            int type = Character.getType(source.charAt(index));

            if (type == Character.MATH_SYMBOL) {
                return "";
            }
        }
        return null;
    };
}
