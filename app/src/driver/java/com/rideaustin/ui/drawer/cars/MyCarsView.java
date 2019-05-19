package com.rideaustin.ui.drawer.cars;

import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.ui.common.BaseView;

/**
 * Created by crossover on 18/01/2017.
 */

interface MyCarsView extends BaseView {
    BaseActivityCallback getCallback();

    void onCarsLoaded();

    void onCarSelectionChanged();

    void onUpdateInsuranceClicked(final Car car);

    void onUpdatePhotosClicked(final Car car);

    void onUpdateInspectionStickerClicked(DriverRegistration registrationConfiguration, final Car car);

    void onCarsLoadingFailed();
}
