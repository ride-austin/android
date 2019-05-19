package com.rideaustin.ui.map;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.DriverLocation;

import java.util.List;

import rx.Observable;

/**
 * Created by Viktor Kifer
 * On 10-Jan-2017.
 */

public class DriversNearByHelper {

    public static Observable<List<DriverLocation>> getAvailableDriversNearBy(LatLng position) {
        final GlobalConfig lastConfiguration = App.getConfigurationManager().getLastConfiguration();
        final Integer cityId = lastConfiguration.getCurrentCity().getCityId();

        return App.getDataManager().getNearestDrivers(position.latitude, position.longitude, cityId);
    }

}
