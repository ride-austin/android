package com.rideaustin.ui.drawer.favorite;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.rideaustin.utils.TouchWrapper;

/**
 * Created by crossover on 04/07/2017.
 */

public abstract class FavoritesTouchWrapperListener implements TouchWrapper.TouchWrapperListener {

    private final GoogleMap gMap;

    private static final int UPDATE_ZOOM_ANIMATION_MS = 100;
    private static final int DOUBLE_TAP_ANIMATION_MS = 400;

    public FavoritesTouchWrapperListener(GoogleMap map) {
        gMap = map;
    }

    @Override
    public void onTouched() {
    }

    @Override
    public void onMapZoom(float zoomValue) {
        if (gMap != null) {
            gMap.animateCamera(CameraUpdateFactory.zoomBy(zoomValue), UPDATE_ZOOM_ANIMATION_MS, null);
        }
    }

    @Override
    public void onMapZoomIn() {
        if (gMap != null) {
            gMap.animateCamera(CameraUpdateFactory.zoomIn(), DOUBLE_TAP_ANIMATION_MS, null);
        }
    }

    @Override
    public void onMapScrollingEnabled(boolean enabled) {
        if (gMap != null) {
            gMap.getUiSettings().setScrollGesturesEnabled(enabled);
        }
    }
}
