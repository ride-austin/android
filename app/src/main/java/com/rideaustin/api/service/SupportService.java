package com.rideaustin.api.service;

import com.rideaustin.api.model.RideCancellationReason;
import com.rideaustin.api.model.ServerMessage;
import com.rideaustin.api.model.SupportForm;
import com.rideaustin.api.model.SupportRequest;
import com.rideaustin.api.model.SupportTopic;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by crossover on 15/11/2016.
 */

public interface SupportService {

    @POST("rest/support")
    Observable<Void> sendSupportMessage(@Query("message") String message, @Query("rideId") long rideId, @Query("cityId") long cityId);

    @POST("rest/support")
    Observable<Void> sendSupportMessage(@Query("message") String message, @Query("cityId") long cityId);

    @GET("rest/supporttopics/list/{avatarType}")
    Observable<List<SupportTopic>> getSupportTopics(@Path("avatarType") String avatarType);

    @GET("rest/supporttopics/{parentTopicId}/children")
    Observable<List<SupportTopic>> getSupportTopicsByParent(@Path("parentTopicId") int parentTopicId);

    @GET("rest/supporttopics/{topicId}/form")
    Observable<SupportForm> getSupportFormByTopic(@Path("topicId") int topicId);

    @POST("rest/support/default")
    Observable<Void> sendSupportMessage(@Body SupportRequest request);

    @POST("rest/lostandfound/contact")
    Observable<ServerMessage> contactPair(@Query("rideId") final long rideId, @QueryMap Map<String, String> params);

    @POST("rest/lostandfound/lost")
    Observable<ServerMessage> postLostItem(@Query("rideId") final long rideId, @QueryMap Map<String, String> params);

    @Multipart
    @POST("rest/lostandfound/found")
    Observable<ServerMessage> postFoundItem(@PartMap() Map<String, RequestBody> partMap);

    @GET("rest/rides/cancellation")
    Observable<List<RideCancellationReason>> getRideCancellationReasons(@Query("avatarType") String avatarType);

    @POST("rest/rides/cancellation/{rideId}")
    Observable<Void> postRideCancellationFeedback(@Path("rideId") long rideId, @Query("reason") String code, @Query("comment") String comment);

}
