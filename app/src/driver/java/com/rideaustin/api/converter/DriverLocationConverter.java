package com.rideaustin.api.converter;

import android.location.Location;

import com.rideaustin.api.model.DriverLocation;
import com.rideaustin.manager.location.RALocation;
import com.rideaustin.manager.location.RALocationType;
import com.rideaustin.utils.TimeUtils;

/**
 * Created by rost on 8/16/16.
 */
public class DriverLocationConverter implements Converter<RALocation, DriverLocation> {

    @Override
    public RALocation convert(DriverLocation item) {
        Location location = new Location("");
        location.setBearing(item.getCourse());
        location.setLatitude(item.getLatitude());
        location.setLongitude(item.getLongitude());
        return new RALocation(new RALocationType(item.getDriver().getId()), RALocation.State.ON, location, TimeUtils.currentTimeMillis());
    }
}
