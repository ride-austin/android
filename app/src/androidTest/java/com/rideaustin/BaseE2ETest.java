package com.rideaustin;

import android.support.annotation.CallSuper;

import com.rideaustin.utils.gradle.BuildConfigProxy;

/**
 * Created by Sergey Petrov on 04/08/2017.
 */

public class BaseE2ETest extends BaseTest {

    @Override
    @CallSuper
    public void setUp() {
        super.setUp();
        BuildConfigProxy.setSelectedEnv("custom");
        BuildConfigProxy.setCustomEnvironmentEndpoint(BuildConfig.API_TESTS);
    }
}
