package com.rideaustin.ui.drawer.cars;

import com.rideaustin.api.model.driver.Car;
import com.rideaustin.ui.common.TakePhotoFragment;

/**
 * Created by hatak on 2/6/17.
 */

public interface UpdateFragmentCallback {
    Car getCar();

    void setTakePhotoListener(TakePhotoFragment.TakePhotoListener photoListener);

    void onCompleted();
}
