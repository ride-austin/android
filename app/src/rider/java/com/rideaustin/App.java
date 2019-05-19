package com.rideaustin;

import android.support.annotation.NonNull;

import com.rideaustin.api.DataManager;
import com.rideaustin.api.StripeManager;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.manager.StateManager;

import io.branch.referral.Branch;

/**
 * Created by supreethks on 23/10/16.
 */

public class App extends BaseApp {

    private static App instance;
    private static StripeManager stripeManager;
    private static DataManager dataManager;
    private static StateManager stateManager;

    @Override
    protected void init() {
        super.init();
        instance = this;
        stripeManager = new StripeManager(this);
        dataManager = initDataManager();
        stateManager = new StateManager();
        Branch.getAutoInstance(this);
    }

    public static String getAppName() {
        GlobalConfig lastConfiguration = App.getConfigurationManager().getLastConfiguration();
        if (lastConfiguration != null) {
            return lastConfiguration.getGeneralInformation().getApplicationName();
        }
        String cityName = getCityName();
        if (cityName == null) {
            return getInstance().getString(R.string.app_name);
        }
        return getInstance().getString(R.string.app_name_placeholder, cityName);
    }

    public static String getFormattedAppName() {
        GlobalConfig lastConfiguration = App.getConfigurationManager().getLastConfiguration();
        if (lastConfiguration != null) {
            return lastConfiguration.getGeneralInformation().getApplicationNamePipe();
        }
        String cityName = getCityName();
        if (cityName == null) {
            return getInstance().getString(R.string.app_name);
        }
        return getInstance().getString(R.string.app_name_formatted_placeholder, cityName);
    }

    public static String getCompanyName() {
        return getAppName().replace(" ", "");
    }

    public static App getInstance() {
        return instance;
    }

    public static App i() {
        return instance;
    }

    public static DataManager getDataManager() {
        return dataManager;
    }

    public static StripeManager getStripeManager() {
        return stripeManager;
    }

    public void refreshEndpoints() {
        if (dataManager != null) {
            dataManager.dispose();
        }
        dataManager = initDataManager();
    }

    public static StateManager getStateManager() {
        return stateManager;
    }

    @NonNull
    private DataManager initDataManager() {
        return new DataManager();
    }
}
