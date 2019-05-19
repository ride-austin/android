package com.rideaustin.ui.map.strategy;

import android.support.annotation.Nullable;

import com.rideaustin.ui.map.MapFragmentInterface;

/**
 * Created by rost on 8/12/16.
 */
public interface UIStrategy {
    void attach(MapFragmentInterface mapFragmentInterface);

    void updateToolbar();

    void detach();

    void destroy(MapFragmentInterface mapFragmentInterface);

    boolean onMenuItemSelected(int menuId);

    void onCancelRideSelected(@Nullable String code, @Nullable String reason);

    void onFlowAction(boolean confirmed);

    boolean hasView();
}
