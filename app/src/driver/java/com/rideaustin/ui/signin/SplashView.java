package com.rideaustin.ui.signin;

import com.rideaustin.api.config.GlobalConfig;

/**
 * Created by Viktor Kifer
 * On 24-Dec-2016.
 */


public interface SplashView {

    void onGlobalConfigUpdate(final GlobalConfig globalConfig, boolean shouldFadeIn);

    SigninApiSubscriber getSubscriber();

    void displayEnvironmentPicker();

    void onNetworkUnavailable();

    void onUpgradeNeeded();

    void onUpgradeCheckFailed();

    void onUpgradeNotNeeded();

    void onSignInError();
}
