package com.rideaustin.ui.map;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by hatak on 22.02.2017.
 */

public interface AnimateObject {

    LatLng getPosition();

    float getRotation();

    void setRotation(float finalCourse);

    void setPosition(LatLng target);
}
