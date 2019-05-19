package com.rideaustin.manager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.rideaustin.App;
import com.rideaustin.BaseApp;
import com.rideaustin.BuildConfig;
import com.rideaustin.api.config.ConfigAppInfoResponse;
import com.rideaustin.api.config.GeocodingConfiguration;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.Coordinates;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.CommonConstants;
import com.rideaustin.utils.SingleSubject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import java8.util.Optional;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * Created by hatak on 23.11.16.
 */

public class ConfigurationManager {

    private final Context context;

    public ConfigurationManager(final Context context) {
        this.context = context.getApplicationContext();
    }

    private BehaviorSubject<GlobalConfig> configSubject = BehaviorSubject.create();
    private SingleSubject<Boolean> locationPermissionSubject = SingleSubject.create();
    private LatLngBounds cityBounds;

    public void restore(@NonNull ConfigAppInfoResponse config) {
        doOnConfigurationResponse(config);
    }

    public void checkMandatoryUpdateAndRefreshConfiguration() {
        getConfiguration()
                .onErrorResumeNext(throwable -> {
                    Timber.e(throwable, "Can't fetch mandatory updates configuration");
                    return Observable.empty();
                })
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe();
    }

    public Observable<ConfigAppInfoResponse> getConfiguration() {
        return App.getDataManager().fetchAppInfoConfig()
                .doOnNext(this::doOnConfigurationResponse);
    }

    private void doOnConfigurationResponse(ConfigAppInfoResponse response) {
        if (AppInfoUtil.isMandatoryRequired(response) && AppInfoUtil.canShowUpdate()) {
            // do not proceed if update required
            return;
        }
        if (App.getDataManager().isAuthorised()) {
            refreshConfiguration();
        } else {
            postSavedOrDefault();
        }
    }

    private void refreshConfiguration() {
        BaseApp.getLocationManager()
                .getLastLocation(false, true)
                // execute below code on network thread
                .observeOn(RxSchedulers.network())
                .flatMap(location -> getConfigurationObservable(location.getCoordinates()))
                .doOnNext(this::storeConfig)
                // execute subscription code on main
                .observeOn(RxSchedulers.main())
                .subscribe(this::publishConfiguration, throwable -> {
                    postSavedOrDefault();
                    if (throwable instanceof MissingLocationPermissionException) {
                        // Any BaseActivity which is (or will be) started would request location permission
                        // NOTE: SingleSubject acts like trigger, it saves last value until first subscription.
                        locationPermissionSubject.onNext(true);
                        Timber.w(throwable, "Missing location permissions during configuration refresh.");
                    } else if (throwable instanceof LocationTimeoutException) {
                        RxSchedulers.schedule(this::refreshConfiguration, 30, TimeUnit.SECONDS);
                        Timber.w(throwable, "Location timeout during configuration refresh");
                    } else {
                        Timber.e(throwable, "Exception on refreshing config.");
                    }
                });
    }

    public void onLocationPermissionGranted() {
        checkMandatoryUpdateAndRefreshConfiguration();
    }

    @Nullable
    public LatLngBounds getCityBounds() {
        return cityBounds;
    }

    public boolean isInsideCityBoundaries(@NonNull final LatLng location) {
        return cityBounds == null || cityBounds.contains(location);
    }

    private void publishConfiguration(final GlobalConfig globalConfig) {
        if (globalConfig != null) {
            configSubject.onNext(globalConfig);
            updateCityBoundary(globalConfig.getCurrentCity().getCityBoundaryPolygon());
            resetPickupDestinationHints(globalConfig.getGeocodingConfiguration());
        }
    }

    private void updateCityBoundary(final List<Coordinates> cityBoundaryPolygon) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (Coordinates coordinates : cityBoundaryPolygon) {
            builder.include(new LatLng(coordinates.getLat(), coordinates.getLng()));
        }
        cityBounds = builder.build();
    }

    private void resetPickupDestinationHints(GeocodingConfiguration geocodingConfiguration) {
        App.getDataManager().getLocationHintHelper().setHints(
                Optional.ofNullable(geocodingConfiguration).map(GeocodingConfiguration::getPickupHints).orElse(null),
                Optional.ofNullable(geocodingConfiguration).map(GeocodingConfiguration::getDestinationHints).orElse(null));
    }

    private Observable<? extends GlobalConfig> getConfigurationObservable(final LatLng location) {
        if (!App.getDataManager().isAuthorised()) {
            return Observable.error(new IllegalStateException("Auth not provided"));
        } else if (BuildConfig.FLAVOR.contains(CommonConstants.DRIVER_FLAVOR_NAME)) {
            return App.getDataManager().getConfigService().getGlobalConfigDriver(location.latitude, location.longitude);
        } else if (BuildConfig.FLAVOR.contains(CommonConstants.RIDER_FLAVOR_NAME)) {
            return App.getDataManager().getConfigService().getGlobalConfigRider(location.latitude, location.longitude);
        } else {
            throw new RuntimeException("application is not raider or driver");
        }
    }

    private void storeConfig(final GlobalConfig globalConfig) {
        if (globalConfig != null) {
            App.getPrefs().storeConfiguration(globalConfig);
        }
    }

    private void postSavedOrDefault() {
        Observable.defer(() -> Observable.just(App.getPrefs().loadConfiguration()))
                .subscribeOn(RxSchedulers.serializer())
                .observeOn(RxSchedulers.main())
                .subscribe(this::publishConfiguration, throwable -> Timber.w(throwable, "Unable to read saved or default config"));
    }

    public Observable<GlobalConfig> getConfigurationUpdates() {
        return configSubject;
    }

    /**
     * Observable of the most recent config that came from server.
     * Consider using timeout on it
     */
    public Observable<GlobalConfig> getLiveConfig() {
        return App.getConfigurationManager().getConfigurationUpdates()
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof MissingLocationPermissionException ||
                            throwable instanceof LocationSettingsException) {
                        // skip and wait
                        return Observable.just(null);
                    }
                    return Observable.error(throwable);
                })
                .filter(config -> config != null && !config.isEmbedded()).take(1);
    }

    /**
     * Create an observable of configuration updates which starts with most recent available configuration
     */
    public Observable<GlobalConfig> getLastAndRequestUpdates() {
        return configSubject
                .doOnSubscribe(this::checkMandatoryUpdateAndRefreshConfiguration)
                .startWith(configSubject.getValue())
                .filter(config -> config != null);
    }

    public GlobalConfig getLastConfiguration() {
        if (configSubject.getValue() == null) {
            Timber.w("::: Configuration was null falling back to configuration file bundled with apk :::");
            final GlobalConfig config = App.getPrefs().loadDefaultConfiguration();
            configSubject.onNext(config);
            return config;
        }
        return configSubject.getValue();
    }

    public Observable<Boolean> getLocationPermissionRequest() {
        return locationPermissionSubject.asObservable();
    }
}
