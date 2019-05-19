package com.rideaustin.api.service;

import com.rideaustin.api.config.ConfigAppInfoResponse;
import com.rideaustin.api.config.DriverRegistrationWrapper;
import com.rideaustin.api.config.GlobalConfig;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by supreethks on 10/09/16.
 */
public interface ConfigService {

    @GET("rest/configs/app/info/current")
    Observable<ConfigAppInfoResponse> getConfigAppInfo(@Query("avatarType") String avatarType,
                                                       @Query("platformType") String platformType);

    @GET("rest/configs/rider/global")
    Observable<GlobalConfig> getGlobalConfigRider(@Query("lat") double latitude, @Query("lng") double longitude);

    @GET("rest/configs/rider/global")
    Observable<GlobalConfig> getGlobalConfigRider(@Query("cityId") long cityId);

    @GET("rest/configs/driver/global")
    Observable<GlobalConfig> getGlobalConfigDriver(@Query("lat") double latitude, @Query("lng") double longitude);

    @GET("rest/configs/driver/global")
    Observable<GlobalConfig> getGlobalConfigDriver(@Query("cityId") long cityId);

    @GET("rest/configs/rider/global")
    Observable<DriverRegistrationWrapper> getDriverRegistration(@Query("cityId") long cityId, @Query("configAttributes") String configAttributes);
}
