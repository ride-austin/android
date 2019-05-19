package com.rideaustin.ui.utils;

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;

import com.rideaustin.BuildConfig;
import com.rideaustin.R;
import com.rideaustin.api.config.CityType;
import com.rideaustin.api.config.GlobalConfig;

import static com.rideaustin.utils.CommonConstants.AUSTIN_FLAVOR_NAME;
import static com.rideaustin.utils.CommonConstants.HOUSTON_FLAVOR_NAME;

/**
 * Helper class to get build-specific resources
 * Created by Sergey Petrov on 24/03/2017.
 */

public class ResourceHelper {

    /**
     * Returns resource for app splash background
     * @param config global config (can be null)
     * @return drawable resource id or 0
     */
    @DrawableRes
    public static int getSplashBackgroundDrawableRes(@Nullable GlobalConfig config) {
        if (config != null && config.getCurrentCity() != null) {
            return getSplashBackgroundDrawableResImpl(config);
        } else {
            return getSplashBackgroundDrawableResImpl();
        }
    }

    /**
     * Returns resource for app white logo
     * @param config global config (can be null)
     * @return drawable resource id or 0
     */
    @DrawableRes
    public static int getWhiteLogoDrawableRes(@Nullable GlobalConfig config) {
        if (config != null && config.getCurrentCity() != null) {
            return getWhiteLogDrawableResImpl(config);
        } else {
            return getWhiteLogDrawableResImpl();
        }
    }

    /**
     * Returns resource for app black logo
     * @param config global config (can be null)
     * @return drawable resource id or 0
     */
    @DrawableRes
    public static int getBlackLogoDrawableRes(@Nullable GlobalConfig config) {
        if (config != null && config.getCurrentCity() != null) {
            return getBlackLogDrawableResImpl(config);
        } else {
            return getBlackLogDrawableResImpl();
        }
    }

    @DrawableRes
    private static int getSplashBackgroundDrawableResImpl(GlobalConfig config) {
        CityType cityType = CityType.from(config.getCurrentCity().getCityName());
        switch (cityType) {
            case AUSTIN: return R.drawable.bg_austin_splash_screen;
            case HOUSTON: return R.drawable.bg_houston_splash_screen;
            default: return 0;
        }
    }

    @DrawableRes
    private static int getSplashBackgroundDrawableResImpl() {
        if (BuildConfig.FLAVOR.contains(AUSTIN_FLAVOR_NAME)) {
            return R.drawable.bg_austin_splash_screen;
        } else if (BuildConfig.FLAVOR.contains(HOUSTON_FLAVOR_NAME)) {
            return R.drawable.bg_houston_splash_screen;
        }
        return 0;
    }

    @DrawableRes
    private static int getWhiteLogDrawableResImpl(GlobalConfig config) {
        CityType cityType = CityType.from(config.getCurrentCity().getCityName());
        switch (cityType) {
            case AUSTIN: return R.drawable.app_austin_white_logo;
            case HOUSTON: return R.drawable.app_houston_white_logo;
            default: return 0;
        }
    }

    @DrawableRes
    private static int getWhiteLogDrawableResImpl() {
        if (BuildConfig.FLAVOR.contains(AUSTIN_FLAVOR_NAME)) {
            return R.drawable.app_austin_white_logo;
        } else if (BuildConfig.FLAVOR.contains(HOUSTON_FLAVOR_NAME)) {
            return R.drawable.app_houston_white_logo;
        }
        return 0;
    }

    @DrawableRes
    private static int getBlackLogDrawableResImpl(GlobalConfig config) {
        CityType cityType = CityType.from(config.getCurrentCity().getCityName());
        switch (cityType) {
            case AUSTIN: return R.drawable.app_austin_black_logo;
            case HOUSTON: return R.drawable.app_houston_black_logo;
            default: return 0;
        }
    }

    @DrawableRes
    private static int getBlackLogDrawableResImpl() {
        if (BuildConfig.FLAVOR.contains(AUSTIN_FLAVOR_NAME)) {
            return R.drawable.app_austin_black_logo;
        } else if (BuildConfig.FLAVOR.contains(HOUSTON_FLAVOR_NAME)) {
            return R.drawable.app_houston_black_logo;
        }
        return 0;
    }

}
