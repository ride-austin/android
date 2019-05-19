package com.rideaustin.api.service;

import com.rideaustin.api.model.Document;
import com.rideaustin.api.model.DriverStat;
import com.rideaustin.api.model.QueueResponse;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.DirectConnectResponse;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.api.model.driver.RequestedDriverType;
import com.rideaustin.models.CarPhoto;
import com.rideaustin.models.CarUpdate;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by kshumelchyk on 8/16/16.
 */
public interface DriverService {

    @POST("rest/drivers/{id}/cars")
    @Multipart
    Observable<Car> addCar(@Path("id") Long driverId, @PartMap() Map<String, RequestBody> partMap);

    @PUT("rest/drivers/{id}/cars/{carId}")
    Observable<Car> updateCar(@Path("id") Long driverId, @Path("carId") Long carId, @Body CarUpdate carUpdate);

    @DELETE("rest/drivers/{id}/cars/{carId}")
    Observable<Void> removeCar(@Path("id") Long driverId, @Path("carId") Long carId);

    @GET("rest/carphotos/car/{id}")
    Observable<List<CarPhoto>> getCarPhotos(@Path("id") long carId);

    @POST("rest/carphotos/car/{id}")
    @Multipart
    Observable<CarPhoto> addCarPhoto(@Path("id") long carId, @PartMap() Map<String, RequestBody> partMap);


    @POST("rest/driversDocuments/{driverId}")
    @Multipart
    Observable<Driver> uploadDriverDocuments(@Path("driverId") long driverId,
                                             @Query("driverPhotoType") String driverPhotoType,
                                             @Query("carId") long carId,
                                             @Query("validityDate") String validityDate,
                                             @Part MultipartBody.Part photoData);

    @POST("rest/driversDocuments/{driverId}")
    @Multipart
    Observable<Driver> uploadDriverDocuments(@Path("driverId") long driverId,
                                             @Query("driverPhotoType") String driverPhotoType,
                                             @Query("validityDate") String validityDate,
                                             @Part MultipartBody.Part photoData);


    @PUT("rest/driversDocuments/{documentId}")
    Observable<Document> updateDocument(@Path("documentId") long documentId, @Body Document document);

    @GET("rest/drivers/{id}/allCars")
    Observable<List<Car>> getCars(@Path("id") Long id);

    @PUT("rest/drivers/selected")
    Observable<Car> selectCar(@Query("driverId") Long driverId, @Query("carId") Long carId);

    @PUT("rest/drivers/{id}")
    Observable<Driver> putDriver(@Body Driver driver, @Path("id") Long id);

    @GET("rest/drivers/carTypes")
    Observable<List<RequestedCarType>> getCarTypes(@Query("cityId") long cityId);

    @GET("rest/drivers/{id}/queue")
    Observable<QueueResponse> getQueue(@Path("id") Long id);

    @GET("rest/driverTypes")
    Observable<List<RequestedDriverType>> getDriverTypes(@Query("cityId") long cityId);

    @GET("rest/queues")
    Observable<List<QueueResponse>> getActiveAreaDetails(@Query("cityId") long cityId);

    @GET("rest/drivers/current")
    Observable<Driver> getCurrentDriver();

    @POST("rest/drivers/{id}/referAFriendByEmail")
    Observable<Object> referFriendByEmail(@Path("id") long id,
                                          @Query("email") String email,
                                          @Query("cityId") int cityId);

    @POST("rest/drivers/{id}/referAFriendBySMS")
    Observable<Object> referFriendBySMS(@Path("id") long id,
                                        @Query("phoneNumber") String phoneNumber,
                                        @Query("cityId") int cityId);

    @POST("rest/drivers/{id}/photo")
    @Multipart
    Observable<Driver> postDriverPhoto(@Path("id") long driverId, @Part MultipartBody.Part photoData);

    @PUT("rest/drivers/terms/{id}")
    Observable<Driver> acceptDriverTerms(@Path("id") long termsId);

    @GET("rest/drivers/{id}/stats")
    Observable<List<DriverStat>> getDriverStats(@Path("id") long driverId);

    @GET("rest/drivers/{id}/dcid")
    Observable<DirectConnectResponse> getNewDirectConnectId(@Path("id") long driverId);
}
