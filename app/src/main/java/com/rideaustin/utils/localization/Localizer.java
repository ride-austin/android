package com.rideaustin.utils.localization;

import android.support.annotation.StringRes;

/**
 * Created by Viktor Kifer
 * On 17-Dec-2016.
 */
public interface Localizer {

    String getString(@StringRes int resId);

    String getString(@StringRes int resId, Object... args);

}
