package com.rideaustin.manager.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.rideaustin.App;

/**
 * Created by hatak on 16.03.2017.
 */

public class GPSStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
            App.getLocationManager().onGpsProvidersChanged();
        }
    }
}
