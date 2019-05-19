package com.rideaustin.api.service;

import com.rideaustin.api.model.Rider;
import com.rideaustin.api.model.RiderData;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by nata on 10/07/16.
 */
public interface RiderService {
    @PUT("rest/riders/{id}")
    Observable<Rider> putRider(@Body Rider rider, @Path("id") Long id);

    @GET("rest/riders/{id}")
    Observable<Rider> getRider(@Path("id") Long id);

    @GET("rest/riders/current")
    Observable<RiderData> getRiderData();

}
