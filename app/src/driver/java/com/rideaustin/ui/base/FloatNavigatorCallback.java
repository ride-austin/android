package com.rideaustin.ui.base;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ridedriverandroid on 23.08.2016.
 */
public interface FloatNavigatorCallback {
    void navigateTo(LatLng navigateTo);
    void navigateTo(String place);
}
