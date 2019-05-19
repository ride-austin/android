package com.rideaustin.ui.drawer.cars.sticker;

import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.ui.drawer.cars.UpdateFragmentCallback;

/**
 * Created by hatak on 2/7/17.
 */

public interface UpdateStickerFragmentCallback extends UpdateFragmentCallback {

    DriverRegistration getRegistrationConfiguration();

}
