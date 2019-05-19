package com.rideaustin.utils;

import android.support.annotation.StringDef;

import com.rideaustin.BuildConfig;

/**
 * Created by supreethks on 23/10/16.
 */

public interface CommonConstants {

    boolean IS_DRIVER = BuildConfig.FLAVOR_avatar.equals("driver");

    String VERSION_DIVIDER = "\\.";  //NOTE: this is regex
    String VERSION_PATTERN = "[0-9]+\\.[0-9]+\\.[0-9]+";  //NOTE: this is regex
    int VERSION_COUNT = 3;

    String EXTRA_KEY_NOTIFICATION_MESSAGE = "notification_message";

    /**
     * To be removed on v 3.0.0
     */
    @Deprecated
    String TOKEN_KEY = "TOKEN";
    String X_TOKEN_KEY = "X_TOKEN";

    int DAYS_COUNT = 7;
    int SEND_EMAIL_REQUEST_CODE = 1200;

    String GOOGLE_DIRECTIONS_STATUS_OK = "OK";

    String RIDEID_KEY = "rideId";

    String MARKET_DETAILS_ID = "market://details?id=";
    String GOOGLE_PLAY_HTTP_URL = "https://play.google.com/store/apps/details?id=";

    String HOUSTON_FLAVOR_NAME = "Houston";
    String AUSTIN_FLAVOR_NAME = "Austin";

    String DRIVER_FLAVOR_NAME = "Driver";
    String RIDER_FLAVOR_NAME = "Rider";
    String NO_ENVIRONMENT_SELECTED = "no.flavor.selected";
    int NAME_FIELD_LENGTH = 2;
    String ENV_PROD = "prod";
    String ENV_RC = "rc";
    String ENV_CUSTOM = "custom";

    String LAST_EVENT_ID = "last_event_id";
    long NOT_EXISTING_LAST_EVENT_ID = -1;

    String FACEBOOK_EMPTY_LAST_NAME = "-";
    int MAX_COMMENT_LENGTH = 1000;

    // city lower-case names
    String AUSTIN_CITY_NAME_LC = "austin";
    String HOUSTON_CITY_NAME_LC = "houston";

    long MAX_DISTANCE_BETWEEN_TO_ANIMATE_M = 200;
    long MIN_DISTANCE_BETWEEN_TO_ANIMATE_M = 3;

    /**
     * Delay for animation in cases when OS is doing heavy job.
     * Should be used to prevent animation twitches
     */
    int ANIMATION_DELAY_MS = 250;

    long LOCATION_TIMEOUT_IN_SECONDS = 5;
    long NETWORK_TIMEOUT_IN_SECONDS = 15;
    double ROUTE_TOLERANCE = 10.0;

    /**
     * Min amount of time between similar network calls.
     * Should be used to prevent calls with same parameters
     * caused by different actors (due to bad design)
     */
    long API_CALL_THRESHOLD_MS = 500L;

    String UNEXPECTED_STATE_KEY = "::UnexpectedState::";

    @StringDef({TNCCardSide.FRONT, TNCCardSide.BACK})
    @interface TNCCardSide {
        String FRONT = "FRONT";
        String BACK = "BACK";
    }

    String TEMP_DIRECTORY = "java.io.tmpdir";

    @StringDef({CarPhotoType.FRONT, CarPhotoType.BACK, CarPhotoType.INSIDE, CarPhotoType.TRUNK})
    @interface CarPhotoType {
        String FRONT = "FRONT";
        String BACK = "BACK";
        String INSIDE = "INSIDE";
        String TRUNK = "TRUNK";
    }

    @StringDef({CarPropertyType.YEAR, CarPropertyType.MAKE, CarPropertyType.MODEL, CarPropertyType.COLOR})
    @interface CarPropertyType {
        String YEAR = "YEAR";
        String MAKE = "MAKE";
        String MODEL = "MODEL";
        String COLOR = "COLOR";
    }

    @StringDef({CarCategory.REGULAR, CarCategory.SUV, CarCategory.PREMIUM, CarCategory.LUXURY, CarCategory.HONDA})
    @interface CarCategory {
        String REGULAR = "REGULAR";
        String SUV = "SUV";
        String PREMIUM = "PREMIUM";
        String LUXURY = "LUXURY";
        String HONDA = "HONDA";
    }

    String REGULAR_CAR_TYPE = "REGULAR";
    String WOMEN_ONLY_DRIVER_TYPE = "WOMEN_ONLY";
    String DIRECT_CONNECT_DRIVER_TYPE = "DIRECT_CONNECT";
    String FINGERPRINTED_DRIVER_TYPE = "FINGERPRINTED";

    final class LocationManager {
        public static final long LOCATION_UPDATE_TIME_INTERVAL_MS = 2000;
        public static final float LOCATION_UPDATE_SMALLEST_DISPLACEMENT_M = 3f;
    }

    int RESEND_SMS_CODE_DELAY_S = 10;
}
