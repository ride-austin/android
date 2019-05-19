package com.rideaustin.utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.Rides;
import com.rideaustin.schedulers.RxSchedulers;

import java8.util.Optional;
import rx.Subscription;

/**
 * @author sdelaysam.
 */

public class PickupHelper {

    private static final int LOCATION_MAX_DISTANCE_M = 200;

    public static Subscription checkIsFar(LatLng location, Runnable yes, Runnable no) {
        return App.getLocationManager().getLastLocation(true, true)
                .observeOn(RxSchedulers.main())
                .subscribe((raLocation) -> {
                    Location pickup = new Location("");
                    pickup.setLatitude(location.latitude);
                    pickup.setLongitude(location.longitude);
                    if (raLocation.getLocation().distanceTo(pickup) > getDistantPickUpNotificationThreshold()) {
                        yes.run();
                    } else {
                        no.run();
                    }
                }, throwable -> no.run());
    }


    public static int getDistantPickUpNotificationThreshold() {
        return Optional.ofNullable(App.getConfigurationManager().getLastConfiguration())
                .map(GlobalConfig::getRides)
                .map(Rides::getDistantPickUpNotificationThreshold)
                .filter(integer -> integer != null)
                .orElse(LOCATION_MAX_DISTANCE_M);
    }


}
