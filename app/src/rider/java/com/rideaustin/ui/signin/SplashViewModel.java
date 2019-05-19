package com.rideaustin.ui.signin;

import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.manager.ConfigurationManager;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseObservable;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.NetworkHelper;

import timber.log.Timber;

/**
 * Created by hatak on 23.11.16.
 */

public class SplashViewModel extends RxBaseObservable {

    private final SplashView splashView;

    public SplashViewModel(final SplashView splashView) {
        this.splashView = splashView;
    }

    @Override
    public void onStart() {
        super.onStart();
        performVersionCheck();
    }

    public void performVersionCheck() {
        addSubscription(App.getConfigurationManager()
                .getConfiguration()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(
                        configAppInfoResponse -> {
                            if (AppInfoUtil.isMandatoryRequired(configAppInfoResponse) &&
                                    AppInfoUtil.canShowUpdate()) {
                                splashView.onUpgradeNeeded();
                            } else {
                                splashView.onUpgradeNotNeeded();
                                performSignIn();
                            }
                        },
                        throwable -> {
                            Timber.e(throwable, "Version check failed");
                            splashView.onUpgradeCheckFailed();
                        }));
    }

    public void performSignIn() {

        if (!App.getDataManager().isAuthorised()) {
            splashView.displayEnvironmentPicker();
            return;
        }

        if (!NetworkHelper.isNetworkAvailable()) {
            splashView.onNetworkUnavailable();
            return;
        }

        if (TextUtils.isEmpty(App.getDataManager().getXToken())) {
            //Note: this is to be removed on 3.0.0
            addSubscription(
                    App.getDataManager().loginWithOldToken()
                            .subscribeOn(RxSchedulers.network())
                            .observeOn(RxSchedulers.main())
                            .subscribe(splashView.getSubscriber())
            );
        } else {
            addSubscription(
                    App.getDataManager().getUserObservable()
                            .subscribeOn(RxSchedulers.network())
                            .observeOn(RxSchedulers.main())
                            .subscribe(splashView.getSubscriber())
            );
        }
    }

    public void subscribeForConfigChanges() {
        addSubscription(
                getConfigurationManager()
                        .getConfigurationUpdates()
                        .subscribeOn(RxSchedulers.computation())
                        .observeOn(RxSchedulers.main())
                        .subscribe(globalConfig -> splashView.onGlobalConfigUpdate(globalConfig, true))
        );
    }

    protected ConfigurationManager getConfigurationManager() {
        return App.getConfigurationManager();
    }
}
