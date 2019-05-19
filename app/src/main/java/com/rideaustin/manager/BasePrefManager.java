package com.rideaustin.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.rideaustin.App;
import com.rideaustin.BuildConfig;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.RideSpecificUpgradeStatus;
import com.rideaustin.api.model.UpgradeRequestStatus;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.SerializationHelper;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * @author shumelchyk
 */
public abstract class BasePrefManager {

    private static final String PERMANENT_PREFERENCES = "permanent_preferences";
    private static final String USER_SPECIFIC_PREFERENCES = "user_specific_preferences_";
    private static final String CONFIG_PREFERENCES = "config_preferences";
    /**
     * It is the updaters duty to check if {@link GlobalConfig} is compatible with updated one.
     * versionName history (latest at top):
     * -1138343532673324235L
     */
    private static final String CONFIGURATION_KEY = "app_configuration_" + GlobalConfig.getVersion();
    private static final String SELECTED_ENVIRONMENT = "SELECTED_ENVIRONMENT";
    private static final String CUSTOM_ENVIRONMENT_ENDPOINT = "CUSTOM_ENVIRONMENT_ENDPOINT";

    private static final String UPGRADE_DIALOG = "upgrade_dialog";

    public BasePrefManager() {

    }

    /**
     * Preferences that are not cleared on logout
     *
     * @return SharedPreferences
     */
    public SharedPreferences getPermanentPreferences() {
        return App.getInstance().getSharedPreferences(PERMANENT_PREFERENCES, Context.MODE_PRIVATE);
    }

    protected SharedPreferences getConfigPreferences() {
        return App.getInstance().getSharedPreferences(CONFIG_PREFERENCES, Context.MODE_PRIVATE);
    }

    protected SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance());
    }

    /**
     * This preferences shall not be deleted once users logs out.
     */
    public SharedPreferences getUserSpecificPreferences() {
        String prefsFile = USER_SPECIFIC_PREFERENCES;
        if (App.getDataManager().getUser().isPresent()) {
            prefsFile += App.getDataManager().getUser().get().getId();
        }
        return App.getInstance().getSharedPreferences(prefsFile, Context.MODE_PRIVATE);
    }

    /**
     * This method is used only for testing, it returns preferences for user with specific id
     * It's mandatory to provide user id because before login it's not known therefore
     * int's not possible to correctly mock shared prefs. After user is logged in
     * it's to late for mocking.
     */
    @VisibleForTesting
    public SharedPreferences getUserSpecificPreferences(long userId) {
        String prefsFile = USER_SPECIFIC_PREFERENCES + userId;
        return App.getInstance().getSharedPreferences(prefsFile, Context.MODE_PRIVATE);
    }

    public void clearPrefs() {
        getPreferences().edit().clear().apply();
    }

    public int getInt(String preferenceKey, int preferenceDefaultValue) {
        return getPreferences().getInt(preferenceKey, preferenceDefaultValue);
    }

    public int getUserInt(String preferenceKey, int preferenceDefaultValue) {
        return getUserSpecificPreferences().getInt(preferenceKey, preferenceDefaultValue);
    }

    public void putInt(String preferenceKey, int preferenceValue) {
        getPreferences().edit().putInt(preferenceKey, preferenceValue).apply();
    }

    public void putUserInt(String preferenceKey, int preferenceValue) {
        getUserSpecificPreferences().edit().putInt(preferenceKey, preferenceValue).apply();
    }

    public long getLong(String preferenceKey, long preferenceDefaultValue) {
        return getPreferences().getLong(preferenceKey, preferenceDefaultValue);
    }

    public long getUserLong(String preferenceKey, long preferenceDefaultValue) {
        return getUserSpecificPreferences().getLong(preferenceKey, preferenceDefaultValue);
    }

    public void putLong(String preferenceKey, long preferenceValue) {
        getPreferences().edit().putLong(preferenceKey, preferenceValue).apply();
    }

    public void putUserLong(String preferenceKey, long preferenceValue) {
        getUserSpecificPreferences().edit().putLong(preferenceKey, preferenceValue).apply();
    }

    public float getFloat(String preferenceKey, float preferenceDefaultValue) {
        return getPreferences().getFloat(preferenceKey, preferenceDefaultValue);
    }

    public float getUserFloat(String preferenceKey, float preferenceDefaultValue) {
        return getUserSpecificPreferences().getFloat(preferenceKey, preferenceDefaultValue);
    }

    public void putFloat(String preferenceKey, float preferenceValue) {
        getPreferences().edit().putFloat(preferenceKey, preferenceValue).apply();
    }

    public void putUserFloat(String preferenceKey, float preferenceValue) {
        getUserSpecificPreferences().edit().putFloat(preferenceKey, preferenceValue).apply();
    }

    public void putDouble(String preferenceKey, double preferenceValue) {
        getPreferences().edit().putLong(preferenceKey, Double.doubleToRawLongBits(preferenceValue)).apply();
    }

    public double getDouble(String preferenceKey, double preferenceDefaultValue) {
        return Double.longBitsToDouble(getPreferences().getLong(preferenceKey,
                Double.doubleToLongBits(preferenceDefaultValue)));
    }

    public void clearValue(String preferenceKey) {
        getPreferences().edit().remove(preferenceKey).apply();
    }

    public void clearUserValue(String preferenceKey) {
        getUserSpecificPreferences().edit().remove(preferenceKey).apply();
    }

    public boolean getBoolean(String preferenceKey, boolean preferenceDefaultValue) {
        return getPreferences().getBoolean(preferenceKey, preferenceDefaultValue);
    }

    public boolean getUserBoolean(String preferenceKey, boolean preferenceDefaultValue) {
        return getUserSpecificPreferences().getBoolean(preferenceKey, preferenceDefaultValue);
    }

    public void putBoolean(String preferenceKey, boolean preferenceValue) {
        getPreferences().edit().putBoolean(preferenceKey, preferenceValue).apply();
    }

    public void putUserBoolean(String preferenceKey, boolean preferenceValue) {
        getUserSpecificPreferences().edit().putBoolean(preferenceKey, preferenceValue).apply();
    }

    public String getString(String preferenceKey, String preferenceDefaultValue) {
        return getPreferences().getString(preferenceKey, preferenceDefaultValue);
    }

    public String getUserString(String preferenceKey, String preferenceDefaultValue) {
        return getUserSpecificPreferences().getString(preferenceKey, preferenceDefaultValue);
    }

    public void putString(String preferenceKey, String preferenceValue) {
        getPreferences().edit().putString(preferenceKey, preferenceValue).apply();
    }

    public void putUserString(String preferenceKey, String preferenceValue) {
        getUserSpecificPreferences().edit().putString(preferenceKey, preferenceValue).apply();
    }

    public void saveSelectedEnvironment(final String flavorName) {
        getPermanentPreferences().edit().putString(SELECTED_ENVIRONMENT, flavorName).apply();
    }

    public String getSelectedEnvironment() {
        return getPermanentPreferences().getString(SELECTED_ENVIRONMENT, CommonConstants.NO_ENVIRONMENT_SELECTED);
    }

    public void saveCustomEnvironmentEndpoint(final String endpoint) {
        getPermanentPreferences().edit().putString(CUSTOM_ENVIRONMENT_ENDPOINT, endpoint).apply();
    }

    public String getCustomEnvironmentEndpoint() {
        return getPermanentPreferences().getString(CUSTOM_ENVIRONMENT_ENDPOINT, "");
    }

    public boolean storeConfiguration(final GlobalConfig globalConfig) {
        Gson gson = new Gson();
        final String serialized = gson.toJson(globalConfig);
        return getConfigPreferences().edit().clear().putString(CONFIGURATION_KEY, serialized).commit();
    }

    @NonNull
    public GlobalConfig loadConfiguration() {
        final SharedPreferences configPreferences = getConfigPreferences();
        String serialized = null;
        if (configPreferences != null) {
            serialized = configPreferences.getString(CONFIGURATION_KEY, null);
        }
        if (serialized != null) {
            try {
                Gson gson = new Gson();
                return gson.fromJson(serialized, GlobalConfig.class);
            } catch (Exception e) {
                Timber.e(e);
                configPreferences.edit().clear().apply();
                return loadDefaultConfiguration();
            }
        } else {
            return loadDefaultConfiguration();
        }
    }

    public GlobalConfig loadDefaultConfiguration() {
        if (BuildConfig.FLAVOR.contains(CommonConstants.AUSTIN_FLAVOR_NAME) && BuildConfig.FLAVOR.contains(CommonConstants.DRIVER_FLAVOR_NAME)) {
            return loadConfigFromFile(R.raw.austin_driver_default_conf);
        } else if (BuildConfig.FLAVOR.contains(CommonConstants.HOUSTON_FLAVOR_NAME) && BuildConfig.FLAVOR.contains(CommonConstants.DRIVER_FLAVOR_NAME)) {
            return loadConfigFromFile(R.raw.houston_driver_default_conf);
        } else if (BuildConfig.FLAVOR.contains(CommonConstants.AUSTIN_FLAVOR_NAME) && BuildConfig.FLAVOR.contains(CommonConstants.RIDER_FLAVOR_NAME)) {
            return loadConfigFromFile(R.raw.austin_rider_default_conf);
        } else if (BuildConfig.FLAVOR.contains(CommonConstants.HOUSTON_FLAVOR_NAME) && BuildConfig.FLAVOR.contains(CommonConstants.RIDER_FLAVOR_NAME)) {
            return loadConfigFromFile(R.raw.houston_rider_default_conf);
        } else {
            throw new RuntimeException("default city configuration file, not defined for flavor");
        }
    }

    private GlobalConfig loadConfigFromFile(@RawRes int configFile) {
        Gson gson = new Gson();
        JsonReader jsonReader = null;
        Reader streamReader = null;
        try {
            InputStream inputStream = App.getInstance().getResources().openRawResource(configFile);
            streamReader = new InputStreamReader(inputStream);
            jsonReader = new JsonReader(streamReader);
            GlobalConfig config = gson.fromJson(jsonReader, GlobalConfig.class);
            config.setEmbedded(true);
            return config;
        } finally {
            IOUtils.closeQuietly(jsonReader);
            IOUtils.closeQuietly(streamReader);
        }
    }

    public RideSpecificUpgradeStatus getLastShownUpgradeDialog() {
        String savedStatus = getUserSpecificPreferences().getString(UPGRADE_DIALOG, null);
        if (TextUtils.isEmpty(savedStatus)) {
            return RideSpecificUpgradeStatus.create(0L, UpgradeRequestStatus.NONE);
        } else {
            return new Gson().fromJson(savedStatus, RideSpecificUpgradeStatus.class);
        }
    }

    public void setLastShownUpgradeDialog(RideSpecificUpgradeStatus status) {
        String json = new Gson().toJson(status, RideSpecificUpgradeStatus.class);
        getUserSpecificPreferences().edit().putString(UPGRADE_DIALOG, json).apply();
    }

    protected <T> void addToList(String key, SharedPreferences prefs,  List<T> list, T value) {
        list.add(value);
        prefs.edit().putString(key, SerializationHelper.serialize(list)).apply();
    }

    protected <T> void removeFromList(String key, SharedPreferences prefs,  List<T> list, T value) {
        list.remove(value);
        if (!list.isEmpty()) {
            prefs.edit().putString(key, SerializationHelper.serialize(list)).apply();
        } else {
            prefs.edit().remove(key).apply();
        }
    }

    protected <T> List<T> getList(String key, SharedPreferences prefs, Type type) {
        List<T> list = null;
        String str = prefs.getString(key, "");
        if (!str.isEmpty()) {
            list = SerializationHelper.deSerialize(str, type);
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }
}
