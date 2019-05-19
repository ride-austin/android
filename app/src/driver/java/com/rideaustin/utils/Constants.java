package com.rideaustin.utils;

/**
 * Created by vokol on 30.06.2016.
 */

public interface Constants extends CommonConstants {

    // Drawer
    int PHONE_FIELD_LENGTH = 10;

    // Map
    int DEFAULT_CAMERA_ZOOM = 18;
    long MAX_CHANGE_CAR_POSITION_ANIMATION_DURATION = 4000;
    long ANIMATION_FILTER_TIME_MS = 250;
    long LOCATION_HORIZONTAL_ACCURACY_FILTER = 70;

    long LOAD_QUEUED_TIMEOUT_S = 15;
    long BACKGROUND_ERROR_RETRY_DELAY_S = 10;
    long SWITCH_OFFLINE_TIMOUT_S = 25;

    long LOCATION_IS_VALID_TIMEOUT_MS = 3 * 60 * 1000L;

    long UPDATE_DRIVER_THRESHOLD_M = 2;
    double UPDATE_DRIVER_THRESHOLD_DEGREE = 22.5;
    int MOVEMENT_DETECTION_THRESHOLD_M = 50;
    long DAY_IN_MILLIS = 86400000;
    long TWO_HOURS_IN_MILLIS = 7200000;


    // Sliding CardView
    long NEEDED_PROGRESS_TO_PRCESS = 75;

    String SELECTED_EARNING = "selected_earning";
    int DAYS_COUNT = 7;
    int NO_TRIPS = 0;

    //Ride
    String DAY_ID = "day_id";
    String WEEK_ID = "week_id";
    long MINUTE_IN_MILLIS = 60000;
    String RESPONSE = "response";
    int MIN_PASSWORD_LENGTH = 6;

    float CHANGE_DESTINATION_THRESHOLD_M = 200;
    int SEND_EMAIL_REQUEST_CODE = 1200;

    float SEMI_TRANSPARENT = 0.5f;
    float NON_TRANSPARENT = 1;

    String QUEUE_NAME = "QUEUE_NAME";

    String PLATFORM_TYPE = "ANDROID";
    String LOG_TAG = "RideAustinDriver";

    String EMPTY_STRING = "";
    long TIMEOUT_RETRY_GET_NEAREST_DRIVERS_S = 60;
    int INACTIVE_DRIVER = 409;
    Integer MINIMAL_QUEUE_POSITION_TO_NOTIFY = 10;
    String DIRECTION_KEY = "direction_key";

    class FacebookFields {
        public static final String PUBLIC_PROFILE = "public_profile";
        public static final String EMAIL = "email";
        public static final String FIELDS_KEY = "fields";
        public static final String FIELDS_VALUE = "id,name,email,first_name,last_name";
    }

    String CAR_TYPE_DELIMITER = " + ";
}
