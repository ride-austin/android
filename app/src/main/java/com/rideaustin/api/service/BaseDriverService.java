package com.rideaustin.api.service;

import com.rideaustin.api.model.Document;
import com.rideaustin.api.model.driver.Driver;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by crossover on 22/01/2017.
 */

public interface BaseDriverService {

    @POST("rest/driversDocuments/{driverId}")
    @Multipart
    Observable<Driver> uploadDriverDocuments(@Path("driverId") long driverId,
                                             @Query("driverPhotoType") String driverPhotoType,
                                             @Query("carId") long carId,
                                             @Query("cityId") long cityId,
                                             @Query("validityDate") String validityDate,
                                             @Part MultipartBody.Part photoData);

    @POST("rest/driversDocuments/{driverId}")
    @Multipart
    Observable<Driver> uploadDriverDocuments(@Path("driverId") long driverId,
                                             @Query("driverPhotoType") String driverPhotoType,
                                             @Query("cityId") long cityId,
                                             @Query("validityDate") String validityDate,
                                             @Part MultipartBody.Part photoData);

    @GET("rest/driversDocuments/{driverId}")
    Observable<List<Document>> getDriverDocuments(@Path("driverId") long driverId,
                                                  @Query("documentType") String driverPhotoType,
                                                  @Query("cityId") long cityId);

    @GET("rest/driversDocuments/{driverId}")
    Observable<List<Document>> getDriverDocuments(@Path("driverId") long driverId,
                                                  @Query("documentType") String driverPhotoType);

    @GET("rest/driversDocuments/{driverId}/cars/{carId}")
    Observable<List<Document>> getCarDocuments(@Path("driverId") long driverId,
                                               @Path("carId") Long carId,
                                               @Query("documentType") String driverPhotoType);
}
