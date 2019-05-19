package com.rideaustin.api;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.CurrentAvatarType;
import com.rideaustin.R;
import com.rideaustin.api.config.ConfigAppInfoResponse;
import com.rideaustin.api.config.GenderSelection;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.converter.ConverterUtil;
import com.rideaustin.api.converter.DriverLocationConverter;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.Coordinates;
import com.rideaustin.api.model.DriverLocation;
import com.rideaustin.api.model.DriverStat;
import com.rideaustin.api.model.Gender;
import com.rideaustin.api.model.QueueResponse;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideCancellationReason;
import com.rideaustin.api.model.Rider;
import com.rideaustin.api.model.User;
import com.rideaustin.api.model.driver.ActiveDriver;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.DirectConnectResponse;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.DriverPhotoType;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.api.model.driver.RequestedDriverType;
import com.rideaustin.api.model.surgearea.SurgeAreasResponse;
import com.rideaustin.base.retrofit.RetrofitException;
import com.rideaustin.engine.Direction;
import com.rideaustin.engine.EngineService;
import com.rideaustin.manager.RideRequestManager;
import com.rideaustin.manager.location.RALocation;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.editprofile.UpdateDriverDelegate;
import com.rideaustin.ui.drawer.editprofile.UpdateUserDelegate;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.utils.AnswersUtils;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.Md5Helper;
import com.rideaustin.utils.TimeUtils;
import com.rideaustin.utils.location.LocationHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import java8.util.Optional;
import okhttp3.MultipartBody;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.rideaustin.utils.CommonConstants.DIRECT_CONNECT_DRIVER_TYPE;
import static com.rideaustin.utils.CommonConstants.WOMEN_ONLY_DRIVER_TYPE;
import static com.rideaustin.utils.Constants.DIRECTION_KEY;

/**
 * @author shumelchyk
 */
public class DataManager extends BaseDataManager {

    private BehaviorSubject<Driver> currentDriverSubject = BehaviorSubject.create();
    private BehaviorSubject<User> userSubject = BehaviorSubject.create();
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Long> rideCancelledSubject = PublishSubject.create();
    private float lastBearing = 0.0f;

    public DataManager() {
        super();
        init();
    }

    @Override
    public void dispose() {
        super.dispose();
        subscriptions.clear();
    }

    public boolean isLoggedIn() {
        return userSubject.getValue() != null && currentDriverSubject.getValue() != null;
    }

    public boolean ifLoggedIn(final Action1<User> loggedInAction) {
        if (isLoggedIn()) {
            loggedInAction.call(getCurrentUser());
            return true;
        } else {
            return false;
        }
    }

    public void setCurrentUser(User currentUser) {
        if (currentUser == null) {
            userSubject.onCompleted();
            userSubject = BehaviorSubject.create();
        } else {
            userSubject.onNext(currentUser);
        }
    }

    // TODO : convert this to async call - https://issue-tracker.devfactory.com/browse/RA-6572
    public User getCurrentUser() {
        if (userSubject.getValue() == null) {
            return userSubject.asObservable().toBlocking().first();
        } else {
            return userSubject.getValue();
        }
    }

    public Optional<User> getUser() {
        return Optional.ofNullable(userSubject.getValue());
    }

    public Observable<User> updateUser(User user) {
        if (user == null) {
            return Observable.empty();
        }
        return getAuthService().updateUser(user, user.getId())
                .subscribeOn(RxSchedulers.network())
                .doOnNext(updated -> {
                    setCurrentUser(updated);
                    getDriver().ifPresent(driver -> {
                        driver.setUser(updated);
                        driver.setUser(updated);
                        driver.setFirstname(updated.getFirstName());
                        driver.setLastname(updated.getLastName());
                        driver.setFullName(updated.getFullName());
                        driver.setPhoneNumber(updated.getPhoneNumber());
                        driver.setEmail(updated.getEmail());
                        setCurrentDriver(driver);
                    });
                });
    }

    public Gender getUserGender() {
        return Optional.ofNullable(App.getDataManager().getCurrentUser())
                .map(User::getGender)
                .filter(s -> !TextUtils.isEmpty(s))
                .map(Gender::fromString)
                .orElse(Gender.UNKNOWN);
    }

    public Observable<GenderSelection> getGenderSelection() {
        return App.getConfigurationManager().getLiveConfig()
                .map(GlobalConfig::getGenderSelection);
    }

    public ConfigAppInfoResponse getConfigAppInfoResponse() {
        return appInfoSubject.getValue();
    }

    public Observable<Driver> getOrLoadCurrentDriver() {
        return Observable.defer(() -> {
            if (isLoggedIn()) {
                return currentDriverSubject;
            } else {
                return getUserObservable();
            }
        });
    }

    public Observable<ActiveDriver> loadActiveDriver() {
        return getActiveDriverService().getActiveDriver()
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<SurgeAreasResponse> loadSurgeAreasResponse(final long cityId) {
        return getSurgeAreasService().getSurgeAreasList(cityId)
                .observeOn(RxSchedulers.main())
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<Ride> loadRideStatus(long rideId) {
        return getRidesService().getRide(rideId, AvatarType.DRIVER.avatarType)
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<Ride> getCurrentRideStatus() {
        return getRidesService().getCurrentRide(AvatarType.DRIVER.avatarType)
                .subscribeOn(RxSchedulers.network());
    }

    /**
     * @return a Stream
     */
    public Observable<Driver> getDriverObservable() {
        return currentDriverSubject
                .serialize()
                .onBackpressureLatest()
                .asObservable();
    }


    /**
     * Please use getDriver instead
     *
     * @return
     */
    @Deprecated
    public Driver getCurrentDriver() {
        return currentDriverSubject.getValue();
    }

    public Optional<Driver> getDriver() {
        return Optional.ofNullable(currentDriverSubject.getValue());
    }

    public void setCurrentDriver(Driver currentDriver) {
        currentDriverSubject.onNext(currentDriver);
    }

    public Observable<Driver> loginEmail(String email, String password) {
        updateCredentials(email, Md5Helper.calculateMd5Hash(email, password));
        return getAuthService()
                .login()
                .subscribeOn(RxSchedulers.network())
                .doOnNext(loginResponse -> setXAuth(loginResponse.getToken()))
                .flatMap(loginResponse -> getUserObservable())
                // Let's clear auth on unsuccessful operation in chain of manual login
                // Or we can have a use of old auth token
                .doOnError(throwable -> clearAuth());
    }

    /**
     * To be removed on 3.0.0
     *
     * @return
     */
    @Deprecated
    public Observable<Driver> loginWithOldToken() {
        return getAuthService()
                .login()
                .subscribeOn(RxSchedulers.network())
                .doOnNext(loginResponse -> setXAuth(loginResponse.getToken()))
                .flatMap(loginResponse -> getUserObservable());
    }


    private Observable<Driver> getUserObservable() {
        return getAuthService().getCurrentUser()
                .subscribeOn(RxSchedulers.network())
                .flatMap(user -> {
                    if (!user.isDriver()) {
                        return logout(false).switchMap(aVoid -> Observable.error(new Throwable("Invalid username or password.")));
                    }
                    setCurrentUser(user);
                    return Observable.just(user);
                })
                .flatMap(user -> registerGCMToken(CurrentAvatarType.getAvatarType())
                        .map(aVoid -> user))
                .flatMap(user -> loadDriver());
    }

    public Observable<Integer> loginFacebook(String token) {
        return getAuthService()
                .loginFacebook(token)
                .subscribeOn(RxSchedulers.network())
                .flatMap(resp -> Observable.just(resp.code()));
    }

    public Observable<Void> logoutDriver() {
        return deactivateDriver()
                .switchMap(o -> logout());
    }

    public Observable<Void> logout() {
        return logout(true);
    }

    public Observable<Void> logout(final boolean shouldLogoutFromApp) {
        return getAuthService().logout()
                .subscribeOn(RxSchedulers.network())
                .doOnNext(aVoid -> {
                    if (shouldLogoutFromApp) {
                        logoutUserFromApp();
                    }
                });
    }

    public Observable<Driver> loadDriver() {
        return getDriverService().getCurrentDriver()
                .doOnNext(this::setCurrentDriver)
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<List<Car>> getCars() {
        if (getCurrentUser().getDriverId() != -1) {
            return getDriverService().getCars(getCurrentUser().getDriverId());
        } else {
            return Observable.error(new Throwable("Invalid Driver Id."));
        }
    }

    public Observable<Car> selectCar(Long carId) {
        if (getCurrentUser().getDriverId() != -1) {
            return getDriverService().selectCar(getCurrentUser().getDriverId(), carId);
        } else {
            return Observable.error(new Throwable("Invalid Driver Id."));
        }
    }

    public Observable<Rider> getCurrentRider() {
        if (getCurrentUser().getRiderId() != -1) {
            return getRiderService().getRider(getCurrentUser().getRiderId());
        } else {
            return Observable.error(new Throwable("Invalid Rider Id."));
        }
    }

    public Observable<User> postUsersPhoto(String filePath) {
        return getAuthService()
                .postUsersPhoto(ImageHelper.getTypedFileFromPath(filePath))
                .subscribeOn(RxSchedulers.network())
                .flatMap(aVoid -> getAuthService().getCurrentUser());
    }

    public Observable<Driver> postDriverPhoto(final long driverId, final String filePath) {
        return getDriverService()
                .postDriverPhoto(driverId, ImageHelper.getTypedFileFromPath("photoData", filePath))
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<Driver> acceptDriverTerms(final long termsId) {
        return getDriverService().acceptDriverTerms(termsId).subscribeOn(RxSchedulers.network());
    }

    public Observable<Driver> putDriver(Driver driver, Long id) {
        return getDriverService().putDriver(driver, id).subscribeOn(RxSchedulers.network());
    }

    public Observable<QueueResponse> getDriverQueue() {
        return getDriverService().getQueue(getCurrentDriver().getId()).subscribeOn(RxSchedulers.network());
    }

    public Observable<String> getBaseEncodedImage(final String filePath) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                final String encodedImage = ImageHelper.getEncodedImageFromPath(filePath);
                subscriber.onNext(encodedImage);
                subscriber.onCompleted();
            }
        });
    }

    public Observable<Boolean> checkIsDriverActivePeriodically(long interval, TimeUnit timeUnit) {
        return Observable.interval(interval, timeUnit, RxSchedulers.computation())
                .observeOn(RxSchedulers.network())
                .switchMap(aLong -> loadDriver())
                .map(driver -> driver.getUser().isDriverActive());
    }

    public Observable<?> activateDriver(RALocation myLocation, long cityId) {
        String driverType = null;
        RideRequestManager manager = App.getInstance().getRideRequestManager();
        if (manager.isWomenOnly()) {
            driverType = WOMEN_ONLY_DRIVER_TYPE;
        } else if (manager.isDirectConnect()) {
            driverType = DIRECT_CONNECT_DRIVER_TYPE;
        }
        return activateDriver(myLocation, driverType, cityId)
                .ignoreElements()
                .subscribeOn(RxSchedulers.network());
    }

    private Observable activateDriver(RALocation myLocation, @Nullable String customDriverType, long cityId) {
        final LatLng location = myLocation.getCoordinates();
        Set<String> selectedCategories = App.getInstance().getRideRequestManager()
                .getSelectedCategories();

        if (customDriverType != null) {
            return getActiveDriverService().activateDriver(
                    location.latitude,
                    location.longitude,
                    selectedCategories,
                    customDriverType,
                    cityId);
        } else {
            return getActiveDriverService().activateDriver(
                    location.latitude,
                    location.longitude,
                    selectedCategories,
                    cityId);
        }
    }

    public Observable<Void> deactivateDriver() {
        return getActiveDriverService()
                .deactivateDriver()
                .timeout(Constants.SWITCH_OFFLINE_TIMOUT_S, TimeUnit.SECONDS)
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<List<RALocation>> getNearestDrivers(RALocation myLocation, long cityId) {
        final LatLng location = myLocation.getCoordinates();
        return getActiveDriverService()
                .getActiveDrivers(AvatarType.DRIVER.avatarType, location.latitude, location.longitude, cityId)
                .subscribeOn(RxSchedulers.network())
                .map(driverLocations -> {
                    List<DriverLocation> filteredDrivers = new ArrayList<>(driverLocations.size());
                    for (DriverLocation driverLocation : driverLocations) {
                        if (driverLocation != null && driverLocation.getDriver() != null && getCurrentDriver() != null) {
                            if (!getCurrentDriver().getId().equals(driverLocation.getDriver().getId())) {
                                filteredDrivers.add(driverLocation);
                            }
                        }
                    }
                    return ConverterUtil.convertToList(filteredDrivers, new DriverLocationConverter());
                });
    }

    public Observable<Coordinates> updateDriver(RALocation myLocation) {
        String driverType = null;
        RideRequestManager manager = App.getInstance().getRideRequestManager();
        if (manager.isWomenOnly()) {
            driverType = WOMEN_ONLY_DRIVER_TYPE;
        } else if (manager.isDirectConnect()) {
            driverType = DIRECT_CONNECT_DRIVER_TYPE;
        }
        return updateDriver(myLocation, driverType).subscribeOn(RxSchedulers.network());
    }

    private Observable<Coordinates> updateDriver(RALocation myLocation, @Nullable String customDriverType) {
        // RA-12952: driver may stop and lose bearing
        // use cached bearing in case actual is empty
        if (myLocation.getLocation().hasBearing()) {
            lastBearing = myLocation.getLocation().getBearing();
        } else {
            myLocation.getLocation().setBearing(lastBearing);
        }
        Set<String> selectedCategories = App.getInstance().getRideRequestManager()
                .getSelectedCategories();

        final LatLng location = myLocation.getCoordinates();
        if (customDriverType != null) {
            return getActiveDriverService().updateDriver(
                    location.latitude,
                    location.longitude,
                    0f,
                    myLocation.getLocation().getSpeed(),
                    myLocation.getLocation().getBearing(),
                    TimeUtils.currentTimeMillis() / 1000,
                    selectedCategories,
                    customDriverType);
        } else {
            return getActiveDriverService().updateDriver(
                    location.latitude,
                    location.longitude,
                    0f,
                    myLocation.getLocation().getSpeed(),
                    myLocation.getLocation().getBearing(),
                    TimeUtils.currentTimeMillis() / 1000,
                    selectedCategories);
        }
    }

    public Observable<Object> acceptRide(Ride ride) {
        return getRidesService().acceptRide(ride.getId()).subscribeOn(RxSchedulers.network());
    }

    public Observable<Object> reachedRide(Ride ride) {
        return getRidesService().reachedRide(ride.getId()).subscribeOn(RxSchedulers.network());
    }

    public Observable<Object> startRide(Ride ride) {
        return getRidesService().startRide(ride.getId()).subscribeOn(RxSchedulers.network());
    }

    public Observable<Boolean> declineRide(Ride ride, @Nullable String code, @Nullable String comment) {
        return getRidesService().cancelRide(ride.getId(), code, comment, AvatarType.DRIVER.avatarType)
                .doOnNext(aBoolean -> rideCancelledSubject.onNext(ride.getId()))
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<Long> getCancelledRide() {
        return rideCancelledSubject.asObservable().serialize().onBackpressureBuffer();
    }

    public Observable<Object> acknowledgeRide(long rideId) {
        return getRidesService().acknowledgeRide(rideId).subscribeOn(RxSchedulers.network());
    }

    public Observable<String> loadAddress(final LatLng latLng) {
        return Observable.fromCallable(() -> LocationHelper.getFromLocation(latLng)
                .flatMap(addresses -> LocationHelper.getAddressString(addresses, false)))
                // can't use orElseThrow()
                // see https://stackoverflow.com/a/39109368
                // using Observable.flatMap() instead
                //.orElseThrow(() -> new IllegalStateException("can't load address")))
                .flatMap(o -> {
                    if (o.isPresent()) {
                        return Observable.just(o.get());
                    } else {
                        throw new RuntimeException("can't load address");
                    }
                })
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<Direction> loadDirection(LatLng startPoint, LatLng endPoint) {
        return getDirection(startPoint, endPoint)
                .map(Direction::new)
                .doOnNext(direction -> App.getInstance().getLocalSerializer().save(DIRECTION_KEY, direction));
    }

    public Observable<List<RideCancellationReason>> getRideCancellationReasons() {
        return getSupportService().getRideCancellationReasons(AvatarType.DRIVER.name())
                .subscribeOn(RxSchedulers.network());
    }

    public void logoutUserFromApp(String message) {
        Timber.d("::logoutUserFromApp::");
        App.getInstance().getStateManager().logout();
        App.getDataManager().cancelAllRequests();
        App.getInstance().getRideRequestManager().reset();
        App.getDataManager().cancelAllRequests();
        App.getDataManager().setCurrentUser(null);
        App.getDataManager().clearAuth();
        App.getPrefs().clearPrefs();
        App.logoutFacebook();

        //Start activity
        Intent intent = new Intent(App.getInstance(), SplashActivity.class);
        intent.putExtra(SplashActivity.LOGOUT_REASON, message);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getInstance().startActivity(intent);
    }

    public void logoutUserFromApp() {
        logoutUserFromApp(App.getInstance().getString(R.string.signed_out));
    }

    public Observable<Driver> uploadTNCCard(final String imagePath, final Date expirationDate, final long driverId, final long cityId) {
        final MultipartBody.Part fileData = ImageHelper.getTypedFileFromPath("fileData", imagePath);
        String validityDate = expirationDate != null ? DateHelper.dateToServerDateFormat(expirationDate) : null;
        return getBaseDriverService()
                .uploadDriverDocuments(
                        driverId,
                        DriverPhotoType.CHAUFFEUR_LICENSE.name(),
                        cityId,
                        validityDate,
                        fileData);
    }

    public Observable<List<DriverStat>> getDriverStats() {
        if (getCurrentUser().getDriverId() != -1) {
            return getDriverService().getDriverStats(getCurrentUser().getDriverId());
        } else {
            return Observable.error(new Throwable("Invalid Driver Id."));
        }
    }

    private void init() {
        subscriptions.add(getHttpError()
                // RA-9634: if backpressure occurs,
                // it means we already are in process
                // can safely skip further observation here
                // Also, subject need to be serialized,
                // if onNext can be called from different threads
                // https://github.com/ReactiveX/RxJava/issues/4933
                // http://stackoverflow.com/a/40118307
                .serialize()
                .onBackpressureDrop()
                .observeOn(RxSchedulers.main())
                .subscribe(this::checkLoginStatus, throwable -> Timber.e(throwable, "Unable to handle http error!")));
        subscriptions.add(getNetworkError()
                .serialize()
                .onBackpressureBuffer()
                .observeOn(RxSchedulers.main())
                .subscribe(e -> AnswersUtils.logConnectionError(getCurrentDriver(), e),
                        throwable -> Timber.e(throwable, "Unable to handle network error!")));
        subscriptions.add(getCancelledRide().subscribe(rideId ->
                AnswersUtils.logCancelledRide(App.getDataManager().getCurrentDriver())));
    }

    private void checkLoginStatus(Throwable throwable) {
        if (throwable instanceof RetrofitException) {
            if (((RetrofitException) throwable).getStatusCode() == HttpsURLConnection.HTTP_UNAUTHORIZED && isLoggedIn()) {
                logoutUserFromApp(throwable.getMessage());
            }
        }
    }

    public Observable<GlobalConfig> getDriverCityConfig() {
        int currentCityId = App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId();
        int driverCityId = getDriver().map(Driver::getCityId).orElse(currentCityId);
        if (currentCityId == driverCityId) {
            return Observable.just(App.getConfigurationManager().getLastConfiguration());
        } else {
            return getConfigService().getGlobalConfigDriver(driverCityId);
        }
    }

    public Observable<List<RequestedCarType>> loadCarTypes(int cityId) {
        return App.getDataManager().getDriverService().getCarTypes(cityId)
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<List<RequestedDriverType>> loadDriverTypes(int cityId) {
        return App.getDataManager().getDriverService().getDriverTypes(cityId)
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<DirectConnectResponse> getNewDirectConnectId() {
        if (getDriver().isPresent()) {
            return getDriverService().getNewDirectConnectId(getDriver().get().getId())
                    .subscribeOn(RxSchedulers.network())
                    .doOnNext(response -> {
                        getDriver().get().setDirectConnectId(response.getDirectConnectId());
                    });
        } else {
            return Observable.error(new Throwable("Invalid Driver Id."));
        }
    }

    public UpdateUserDelegate getUpdateUserDelegateInstance() {
        return new UpdateDriverDelegate();
    }

    @VisibleForTesting
    public void clear() {
        EngineService.shutdown(App.getInstance());
        App.getInstance().getLocalSerializer().removeAll();
    }
}
