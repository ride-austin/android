package com.rideaustin.api;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.LocalConfig;
import com.rideaustin.R;
import com.rideaustin.api.config.CampaignProvider;
import com.rideaustin.api.config.ConfigAppInfoResponse;
import com.rideaustin.api.config.GenderSelection;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.ut.PayWithBevoBucks;
import com.rideaustin.api.config.ut.UT;
import com.rideaustin.api.model.Address;
import com.rideaustin.api.model.Avatar;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.CampaignParams;
import com.rideaustin.api.model.Charity;
import com.rideaustin.api.model.DriverLocation;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.FareEstimateResponse;
import com.rideaustin.api.model.Gender;
import com.rideaustin.api.model.Payment;
import com.rideaustin.api.model.PaymentProvider;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideCancellationReason;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.Rider;
import com.rideaustin.api.model.RiderData;
import com.rideaustin.api.model.UnpaidBalance;
import com.rideaustin.api.model.User;
import com.rideaustin.api.model.UserDataResponse;
import com.rideaustin.api.model.UserExistsResponse;
import com.rideaustin.api.model.campaigns.CampaignDetails;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.DriverCarData;
import com.rideaustin.api.model.driver.DriverCarPhotoData;
import com.rideaustin.api.model.driver.DriverData;
import com.rideaustin.api.model.driver.DriverPhotoType;
import com.rideaustin.api.model.driver.DriverRegistration;
import com.rideaustin.api.model.driver.DriverUserRegistration;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.api.model.driver.RequestedDriverType;
import com.rideaustin.api.model.faresplit.FareSplitResponse;
import com.rideaustin.api.model.paymenthistory.PaymentHistoryResponse;
import com.rideaustin.api.model.surgearea.SurgeArea;
import com.rideaustin.api.model.surgearea.SurgeAreas;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.retrofit.RetrofitException;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.events.DriverRatedEvent;
import com.rideaustin.models.DriverRegistrationData;
import com.rideaustin.models.UserRegistrationData;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.editprofile.UpdateRiderDelegate;
import com.rideaustin.ui.drawer.editprofile.UpdateUserDelegate;
import com.rideaustin.ui.drawer.triphistory.TripHistoryModel;
import com.rideaustin.ui.payment.PaymentType;
import com.rideaustin.ui.ride.RideStatusProcessor;
import com.rideaustin.ui.ride.RideStatusService;
import com.rideaustin.ui.signin.SplashActivity;
import com.rideaustin.ui.signin.StartupError;
import com.rideaustin.ui.utils.SurgeAreaUtils;
import com.rideaustin.utils.AnswersUtils;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.DateHelper;
import com.rideaustin.utils.ImageHelper;
import com.rideaustin.utils.Md5Helper;
import com.rideaustin.utils.RetryWhenNoNetwork;
import com.rideaustin.utils.RetryWithDelay;
import com.rideaustin.utils.RxponentialBackoffRetry;
import com.rideaustin.utils.SerializationHelper;
import com.rideaustin.utils.TimeUtils;
import com.rideaustin.utils.UnpaidHelper;
import com.rideaustin.utils.gradle.BuildConfigProxy;
import com.rideaustin.utils.location.CustomLocations;
import com.rideaustin.utils.location.RideComments;
import com.stripe.android.model.Card;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import java8.util.Optional;
import java8.util.stream.StreamSupport;
import okhttp3.MultipartBody;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.BaseApp.getPrefs;
import static com.rideaustin.api.model.RideStatus.valueOf;
import static com.rideaustin.utils.CommonConstants.DIRECT_CONNECT_DRIVER_TYPE;
import static com.rideaustin.utils.CommonConstants.FINGERPRINTED_DRIVER_TYPE;
import static com.rideaustin.utils.CommonConstants.WOMEN_ONLY_DRIVER_TYPE;

/**
 * @author shumelchyk
 */
public class DataManager extends BaseDataManager {

    private static final long BEVO_BUCKS_PAYMENT_ID = Long.MAX_VALUE;

    private UserRegistrationData userRegistrationData;
    private DriverRegistrationData driverRegistrationData;
    private User currentUser;
    private Rider currentRider;
    private List<Payment> paymentMethods = new CopyOnWriteArrayList<>();
    private final Set<SurgeArea> surgeAreas = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private RequestedCarType requestedCarType;
    private ConfigAppInfoResponse configAppInfoResponse;
    private BehaviorSubject<Boolean> femaleOnlyModeEditable = BehaviorSubject.create(true);
    private BehaviorSubject<Optional<Ride>> currentRideSubject = BehaviorSubject.create(Optional.empty());
    private TripHistoryModel tripHistoryModel;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Event> serverEvents = PublishSubject.create();
    private Subscription eventSubscription = Subscriptions.empty();
    private PublishSubject<Collection<SurgeArea>> surgeAreaUpdates = PublishSubject.create();
    private RideComments rideComments = new RideComments();
    private StartupError startupError;
    private String requestToken = null;
    private boolean needSync = false;
    private BehaviorSubject<Optional<UnpaidBalance>> unpaidSubject = BehaviorSubject.create(Optional.empty());
    private Subscription unpaidTimerSubscription = Subscriptions.empty();
    private PublishSubject<Void> splitFareChanged = PublishSubject.create();
    private PublishSubject<Long> rideCancelledSubject = PublishSubject.create();
    private PublishSubject<Boolean> femaleOnlyEnabled = PublishSubject.create();
    private BehaviorSubject<Optional<RequestedCarType>> sliderCarType = BehaviorSubject.create();

    public DataManager() {
        super();
        init();
    }

    public DataManager(final String apiEndpoint) {
        super(apiEndpoint);
        init();
    }

    @Override
    public void dispose() {
        super.dispose();
        subscriptions.clear();
        unpaidTimerSubscription.unsubscribe();
    }

    public UserRegistrationData getUserRegistrationData() {
        return userRegistrationData;
    }

    public void setUserRegistrationData(UserRegistrationData userRegistrationData) {
        this.userRegistrationData = userRegistrationData;
        if (this.userRegistrationData.getCityId() == 0) {
            userRegistrationData.setCityId(App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId());
        }
    }

    public void saveUserRegistrationData(Bundle bundle) {
        if (userRegistrationData != null) {
            bundle.putString(Constants.REGISTRATION_DATA, SerializationHelper.serialize(userRegistrationData));
        }
    }

    public void restoreUserRegistrationData(@NonNull Bundle bundle) {
        if (bundle.containsKey(Constants.REGISTRATION_DATA)) {
            String dataStr = bundle.getString(Constants.REGISTRATION_DATA);
            if (!TextUtils.isEmpty(dataStr)) {
                userRegistrationData = SerializationHelper.deSerialize(dataStr, UserRegistrationData.class);
            }
        }
    }

    public DriverRegistrationData getOrCreateDriverRegistrationData() {
        if (driverRegistrationData == null) {
            driverRegistrationData = new DriverRegistrationData(new DriverRegistration(new DriverUserRegistration(new Address())));
        }
        return driverRegistrationData;
    }

    public Observable<Collection<SurgeArea>> getSurgeAreaUpdates() {
        return surgeAreaUpdates
                // RA-11054: avoid back-pressure on subject using buffer
                // Also, need to be serialized as long as "onNext" can be called from different threads
                // https://github.com/ReactiveX/RxJava/issues/4933
                // http://stackoverflow.com/a/40118307
                .serialize()
                .onBackpressureBuffer()
                .asObservable();
    }

    /**
     * Please do not use it whenever possible.
     * This collection is now synchronized and all modifications and iterations are encapsulated.
     * See RA-10259, RA-10914
     * See CollectionTest.java
     */
    @NonNull
    public Collection<SurgeArea> getSurgeAreas() {
        return surgeAreas;
    }

    public boolean isSurge(String carCategory, LatLng location) {
        // RA-10259, RA-10914 - collection is  synchronized
        for (SurgeArea surgeArea : surgeAreas) {
            // RA-11436: need to check location belongs to surge
            if (isSurgeRelevant(surgeArea, carCategory, location)) {
                return true;
            }
        }
        return false;
    }

    public Optional<SurgeArea> findSurgeArea(final String carCategory, LatLng location) {
        List<SurgeArea> surgeAreasWithCategory = new ArrayList<>();
        // RA-10259, RA-10914 - collection is  synchronized
        for (SurgeArea surgeArea : surgeAreas) {
            if (isSurgeRelevant(surgeArea, carCategory, location)) {
                surgeAreasWithCategory.add(surgeArea);
            }
        }
        if (!surgeAreasWithCategory.isEmpty()) {
            return Optional.of(Collections.max(surgeAreasWithCategory, (o1, o2) -> Float.compare(SurgeAreaUtils.getPriceFactor(o1, carCategory), SurgeAreaUtils.getPriceFactor(o2, carCategory))));
        }
        return Optional.empty();
    }

    private boolean isSurgeRelevant(SurgeArea surgeArea, String carCategory, LatLng location) {
        return SurgeAreaUtils.isSurge(surgeArea, carCategory) && surgeArea.contains(location) && Optional.ofNullable(surgeArea.getActive()).orElse(true);
    }

    public void setSurgeArea(Collection<SurgeArea> surgeAreas) {
        if (this.surgeAreas == surgeAreas) {
            // same pointer, skip
            return;
        }
        this.surgeAreas.clear();
        if (surgeAreas != null) {
            for (SurgeArea surgeArea : surgeAreas) {
                if (SurgeAreaUtils.getHighestFactor(surgeArea) > 1f) {
                    this.surgeAreas.add(surgeArea);
                }
            }
        }
        if (!this.surgeAreas.isEmpty()) {
            surgeAreaUpdates.onNext(this.surgeAreas);
        }
    }

    public void clearDriverRegistrationData() {
        if (driverRegistrationData != null) {
            driverRegistrationData.removeFiles();
            driverRegistrationData = null;
        }
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public boolean ifLoggedIn(final Action1<User> loggedInAction) {
        if (getCurrentUser() != null) {
            loggedInAction.call(getCurrentUser());
            return true;
        } else {
            return false;
        }
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Optional<User> getUser() {
        return Optional.ofNullable(currentUser);
    }

    public Observable<User> updateUser(User user) {
        if (user == null) {
            return Observable.empty();
        }
        return getAuthService().updateUser(user, user.getId())
                .subscribeOn(RxSchedulers.network())
                .doOnNext(updated -> {
                    setCurrentUser(updated);
                    if (currentRider != null) {
                        currentRider.setUser(updated);
                        currentRider.setFirstname(updated.getFirstName());
                        currentRider.setLastname(updated.getLastName());
                        currentRider.setFullName(updated.getFullName());
                        currentRider.setPhoneNumber(updated.getPhoneNumber());
                        currentRider.setEmail(updated.getEmail());
                        setCurrentRider(currentRider);
                    }
                });
    }

    public Gender getUserGender() {
        return Optional.ofNullable(App.getDataManager().getCurrentUser())
                .map(User::getGender)
                .filter(s -> !TextUtils.isEmpty(s))
                .map(Gender::fromString)
                .orElse(Gender.UNKNOWN);
    }

    public Observable<User> updateGender(Gender gender) {
        if (currentUser == null) {
            return Observable.empty();
        }
        currentUser.setGender(gender.toString());
        return updateUser(currentUser);
    }

    public Observable<GenderSelection> getGenderSelection() {
        return App.getConfigurationManager().getLiveConfig()
                .map(GlobalConfig::getGenderSelection);
    }

    public void setCarType(RequestedCarType carType) {
        this.requestedCarType = carType;
        getPrefs().setRequestedCarType(carType);
    }

    public RequestedCarType getRequestedCarType() {
        if (requestedCarType == null) {
            requestedCarType = getPrefs().getRequestedCarType();
        }
        return requestedCarType;
    }

    public Optional<RequestedCarType> getSliderSelectedCarType() {
        return getPrefs().getSliderRequestedCarType().or(() -> Optional.ofNullable(getRequestedCarType()));
    }

    public void setSliderSelectedCarType(RequestedCarType carType) {
        getPrefs().setSliderRequestedCarType(carType);
        sliderCarType.onNext(Optional.ofNullable(carType));
    }

    public Observable<RequestedCarType> getSliderCarTypeObservable() {
        return sliderCarType.asObservable().filter(Optional::isPresent).map(Optional::get);
    }

    public List<Payment> getUserPaymentMethods() {
        ArrayList<Payment> payments = new ArrayList<>(paymentMethods);
        if (shouldShowBevoPayment(payments)) {
            // add virtual payment for bevo
            payments.add(getBevoBucksPayment());
            // check if there is saved primary card id
            applyDeviceState(payments);
        } else {
            StreamSupport.stream(payments)
                    .forEach(payment -> payment.setLocalPrimary(null));
        }
        return payments;
    }

    public boolean isPaymentSelected(PaymentType paymentType) {
        return getLocalSelectedPayment()
                .map(payment -> payment.getCardBrand().equalsIgnoreCase(paymentType.name()))
                .orElse(false);
    }

    public Optional<Payment> getLocalSelectedPayment() {
        return StreamSupport.stream(getUserPaymentMethods())
                .filter(Payment::isLocalPrimary)
                .findAny();
    }

    public void selectBevoBucksPayment(boolean isSelected) {
        if (isSelected) {
            App.getPrefs().savePaymentAsLocalPrimary(BEVO_BUCKS_PAYMENT_ID);
        } else {
            App.getPrefs().removePrimaryLocalPayment();
        }
    }

    private boolean shouldShowBevoPayment(ArrayList<Payment> payments) {
        return payments.size() > 0 &&
                Optional.ofNullable(App.getConfigurationManager().getLastConfiguration().getUt())
                        .map(UT::getPayWithBevoBucks)
                        .map(PayWithBevoBucks::getEnabled).orElse(false);
    }

    private Payment getBevoBucksPayment() {
        Payment payment = new Payment();
        payment.setId(BEVO_BUCKS_PAYMENT_ID);
        payment.setCardNumber(Constants.BEVO_BUCKS_CARD_NUMBER);
        payment.setCardBrand(PaymentType.BEVO_BUCKS.name());
        return payment;
    }

    private PaymentProvider getSelectedPaymentProvider() {
        return isPaymentSelected(PaymentType.BEVO_BUCKS)
                ? PaymentProvider.BEVO_BUCKS
                : PaymentProvider.CREDIT_CARD;
    }

    private void applyDeviceState(List<Payment> payments) {
        App.getPrefs().getLocalPrimaryPayment()
                .ifPresent(id -> {
                    for (Payment payment : payments) {
                        if (payment.getId() == id) {
                            payment.setLocalPrimary(true);
                        } else {
                            payment.setLocalPrimary(false);
                        }
                    }
                });
    }

    public boolean hasPaymentMethods() {
        return !paymentMethods.isEmpty();
    }

    public boolean isPrimaryCardExpired() {
        for (Payment payment : paymentMethods) {
            if (payment.isLocalPrimary()) {
                return payment.isExpired();
            }
        }
        return false;
    }

    public boolean isSplitFareEnabled() {
        return !isPaymentSelected(PaymentType.BEVO_BUCKS)
                || Optional.ofNullable(App.getConfigurationManager().getLastConfiguration().getUt())
                .map(UT::getPayWithBevoBucks)
                .map(PayWithBevoBucks::isAvailableForSplitFare)
                .orElse(false);
    }

    public String getSplitFareDisabledMessage() {
        return Optional.ofNullable(App.getConfigurationManager().getLastConfiguration().getUt())
                .map(UT::getPayWithBevoBucks)
                .map(PayWithBevoBucks::getSplitFareMessage)
                .orElse(App.getInstance().getString(R.string.bevo_split_fare_disabled));
    }

    public ConfigAppInfoResponse getConfigAppInfoResponse() {
        return configAppInfoResponse;
    }

    public Observable<RequestedDriverType> getFingerprintedDriverType() {
        return getDriverTypes()
                .flatMapIterable(list -> list)
                .filter(type -> FINGERPRINTED_DRIVER_TYPE.equalsIgnoreCase(type.getName()));
    }

    public Observable<RequestedDriverType> getFemaleDriverType() {
        return getDriverTypes()
                .flatMapIterable(list -> list)
                .filter(type -> WOMEN_ONLY_DRIVER_TYPE.equalsIgnoreCase(type.getName()));
    }

    public Observable<Boolean> isFemaleOnlyAllowed() {
        return isFemaleOnlyAllowedObservable().take(1);
    }

    public boolean isFingerprintedOnlyEnabled() {
        return App.getPrefs().isFingerprintedOnlyEnabled();
    }

    public void setFingerprintedOnlyEnabled(boolean fingerprintedOnlyEnabled) {
        App.getPrefs().setFingerprintedOnlyEnabled(fingerprintedOnlyEnabled);
    }

    public boolean isFemaleOnlyEnabled() {
        return App.getPrefs().isFemaleOnlyEnabled();
    }

    public void setFemaleOnlyEnabled(boolean femaleOnlyEnabled) {
        App.getPrefs().setFemaleOnlyEnabled(femaleOnlyEnabled);
        this.femaleOnlyEnabled.onNext(femaleOnlyEnabled);
    }

    public Observable<Boolean> isFemaleOnlyEnabledObservable() {
        return femaleOnlyEnabled.startWith(isFemaleOnlyEnabled());
    }

    public Observable<Boolean> isFemaleOnlyAllowedObservable() {
        return isFemaleOnlyEnabledObservable().flatMap(isEnabled -> {
            if (!isEnabled) {
                return Observable.just(false);
            }
            return getFemaleDriverType()
                    .map(driverType -> driverType.getEligibleGenders() != null
                            && driverType.getEligibleGenders().contains(currentUser.getGender()))
                    .switchIfEmpty(Observable.just(false));
        });
    }

    public Observable<Boolean> getFemaleModeEditable() {
        return femaleOnlyModeEditable
                .serialize()
                .onBackpressureLatest()
                .asObservable();
    }

    public void postFemaleModeEditable(boolean editable) {
        femaleOnlyModeEditable.onNext(editable);
    }

    public void setCurrentRide(Ride currentRide) {
        setCurrentRideImpl(currentRide);
        App.getPrefs().setRide(currentRide);
    }

    private void setCurrentRideImpl(Ride currentRide) {
        currentRideSubject.onNext(Optional.ofNullable(currentRide));
        if (currentRide != null) {
            App.getPrefs().updateRideInfo(currentRide.getId(), currentRide.getStatus());
            App.getDataManager().setCarType(currentRide.getRequestedCarType());
        }
    }

    public Ride getCurrentRide() {
        return currentRideSubject.getValue().orElse(null);
    }

    public Observable<Optional<Ride>> getCurrentRideObservable() {
        return currentRideSubject
                .serialize()
                .onBackpressureLatest()
                .asObservable();
    }

    @Nullable
    public CampaignParams campaignParams;

    // AUTHORIZATION BLOCK
    public Observable<User> signUpEmail(final String email, final String socialId, final String password, String firstName, String lastName, String phone, String data, long cityId) {
        String timeZone = TimeZone.getDefault().getID();
        String ssPassword = Md5Helper.calculateMd5Hash(email, password);
        return getAuthService()
                .signUpWithEmail(email,
                        socialId,
                        firstName,
                        lastName,
                        ssPassword,
                        phone,
                        timeZone,
                        data,
                        cityId,
                        campaignParams != null ? campaignParams.utmSource : null,
                        campaignParams != null ? campaignParams.promoCode : null,
                        campaignParams != null ? campaignParams.marketingTitle : null,
                        campaignParams != null ? campaignParams.utmMedium : null,
                        campaignParams != null ? campaignParams.utmCampaign : null)
                .subscribeOn(RxSchedulers.network())
                .flatMap((Func1<UserDataResponse, Observable<User>>) userDataResponse -> loginEmail(email, password));
    }

    public Observable<User> loginEmail(String email, String password) {
        campaignParams = null;
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
    public Observable<User> loginWithOldToken() {
        return getAuthService()
                .login()
                .subscribeOn(RxSchedulers.network())
                .doOnNext(loginResponse -> setXAuth(loginResponse.getToken()))
                .flatMap(loginResponse -> getUserObservable());
    }

    public Observable<User> getUserObservable() {
        return Observable.zip(getRiderService().getRiderData(),
                registerGCMToken(LocalConfig.getAvatarType()),
                getAppInfoObservable().doOnNext(response -> configAppInfoResponse = response),
                (riderData, o, response) -> {
                    doOnRiderData(riderData);
                    return riderData.getRider().getUser();
                });
    }

    private void doOnRiderData(RiderData riderData) {
        needSync = false;
        setCurrentUser(riderData.getRider().getUser());
        setCurrentRider(riderData.getRider());
        setCurrentRide(riderData.getRide());
        setPayments(riderData.getPayments());
        postUnpaid(riderData.getUnpaid());
        resetCustomLocations();
        restoreComments();
    }

    public void restoreData() {
        needSync = true;
        currentRider = App.getPrefs().getRider();
        currentUser = currentRider != null ? currentRider.getUser() : null;
        configAppInfoResponse = App.getPrefs().getConfig();
        if (configAppInfoResponse != null) {
            App.getConfigurationManager().restore(configAppInfoResponse);
        }
        setCurrentRideImpl(App.getPrefs().getRide());
        setPaymentsImpl(App.getPrefs().getPayments());
        resetCustomLocations();
        restoreComments();
    }

    public void resetCustomLocations() {
        CustomLocations.reset();
        App.getPrefs().getWork().ifPresent(CustomLocations::put);
        App.getPrefs().getHome().ifPresent(CustomLocations::put);
    }

    public Observable<Integer> loginFacebook(String token) {
        return getAuthService()
                .loginFacebook(token)
                .subscribeOn(RxSchedulers.network())
                .flatMap(resp -> Observable.just(resp.code()));
    }

    public Observable<Void> logout() {
        return getAuthService().logout()
                .subscribeOn(RxSchedulers.network())
                .doOnNext(aVoid -> logoutUserFromApp());
    }

    @VisibleForTesting
    public void clearUserData() {
        paymentMethods.clear();
        rideComments.clear();
        clearAuth();
        getPrefs().clearPrefs();
        setCurrentUser(null);
        App.getStateManager().clearRideState();
    }

    public void logoutUserFromApp(String message) {
        clear();
        cancelAllRequests();
        clearUserData();
        App.logoutFacebook();
        App.getInstance().getInAppMessageManager().readAll();

        // send intent for UI logout
        Intent intent = new Intent(App.getInstance(), SplashActivity.class);
        intent.putExtra(SplashActivity.LOGOUT_REASON, message);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getInstance().startActivity(intent);
    }

    public void logoutUserFromApp() {
        logoutUserFromApp(App.getInstance().getString(R.string.signed_out));
    }

    public Observable<UserExistsResponse> checkIsUserExisted(String email, String phoneNumber) {
        return getAuthService().isUserExists(email, phoneNumber)
                .map(response -> UserExistsResponse.valid())
                .onErrorResumeNext(new Func1<Throwable, Observable<UserExistsResponse>>() {
                    @Override
                    public Observable<UserExistsResponse> call(Throwable throwable) {
                        if (throwable instanceof RetrofitException) {
                            final RetrofitException RetrofitException = (RetrofitException) throwable;
                            if (RetrofitException.getStatusCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                                String errorMessage = throwable.getMessage();
                                if (!TextUtils.isEmpty(errorMessage))
                                    return Observable.just(UserExistsResponse.error(errorMessage));
                            }
                        }
                        return Observable.error(throwable);
                    }
                })
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main());
    }

    private Observable<String> getSelectedDriverTypes() {
        return isFemaleOnlyAllowed()
                .map(allowed -> {
                    String driverTypes = null;
                    if (allowed) {
                        driverTypes = WOMEN_ONLY_DRIVER_TYPE;
                    }
                    if (isFingerprintedOnlyEnabled()) {
                        if (driverTypes == null) {
                            driverTypes = FINGERPRINTED_DRIVER_TYPE;
                        } else {
                            driverTypes += "," + FINGERPRINTED_DRIVER_TYPE;
                        }
                    }
                    return driverTypes;
                });
    }

    // REQUEST RIDE BLOCK
    public Observable<Ride> requestRide(@NonNull GeoPosition startAddress,
                                        Optional<GeoPosition> endAddress,
                                        long cityId,
                                        boolean isSurgeAccepted,
                                        Optional<String> comments,
                                        String carCategory) {

        return getSelectedDriverTypes()
                .flatMap(driverTypes -> getRidesService().requestRide(
                        startAddress.getLat(),
                        startAddress.getLng(),
                        startAddress.getAddressLine(),
                        startAddress.getZipCode(),
                        endAddress.map(GeoPosition::getLat).orElse(null),
                        endAddress.map(GeoPosition::getLng).orElse(null),
                        endAddress.map(GeoPosition::getAddressLine).orElse(null),
                        endAddress.map(GeoPosition::getZipCode).orElse(null),
                        carCategory,
                        isSurgeAccepted,
                        driverTypes,
                        cityId,
                        comments.orElse(null),
                        getSelectedPaymentProvider().name(),
                        null)
                        .subscribeOn(RxSchedulers.network()));
    }

    public Observable<Ride> requestDirectConnect(String directConnectId,
                                                 GeoPosition startAddress,
                                                 Optional<GeoPosition> endAddress,
                                                 long cityId,
                                                 Optional<String> comments,
                                                 String carCategory) {
        return getRidesService().requestRide(
                startAddress.getLat(),
                startAddress.getLng(),
                startAddress.getAddressLine(),
                startAddress.getZipCode(),
                endAddress.map(GeoPosition::getLat).orElse(null),
                endAddress.map(GeoPosition::getLng).orElse(null),
                endAddress.map(GeoPosition::getAddressLine).orElse(null),
                endAddress.map(GeoPosition::getZipCode).orElse(null),
                carCategory,
                true,
                DIRECT_CONNECT_DRIVER_TYPE,
                cityId,
                comments.orElse(null),
                getSelectedPaymentProvider().name(),
                directConnectId)
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<Ride> checkRideAcceptanceStatus(long rideId, String roleName) {
        return getRidesService().getRideByID(rideId, roleName).subscribeOn(RxSchedulers.network());
    }

    public Observable<Ride> checkRideAcceptanceStatus(long rideId, String roleName, double latitude, double longitude) {
        return getRidesService().getRideByID(rideId, roleName, latitude, longitude).subscribeOn(RxSchedulers.network());
    }

    public Observable<Object> cancelRide(long rideId, String avatarType) {
        return getRidesService().cancelRide(rideId, avatarType)
                .doOnNext(aBoolean -> rideCancelledSubject.onNext(rideId))
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<Long> getCancelledRide() {
        return rideCancelledSubject.asObservable().serialize().onBackpressureBuffer();
    }

    public Observable<FareEstimateResponse> getFareEstimate(double startLatitude,
                                                            double startLongitude,
                                                            double endLatitude,
                                                            double endLongitude,
                                                            long cityId,
                                                            boolean inSurgeArea) {
        String carCategory = App.getDataManager().getSliderSelectedCarType().map(RequestedCarType::getCarCategory).orElse(Constants.CarCategory.REGULAR_CAR.getRequestValue());
        return getRidesService().getEstimatedFare(startLatitude, startLongitude, endLatitude, endLongitude, carCategory, inSurgeArea, cityId)
                .subscribeOn(RxSchedulers.network());
    }

    // PAYMENTS BLOCK

    /**
     * Add new payment method
     *
     * @param card - stripe card
     * @return Observable<Payment>
     */
    public Observable<Payment> addNewPayment(final Card card) {
        if (!isLoggedIn()) {
            Timber.w("currentUser is somehow null while 'addNewPayment'");
            return Observable.error(new IllegalStateException(App.getInstance().getString(R.string.error_unknown)));
        }
        final long riderId = getCurrentUser().getRiderId();
        return getPaymentService()
                .getListCards(riderId)
                .subscribeOn(RxSchedulers.network())
                .flatMap(payments -> {
                    for (Payment payment : payments) {
                        if (card.getLast4().equals(payment.getCardNumber())) {
                            return Observable.just(Boolean.FALSE);
                        }
                    }
                    return Observable.just(Boolean.TRUE);
                })
                .flatMap(result -> result
                        ? App.getStripeManager().getStripeToken(card)
                        : Observable.error(new Throwable(App.getInstance().getString(R.string.payment_add_error_already_exists))))
                .flatMap(token -> getPaymentService().addCard(riderId, token.getId()).subscribeOn(RxSchedulers.network()))
                .doOnNext(payment -> paymentMethods.add(payment));
    }

    /**
     * Remove payment  by card Id
     * if return code 200 - Ok. PhoneNumberVerificationBody response is empty
     *
     * @param cardId card id
     * @return {@link List<Payment>}
     */
    public Observable<List<Payment>> deletePayment(long cardId) {
        final long riderId = getCurrentUser().getRiderId();
        return getPaymentService()
                .deleteCard(riderId, cardId)
                .subscribeOn(RxSchedulers.network())
                .flatMap(aVoid -> getPaymentService().getListCards(riderId))
                .doOnNext(this::setPayments)
                .flatMap(payments -> Observable.just(getUserPaymentMethods()));
    }

    /**
     * Make passed card as primary
     *
     * @return List<Payment>
     */
    public Observable<List<Payment>> setCardPrimary(final Payment payment) {
        final long riderId = getCurrentUser().getRiderId();
        PaymentType paymentType = PaymentType.parse(payment.getCardBrand());
        Observable<List<Payment>> observable;
        if (paymentType != PaymentType.BEVO_BUCKS) {
            observable = getPaymentService()
                    // update primary card
                    .updateCard(riderId, payment.getId(), true, null, null)
                    // get server payments list
                    .flatMap(aVoid -> getPaymentService().getListCards(riderId))
                    // save server payments
                    .doOnNext(this::setPayments)
                    // save primary local selection
                    .doOnNext(payments -> App.getPrefs().savePaymentAsLocalPrimary(payment.getId()))
                    // return modified payments client-side
                    .flatMap(payments -> Observable.just(getUserPaymentMethods()))
                    .subscribeOn(RxSchedulers.network());
        } else {
            App.getPrefs().savePaymentAsLocalPrimary(payment.getId());
            observable = Observable.just(getUserPaymentMethods());
        }
        return observable;
    }

    public Observable<Void> updatePayment(final Payment payment, int month, int year) {
        final long riderId = getCurrentUser().getRiderId();
        return getPaymentService().updateCard(riderId, payment.getId(), payment.isPrimary(), month, year);
    }

    private void setPayments(@Nullable List<Payment> payments) {
        setPaymentsImpl(payments);
        App.getPrefs().setPayments(payments);
    }

    private void setPaymentsImpl(@Nullable List<Payment> payments) {
        paymentMethods.clear();
        if (payments != null) {
            paymentMethods.addAll(payments);
        }
    }

    private void postUnpaid(@Nullable UnpaidBalance unpaid) {
        unpaidSubject.onNext(Optional.ofNullable(unpaid));
        if (hasUnpaid()) {
            //noinspection ConstantConditions
            long willChargeAfter = unpaid.getWillChargeOn() - TimeUtils.currentTimeMillis();
            unpaidTimerSubscription = Observable.timer(willChargeAfter + 1000, TimeUnit.MILLISECONDS, RxSchedulers.computation())
                    .doOnCompleted(this::requestUnpaid)
                    .subscribe();
        } else {
            unpaidTimerSubscription.unsubscribe();
        }
    }

    public void requestUnpaid() {
        if (currentRider != null) {
            subscriptions.add(getPaymentService().getUnpaid(currentRider.getId())
                    .subscribeOn(RxSchedulers.network())
                    .retryWhen(new RxponentialBackoffRetry().getNotificationHandler())
                    .subscribe(new ApiSubscriber2<UnpaidBalance[]>(false) {
                        @Override
                        public void onNext(UnpaidBalance[] unpaid) {
                            postUnpaid(unpaid != null && unpaid.length > 0 ? unpaid[0] : null);
                        }
                    }));
        }
    }

    public boolean hasUnpaid() {
        return UnpaidHelper.isUnpaid(getUnpaidBalance());
    }

    public Observable<Optional<UnpaidBalance>> getUnpaidBalanceObservable() {
        return unpaidSubject
                .serialize()
                .onBackpressureBuffer()
                .asObservable();
    }

    public Optional<UnpaidBalance> getUnpaidBalance() {
        return unpaidSubject.getValue();
    }

    public Observable<Void> payUnpaidBalance() {
        if (hasUnpaid()) {
            return getPaymentService().payUnpaid(currentRider.getId(), getUnpaidBalance().get().getRideId())
                    .subscribeOn(RxSchedulers.network())
                    .doOnCompleted(this::requestUnpaid);
        } else {
            return Observable.empty();
        }
    }

    // MAP BLOCK
    public Observable<List<DriverLocation>> getNearestDrivers(double lat, double lng, long cityId) {
        if (requestedCarType == null) {
            return Observable.empty();
        }
        String carCategory = requestedCarType.getCarCategory();
        boolean isInSurgeArea = isSurge(carCategory, new LatLng(lat, lng));
        return getSelectedDriverTypes()
                .flatMap(driverTypes -> getRidesService()
                        .getActiveDrivers(lat, lng, carCategory, isInSurgeArea, driverTypes, cityId)
                        .subscribeOn(RxSchedulers.network()));
    }

    // CURRENT RIDER BLOCK
    public Observable<Rider> putRider(Rider rider, Long id) {
        return getRiderService().putRider(rider, id).subscribeOn(RxSchedulers.network());
    }

    public Observable<Rider> getCurrentRider() {
        if (currentRider != null) {
            return Observable.just(currentRider);
        } else {
            return fetchCurrentRider();
        }
    }

    public Observable<Rider> fetchCurrentRider() {
        long ridersId = 0;
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            for (Avatar avatar : currentUser.getAvatars()) {
                if (AvatarType.RIDER.name().equals(avatar.getType())) {
                    ridersId = avatar.getId();
                }
            }
            return getRiderService()
                    .getRider(ridersId)
                    .doOnNext(this::setCurrentRider)
                    .subscribeOn(RxSchedulers.network());
        } else {
            return Observable.empty();
        }
    }

    private void setCurrentRider(Rider rider) {
        currentRider = rider;
        App.getPrefs().setRider(rider);
    }

    public Observable<User> postUsersPhoto(String filePath) {
        return getAuthService()
                .postUsersPhoto(ImageHelper.getTypedFileFromPath(filePath))
                .flatMap(aVoid -> getAuthService().getCurrentUser())
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<Driver> postDriverPhoto(final long driverId, final String filePath) {
        return getDriverService()
                .postDriverPhoto(driverId, ImageHelper.getTypedFileFromPath("photoData", filePath))
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<Rider> putCurrentRidersCharity(final Charity charity) {
        return fetchCurrentRider()
                .subscribeOn(RxSchedulers.network())
                .flatMap(rider -> {
                    rider.setCharity(charity);
                    return getRiderService().putRider(rider, rider.getId());
                });
    }

    public Observable<String> getBaseEncodedImage(final String filePath) {
        return Observable.fromCallable(() -> ImageHelper.getEncodedImageFromPath(filePath));
    }

    public Observable<com.rideaustin.api.model.Map> getRideMap(final long rideId) {
        return getRidesService().getRideMap(rideId).subscribeOn(RxSchedulers.network());
    }

    public Observable<Driver> createNewDriver(final DriverRegistrationData driverRegistrationData) {
        return Observable.fromCallable(() -> new DriverData(driverRegistrationData))
                .flatMap(driverData -> getDriverService().signUpNewDriver(driverData.getDriverRegistrationData(), driverRegistrationData.getAcceptedTermId()))
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<Car> uploadCarData(final DriverRegistrationData driverRegistrationData, final String driverUuid) {
        return Observable.fromCallable(() -> new DriverCarData(driverRegistrationData))
                .flatMap(carData -> getDriverService().addCarInformation(driverUuid, carData.getCarData()))
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<Object> uploadCarPhotoData(final DriverRegistrationData driverRegistrationData, final long carId) {
        return Observable.just(Constants.CarPhotoType.FRONT, Constants.CarPhotoType.BACK, Constants.CarPhotoType.INSIDE, Constants.CarPhotoType.TRUNK)
                .map(type -> {
                    if (!driverRegistrationData.getCarPhotoFilePathMap().containsKey(type)) {
                        return Boolean.FALSE;
                    }
                    DriverCarPhotoData driverCarPhotoData = new DriverCarPhotoData(driverRegistrationData, type);
                    return getDriverService().addCarPhotos(carId, driverCarPhotoData.getPhotoData()).toBlocking().single();
                });
    }

    public Observable<Driver> uploadTNCCard(final DriverRegistrationData driverRegistrationData, final long driverId, final long cityId) {
        final MultipartBody.Part fileData = ImageHelper.getTypedFileFromPath("fileData", driverRegistrationData.getDriverTncCardImagePath());
        final String validityDate = driverRegistrationData.getDriverTncCardExpirationDate() != null
                ? DateHelper.dateToServerDateFormat(driverRegistrationData.getDriverTncCardExpirationDate())
                : null;
        return getBaseDriverService()
                .uploadDriverDocuments(driverId, DriverPhotoType.CHAUFFEUR_LICENSE.name(), cityId, validityDate, fileData)
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<Driver> uploadTNCSticker(final DriverRegistrationData driverRegistrationData, final long cardId, final long driverId, final long cityId) {
        Timber.d("Loading image: %s", driverRegistrationData.getDriverTncStickerImagePath());
        final String validityDate = driverRegistrationData.getTncStickerExpirationDate() != null
                ? DateHelper.dateToServerDateFormat(driverRegistrationData.getTncStickerExpirationDate())
                : null;
        return Observable.fromCallable(() ->
                ImageHelper.getTypedFileFromPath("fileData", driverRegistrationData.getDriverTncStickerImagePath()))
                .flatMap(fileData -> getBaseDriverService()
                        .uploadDriverDocuments(driverId,
                                DriverPhotoType.CAR_STICKER.name(),
                                cardId,
                                cityId,
                                validityDate,
                                fileData)
                        .subscribeOn(RxSchedulers.network()));
    }

    public Observable<PaymentHistoryResponse> getPaymentHistory() {
        return getPaymentService().getPaymentHistory(getCurrentUser().getRiderId(), 0, Integer.MAX_VALUE, true)
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<PaymentHistoryResponse> getPaymentHistory(int page, int pageSize) {
        return getPaymentService().getPaymentHistory(getCurrentUser().getRiderId(), page, pageSize, true)
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<FareSplitResponse> requestFareSplit(String phoneNumber) {
        return getFareService().requestFareSplit(getPrefs().getRideId(), phoneNumber)
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<List<FareSplitResponse>> getFareSplitRequestList() {
        return getFareService().getFareSplitRequestList(getPrefs().getRideId())
                .subscribeOn(RxSchedulers.network());
    }

    public Observable<List<FareSplitResponse>> deleteFareSplitRequest(long fareRequestId) {
        return getFareService().deleteFareSplitRequest(fareRequestId)
                .flatMap(aVoid -> getFareSplitRequestList())
                .subscribeOn(RxSchedulers.network());
    }

    private Observable<List<RequestedDriverType>> getDriverTypes() {
        return App.getConfigurationManager().getLiveConfig()
                .map(GlobalConfig::getDriverTypes)
                .timeout(5, TimeUnit.SECONDS)
                .onErrorResumeNext(throwable -> loadDriverTypes())
                .switchIfEmpty(loadDriverTypes());
    }

    private Observable<List<RequestedDriverType>> loadDriverTypes() {
        return getDriverService().getDriverTypes(App.getConfigurationManager()
                .getLastConfiguration().getCurrentCity().getCityId())
                .subscribeOn(RxSchedulers.network());
    }


    public Observable<String> getRideEtaShareText() {
        return getRidesService().getRideEtaShareToken(getPrefs().getRideId())
                .subscribeOn(RxSchedulers.network())
                .map(shareTokenResponse -> {
                    String domain = App.getConfigurationManager().getLastConfiguration().getGeneralInformation().getCompanyWebsite();
                    String tokenUrl = BuildConfigProxy.getEnv().equals(Constants.ENV_PROD) ?
                            String.format(Locale.US, Constants.REAL_TIME_TRACKING_URL_FORMAT_NO_ENV, domain, shareTokenResponse.getToken()) :
                            String.format(Locale.US, Constants.REAL_TIME_TRACKING_URL_FORMAT, domain, shareTokenResponse.getToken(), BuildConfigProxy.getEnv());
                    Timber.d("::getRideEtaShareText:: Token: %s", tokenUrl);
                    return App.getInstance().getString(R.string.realtime_tracking_share_message, tokenUrl);
                });
    }

    public Observable<List<RideCancellationReason>> getRideCancellationReasons() {
        return getSupportService().getRideCancellationReasons(AvatarType.RIDER.name())
                .subscribeOn(RxSchedulers.network());
    }

    public void setTripHistoryModel(TripHistoryModel tripHistoryModel) {
        if (this.tripHistoryModel != null) {
            this.tripHistoryModel.destroy();
        }
        this.tripHistoryModel = tripHistoryModel;
    }

    public TripHistoryModel getTripHistoryModel() {
        return tripHistoryModel;
    }

    private void init() {
        requestedCarType = getRequestedCarType();
        if (requestedCarType == null) {
            // avoid NPE in DataManager calls
            requestedCarType = new RequestedCarType(Constants.CarCategory.REGULAR_CAR.value(), Constants.CarCategory.REGULAR_CAR.value());
        }
        sliderCarType.onNext(getSliderSelectedCarType());
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
                .subscribe(e -> AnswersUtils.logConnectionError(currentRider, e),
                        throwable -> Timber.e(throwable, "Unable to handle network error!")));

    }

    public Subscription ifRideRateNeededThen(Action1<Ride> onRideRateNeeded) {
        return getRideToRate()
                .subscribeOn(RxSchedulers.network())
                .mergeWith(getRideIfCompleted())
                .retryWhen(new RetryWhenNoNetwork(5000))
                .distinctUntilChanged((ride, ride2) -> ride.getId().equals(ride2.getId()))
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<Ride>(true) {
                    @Override
                    public void onNext(Ride ride) {
                        if (ride.getDriverRating() == null && ride.getTip() == null) {
                            onRideRateNeeded.call(ride);
                        } else {
                            App.getStateManager().post(new DriverRatedEvent(true));
                        }
                    }
                });
    }

    private Observable<Ride> getRideIfCompleted() {
        return App.getStateManager().getRideStatus()
                .filter(event -> event.getData() == RideStatus.COMPLETED)
                .flatMap(event -> getRideToRate());
    }

    private Observable<Ride> getRideToRate() {
        long rideToRate = App.getPrefs().getRideToRate();
        if (rideToRate != 0) {
            Ride ride = App.getDataManager().getCurrentRide();
            if (ride == null) {
                return App.getDataManager().checkRideAcceptanceStatus(rideToRate, AvatarType.RIDER.name());
            } else {
                return Observable.just(ride);
            }
        } else {
            return Observable.empty();
        }
    }

    private void checkLoginStatus(Throwable throwable) {
        if (throwable instanceof RetrofitException) {
            if (((RetrofitException) throwable).getStatusCode() == HttpsURLConnection.HTTP_UNAUTHORIZED && isLoggedIn()) {
                logoutUserFromApp(throwable.getMessage());
            }
        }
    }

    public void startServerEventsListening() {
        stopServerEventsListening();
        eventSubscription = getEvents()
                .repeatWhen(observable -> observable)
                .retryWhen(new RetryWithDelay(5000))
                .filter(event -> event != null)
                .subscribe(event -> {
                    if (event.getEventType() == RideStatus.SURGE_AREA_UPDATES) {
                        SurgeAreas surgeAreas = SerializationHelper
                                .deSerialize(event.getParameters(), SurgeAreas.class);
                        if (surgeAreas != null) {
                            onSurgeAreasUpdated(surgeAreas.getSurgeAreas());
                        }
                    } else if (event.getEventType() == RideStatus.SURGE_AREA_UPDATE) {
                        SurgeArea surgeArea = SerializationHelper
                                .deSerialize(event.getParameters(), SurgeArea.class);
                        if (surgeArea != null) {
                            onSurgeAreasUpdated(Collections.singletonList(surgeArea));
                        }
                    } else {
                        serverEvents.onNext(event);
                    }
                },
                throwable -> Timber.e(throwable, "Unexpected error during server event"));
    }

    private void onSurgeAreasUpdated(List<SurgeArea> update) {
        for (SurgeArea surgeArea : update) {
            surgeAreas.remove(surgeArea);
            surgeAreas.add(surgeArea);
        }
        surgeAreaUpdates.onNext(update);
    }

    public void stopServerEventsListening() {
        eventSubscription.unsubscribe();
    }

    public Observable<GlobalConfig> getDriverCityConfig() {
        Timber.e("Trying to get DriverCity from rider app.");
        return Observable.error(new UnsupportedOperationException("This feature is not implemented on rider app so far"));
    }

    public UpdateUserDelegate getUpdateUserDelegateInstance() {
        return new UpdateRiderDelegate();
    }

    public StartupError getStartupError() {
        return startupError;
    }

    public void setStartupError(StartupError startupError) {
        this.startupError = startupError;
    }

    public void setRequestToken(String requestToken) {
        this.requestToken = requestToken;
    }

    public Subscription checkRequestToken() {
        if (App.getPrefs().hasRideId()) {
            requestToken = null;
            return Subscriptions.empty();
        }
        if (App.getDataManager().isAuthorised()) {
            if (requestToken != null) {
                return getRidesService().checkRequestToken(requestToken)
                        .subscribeOn(RxSchedulers.network())
                        .retryWhen(new RetryWhenNoNetwork(1000))
                        .observeOn(RxSchedulers.main())
                        .subscribe(new ApiSubscriber2<Ride>(false) {
                            @Override
                            public void onNext(Ride ride) {
                                super.onNext(ride);
                                requestToken = null;
                                if (!App.getPrefs().hasRideId() || App.getPrefs().getRideId().equals(ride.getId())) {
                                    App.getDataManager().setCurrentRide(ride);
                                    RideStatusProcessor processor = new RideStatusProcessor(App.getDataManager(),
                                            App.getStateManager(),
                                            App.getNotificationManager(),
                                            App.getPrefs());
                                    processor.processRideStatus(ride);

                                    if (ride == null) {
                                        return;
                                    }
                                    String status = ride.getStatus();
                                    switch (valueOf(status)) {
                                        case NO_AVAILABLE_DRIVER:
                                        case ADMIN_CANCELLED:
                                        case DRIVER_CANCELLED:
                                        case RIDER_CANCELLED:
                                        case COMPLETED:
                                            return;
                                    }
                                    RideStatusService.startIfNeeded();
                                }
                            }
                        });
            }
        }
        return Subscriptions.empty();
    }

    public boolean needSync() {
        return needSync;
    }

    public void saveComment(Optional<String> comment, GeoPosition address) {
        rideComments.set(comment, address);
        rideComments.save(App.getPrefs().getUserSpecificPreferences());
    }

    public Optional<String> getComment(GeoPosition address) {
        return rideComments.get(address);
    }

    private void restoreComments() {
        rideComments.restore(App.getPrefs().getUserSpecificPreferences());
    }

    public void postSplitFareChanged() {
        splitFareChanged.onNext(null);
    }

    public Observable<Void> getSplitFareChanged() {
        return splitFareChanged
                .asObservable()
                .onBackpressureLatest();
    }

    public Observable<List<CampaignDetails>> getCampaignDetails(CampaignProvider provider) {
        return getCampaignsService().getProviderCampaigns(provider.getId())
                .subscribeOn(RxSchedulers.network());
    }

    @VisibleForTesting
    public void clear() {
        RideStatusService.stop();
    }
}
