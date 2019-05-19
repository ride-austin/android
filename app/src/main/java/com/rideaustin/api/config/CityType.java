package com.rideaustin.api.config;

import android.support.annotation.Nullable;

import static com.rideaustin.utils.CommonConstants.AUSTIN_CITY_NAME_LC;
import static com.rideaustin.utils.CommonConstants.HOUSTON_CITY_NAME_LC;

/**
 * Created by Sergey Petrov on 24/03/2017.
 */

public enum CityType {

    AUSTIN,
    HOUSTON;

    public static CityType from(@Nullable String cityName) {
        if (cityName != null) {
            switch (cityName.toLowerCase()) {
                case AUSTIN_CITY_NAME_LC: return AUSTIN;
                case HOUSTON_CITY_NAME_LC: return HOUSTON;
            }
        }
        return AUSTIN;
    }
}
