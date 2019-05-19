package com.rideaustin.base;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by hatak on 21.08.2017.
 */

public interface FragmentHost {

    boolean isInvalid();

    FragmentManager getSupportFragmentManager();

    void commitTransaction(FragmentTransaction transaction);

}
