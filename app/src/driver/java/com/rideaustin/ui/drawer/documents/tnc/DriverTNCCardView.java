package com.rideaustin.ui.drawer.documents.tnc;

import android.content.Context;

import com.rideaustin.ui.common.BaseView;

/**
 * Created by crossover on 22/01/2017.
 */

public interface DriverTNCCardView extends BaseView {
    void onHideBottomSheet();

    void onTNCSelected(String driverTncCardFrontImagePath);

    Context getContext();
}
