package com.rideaustin.api.service;


import com.rideaustin.api.model.surgearea.SurgeAreasResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by RideDriver on 9/13/16.
 */
public interface SurgeAreasService {

    @GET("rest/surgeareas?pageSize=500")
    Observable<SurgeAreasResponse> getSurgeAreasList(@Query("cityId") long cityId);

}
