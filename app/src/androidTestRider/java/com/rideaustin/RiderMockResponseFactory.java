package com.rideaustin;

/**
 * Created by Sergey Petrov on 04/05/2017.
 */

public class RiderMockResponseFactory extends MockResponseFactory {

    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String EMAIL_EXISTS = "\"This email address is already in use\"";
    public static final String PHONE_EXISTS = "\"This phone number is already in use\"";
    public static final String PHONE_VERIFICATION_ERROR = "\"Phone verification error\"";
    public static final String INTERNAL_SERVER_ERROR = "Something went wrong, server code is 123456";
    public static final String ACCOUNT_NOT_ACTIVE = "Your account is not yet active. Please retry after some time";
    public static final String CARD_NOT_APPROVED = "Sorry, your credit card was not approved";
    public static final String UNPAID_RIDE = "Please pay your balance to take another ride";
    public static final String INVALID_PROMOCODE_ERROR_MSG = "This promocode is not valid, my friend";
    public static final String DIRECT_CONNECT_DRIVER_NOT_FOUND = "Driver not found";
    public static final String RATING_ERROR = "Rating error";

    @Override
    public MockResponse create(RequestType requestType) {
        switch (requestType) {
            case GLOBAL_APP_INFO_200_GET:
                return get200Regex(".*rest/configs/app/info/current.*", "GLOBAL_APP_INFO_200");
            case LOGIN_SUCCESS_200_POST:
                return post200("rest/login", "LOGIN_SUCCESS_200");
            case LOGIN_FAILED_401_POST:
                return postWithStringResponse("rest/login", 401, INVALID_CREDENTIALS);
            case CURRENT_USER_200_GET:
                return get200("rest/users/current", "CURRENT_USER_200");
            case CURRENT_USER_INACTIVE_200_GET:
                return get200("rest/users/current", "CURRENT_USER_INACTIVE_200");
            case TOKENS_200_POST:
                return post200Empty("rest/tokens");
            case DRIVER_TYPES_200_GET:
                return get200("rest/driverTypes", "DRIVER_TYPES_200");
            case CAR_TYPES_200_GET:
                return get200("rest/carTypes", "CAR_TYPES_200");
            case CAR_TYPES_SLIDER_200_GET:
                return get200("rest/carTypes", "CAR_TYPES_SLIDER_200");
            case CURRENT_RIDE_EMPTY_200_GET:
                return get200Empty("rest/rides/current");
            case ACDR_EMPTY_200_GET:
                return get200("rest/acdr", "ACDR_EMPTY_200");
            case ACDR_LUXURY_200_GET:
                return get200("rest/acdr", "ACDR_LUXURY_200");
            case ACDR_PREMIUM_200_GET:
                return get200("rest/acdr", "ACDR_PREMIUM_200");
            case ACDR_REGULAR_200_GET:
                return get200("rest/acdr", "ACDR_REGULAR_200");
            case ACDR_REGULAR_MOVED_200_GET:
                return get200("rest/acdr", "ACDR_REGULAR_MOVED_200");
            case ACDR_SUV_200_GET:
                return get200("rest/acdr", "ACDR_SUV_200");
            case ACDR_TOMMY_200_GET:
                return get200("rest/acdr", "ACDR_TOMMY_200");
            case CHARITIES_200_GET:
                return get200Regex(".*rest/charities\\?cityId=\\d+$", "CHARITIES_200");
            case SURGE_AREA_EMPTY_200_GET:
                return get200("rest/surgeareas", "SURGEAREA_EMPTY_200");
            case CONFIG_RIDER_200_GET:
                return get200Regex(".*rest/configs/rider/global\\?(.*lat=.+&lng=.+|cityId=\\d+)$", "CONFIG_GLOBAL_200");
            case CONFIG_RIDER_BEVO_BUCKS_200_GET:
                return get200Regex(".*rest/configs/rider/global\\?(.*lat=.+&lng=.+|cityId=\\d+)$", "CONFIG_GLOBAL_BEVO_BUCKS_200");
            case RIDER_CARDS_200_GET:
                return get200Regex(".*rest/riders/\\d+/cards$", "CARDS_200");
            case RIDER_CARDS_500_GET:
                return getWithStringResponseRegex(".*rest/riders/\\d+/cards$", 500, INTERNAL_SERVER_ERROR);
            case RIDER_ADD_CARD_200_POST:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/riders/\\d+/cards$")
                        .withFileResponse("CARD_ADD_200")
                        .build();
            case RIDER_ADD_CARD_NOT_APPROVED_400_POST:
                return new Builder(400, Method.POST)
                        .withRegexpUrl(".*rest/riders/\\d+/cards$")
                        .withStringResponse(CARD_NOT_APPROVED)
                        .build();
            case RIDER_SELECT_CARD_200_PUT:
                return new Builder(200, Method.PUT)
                        .withRegexpUrl(".*rest/riders/\\d+/cards/\\d+\\?primary=.+$")
                        .withEmptyStringResponse()
                        .build();
            case CURRENT_RIDER_200_GET:
                return get200Regex(".*rest/riders/\\d+$", "CURRENT_RIDER_200");
            case RIDER_DATA_NO_RIDE_200_GET:
                return new Builder(200, Method.GET)
                        .withUrl("rest/riders/current")
                        .withFileResponse("RIDER_DATA_NO_RIDE_200")
                        .build();
            case RIDER_DATA_500_GET:
                return new Builder(500, Method.GET)
                        .withUrl("rest/riders/current")
                        .withStringResponse(INTERNAL_SERVER_ERROR)
                        .build();
            case CURRENT_RIDER_INACTIVE_200_GET:
                return get200Regex(".*rest/riders/\\d+$", "CURRENT_RIDER_INACTIVE_200");
            case CURRENT_RIDER_WO_CHARITY_200_GET:
                return get200Regex(".*rest/riders/\\d+$", "CURRENT_RIDER_WO_CHARITY_200");
            case EVENTS_EMPTY_200_GET:
                return get200("rest/events", "EVENT_EMPTY_200").delayedBy(1000L);
            case CONFIG_ZIPCODES_200_GET:
                return get200Regex(".*rest/configs/rider/global\\?.*configAttributes=servicedZipCodes.*$", "CONFIG_ZIPCODES_200");
            case CONFIG_DRIVER_REGISTRATION_200_GET:
                return get200Regex(".*rest/configs/rider/global\\?.*configAttributes=driverRegistration.*$", "CONFIG_DRIVER_REGISTRATION_200");
            case LOGOUT_200_POST:
                return post200Empty("rest/logout");
            case USERS_EXISTS_200_POST:
                return postWithStringResponse("rest/users/exists", 200, "");
            case USERS_EXISTS_EMAIL_400_POST:
                return postWithStringResponse("rest/users/exists", 400, EMAIL_EXISTS);
            case USERS_EXISTS_PHONE_400_POST:
                return postWithStringResponse("rest/users/exists", 400, PHONE_EXISTS);
            case USERS_200_POST:
                return post200("rest/users", "CREATE_USER_200");
            case FACEBOOK_LOGIN_200_POST:
                return post200Empty("rest/facebook");
            case FACEBOOK_LOGIN_202_POST:
                return postEmpty("rest/facebook", 202);
            case RESET_PASSWORD_200_POST:
                return post200Empty("rest/forgot");
            case PHONE_VERIFICATION_REQUEST_CODE_POST:
                return post200Regex(".*rest/phoneVerification/requestCode\\?phoneNumber.*", "PHONE_VERIFICATION_REQUEST_CODE_200");
            case PHONE_VERIFICATION_SEND_CODE_POST:
                return post200Regex(".*rest/phoneVerification/verify\\?authToken=.*&code=.*", "PHONE_VERIFICATION_SEND_CODE_200");
            case PHONE_VERIFICATION_SEND_CODE_400_POST:
                return postWithStringResponseRegex(".*rest/phoneVerification/verify\\?authToken=.*&code=.*", 400, PHONE_VERIFICATION_ERROR);
            case SPECIALFEES_200_GET:
                return get200("rest/rides/specialFees", "SPECIALFEES_200");
            case SPECIALFEES_EMPTY_200_GET:
                return get200("rest/rides/specialFees", "SPECIALFEES_EMPTY_200");
            case RIDERS_PUT:
                return put200Regex(".*rest/riders/.*", "CURRENT_RIDER_PETS_CHARITY_200");
            case RIDERS_PUT_NO_CHARITY:
                return put200Regex(".*rest/riders/.*", "CURRENT_RIDER_WO_CHARITY_200");
            case RIDE_REQUEST_200_POST:
                return post200Regex(".*rest/rides\\?.*startLocationLat=.+&startLocationLong=.+$", "RIDE_REQUESTED_200");
            case RIDE_REQUEST_400_POST:
                return new Builder(400, Method.POST)
                        .withRegexpUrl(".*rest/rides\\?.*startLocationLat=.+&startLocationLong=.+$")
                        .withStringResponse(ACCOUNT_NOT_ACTIVE)
                        .build();
            case RIDE_REQUEST_402_POST:
                return new Builder(402, Method.POST)
                        .withRegexpUrl(".*rest/rides\\?.*startLocationLat=.+&startLocationLong=.+$")
                        .withStringResponse(UNPAID_RIDE)
                        .build();
            case RIDE_REQUESTED_200_GET:
                return get200Regex(".*rest/rides/\\d+\\?.*avatarType=.*", "RIDE_REQUESTED_200");
            case CURRENT_RIDE_REQUESTED_200_GET:
                return get200("rest/rides/current", "RIDE_REQUESTED_200");
            case RIDE_NO_AVAILABLE_DRIVER_200_GET:
                return get200Regex(".*rest/rides/\\d+\\?.*avatarType=.+$", "RIDE_NO_AVAILABLE_DRIVER_200");
            case CURRENT_RIDE_NO_AVAILABLE_DRIVER_200_GET:
                return get200("rest/rides/current", "RIDE_NO_AVAILABLE_DRIVER_200");
            case RIDE_DRIVER_ASSIGNED_200_GET:
                return get200Regex(".*rest/rides/\\d+\\?.*avatarType=.+$", "RIDE_DRIVER_ASSIGNED_200");
            case CURRENT_RIDE_DRIVER_ASSIGNED_200_GET:
                return get200("rest/rides/current", "RIDE_DRIVER_ASSIGNED_200");
            case RIDE_DRIVER_REACHED_200_GET:
                return get200Regex(".*rest/rides/\\d+\\?.*avatarType=.+$", "RIDE_DRIVER_REACHED_200");
            case CURRENT_RIDE_DRIVER_REACHED_200_GET:
                return get200("rest/rides/current", "RIDE_DRIVER_REACHED_200");
            case RIDE_ACTIVE_200_GET:
                return get200Regex(".*rest/rides/\\d+\\?.*avatarType=.+$", "RIDE_ACTIVE_200");
            case CURRENT_RIDE_ACTIVE_200_GET:
                return get200("rest/rides/current", "RIDE_ACTIVE_200");
            case RIDE_COMPLETED_200_GET:
                return get200Regex(".*rest/rides/\\d+\\?.*avatarType=.+$", "RIDE_COMPLETED_200");
            case CURRENT_RIDE_COMPLETED_200_GET:
                return get200("rest/rides/current", "RIDE_COMPLETED_200");
            case RIDE_CANCELLATION_SETTINGS_200_GET:
                return get200("rest/configs/rideCancellationSettings", "RIDE_CANCELLATION_SETTINGS_200");
            case RIDE_MAP_200_GET:
                return get200Regex(".*rest/rides/\\d+/map$", "RIDE_MAP_200");
            case RIDE_ROUTE_DRIVER_ASSIGNED_200_GET:
                return get200("maps/api/directions/json", "RIDE_ROUTE_DRIVER_ASSIGNED_200");
            case RIDE_RATING_200_PUT:
                return put200EmptyRegex(".*rest/rides/\\d+/rating.*$");
            case RIDE_RATING_400_PUT:
                return new Builder(400, Method.PUT)
                        .withRegexpUrl(".*rest/rides/\\d+/rating.*$")
                        .withStringResponse(RATING_ERROR)
                        .build();
            case RIDE_UPDATE_200_PUT:
                return put200Regex(".*rest/rides/\\d+\\?.*$", "RIDE_REQUESTED_200");
            case SPLIT_FARE_EMPTY_200_GET:
                return get200Regex(".*rest/splitfares/\\d+/list$", "SPLIT_FARE_EMPTY_200");
            case SPLIT_FARE_REQUEST_200_POST:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/splitfares/\\d+.*$")
                        .withFileResponse("SPLIT_FARE_REQUEST_200")
                        .build();
            case SPLIT_FARE_REQUESTED_200_GET:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/splitfares/\\d+/list$")
                        .withFileResponse("SPLIT_FARE_REQUESTED_200")
                        .build();
            case RIDE_PAYMENT_HISTORY_200_GET:
                return get200Regex(".*rest/riders/\\d+/payments\\?page=0&pageSize=25&desc=true", "RIDE_PAYMENT_HISTORY_200");
            case RIDER_SUPPORT_TOPICS_200_GET:
                return get200("rest/supporttopics/list/RIDER", "RIDER_SUPPORT_TOPICS_200");
            case RIDER_SUPPORT_TOPICS_LOST_ITEM_200_GET:
                return get200("rest/supporttopics/39/children", "RIDER_SUPPORT_TOPICS_LOST_ITEM_200");
            case RIDE_CANCEL_200_DELETE:
                return new Builder(200, Method.DELETE)
                        .withRegexpUrl(".*rest/rides/\\d+\\?.*avatarType=.*")
                        .withEmptyStringResponse()
                        .build();
            case RIDE_DRIVER_CANCELLED_200_GET:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/rides/\\d+\\?.*avatarType=.+$")
                        .withFileResponse("RIDE_DRIVER_CANCELLED_200")
                        .build();
            case RIDE_REQUEST_WITH_DESTINATION_200_POST:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/rides\\?.*startLocationLat=.+&startLocationLong=.+$")
                        .withFileResponse("RIDE_REQUESTED_WITH_DESTINATION_200")
                        .build();
            case RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_200_GET:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/rides/\\d+\\?.*avatarType=.+$")
                        .withFileResponse("RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_200")
                        .build();
            case RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200_GET:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/rides/\\d+\\?.*avatarType=.+$")
                        .withFileResponse("RIDE_DRIVER_ASSIGNED_WITH_DESTINATION_NO_COMMENT_200")
                        .build();
            case RIDE_DRIVER_REACHED_WITH_DESTINATION_200_GET:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/rides/\\d+\\?.*avatarType=.+$")
                        .withFileResponse("RIDE_DRIVER_REACHED_WITH_DESTINATION_200")
                        .build();
            case DRIVERS_POST_200:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/drivers\\?acceptedTermId=\\d+$")
                        .withFileResponse("CURRENT_DRIVER_NO_TERMS_200")
                        .build();
            case DRIVERS_PHOTO_POST_200:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/drivers/\\d+/photo$")
                        .withFileResponse("CURRENT_DRIVER_NO_TERMS_200")
                        .build();
            case DRIVERS_CAR_POST_200:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/drivers/\\d+/cars$")
                        .withFileResponse("CAR_200")
                        .build();
            case DRIVERS_CAR_PHOTO_POST_200:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/carphotos/car/\\d+$")
                        .withEmptyStringResponse()
                        .build();
            case DRIVERS_TNC_CARD_POST_200:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/driversDocuments/\\d+\\?.*driverPhotoType=CHAUFFEUR_LICENSE.*$")
                        .withFileResponse("CURRENT_DRIVER_NO_TERMS_200")
                        .build();
            case SUPPORT_MESSAGE_200_POST:
                return new Builder(200, Method.POST)
                        .withUrl("rest/support")
                        .withEmptyStringResponse()
                        .build();
            case RIDES_UPGRADE_ACCEPT:
                return new Builder(200, Method.POST)
                        .withUrl("rest/rides/upgrade/accept")
                        .withFileResponse("ACCEPT_RIDE_UPGRADE")
                        .build();
            case RIDES_UPGRADE_DECLINE:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/rides/upgrade/decline\\?avatarType=RIDER")
                        .withFileResponse("DECLINE_RIDE_UPGRADE")
                        .build();
            case RIDES_ESTIMATE:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/rides/estimate\\?startLat=.*&startLong=.*&endLat=.*&endLong=.*&carCategory=.*&inSurgeArea=.*&cityId=.*")
                        .withFileResponse("FARE_ESTIMATE_200")
                        .build();
            case SPLIT_FARE_DELETE_200:
                return new Builder(200, Method.DELETE)
                        .withRegexpUrl(".*rest/splitfares/\\d+.*$")
                        .withEmptyStringResponse()
                        .build();
            case UNPAID_BEVO_BUCKS_GET_200:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/riders/\\d+/payments/pending$")
                        .withFileResponse("UNPAID_BEVO_BUCKS_200")
                        .build();
            case UNPAID_EMPTY_GET_200:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/riders/\\d+/payments/pending$")
                        .withEmptyStringResponse()
                        .build();
            case UNPAID_POST_200:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/riders/\\d+/payments/pending\\?rideId=\\d+$")
                        .withEmptyStringResponse()
                        .build();
            case PROMOCODE_REMAINDER_GET_200:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/riders/\\d+/promocode/remainder")
                        .withEmptyStringResponse()
                        .build();
            case PROMOCODE_POST_200:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/riders/\\d+/promocode")
                        .withEmptyStringResponse()
                        .build();
            case PROMOCODE_REDEMPTIONS_GET_200:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/riders/\\d+/promocode/redemptions")
                        .withEmptyStringResponse()
                        .build();
            case PROMOCODE_POST_400:
                return new Builder(400, Method.POST)
                        .withRegexpUrl(".*rest/riders/\\d+/promocode")
                        .withStringResponse(INVALID_PROMOCODE_ERROR_MSG)
                        .build();
            case DIRECT_CONNECT_GET_DRIVER_200:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/drivers/connect/\\d+")
                        .withFileResponse("DIRECT_CONNECT_DRIVER_200")
                        .build();
            case DIRECT_CONNECT_GET_DRIVER_404:
                return new Builder(404, Method.GET)
                        .withRegexpUrl(".*rest/drivers/connect/\\d+")
                        .withStringResponse(DIRECT_CONNECT_DRIVER_NOT_FOUND)
                        .build();
            case DIRECT_CONNECT_REQUEST_200_POST:
                return post200Regex(".*rest/rides\\?.*driverType=DIRECT_CONNECT.*&directConnectId=.+$", "RIDE_REQUESTED_200");
            case EVENT_RIDE_CANCELLED_BY_RIDER:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_RIDE_CANCELLED_BY_RIDER").delayedBy(2000L);
            case RIDE_CANCELLATION_REASONS_200_GET:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/rides/cancellation\\?avatarType=.*")
                        .withFileResponse("RIDE_CANCELLATION_REASONS_200_GET")
                        .build();
            case RIDE_CANCELLATION_FEEDBACK_200_POST:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/rides/cancellation/\\d+.*$")
                        .withEmptyStringResponse()
                        .build();
            default:
                throw new IllegalArgumentException("Unknown request type: " + requestType);
        }
    }
}
