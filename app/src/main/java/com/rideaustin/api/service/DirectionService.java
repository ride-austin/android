package com.rideaustin.api.service;


import com.rideaustin.api.model.DirectionResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by e.shloma on 10.07.16.
 */

public interface DirectionService {

    @GET("json")
    Observable<DirectionResponse> getDirection(@Query("origin") String origin,
                                               @Query("destination") String destination,
                                               @Query("key") String key);

    @GET("json")
    Observable<DirectionResponse> getDirectionWithWaypoints(@Query("origin") String origin,
                                                            @Query("destination") String destination,
                                                            @Query("waypoints") String waypoints,
                                                            @Query("key") String key);


}
