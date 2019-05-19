package com.rideaustin;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.Request;
import okhttp3.RequestBody;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Sergey Petrov on 23/05/2017.
 */

@RunWith(AndroidJUnit4.class)
public class DriverMockResponseFactoryTest {

    @Test
    public void shouldUseMockForRequest() {
        DriverMockResponseFactory factory = new DriverMockResponseFactory();
        Request request;
        MockResponse response;

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/configs/app/info/current?avatarType=DRIVER&platformType=ANDROID")
                .build();
        response = factory.create(RequestType.GLOBAL_APP_INFO_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/configs/driver/global?lat=30.2948074&lng=-97.7101059")
                .build();
        response = factory.create(RequestType.CONFIG_DRIVER_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/drivers/current")
                .build();
        response = factory.create(RequestType.CURRENT_DRIVER_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/queues?cityId=1")
                .build();
        response = factory.create(RequestType.QUEUES_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/events?avatarType=DRIVER&lastReceivedEvent=1816069")
                .build();
        response = factory.create(RequestType.EVENTS_EMPTY_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/surgeareas?pageSize=500&cityId=1")
                .build();
        response = factory.create(RequestType.SURGE_AREA_EMPTY_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/rides/current?avatarType=DRIVER")
                .build();
        response = factory.create(RequestType.CURRENT_RIDE_400_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("PUT", RequestBody.create(null, ""))
                .url("https://api-rc.rideaustin.com/rest/driver/terms/1234")
                .build();
        response = factory.create(RequestType.ACCEPT_DRIVER_TERMS_200_PUT);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/driversDocuments/3473?documentType=CHAUFFEUR_LICENSE")
                .build();
        response = factory.create(RequestType.DRIVER_DOCUMENTS_TNC_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("POST", RequestBody.create(null, ""))
                .url("https://api-rc.rideaustin.com/rest/driversDocuments/3473?driverPhotoType=CHAUFFEUR_LICENSE&cityId=1&validityDate=2017-06-30")
                .build();
        response = factory.create(RequestType.DRIVER_DOCUMENTS_TNC_200_POST);
        assertTrue(response.matches(request));
    }
}
