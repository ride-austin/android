package com.rideaustin.api.service;

import com.rideaustin.api.ShareTokenResponse;
import com.rideaustin.api.model.DriverLocation;
import com.rideaustin.api.model.FareEstimateResponse;
import com.rideaustin.api.model.Map;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.ServerMessage;
import com.rideaustin.api.model.SpecialFee;

import java.util.List;

import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by vokol on 04.07.2016.
 */
public interface RidesService {

    @GET("rest/rides/estimate")
    Observable<FareEstimateResponse> getEstimatedFare(@Query("startLat") double startLatitude,
                                                      @Query("startLong") double startLongitude,
                                                      @Query("endLat") double endLatitude,
                                                      @Query("endLong") double endLongitude,
                                                      @Query("carCategory") String carCategory,
                                                      @Query("inSurgeArea") boolean inSurgeArea,
                                                      @Query("cityId") long cityId);

    @GET("rest/acdr")
    Observable<List<DriverLocation>> getActiveDrivers(@Query("latitude") double latitude,
                                                      @Query("longitude") double longitude,
                                                      @Query("carCategory") String carCategory,
                                                      @Query("inSurgeArea") boolean inSurgeArea,
                                                      @Query("driverType") String driverTypes,
                                                      @Query("cityId") long cityId);

    @POST("rest/rides")
    Observable<Ride> requestRide(@Query("startLocationLat") double startLocationLatitude,
                                 @Query("startLocationLong") double startLocationLongitude,
                                 @Query("startAddress") String startAddress,
                                 @Query("startZipCode") String startZipCode,
                                 @Query("endLocationLat") Double endLocationLatitude,
                                 @Query("endLocationLong") Double endLocationLong,
                                 @Query("endAddress") String endAddress,
                                 @Query("endZipCode") String endZipCode,
                                 @Query("carCategory") String carCategory,
                                 @Query("inSurgeArea") boolean inSurgeArea,
                                 @Query("driverType") String driverTypes,
                                 @Query("cityId") long cityId,
                                 @Query("comment") String comments,
                                 @Query("paymentProvider") String paymentProvider,
                                 @Query("directConnectId") String directConnectId);

    @DELETE("rest/rides/{id}")
    Observable<Object> cancelRide(@Path("id") long rideId, @Query("avatarType") String avatarType);

    @GET("rest/rides/{id}")
    Observable<Ride> getRideByID(@Path("id") long rideId, @Query("avatarType") String avatarType);

    @GET("rest/rides/{id}")
    Observable<Ride> getRideByID(@Path("id") long rideId, @Query("avatarType") String avatarType, @Query("lat") double latitude, @Query("lng") double longitude);

    @GET("rest/rides/current")
    Observable<Ride> getCurrentRide(@Query("avatarType") String avatarType);


    @PUT("rest/rides/{id}")
    Observable<Ride> updateCurrentRide(@Path("id") long rideId,
                                       @Query("endLocationLat") Double destinationLatitude,
                                       @Query("endLocationLong") Double destinationLongitude,
                                       @Query("endAddress") final String destinationAddress,
                                       @Query("endZipCode") String endZipCode,
                                       @Query("comment") String comments);


    @PUT("rest/rides/{id}/rating")
    Observable<Void> rateRide(@Path("id") long id,
                              @Query("rating") double rating,
                              @Query("tip") double tip,
                              @Query("avatarType") String avatarType,
                              @Query("comment") String comment,
                              @Query("paymentProvider") String paymentProvider);

    @GET("rest/rides/{id}/map")
    Observable<Map> getRideMap(@Path("id") long id);

    /**
     * Real time tracking url will be of this form. The id here will be given by this api.
     * env is the environment identifier for environment
     * http://www.rideaustin.com/real-time-tracking?id=edcg064ak1&env=rc
     **/
    @Headers("Content-Type: application/json")
    @POST("rest/rides/{id}/getShareToken")
    Observable<ShareTokenResponse> getRideEtaShareToken(@Path("id") long rideID);

    @GET("rest/rides/specialFees")
    Observable<List<SpecialFee>> getSpecialFees(@Query("startLat") double startLat, @Query("startLong") double startLong, @Query("carCategory") String carCategory, @Query("cityId") long cityId);

    @POST("rest/rides/upgrade/accept")
    Observable<ServerMessage> acceptRideUpgrade();

    @POST("rest/rides/upgrade/decline")
    Observable<ServerMessage> declineRideUpgrade(@Query("avatarType") String avatarType);

    @POST("rest/rides/queue/{requestToken}")
    Observable<Ride> checkRequestToken(@Path("requestToken") String requestToken);

}
