package com.rideaustin.api.service;

import com.rideaustin.api.model.faresplit.FareSplitResponse;

import java.util.List;

import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface FareService {

    @FormUrlEncoded
    @POST("rest/splitfares/{rideId}")
    Observable<FareSplitResponse> requestFareSplit(@Path("rideId") long rideId, @Field("phoneNumbers") String riderPhoneNumber);

    @GET("rest/splitfares/{rideId}/list")
    Observable<List<FareSplitResponse>> getFareSplitRequestList(@Path("rideId") long rideId);

    @DELETE("rest/splitfares/{fareRequestId}")
    Observable<Void> deleteFareSplitRequest(@Path("fareRequestId") long fareRequestId);

    @POST("rest/splitfares/{fareRequestId}/accept")
    Observable<Void> acceptFareSplitRequest(@Path("fareRequestId") String fareRequestId, @Query("acceptance") boolean accept);
}