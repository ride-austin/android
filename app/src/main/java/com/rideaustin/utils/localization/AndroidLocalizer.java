package com.rideaustin.utils.localization;

import android.content.Context;
import android.support.annotation.StringRes;

/**
 * Created by Viktor Kifer
 * On 17-Dec-2016.
 */
public class AndroidLocalizer implements Localizer {
    private Context context;

    public AndroidLocalizer(Context context) {
        this.context = context;
    }

    @Override
    public String getString(@StringRes int resId) {
        return context.getString(resId);
    }

    @Override
    public String getString(@StringRes int resId, Object... args) {
        return context.getString(resId, args);
    }
}
