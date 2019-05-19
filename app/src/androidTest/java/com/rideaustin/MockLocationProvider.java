package com.rideaustin;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;

public class MockLocationProvider {
    String providerName;
    Context context;

    public MockLocationProvider(String name, Context ctx) {
        this.providerName = name;
        this.context = ctx;

        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        lm.addTestProvider(providerName, false, false, false, false, false, true, true, 0, 5);
        lm.setTestProviderEnabled(providerName, true);
    }

    public void pushLocation(double lat, double lng, float accuracy) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Location mockLocation = new Location(providerName);
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lng);
        mockLocation.setAltitude(0);
        mockLocation.setAccuracy(accuracy);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        lm.setTestProviderLocation(providerName, mockLocation);
    }

    public void shutdown() {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        lm.removeTestProvider(providerName);
    }
}