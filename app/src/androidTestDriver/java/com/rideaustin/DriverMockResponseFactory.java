package com.rideaustin;

import cases.ra_10852_ride_upgrade.RideUpgradeSuite1;

/**
 * Created by Sergey Petrov on 23/05/2017.
 */

public class DriverMockResponseFactory extends MockResponseFactory {

    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String DRIVER_IS_NOT_ONLINE = "Driver is not online";
    public static final String TERMS_NOT_ACCEPTED = "In order to go online you should read and accept new Driver terms and conditions";
    public static final String PENDING_EVENTS_FAILED = "Some server disaster message here";
    public static final String RIDE_NOT_FOUND = "Ride not found";

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
            case TOKENS_200_POST:
                return post200Empty("rest/tokens");
            case CONFIG_DRIVER_200_GET:
                return get200Regex(".*rest/configs/driver/global\\?.*lat=.+&lng=.+$", "CONFIG_GLOBAL_200");
            case CURRENT_DRIVER_200_GET:
                return get200("rest/drivers/current", "CURRENT_DRIVER_200");
            case CURRENT_DRIVER_NO_TERMS_200_GET:
                return get200("rest/drivers/current", "CURRENT_DRIVER_NO_TERMS_200");
            case CONFIG_DRIVER_REGISTRATION_200_GET:
                return get200Regex(".*rest/configs/rider/global\\?.*configAttributes=driverRegistration.*$", "CONFIG_DRIVER_REGISTRATION_200");
            case QUEUES_200_GET:
                return get200("rest/queues", "QUEUES_200");
            case EVENTS_EMPTY_200_GET:
                return get200("rest/events", "EVENT_EMPTY_200").delayedBy(1000L);
            case SURGE_AREA_EMPTY_200_GET:
                return get200("rest/surgeareas", "SURGEAREA_EMPTY_200");
            case SURGE_AREA_200_GET:
                return get200("rest/surgeareas", "SURGEAREA_200");
            case CURRENT_RIDE_400_GET:
                return getWithStringResponse("rest/rides/current", 400, DRIVER_IS_NOT_ONLINE);
            case CURRENT_RIDE_EMPTY_200_GET:
                return get200Empty("rest/rides/current");
            case CURRENT_RIDE_DRIVER_ASSIGNED_200_GET:
                return get200("rest/rides/current", "RIDE_DRIVER_ASSIGNED_200");
            case CURRENT_RIDE_DRIVER_REACHED_200_GET:
                return get200("rest/rides/current", "RIDE_DRIVER_REACHED_200");
            case CURRENT_RIDE_ACTIVE_200_GET:
                return get200("rest/rides/current", "RIDE_ACTIVE_200");
            case CURRENT_RIDE_COMPLETED_200_GET:
                return get200("rest/rides/current", "RIDE_COMPLETED_200");
            case RIDE_REQUESTED_200_GET:
                return get200Regex(".*rest/rides/\\d+\\?.*avatarType=.*", "RIDE_REQUESTED_200");
            case RIDE_DRIVER_ASSIGNED_200_GET:
                return get200Regex(".*rest/rides/\\d+\\?.*avatarType=.*", "RIDE_DRIVER_ASSIGNED_200");
            case RIDE_DRIVER_REACHED_200_GET:
                return get200Regex(".*rest/rides/\\d+\\?.*avatarType=.*", "RIDE_DRIVER_REACHED_200");
            case RIDE_ACTIVE_200_GET:
                return get200Regex(".*rest/rides/\\d+\\?.*avatarType=.*", "RIDE_ACTIVE_200");
            case RIDE_COMPLETED_200_GET:
                return get200Regex(".*rest/rides/\\d+\\?.*avatarType=.*", "RIDE_COMPLETED_200");
            case RIDE_ADMIN_CANCELLED_200_GET:
                return get200Regex(".*rest/rides/\\d+\\?.*avatarType=.*", "RIDE_ADMIN_CANCELLED_200");
            case RIDE_400_GET:
                return getWithStringResponseRegex(".*rest/rides/\\d+\\?.*avatarType=.*", 400, RIDE_NOT_FOUND);
            case RIDE_RATING_200_PUT:
                return put200EmptyRegex(".*rest/rides/\\d+/rating.*$");
            case PENDING_EVENTS_200_POST:
                return post200Empty("rest/rides/events");
            case PENDING_EVENTS_400_POST:
                return postWithStringResponse("rest/rides/events", 400, PENDING_EVENTS_FAILED);
            case LOGOUT_200_POST:
                return post200Empty("rest/logout");
            case ACTIVE_DRIVERS_EMPTY_200_GET:
                return get200("rest/acdr", "ACTIVE_DRIVERS_EMPTY_200");
            case ACCEPT_DRIVER_TERMS_200_PUT:
                return put200Regex(".*rest/driver/terms/\\d+$", "CURRENT_DRIVER_200");
            case RIDE_ROUTE_DRIVER_ASSIGNED_200_GET:
                return get200("maps/api/directions/json", "RIDE_ROUTE_DRIVER_ASSIGNED_200");
            case DRIVER_GO_ONLINE_412_POST:
                return postWithStringResponse("rest/acdr", 412, TERMS_NOT_ACCEPTED);
            case DRIVER_GO_ONLINE_200_POST:
                return post200Regex(".*rest/acdr\\?latitude=.*&longitude=.*&carCategories=.*&cityId=.*", "DRIVER_GO_ONLINE_200");
            case DRIVER_UPDATE_LOCATION_200_PUT:
                return put200Regex(".*rest/acdr\\?latitude=.*&longitude=.*&heading=.*&speed=.*&course=.*&sequence=.*&carCategories=.*", "DRIVER_GO_ONLINE_200");
            case DRIVER_UPDATE_LOCATION_409_PUT:
                return new Builder(409, Method.PUT)
                        .withRegexpUrl(".*rest/acdr\\?latitude=.*&longitude=.*&heading=.*&speed=.*&course=.*&sequence=.*&carCategories=.*")
                        .withStringResponse(DRIVER_IS_NOT_ONLINE)
                        .build();
            case DRIVER_GO_OFFLINE_200_DELETE:
                return new Builder(200, Method.DELETE)
                        .withUrl("rest/acdr")
                        .withEmptyStringResponse()
                        .build();
            case ACTIVE_DRIVER_AVAILABLE_200_GET:
                return new Builder(200, Method.GET)
                        .withUrl("rest/acdr/current")
                        .withFileResponse("ACTIVE_DRIVER_AVAILABLE_200")
                        .build();
            case ACTIVE_DRIVER_ASSIGNED_200_GET:
                return new Builder(200, Method.GET)
                        .withUrl("rest/acdr/current")
                        .withFileResponse("ACTIVE_DRIVER_ASSIGNED_200")
                        .build();
            case ACTIVE_DRIVER_REACHED_200_GET:
                return new Builder(200, Method.GET)
                        .withUrl("rest/acdr/current")
                        .withFileResponse("ACTIVE_DRIVER_REACHED_200")
                        .build();
            case ACTIVE_DRIVER_ACTIVE_RIDE_200_GET:
                return new Builder(200, Method.GET)
                        .withUrl("rest/acdr/current")
                        .withFileResponse("ACTIVE_DRIVER_ACTIVE_RIDE_200")
                        .build();
            case ACTIVE_DRIVER_EMPTY_200_GET:
                return new Builder(200, Method.GET)
                        .withUrl("rest/acdr/current")
                        .withEmptyStringResponse()
                        .build();
            case ACCEPT_RIDE_200_POST:
                return post200EmptyRegex(".*rest/rides/\\d+/accept$");
            case REACH_RIDE_200_POST:
                return post200EmptyRegex(".*rest/rides/\\d+/reached$");
            case START_RIDE_200_POST:
                return post200EmptyRegex(".*rest/rides/\\d+/start$");
            case END_RIDE_200_POST:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/rides/\\d+/end\\?endLocationLat=.*&endLocationLong=.*$")
                        .withFileResponse("RIDE_COMPLETED_200")
                        .build();
            case DECLINE_RIDE_200_DELETE:
                return new Builder(200, Method.DELETE)
                        .withRegexpUrl(".*rest/rides/\\d+/decline$")
                        .withEmptyStringResponse()
                        .build();
            case ACKNOWLEDGE_RIDE_200_POST:
                return post200EmptyRegex(".*rest/rides/\\d+/received$");
            case EVENT_QUEUED_AREA_ENTERING:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_QUEUED_AREA_ENTERING").delayedBy(2000L);
            case EVENT_RIDE_REQUESTED:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_RIDE_REQUESTED").delayedBy(2000L);
            case EVENT_RIDE_DRIVER_ASSIGNED:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_RIDE_DRIVER_ASSIGNED").delayedBy(2000L);
            case EVENT_RIDE_DRIVER_REACHED:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_RIDE_DRIVER_REACHED").delayedBy(2000L);
            case EVENT_RIDE_ACTIVE:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_RIDE_ACTIVE").delayedBy(2000L);
            case EVENT_RIDE_COMPLETED:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_RIDE_COMPLETED").delayedBy(2000L);
            case EVENT_RIDE_CANCELLED_BY_RIDER:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_RIDE_CANCELLED_BY_RIDER").delayedBy(2000L);
            case EVENT_GO_OFFLINE_INACTIVE:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_GO_OFFLINE_INACTIVE").delayedBy(2000L);
            case EVENT_GO_OFFLINE_TERMS:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_GO_OFFLINE_TERMS").delayedBy(2000L);
            case EVENT_RIDE_REASSIGNED:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_RIDE_REASSIGNED").delayedBy(2000L);
            case DRIVERS_CARTYPES_200_GET:
                return get200Regex(".*rest/drivers/carTypes\\?cityId=.*", "DRIVERS_CARTYPES_200");
            case DRIVER_IN_RIDE_AUSTIN_AIRPORT_QUEUE_FIRST:
                return get200Regex(".*rest/drivers/.*/queue", "DRIVER_IN_RIDE_AUSTIN_AIRPORT_QUEUE_FIRST_200");
            case DRIVER_IN_RIDE_AUSTIN_AIRPORT_QUEUE_SECOND:
                return get200Regex(".*rest/drivers/.*/queue", "DRIVER_IN_RIDE_AUSTIN_AIRPORT_QUEUE_SECOND_200");
            case DRIVER_IN_RIDE_AUSTIN_AIRPORT_QUEUE_MIXED:
                return get200Regex(".*rest/drivers/.*/queue", "DRIVER_IN_RIDE_AUSTIN_AIRPORT_QUEUE_MIXED_200");
            case DRIVER_TYPES_200_GET:
                return get200Regex(".*rest/driverTypes\\?cityId=.*", "DRIVER_TYPES_ALL_200");
            case WEEKLY_EARNINGS_200_GET:
                return get200Regex(".*rest/drivers/\\d+/rides\\?completedOnAfter=.*&completedOnBefore=.*", "WEEKLY_EARNINGS_200");
            case DRIVER_ONLINE_TIME_200_GET:
                return get200Regex(".*rest/drivers/\\d+/online\\?from=.*&to=.*", "DRIVER_ONLINE_TIME_200");
            case RIDE_MAP_200_GET:
                return get200Regex(".*rest/rides/\\d+/map$", "RIDE_MAP_200");
            case DRIVER_SUPPORT_TOPICS_200_GET:
                return get200("rest/supporttopics/list/DRIVER", "DRIVER_SUPPORT_TOPICS_200");
            case DRIVER_SUPPORT_TOPICS_FOUND_ITEM_200_GET:
                return get200("rest/supporttopics/3/form", "DRIVER_SUPPORT_TOPICS_FOUND_ITEM_200");
            case FOUND_ITEM_200_POST:
                return post200("rest/lostandfound/found", "FOUND_ITEM_200");
            case DRIVER_DOCUMENTS_TNC_200_GET:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/driversDocuments/\\d+\\?.*documentType=CHAUFFEUR_LICENSE.*$")
                        .withFileResponse("DRIVER_DOCUMENTS_TNC_200")
                        .build();
            case DRIVER_DOCUMENTS_TNC_200_POST:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/driversDocuments/\\d+\\?.*driverPhotoType=CHAUFFEUR_LICENSE.*$")
                        .withFileResponse("CURRENT_DRIVER_NO_TERMS_200")
                        .build();
            case ACDR_REGULAR_200_GET:
                return new Builder(200, Method.GET)
                        .withUrl("rest/acdr")
                        .withFileResponse("ACDR_REGULAR_200")
                        .build();
            case RIDES_UPGRADE_200_POST:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/rides/upgrade/request\\?target=SUV")
                        .withFileResponse("RIDE_UPGRADE_POST")
                        .build();
            case RIDES_UPGRADE_400_POST:
                return new Builder(400, Method.POST)
                        .withRegexpUrl(".*rest/rides/upgrade/request\\?target=SUV")
                        .withStringResponse(RideUpgradeSuite1.ERROR_CANNOT_SUBMIT)
                        .build();
            case RIDES_UPGRADE_DECLINE_DRIVER:
                return new Builder(200, Method.POST)
                        .withRegexpUrl(".*rest/rides/upgrade/decline\\?avatarType=DRIVER")
                        .withFileResponse("DECLINE_RIDE_UPGRADE")
                        .build();
            case EVENT_RIDE_UPGRADE_ACCEPTED:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_RIDE_UPGRADE_ACCEPTED").delayedBy(2000L);
            case EVENT_RIDE_UPGRADE_EXPIRED:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_RIDE_UPGRADE_EXPIRED").delayedBy(2000L);
            case EVENT_RIDE_UPGRADE_DECLINED:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_RIDE_UPGRADE_DECLINED").delayedBy(2000L);
            case EVENT_RIDER_LOCATION_UPDATED:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_RIDER_LOCATION_UPDATED").delayedBy(2000L);
            case EVENT_END_LOCATION_UPDATED:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_END_LOCATION_UPDATED").delayedBy(2000L);
            case EVENT_ADMIN_CANCELLED:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_ADMIN_CANCELLED").delayedBy(2000L);
            case EVENT_CAR_CATEGORIES_CHANGED:
                return get200Regex(".*rest/events\\?avatarType=DRIVER.*", "EVENT_CAR_CATEGORIES_CHANGED").delayedBy(2000L);
            case RIDE_CANCEL_200_DELETE:
                return new Builder(200, Method.DELETE)
                        .withRegexpUrl(".*rest/rides/\\d+\\?.*avatarType=.*")
                        .withEmptyStringResponse()
                        .build();
            case RESET_PASSWORD_200_POST:
                return post200Empty("rest/forgot");
            case FACEBOOK_LOGIN_200_POST:
                return post200Empty("rest/facebook");
            case DRIVER_GO_ONLINE_400_POST:
                return new Builder(400, Method.POST)
                        .withRegexpUrl(".*rest/acdr\\?latitude=.*&longitude=.*&carCategories=.*&cityId=.*")
                        .withEmptyStringResponse()
                        .build();
            case DRIVER_ALL_CARS_200_GET:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/drivers/\\d+/allCars")
                        .withEmptyStringResponse()
                        .build();
            case DRIVER_SELECT_CAR_400_PUT:
                return new Builder(400, Method.PUT)
                        .withRegexpUrl(".*rest/drivers/selected\\?driverId=\\d+&carId=\\d+")
                        .withEmptyStringResponse()
                        .build();
            case RIDE_CANCELLATION_REASONS_200_GET:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/rides/cancellation\\?avatarType=.*")
                        .withFileResponse("RIDE_CANCELLATION_REASONS_200_GET")
                        .build();

            case RIDE_200_GET_ID_1:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/rides/1\\?.*avatarType=.*")
                        .withEmptyStringResponse() // caller will mock response
                        .build();
            case RIDE_200_GET_ID_2:
                return new Builder(200, Method.GET)
                        .withRegexpUrl(".*rest/rides/2\\?.*avatarType=.*")
                        .withEmptyStringResponse() // caller will mock response
                        .build();

            default:
                throw new IllegalArgumentException("Unknown request type: " + requestType);
        }
    }
}