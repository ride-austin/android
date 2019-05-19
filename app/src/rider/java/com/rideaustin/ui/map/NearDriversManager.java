package com.rideaustin.ui.map;


import com.google.android.gms.maps.GoogleMap;
import com.rideaustin.App;
import com.rideaustin.api.model.DriverLocation;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.manager.location.RALocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java8.util.Optional;
import java8.util.stream.StreamSupport;


public class NearDriversManager {

    public static final int ANIMATION_TIME_IN_MILLIS = 2000;
    private Map<Long, CarMarker> lastDrivers = new HashMap<>();


    public synchronized void updatePositions(final List<DriverLocation> driversList, final GoogleMap googleMap) {
        Map<Long, CarMarker> updatedDrivers = new HashMap<>();
        for (DriverLocation driverLocation : driversList) {
            if (lastDrivers.containsKey(driverLocation.getDriver().getId())) {
                CarMarker marker = lastDrivers.get(driverLocation.getDriver().getId());
                lastDrivers.remove(driverLocation.getDriver().getId());
                updatedDrivers.put(driverLocation.getDriver().getId(), marker);
                marker.setIcon(getCarIconUrl());
                marker.update(RALocation.on(driverLocation.getLocation()), ANIMATION_TIME_IN_MILLIS);
            } else {
                updatedDrivers.put(driverLocation.getDriver().getId(), create(driverLocation, googleMap));
            }
        }
        clearDrivers(lastDrivers);
        lastDrivers = updatedDrivers;

    }

    private CarMarker create(DriverLocation driverLocation, final GoogleMap googleMap) {
        return new CarMarker(App.getInstance(), googleMap, RALocation.on(driverLocation.getLocation()), 1, getCarIconUrl());
    }

    private Optional<String> getCarIconUrl() {
        return Optional.ofNullable(App.getDataManager().getRequestedCarType()).map(RequestedCarType::getMapIconUrl);
    }

    public synchronized void clearDrivers() {
        clearDrivers(lastDrivers);
    }

    public void clearDrivers(final Map<Long, CarMarker> markers) {
        if (!markers.isEmpty()) {
            StreamSupport.stream(markers.entrySet())
                    .forEach(carEntry -> {
                        CarMarker marker = carEntry.getValue();
                        marker.clear();
                    });
            markers.clear();
        }
    }
}
