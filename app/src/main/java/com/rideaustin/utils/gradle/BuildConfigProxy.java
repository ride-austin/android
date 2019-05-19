package com.rideaustin.utils.gradle;


import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.BuildConfig;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.Constants;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hatak on 04.11.16.
 */

public class BuildConfigProxy {
    private static List<Environment> environments;
    private static String selectedEnvironment;
    private static Environment customEnvironment;

    static {
        environments = prepareConfiguration();
        selectedEnvironment = getSelectedEnv();
    }

    public static List<Environment> getEnvironments() {
        return environments;
    }

    public static void setSelectedEnv(final String env) {
        selectedEnvironment = env;
        App.getPrefs().saveSelectedEnvironment(env);
        if (isEndpointValid()) {
            App.getInstance().refreshEndpoints();
        }
    }

    public static String getSelectedEnv() {
        if (selectedEnvironment == null) {
            selectedEnvironment = getSavedEnv();
        }
        return selectedEnvironment;
    }

    public static boolean isCustomEnvironmentSelected() {
        return getSelectedEnv().equals(customEnvironment.getEnv());
    }

    public static void setCustomEnvironmentEndpoint(String endpoint) {
        customEnvironment.setEndpoint(endpoint);
        App.getPrefs().saveCustomEnvironmentEndpoint(endpoint);
        if (isEndpointValid()) {
            App.getInstance().refreshEndpoints();
        }
    }

    public static String getApiEndpoint() {
        return findEnvironment(selectedEnvironment).getEndpoint();
    }

    public static String getHost() {
        try {
            URL endpointUrl = new URL(getApiEndpoint());
            return endpointUrl.getHost();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static int getPort() {
        try {
            URL endpointUrl = new URL(getApiEndpoint());
            return endpointUrl.getPort() != -1 ? endpointUrl.getPort() : 80;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String getEnv() {
        return findEnvironment(selectedEnvironment).getEnv();
    }

    private static String getSavedEnv() {
        if (BuildConfig.FLAVOR.toLowerCase().contains(Constants.ENV_PROD)) {
            // Always use prod for prod flavors
            return Constants.ENV_PROD;
        } else {
            String environment = App.getPrefs().getSelectedEnvironment();
            if (environment.equals(CommonConstants.NO_ENVIRONMENT_SELECTED)) {
                // Set to default if empty
                environment = getDefaultEnv(BuildConfig.FLAVOR);
            } else if (environment.equals(Constants.ENV_CUSTOM) && !isEndpointValid(environment)) {
                // Reset to default if custom endpoint is not valid
                environment = getDefaultEnv(BuildConfig.FLAVOR);
            }
            return environment;
        }
    }

    private static String getDefaultEnv(final String flavorName) {
        if (flavorName.toLowerCase().contains(Constants.ENV_PROD)) {
            return Constants.ENV_PROD;
        }
        return Constants.ENV_RC; // use RC by default
    }

    private static Environment findEnvironment(String env) {
        for (Environment environment : environments) {
            if (env.toLowerCase().contains(environment.getEnv().toLowerCase())) {
                return environment;
            }
        }
        throw new RuntimeException("env " + env + " not found in configuration");
    }

    /**
     * Just check endpoint is not empty.
     * {@link BuildConfig} constants should be correct.
     * User input is validated in {@link com.rideaustin.ui.signin.EndpointFragment}
     */
    private static boolean isEndpointValid() {
        return !TextUtils.isEmpty(getApiEndpoint());
    }

    private static boolean isEndpointValid(String env) {
        return !TextUtils.isEmpty(findEnvironment(env).getEndpoint());
    }

    private static List<Environment> prepareConfiguration() {

        List<Environment> environments = new ArrayList<>();

        customEnvironment = new Environment();
        customEnvironment.setName("custom");
        customEnvironment.setEndpoint(App.getPrefs().getCustomEnvironmentEndpoint());
        customEnvironment.setEnv(Constants.ENV_CUSTOM);
        environments.add(customEnvironment);

        Environment environment = new Environment();
        environment.setName("backup");
        environment.setEndpoint(BuildConfig.API_BACKUP);
        environment.setEnv("rc-backup");
        environments.add(environment);

        environment = new Environment();
        environment.setName("feature");
        environment.setEndpoint(BuildConfig.API_FEATURE);
        environment.setEnv("feature");
        environments.add(environment);

        environment = new Environment();
        environment.setName("staging");
        environment.setEndpoint(BuildConfig.API_STAGE);
        environment.setEnv("stage");
        environments.add(environment);

        environment = new Environment();
        environment.setName("dev");
        environment.setEndpoint(BuildConfig.API_DEV);
        environment.setEnv("dev");
        environments.add(environment);

        environment = new Environment();
        environment.setName("beta");
        environment.setEndpoint(BuildConfig.API_RC);
        environment.setEnv(Constants.ENV_RC);
        environments.add(environment);

        environment = new Environment();
        environment.setName("prod");
        environment.setEndpoint(BuildConfig.API_PROD);
        environment.setEnv(Constants.ENV_PROD);
        environments.add(environment);

        return environments;

    }
}
