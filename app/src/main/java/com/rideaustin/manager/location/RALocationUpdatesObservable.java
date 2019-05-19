package com.rideaustin.manager.location;

import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import pl.charmas.android.reactivelocation.observables.BaseLocationObservable;
import rx.Observable;
import rx.Observer;

/**
 * Location updates observable, based both on:
 * <p>
 * <ul>
 * <li>{@link pl.charmas.android.reactivelocation.ReactiveLocationProvider}'s observable</li>
 * <li>previous version of RA's driver location manager</li>
 * </ul>
 * <p>
 * It requests live location updates and immediately checks if last location is available.
 * It also checks if last location is available on  {@link LocationCallback#onLocationAvailability(LocationAvailability)}
 * <p>
 * Created by Sergey Petrov on 28/04/2017.
 */

public class RALocationUpdatesObservable extends BaseLocationObservable<Location> {

    public static Observable<Location> createObservable(Context ctx, LocationRequest locationRequest) {
        return Observable.create(new RALocationUpdatesObservable(ctx, locationRequest));
    }

    private final LocationRequest locationRequest;
    private LocationCallback callback;

    private RALocationUpdatesObservable(Context ctx, LocationRequest locationRequest) {
        super(ctx);
        this.locationRequest = locationRequest;
    }

    @SuppressWarnings("MissingPermission")
    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<? super Location> observer) {
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    // check if location available and valid
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        observer.onNext(location);
                    }
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if (locationAvailability != null && locationAvailability.isLocationAvailable()) {
                    // check if last location available and valid
                    Location location = LocationServices.FusedLocationApi.getLastLocation(apiClient);
                    if (location != null) {
                        observer.onNext(location);
                    }
                }
            }
        };
        // request location updates on client connected
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, callback, Looper.myLooper());

        // check if last location is available right after
        Location location = LocationServices.FusedLocationApi.getLastLocation(apiClient);
        if (location != null) {
            observer.onNext(location);
        }
    }

    @Override
    protected void onUnsubscribed(GoogleApiClient locationClient) {
        // RA-12444: location client is connected but onGoogleApiClientReady is not called...
        // https://github.com/mcharmas/Android-ReactiveLocation/issues/139
        if (locationClient.isConnected() && callback != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(locationClient, callback);
        }
    }
}
