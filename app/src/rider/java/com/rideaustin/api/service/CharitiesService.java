package com.rideaustin.api.service;


import com.rideaustin.api.model.Charity;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by ysych on 07.07.2016.
 */
public interface CharitiesService {

    @GET("rest/charities")
    Observable<List<Charity>> getCharities(@Query("cityId") long cityId);
}