package com.rideaustin.api.service;


import com.rideaustin.api.model.driver.earnings.DriverEarningResponse;
import com.rideaustin.api.model.driver.earnings.DriverOnlineResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by vokol on 10.08.2016.
 */
public interface EarningsService {

    @GET("rest/drivers/{id}/rides")
    Observable<DriverEarningResponse> getDriverEarnings(@Path("id") long driverId,
                                                        @Query("completedOnAfter") String after,
                                                        @Query("completedOnBefore") String before);

    @GET("rest/drivers/{id}/online")
    Observable<DriverOnlineResponse> getDriverOnline(@Path("id") long driverId,
                                                     @Query("from") String from,
                                                     @Query("to") String to);
}
