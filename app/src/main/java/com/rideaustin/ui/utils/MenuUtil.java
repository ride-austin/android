package com.rideaustin.ui.utils;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.rideaustin.R;

/**
 * Created by rost on 8/9/16.
 */
public final class MenuUtil {

    public static MenuItem inflateNextMenu(Menu menu, MenuInflater inflater, boolean isEnabled) {
        inflater.inflate(R.menu.next_menu, menu);
        final MenuItem nextMenuItem = menu.findItem(R.id.next);
        nextMenuItem.setEnabled(isEnabled);
        return nextMenuItem;
    }
}
