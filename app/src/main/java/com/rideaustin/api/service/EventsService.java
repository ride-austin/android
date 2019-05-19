package com.rideaustin.api.service;


import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.events.PendingEvents;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by kshumelchyk on 8/17/16.
 */
public interface EventsService {
    @GET("rest/events")
    Observable<List<Event>> getEvents(@Query("avatarType") String avatarType);

    @GET("rest/events")
    Observable<List<Event>> getEvents(@Query("avatarType") String avatarType, @Query("lastReceivedEvent") long lastReceivedEvent);

    @POST("rest/rides/events")
    Observable<Object> sendPendingEvents(@Body PendingEvents pendingEvents);
}
