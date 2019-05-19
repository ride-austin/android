package com.rideaustin.ui.drawer.cars.photos;

import com.rideaustin.api.model.driver.Car;
import com.rideaustin.ui.common.TakePhotoFragment;
import com.rideaustin.utils.Constants;

/**
 * Created by crossover on 08/02/2017.
 */

public interface UpdatePhotoFragmentCallback extends TakePhotoFragment.TakePhotoListenerContainer {

    Car getCar();

    @Constants.CarPhotoType
    String getCarPhotoType();

    void onCompleted();
}
