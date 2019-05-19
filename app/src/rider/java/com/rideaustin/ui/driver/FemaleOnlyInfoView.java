package com.rideaustin.ui.driver;

import com.rideaustin.api.config.Alert;
import com.rideaustin.api.config.GenderSelection;
import com.rideaustin.base.ProgressCallback;

/**
 * Created by hatak on 28.07.2017.
 */

public interface FemaleOnlyInfoView {

    void onTitleLoaded(final String title);
    void onGenderAlert(Alert alert);
    void onGenderSelection(final GenderSelection selection, final int index);
    ProgressCallback getCallback();
}
