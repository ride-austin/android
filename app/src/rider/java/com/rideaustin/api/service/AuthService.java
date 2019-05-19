package com.rideaustin.api.service;


import com.rideaustin.api.model.LoginResponse;
import com.rideaustin.api.model.User;
import com.rideaustin.api.model.UserDataResponse;
import com.rideaustin.api.model.auth.PhoneNumber;
import com.rideaustin.api.model.auth.PhoneNumberVerification;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * @author shumelchyk
 */
public interface AuthService {

    @POST("rest/users")
    @FormUrlEncoded
    Observable<UserDataResponse> signUpWithEmail(@Field("email") String email,
                                                 @Field("socialId") String socialId,
                                                 @Field("firstname") String firstName,
                                                 @Field("lastname") String lastName,
                                                 @Field("password") String password,
                                                 @Field("phonenumber") String phoneNumber,
                                                 @Field("timeZone") String timeZone,
                                                 @Field("data") String encodedImage,
                                                 @Field("cityId") long cityId,
                                                 @Field("utm_source") String utmSource,
                                                 @Field("promo_code") String promoCode,
                                                 @Field("marketing_title") String marketingTitle,
                                                 @Field("utm_medium") String utmMedium,
                                                 @Field("utm_campaign") String utmCampaign
    );

    @POST("rest/users/exists")
    @FormUrlEncoded
    Observable<String> isUserExists(@Field("email") String email, @Field("phoneNumber") String phoneNumber);

    @POST("rest/users/exists")
    @FormUrlEncoded
    Observable<Void> isPhoneInUse(@Field("phoneNumber") String phoneNumber);

    @PUT("rest/users/{id}")
    Observable<User> updateUser(@Body User user, @Path("id") long id);

    @POST("rest/login")
    Observable<LoginResponse> login();

    @POST("rest/facebook")
    @FormUrlEncoded
    Observable<Response<ResponseBody>> loginFacebook(@Field("token") String token);

    @POST("rest/logout")
    Observable<Void> logout();

    @POST("rest/forgot")
    @FormUrlEncoded
    Observable<Void> forgotPassword(@Field("email") String email);

    @POST("rest/password")
    @FormUrlEncoded
    Observable<Void> setNewPassword(@Field("password") String password);

    @GET("rest/users/current")
    Observable<User> getCurrentUser();

    @POST("rest/photos")
    @Multipart
    Observable<Void> postUsersPhoto(@Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("rest/tokens")
    Observable<Object> registerGCMToken(@Field("value") String gcmToken, @Field("type") String deviceType,
                                        @Field("deviceId") String deviceId, @Field("avatarType") String avatarType);


    @POST("rest/phoneVerification/requestCode")
    Observable<PhoneNumber> sendPhoneNumber(@Query("phoneNumber") final String phoneNumber);

    @POST("rest/phoneVerification/verify")
    Observable<PhoneNumberVerification> verifyPhoneNumber(@Query("authToken") final String authToken, @Query("code") final String code);

}
