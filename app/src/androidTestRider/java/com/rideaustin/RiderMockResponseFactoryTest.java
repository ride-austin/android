package com.rideaustin;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.Request;
import okhttp3.RequestBody;

import static com.rideaustin.RequestType.CONFIG_RIDER_200_GET;
import static com.rideaustin.RequestType.CONFIG_ZIPCODES_200_GET;
import static com.rideaustin.RequestType.DRIVERS_POST_200;
import static com.rideaustin.RequestType.DRIVERS_TNC_CARD_POST_200;
import static com.rideaustin.RequestType.EVENTS_EMPTY_200_GET;
import static com.rideaustin.RequestType.LOGIN_FAILED_401_POST;
import static com.rideaustin.RequestType.RIDER_CARDS_200_GET;
import static com.rideaustin.RequestType.RIDER_DATA_NO_RIDE_200_GET;
import static com.rideaustin.RequestType.RIDE_CANCELLATION_SETTINGS_200_GET;
import static com.rideaustin.RequestType.RIDE_MAP_200_GET;
import static com.rideaustin.RequestType.RIDE_REQUESTED_200_GET;
import static com.rideaustin.RequestType.RIDE_REQUEST_200_POST;
import static com.rideaustin.RequestType.RIDE_ROUTE_DRIVER_ASSIGNED_200_GET;
import static com.rideaustin.RequestType.SPLIT_FARE_EMPTY_200_GET;
import static com.rideaustin.RiderMockResponseFactory.INVALID_CREDENTIALS;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Sergey Petrov on 05/05/2017.
 */

@RunWith(AndroidJUnit4.class)
public class RiderMockResponseFactoryTest {

    @Test
    public void shouldUseMockForRequest() {
        RiderMockResponseFactory factory = new RiderMockResponseFactory();
        Request request;
        MockResponse response;

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/configs/rider/global?lat=30.2797241&lng=-97.7214203")
                .build();
        response = factory.create(CONFIG_RIDER_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/configs/rider/global?cityId=1")
                .build();
        response = factory.create(CONFIG_RIDER_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/configs/rider/global?some=value&lat=30.2797241&lng=-97.7214203")
                .build();
        response = factory.create(CONFIG_RIDER_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/configs/rider/global?lat=30.2797241&lng=-97.7214203&some=value")
                .build();
        response = factory.create(CONFIG_RIDER_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/configs/rider/global?lat=&lng=")
                .build();
        response = factory.create(CONFIG_RIDER_200_GET);
        assertFalse(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/configs/rider/global?lat=30.2797241")
                .build();
        response = factory.create(CONFIG_RIDER_200_GET);
        assertFalse(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/configs/rider/global?cityId=1&configAttributes=servicedZipCodes")
                .build();
        response = factory.create(CONFIG_ZIPCODES_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/riders/3449/cards")
                .build();
        response = factory.create(RIDER_CARDS_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/riders/cards")
                .build();
        response = factory.create(RIDER_CARDS_200_GET);
        assertFalse(response.matches(request));

        request = new Request.Builder()
                .method("POST", RequestBody.create(null, ""))
                .url("https://api-rc.rideaustin.com/rest/login")
                .build();
        response = factory.create(LOGIN_FAILED_401_POST);
        assertTrue(response.matches(request));
        assertEquals(401, response.getHttpCode());
        assertEquals(INVALID_CREDENTIALS, response.getBody());

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/events?avatarType=RIDER")
                .build();
        response = factory.create(EVENTS_EMPTY_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("POST", RequestBody.create(null, ""))
                .url("https://api-rc.rideaustin.com/rest/rides?startLocationLat=30.27405616295542&startLocationLong=-97.70890038460493&startAddress=1182%20%D0%A5%D0%B0%D1%80%D0%B3%D1%80%D0%B5%D0%B9%D0%B2-%D1%81%D1%82%D1%80%D0%B8%D1%82&startZipCode=78702&carCategory=REGULAR&inSurgeArea=false&cityId=1")
                .build();
        response = factory.create(RIDE_REQUEST_200_POST);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("POST", RequestBody.create(null, ""))
                .url("https://api-rc.rideaustin.com/rest/rides?startLocationLat=30.27405616295542&startLocationLong=-97.70890038460493&comment=whatever")
                .build();
        response = factory.create(RIDE_REQUEST_200_POST);
        assertTrue(response.matches(request));


        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/rides/1230931?avatarType=RIDER&lat=30.2740555&lng=-97.7089005")
                .build();
        response = factory.create(RIDE_REQUESTED_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/configs/rideCancellationSettings")
                .build();
        response = factory.create(RIDE_CANCELLATION_SETTINGS_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/rides/1230968/map")
                .build();
        response = factory.create(RIDE_MAP_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/rides/1230968/map")
                .build();
        response = factory.create(RIDE_REQUESTED_200_GET);
        assertFalse(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/rides/1230968?param=value")
                .build();
        response = factory.create(RIDE_REQUESTED_200_GET);
        assertFalse(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/splitfares/1230968/list")
                .build();
        response = factory.create(SPLIT_FARE_EMPTY_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://maps.googleapis.com/maps/api/directions/json?origin=30.417174,-97.750211&destination=30.416855,-97.749884&key=AIzaSyB1ueVSC0tf_5GGktTWA2A-aWy-NoBXFFw, tag=Request{method=GET, url=https://maps.googleapis.com/maps/api/directions/json?origin=30.417174,-97.750211&destination=30.416855,-97.749884&key=AIzaSyB1ueVSC0tf_5GGktTWA2A-aWy-NoBXFFw")
                .build();
        response = factory.create(RIDE_ROUTE_DRIVER_ASSIGNED_200_GET);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("POST", RequestBody.create(null, ""))
                .url("https://api-rc.rideaustin.com/rest/drivers?acceptedTermId=1")
                .build();
        response = factory.create(DRIVERS_POST_200);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("POST", RequestBody.create(null, ""))
                .url("https://api-rc.rideaustin.com/rest/driversDocuments/3439?driverPhotoType=CHAUFFEUR_LICENSE&cityId=1")
                .build();
        response = factory.create(DRIVERS_TNC_CARD_POST_200);
        assertTrue(response.matches(request));

        request = new Request.Builder()
                .method("GET", null)
                .url("https://api-rc.rideaustin.com/rest/riders/current")
                .build();
        response = factory.create(RIDER_DATA_NO_RIDE_200_GET);
        assertTrue(response.matches(request));
    }
}
