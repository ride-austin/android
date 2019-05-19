package com.rideaustin.ui.base;

import com.rideaustin.base.BaseFragment;

/**
 * Created by ridedriverandroid on 23.08.2016.
 */
public class FloatNavigatorBaseFragment extends BaseFragment {

    public FloatNavigatorCallback getFloatNavigatorCallback() {
        return (FloatNavigatorCallback) getActivity();
    }
}
