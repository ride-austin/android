package com.rideaustin.ui.widgets.dialogs;

import android.support.annotation.DrawableRes;

import com.rideaustin.R;

/**
 * Created by crossover on 18/06/2017.
 */

public enum RADialogIcon {
    CHECK(R.drawable.icn_dialog_check), CROSS(R.drawable.icn_dialog_cross), WAITING(R.drawable.icn_dialog_hourglass);
    @DrawableRes
    int drawable;

    RADialogIcon(int resource) {
        drawable = resource;
    }
}
