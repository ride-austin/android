package com.rideaustin.ui.drawer;

import com.rideaustin.R;
import com.rideaustin.ui.map.MainMapFragment;

import java.lang.ref.WeakReference;

import timber.log.Timber;

class NavigationDrawerEventListener implements MainMapFragment.EventListener {

    WeakReference<NavigationDrawerActivity> activityRef;

    public NavigationDrawerEventListener(NavigationDrawerActivity activity) {
        activityRef = new WeakReference<>(activity);
    }

    @Override
    public void onSplitFare() {
        Timber.d("::onSplitFare::");
        if (activityRef.get() != null) {
            activityRef.get().replaceSplitFare();
        }
    }

    @Override
    public void onShowCharityScreen() {
        Timber.d("::onShowCharityScreen::");
        if (activityRef.get() != null) {
            activityRef.get().navigateTo(R.id.navDonate);
        }
    }
}