package com.rideaustin.utils;

import android.support.annotation.StringDef;

/**
 * Created by supreethks on 23/10/16.
 */

public interface Constants extends CommonConstants {

    String APP_TAG = "Rider";

    String BEVO_BUCKS_CARD_NUMBER = "Bevo Bucks";
    long LOCATION_IS_VALID_TIMEOUT_MS = 3 * 60 * 1000;
    String RESOURCE_ID = "view_stub_resource_id";
    String START_ADDRESS = "start_address";
    String DESTINATION_ADDRESS = "destination_address";
    String PICKUP_COMMENTS = "pickup_comments";
    String SELECTED_CAR_CATEGORY = "selected_car_category";
    String SURGE_ACCEPTED = "surge_accepted";
    String DIRECT_CONNECT_ID = "direct_connect_id";
    long CONTACT_DRIVER_SHOW_DELAY = 500;
    int PASSWORD_MIN_LENGTH = 6;
    long MAP_REFRESH_INTERVAL = 5; //in seconds
    long RIDE_STATUS_CHECK_TIMEOUT = 2500;
    long RIDE_STATUS_TIMER_DELAY = 2000;
    String ACTIVE_DRIVER = "active_driver";
    String DRIVER_REACHED = "reached_driver";
    String RIDE_ID = "ride_id";
    String CURERNT_RIDE = "current_ride";
    String SURGE_AREA = "surge_area";
    String SURGE_FACTOR = "surge_factor";
    String CAR_TYPE = "car_type";
    String LOCATION = "location";
    long SLEEP_AMOUNT = 2000;
    float SELECTED_CAR_CATEGORY_TEXT = 14;
    float REGULAR_CAR_CATEGORY_TEXT = 12;
    int PHONE_FIELD_LENGTH = 10;
    long EXPIRATION_TIME = 3600000;
    String PICKUP_LATITUDE_LOCATION_KEY = "pickup_latitude";
    String PICKUP_LONGITUDE_LOCATION_KEY = "pickup_longitude";
    String DESTINATION_LATITUDE_LOCATION_KEY = "destination_latitude";
    String DESTINATION_LONGITUDE_LOCATION_KEY = "destination_longitude";
    /**
     * this should be saved on user getUserSpecificPreferences
     * check this one: https://issue-tracker.devfactory.com/browse/RA-4835
     */
    String RECENTLY_VISITED_PLACES = "recently_visited_places_v2";
    String MARKER_STORED_TIME_KEY = "marker_stored_time";
    int DEFAULT_CAMERA_ZOOM = 18;
    int MIN_PASSWORD_LENGTH = 6;
    String LOG_TAG = "RideAustinDriver";

    String USER_EMAIL_KEY = "user.email";

    String TEMP_DIRECTORY = "java.io.tmpdir";

    int SEND_EMAIL_REQUEST_CODE = 1001;

    int MAX_ELEMENTS_TO_SAVE_IN_HISTORY = 10;
    int MAX_ADDRESS_STRING_LENGTH = 42;
    int MAX_IMAGE_COMPRESS = 20; // [1;100]
    String PLATFORM_TYPE = "ANDROID";
    String REAL_TIME_TRACKING_URL_FORMAT = "%s/real-time-tracking?id=%s&env=%s";
    String REAL_TIME_TRACKING_URL_FORMAT_NO_ENV = "%s/real-time-tracking?id=%s";

    String MASKING_ENABLED_KEY = "masking.enabled";
    String EMPTY_ESTIMATE_TIME = "n/a";
    String INTERACTOR_KEY = "config.cityid";
    String REGISTRATION_DATA = "registrationData";

    String EXTRA_KEY_SPLIT_FARE_MESSAGE = "split_fare_message";
    String EXTRA_KEY_RETURN_TO_MAP = "return_to_map";

    int ROUNDUP_SHOW_POPUP_LIMIT = 3;
    long CAR_ANIMATION_MS = 1000;
    String LESS_THAN_MIN = "<1";
    String MINS = " MINS";
    String MIN = " MIN";

    @StringDef({SplitFare.REQUESTED, SplitFare.ACCEPTED, SplitFare.DECLINED})
    @interface SplitFare {
        String REQUESTED = "SPLIT_FARE";
        String ACCEPTED = "SPLIT_FARE_ACCEPTED";
        String DECLINED = "SPLIT_FARE_DECLINED";
    }


    enum CarCategory {
        REGULAR_CAR(0, "REGULAR"),
        SUV_CAR(1, "SUV"),
        PREMIUM(2, "PREMIUM"),
        HONDA_CAR(3, "HONDA"),
        LUXURY(4, "LUXURY");

        int id;
        String value;

        CarCategory(int id, String value) {
            this.id = id;
            this.value = value;
        }

        public static CarCategory valueOf(int id) {
            switch (id) {
                case 0:
                    return REGULAR_CAR;
                case 1:
                    return SUV_CAR;
                case 2:
                    return PREMIUM;
                case 3:
                    return HONDA_CAR;
                case 4:
                    return LUXURY;
                default:
                    return REGULAR_CAR;
            }
        }

        public static CarCategory valueFromName(String name) {
            CarCategory[] values = values();
            for (CarCategory carCategory : values) {
                if (name.equals(carCategory.value)) {
                    return carCategory;
                }
            }
            return REGULAR_CAR;
        }

        public int getCategoryId() {
            return id;
        }

        public String getRequestValue() {
            return value;
        }

        public String value() {
            return value;
        }
    }

    long TWO_HOURS_IN_MILLIS = 7200000;

    class FacebookFields {
        public static final String PUBLIC_PROFILE = "public_profile";
        public static final String EMAIL = "email";
        public static final String ID = "id";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String NAME = "name";
        public static final String FIELDS_KEY = "fields";
        public static final String FIELDS_VALUE = "id,name,email,first_name,last_name";
    }

}
