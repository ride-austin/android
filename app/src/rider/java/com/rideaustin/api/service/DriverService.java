package com.rideaustin.api.service;

import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.DirectConnectDriver;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.RequestedDriverType;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by nata on 10/07/16.
 */
public interface DriverService {

    @POST("rest/drivers")
    @Multipart
    Observable<Driver> signUpNewDriver(@PartMap() Map<String, RequestBody> partMap, @Query("acceptedTermId") long acceptedTermsId);

    @POST("rest/drivers/{uuid}/cars")
    @Multipart
    Observable<Car> addCarInformation(@Path("uuid") String driverUUID, @PartMap() Map<String, RequestBody> partMap);

    @POST("rest/carphotos/car/{id}")
    @Multipart
    Observable<Object> addCarPhotos(@Path("id") long carId, @PartMap() Map<String, RequestBody> partMap);

    @POST("rest/drivers/{id}/photo")
    @Multipart
    Observable<Driver> postDriverPhoto(@Path("id") long driverId, @Part MultipartBody.Part photoData);

    @GET("rest/driverTypes")
    Observable<List<RequestedDriverType>> getDriverTypes(@Query("cityId") long cityId);

    @GET("rest/drivers/connect/{id}")
    Observable<DirectConnectDriver> getDriverByDirectConnectId(@Path("id") String id);
}
