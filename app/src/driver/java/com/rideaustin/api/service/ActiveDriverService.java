package com.rideaustin.api.service;

import com.rideaustin.api.model.Coordinates;
import com.rideaustin.api.model.DriverLocation;
import com.rideaustin.api.model.driver.ActiveDriver;

import java.util.List;
import java.util.Set;

import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by kshumelchyk on 8/16/16.
 */
public interface ActiveDriverService {


    @GET("rest/acdr")
    Observable<List<DriverLocation>> getActiveDrivers(@Query("avatarType") String avatarType,
                                                      @Query("latitude") double latitude,
                                                      @Query("longitude") double longitude,
                                                      @Query("cityId") long cityId);

    @POST("rest/acdr")
    Observable<Coordinates> activateDriver(@Query("latitude") double latitude,
                                           @Query("longitude") double longitude,
                                           @Query("carCategories") Set<String> carCategories,
                                           @Query("cityId") long cityId);

    @POST("rest/acdr")
    Observable<Coordinates> activateDriver(@Query("latitude") double latitude,
                                           @Query("longitude") double longitude,
                                           @Query("carCategories") Set<String> carCategories,
                                           @Query("driverTypes") String driverTypes,
                                           @Query("cityId") long cityId);


    @PUT("rest/acdr")
    Observable<Coordinates> updateDriver(@Query("latitude") double latitude,
                                         @Query("longitude") double longitude,
                                         @Query("heading") double heading,
                                         @Query("speed") double speed,
                                         @Query("course") double course,
                                         @Query("sequence") long sequence,
                                         @Query("carCategories") Set<String> carCategories,
                                         @Query("driverTypes") String driverTypes);
    @PUT("rest/acdr")
    Observable<Coordinates> updateDriver(@Query("latitude") double latitude,
                                         @Query("longitude") double longitude,
                                         @Query("heading") double heading,
                                         @Query("speed") double speed,
                                         @Query("course") double course,
                                         @Query("sequence") long sequence,
                                         @Query("carCategories") Set<String> carCategories);

    @DELETE("rest/acdr")
    Observable<Void> deactivateDriver();

    @GET("rest/acdr/current")
    Observable<ActiveDriver> getActiveDriver();
}
