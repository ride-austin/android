package com.rideaustin.api.service;


import com.rideaustin.api.model.surgearea.SurgeAreasResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by ysych on 9/6/16.
 */
public interface SurgeAreasService {

    @GET("rest/surgeareas")
    Observable<SurgeAreasResponse> getSurgeAreasList(@Query("latitude") double lat,
                                                     @Query("longitude") double lon,
                                                     @Query("cityId") long cityId);
}