package com.rideaustin.helpers;

import android.support.annotation.Nullable;
import android.util.Base64;

import com.rideaustin.BaseTest;
import com.rideaustin.BuildConfig;
import com.rideaustin.TestConstants;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.Coordinates;
import com.rideaustin.api.model.LoginResponse;
import com.rideaustin.api.model.Ride;
import com.rideaustin.base.retrofit.NullOnEmptyConverterFactory;
import com.rideaustin.utils.Md5Helper;
import com.rideaustin.utils.file.logging.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Sergey Petrov on 09/08/2017.
 */

public class ServerTestHelper {

    private static String ADMIN_TOKEN = null;

    private static TestServiceApi api = createApi();

    public static String getToken(String username, String password) {
        String credentials = username + ":" + Md5Helper.calculateMd5Hash(username, password);
        String basicAuth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        return expectSuccess(api.login(basicAuth)).getToken();
    }

    public static boolean goOnline(String token, double lat, double lng, String... carCategories) {
        return goOnline(token, lat, lng, new HashSet<>(Arrays.asList(carCategories)), 1);
    }

    public static boolean goOnline(String token, double lat, double lng, Set<String> carCategories, long cityId) {
        Coordinates coordinates = expectSuccess(api.activateDriver(token, lat, lng, carCategories, cityId));
        return Double.compare(lat, coordinates.getLat()) == 0 && Double.compare(lng, coordinates.getLng()) == 0;
    }

    public static void goOffline(String token) {
        expectSuccess(api.deactivateDriver(token));
    }

    public static void tryGoOffline(@Nullable String token) {
        if (token != null) {
            execute(api.deactivateDriver(token));
        }
    }

    public static Ride getCurrentRide(String token, AvatarType avatarType) {
        return expectSuccess(api.getCurrentRide(token, avatarType.avatarType));
    }

    @Nullable
    public static Ride tryGetCurrentRide(@Nullable String token, AvatarType avatarType) {
        if (token != null) {
            Response<Ride> response = execute(api.getCurrentRide(token, avatarType.avatarType));
            if (response.isSuccessful()) {
                return response.body();
            }
        }
        return null;
    }

    public static void acceptRide(String token, long rideId) {
        expectSuccess(api.acceptRide(token, rideId));
    }

    public static void arrive(String token, long rideId) {
        expectSuccess(api.reachedRide(token, rideId));
    }

    public static void startRide(String token, long rideId) {
        expectSuccess(api.startRide(token, rideId));
    }

    public static void endRide(String token, long rideId, double lat, double lng) {
        expectSuccess(api.endRide(token, rideId, lat, lng));
    }

    public static void cancelRide(String token, long rideId, AvatarType avatarType) {
        expectSuccess(api.cancelRide(token, rideId, avatarType.avatarType));
    }

    public static void tryCancelDriverRideByAdmin(@Nullable String token) {
        Ride ride = tryGetCurrentRide(token, AvatarType.DRIVER);
        if (ride != null) {
            execute(api.cancelRide(getAdminToken(), ride.getId(), AvatarType.ADMIN.avatarType));
        }
    }

    private static String getAdminToken() {
        if (ADMIN_TOKEN == null) {
            ADMIN_TOKEN = getToken(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
        }
        return ADMIN_TOKEN;
    }

    private static <T> T expectSuccess(Call<T> call) {
        Response<T> response = execute(call);
        if (response.isSuccessful()) {
            return response.body();
        }
        throw new IllegalStateException(response.raw().toString());
    }

    private static <T> Response<T> execute(Call<T> call) {
        try {
            return call.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static TestServiceApi createApi() {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new Logger("ServerTestHelper"));
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpBuilder.addInterceptor(httpLoggingInterceptor);
        httpBuilder.readTimeout(BaseTest.IDLE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        httpBuilder.writeTimeout(BaseTest.IDLE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        httpBuilder.connectTimeout(BaseTest.IDLE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        OkHttpClient client = httpBuilder.build();
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_TESTS)
                .client(client)
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return restAdapter.create(TestServiceApi.class);
    }

    private interface TestServiceApi {

        @POST("rest/login")
        Call<LoginResponse> login(@Header("Authorization") String basicAuth);

        @POST("rest/acdr")
        Call<Coordinates> activateDriver(@Header("X-Auth-Token") String token,
                                         @Query("latitude") double latitude,
                                         @Query("longitude") double longitude,
                                         @Query("carCategories") Set<String> carCategories,
                                         @Query("cityId") long cityId);

        @DELETE("rest/acdr")
        Call<Void> deactivateDriver(@Header("X-Auth-Token") String token);

        @GET("rest/rides/current")
        Call<Ride> getCurrentRide(@Header("X-Auth-Token") String token,
                                  @Query("avatarType") String avatarType);

        @POST("rest/rides/{id}/accept")
        Call<Void> acceptRide(@Header("X-Auth-Token") String token,
                              @Path("id") long id);

        @POST("rest/rides/{id}/reached")
        Call<Object> reachedRide(@Header("X-Auth-Token") String token,
                                 @Path("id") long id);

        @POST("rest/rides/{id}/start")
        Call<Object> startRide(@Header("X-Auth-Token") String token,
                               @Path("id") long id);

        @POST("rest/rides/{id}/end")
        Call<Ride> endRide(@Header("X-Auth-Token") String token,
                           @Path("id") long id,
                           @Query("endLocationLat") double endLocationLat,
                           @Query("endLocationLong") double endLocationLong);


        @DELETE("rest/rides/{id}")
        Call<Void> cancelRide(@Header("X-Auth-Token") String token,
                              @Path("id") long rideId,
                              @Query("avatarType") String avatarType);


    }
}
