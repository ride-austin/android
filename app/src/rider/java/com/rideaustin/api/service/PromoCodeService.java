package com.rideaustin.api.service;


import com.rideaustin.api.model.promocode.PromoCode;
import com.rideaustin.api.model.promocode.PromoCodeBalance;
import com.rideaustin.api.model.promocode.PromoCodeResponse;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by vokol on 03.08.2016.
 */
public interface PromoCodeService {

    @POST("rest/riders/{riderId}/promocode")
    Observable<PromoCodeResponse> applyPromoCode(@Path("riderId") long riderAvatarId,
                                                 @Body PromoCode promoCode);


    @GET("rest/riders/{riderId}/promocode")
    Observable<PromoCodeResponse> getRiderPromoCode(@Path("riderId") long riderAvatarId);

    @GET("rest/riders/{riderId}/promocode/redemptions")
    Observable<List<PromoCode>> getRiderPromoCodes(@Path("riderId") long riderId);


    @GET("rest/riders/{riderId}/promocode/remainder")
    Observable<PromoCodeBalance> getRiderPromoCodeBalance(@Path("riderId") long riderId);
}
