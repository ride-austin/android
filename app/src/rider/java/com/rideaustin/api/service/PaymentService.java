package com.rideaustin.api.service;

import com.rideaustin.api.model.Payment;
import com.rideaustin.api.model.UnpaidBalance;
import com.rideaustin.api.model.paymenthistory.PaymentHistoryResponse;

import java.util.List;

import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by v.garshyn on 06.07.16.
 */
public interface PaymentService {

    @GET("rest/riders/{riderId}/cards")
    Observable<List<Payment>> getListCards(@Path("riderId") long riderId);

    @POST("rest/riders/{riderId}/cards")
    @FormUrlEncoded
    Observable<Payment> addCard(@Path("riderId") long riderId, @Field("token") String cardToken);

    @PUT("rest/riders/{riderId}/cards/{cardId}")
    Observable<Void> updateCard(@Path("riderId") long riderId,
                                @Path("cardId") long cardId,
                                @Query("primary") boolean primary,
                                @Query("expMonth") Integer expMonth,
                                @Query("expYear") Integer expYear);

    @DELETE("rest/riders/{riderId}/cards/{cardId}")
    Observable<Void> deleteCard(@Path("riderId") long riderId, @Path("cardId") long cardId);

    /**
     * Returns payment history for completed, rider_cancelled and driver_cancelled rides `
     *
     * @param riderId
     * @param page
     * @param pageSize
     * @param sortDesc true for latest first
     * @return
     */
    @GET("rest/riders/{riderId}/payments")
    Observable<PaymentHistoryResponse> getPaymentHistory(@Path("riderId") long riderId,
                                                         @Query("page") int page,
                                                         @Query("pageSize") int pageSize,
                                                         @Query("desc") boolean sortDesc);

    /**
     * Tmp solution to declare it as array
     * Client is not ready to have several unpaid, but server seem to be.
     * After API will be finally considered, it will be refactored.
     */
    @GET("rest/riders/{riderId}/payments/pending")
    Observable<UnpaidBalance[]> getUnpaid(@Path("riderId") long riderId);

    @POST("rest/riders/{riderId}/payments/pending")
    Observable<Void> payUnpaid(@Path("riderId") long riderId, @Query("rideId") long rideId);

}
