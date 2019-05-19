package com.rideaustin.ui.base;

import com.rideaustin.base.BaseActivity;
import com.rideaustin.ui.common.RxBaseViewModel;

/**
 * Created by ridedriverandroid on 7.09.2016.
 */
public abstract class EngineStatelessActivity<T extends RxBaseViewModel> extends BaseActivity<T> {

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
