package com.rideaustin.api.service;

import com.rideaustin.api.model.Map;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.ServerMessage;

import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by vokol on 04.07.2016.
 */
public interface RidesService {


    @GET("rest/rides/{id}")
    Observable<Ride> getRide(@Path("id") long rideId,
                             @Query("avatarType") String avatarType);

    @DELETE("rest/rides/{id}")
    Observable<Boolean> cancelRide(@Path("id") long rideId,
                                   @Query("reason") String code,
                                   @Query("comment") String comment,
                                   @Query("avatarType") String avatarType);

    @GET("rest/rides/current")
    Observable<Ride> getCurrentRide(@Query("avatarType") String avatarType);

    @POST("rest/rides/{id}/accept")
    Observable<Object> acceptRide(@Path("id") long id);

    @POST("rest/rides/{id}/reached")
    Observable<Object> reachedRide(@Path("id") long id);

    @POST("rest/rides/{id}/start")
    Observable<Object> startRide(@Path("id") long id);

    @POST("rest/rides/{id}/end")
    Observable<Ride> endRide(@Path("id") long id,
                             @Query("endLocationLat") double endLocationLat,
                             @Query("endLocationLong") double endLocationLong);

    @PUT("rest/rides/{id}/rating")
    Observable<Object> rateRide(@Path("id") long id,
                                @Query("rating") double rating,
                                @Query("avatarType") String avatarType);

    @GET("rest/rides/{id}/map")
    Observable<Map> getRideMap(@Path("id") long id);

    @DELETE("rest/rides/{id}/decline")
    Observable<Object> declineRide(@Path("id") long id);

    @POST("rest/rides/upgrade/request")
    Observable<ServerMessage> requestRideUpgrade(@Query("target") String target);

    @POST("rest/rides/upgrade/decline")
    Observable<ServerMessage> cancelRideUpgrade(@Query("avatarType") String avatarType);

    @POST("rest/rides/{id}/received")
    Observable<Object> acknowledgeRide(@Path("id") long id);

}
