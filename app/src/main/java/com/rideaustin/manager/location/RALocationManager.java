package com.rideaustin.manager.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.RequiresPermission;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.rideaustin.manager.LocationSettingsException;
import com.rideaustin.manager.LocationTimeoutException;
import com.rideaustin.manager.MissingLocationPermissionException;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.PermissionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Scheduler;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

/**
 * Created by Sergey Petrov on 03/05/2017.
 */
public class RALocationManager {

    private final Context context;
    private final LocationConfiguration config;
    private final ReactiveLocationProvider locationProvider;
    private final LocationManager locationManager;
    private final PublishSubject<RALocation> offLocationSubject;
    private final BehaviorSubject<RALocation> lastLocationSubject;

    public RALocationManager(LocationConfiguration config, Context context) {
        this.config = config;
        this.context = context;
        this.locationProvider = new ReactiveLocationProvider(context);
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.offLocationSubject = PublishSubject.create();
        this.lastLocationSubject = BehaviorSubject.create();
    }

    /**
     * Returns observable with live location updates backed with {@link ReactiveLocationProvider}.
     * Would also emit {@link RALocation#off()} if gps signal is lost.
     * May throw {@link MissingLocationPermissionException}
     * May throw {@link LocationSettingsException}
     * If you need single actual live location, consider {@link RALocationManager#getLastLocation(boolean, boolean)}
     * NOTE: Observable emit items on main thread, please specify desired thread by {@link Observable#observeOn(Scheduler)}
     *
     * @return observable of location updates
     */
    public Observable<RALocation> getLocationUpdates() {
        return getLocationUpdate().map(RALocation::on)
                .mergeWith(offLocationSubject)
                .doOnNext(lastLocationSubject::onNext);
    }

    /**
     * Returns observable which would emit single valid location.
     * If <code>fromCache</code> is set to true, observable may return last saved
     * valid location without subscribing to location update logic.
     * Otherwise, location would be fetched from {@link ReactiveLocationProvider}.
     * May throw {@link MissingLocationPermissionException}
     * May throw {@link LocationSettingsException}
     * May throw {@link LocationTimeoutException} if <code>withTimeout</code> is set to true
     * NOTE: Observable emit items on main thread, please specify desired thread by {@link Observable#observeOn(Scheduler)}
     *
     * @param fromCache whether to use cached location (if any)
     * @param withTimeout whether to use timeout
     * @return observable of last valid location
     */
    public Observable<RALocation> getLastLocation(boolean fromCache, boolean withTimeout) {
        return getLocation(fromCache, withTimeout)
                .map(RALocation::on)
                .doOnNext(lastLocationSubject::onNext);
    }

    /**
     * Returns last saved location or {@link RALocation#invalid()} if empty
     * @return last saved location
     */
    public RALocation getLastOfDefault() {
        if (lastLocationSubject.hasValue()) {
            return lastLocationSubject.getValue();
        } else {
            return RALocation.invalid();
        }
    }

    /**
     * Called when device reports about gps providers change
     * If location providers are all off - report {@link RALocation#off()} to live subscribers
     */
    public void onGpsProvidersChanged() {
        if (!isLocationOn()) {
            offLocationSubject.onNext(RALocation.off());
            lastLocationSubject.onNext(RALocation.off());
        }
    }

    /**
     * Returns if gps signal is available.
     *
     * @return <code>true</code> if signal is available, otherwise <code>false</code>
     */
    public boolean isLocationOn() {
        List<String> allProviders = locationManager.getAllProviders();
        return allProviders != null // RA-12965: location manager can return null
                && ((allProviders.contains(GPS_PROVIDER) && locationManager.isProviderEnabled(GPS_PROVIDER))
                || (allProviders.contains(NETWORK_PROVIDER) && locationManager.isProviderEnabled(NETWORK_PROVIDER)));
    }

    @SuppressWarnings("MissingPermission")
    private Observable<Location> getLocation(boolean fromCache, boolean withTimeout) {
        if (!PermissionUtils.isLocationPermissionGranted(context)) {
            return Observable.error(new MissingLocationPermissionException());
        }
        Observable<Location> observable;
        if (fromCache) {
            // return value from last known location if it exists
            // otherwise switch to live location updates
            observable = locationProvider.getLastKnownLocation().switchIfEmpty(getLocationUpdate());
        } else {
            // return live location updates
            observable = getLocationUpdate();
        }
        // take single valid location
        observable = observable.filter(location -> location != null).take(1);
        if (withTimeout) {
            // setup timeout
            observable = observable.timeout(CommonConstants.LOCATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .onErrorResumeNext(this::processThrowable);
        }
        return observable;
    }

    @SuppressWarnings("MissingPermission")
    private Observable<Location> getLocationUpdate() {
        if (!PermissionUtils.isLocationPermissionGranted(context)) {
            return Observable.error(new MissingLocationPermissionException());
        }
        // create standard GMS request
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(config.getSmallestDisplacementMeters())
                .setInterval(config.getTimeIntervalMillis());

        // create settings request
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                // always show request window
                // http://stackoverflow.com/questions/29824408/google-play-services-locationservices-api-new-option-never
                .setAlwaysShow(true)
                .build();

        // check settings and request location updates if all is ok
        return locationProvider.checkLocationSettings(settingsRequest)
                .flatMap(result -> {
                    if (!isLocationSettingsUsable(result)) {
                        // something wrong with settings, let subscriber decide
                        return Observable.error(new LocationSettingsException(result));
                    }
                    return getLocationObservable(locationRequest);
                });
    }

    @RequiresPermission(anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"})
    private Observable<Location> getLocationObservable(LocationRequest request) {
        return RALocationUpdatesObservable.createObservable(context, request);
    }

    private Observable<Location> processThrowable(Throwable throwable) {
        if (throwable instanceof TimeoutException) {
            // NOTE: this logic moved from old driver location manager
            // return last location if we got timeout, but last location is still valid
            if (lastLocationSubject.hasValue() && lastLocationSubject.getValue().isValid()) {
                return Observable.just(lastLocationSubject.getValue().getLocation());
            }
            // wrap timeout exception to be able to distinguish it
            // from other timeout exceptions that could occur in stream
            return Observable.error(new LocationTimeoutException(throwable));
        }
        return Observable.error(throwable);
    }

    private boolean isLocationSettingsUsable(LocationSettingsResult result) {
        return result.getStatus().isSuccess() || result.getLocationSettingsStates().isLocationUsable();
    }
}
