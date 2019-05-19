package com.rideaustin.ui.map;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.CampaignProvider;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.UnpaidConfig;
import com.rideaustin.api.model.DriverLocation;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideStatus;
import com.rideaustin.api.model.Rider;
import com.rideaustin.api.model.campaigns.Campaign;
import com.rideaustin.api.model.surgearea.SurgeAreasResponse;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.entities.GeoPosition;
import com.rideaustin.events.BaseEvent;
import com.rideaustin.events.DriverRatedEvent;
import com.rideaustin.events.RideStatusEvent;
import com.rideaustin.manager.ConnectionStatus;
import com.rideaustin.models.CachedDirection;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseObservable;
import com.rideaustin.ui.map.views.MapView;
import com.rideaustin.ui.viewstub.RequestRideFragment;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.CachedValue;
import com.rideaustin.utils.CarCategoriesHelper;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.LocalizeUtils;
import com.rideaustin.utils.ObjectUtils;
import com.rideaustin.utils.RetryWhenNoNetwork;
import com.rideaustin.utils.SingleSubject;
import com.rideaustin.utils.TimeUtils;
import com.rideaustin.utils.location.LocationHelper;

import org.joda.time.DateTimeConstants;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java8.util.Optional;
import java8.util.stream.StreamSupport;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.api.model.RideStatus.ACTIVE;
import static com.rideaustin.api.model.RideStatus.DRIVER_ASSIGNED;
import static com.rideaustin.api.model.RideStatus.DRIVER_REACHED;
import static com.rideaustin.api.model.RideStatus.REQUESTED;
import static com.rideaustin.utils.CommonConstants.API_CALL_THRESHOLD_MS;

/**
 * Created by yshloma on 23.06.2016.
 */
public class MapViewModel extends RxBaseObservable {

    private static final int TIME_HIDDEN = -1;

    private LatLng pickupLocation;
    private GoogleMap map;
    private BehaviorSubject<LatLng> fetchCarTypesSubject = BehaviorSubject.create();
    private BehaviorSubject<Integer> timeEstimateSubject = BehaviorSubject.create();
    private BehaviorSubject<Integer> bottomOffset = BehaviorSubject.create();
    private BehaviorSubject<Optional<LatLng>> prevRideLocation = BehaviorSubject.create(Optional.empty());
    private SingleSubject<GeoPosition> startAddressChanged = SingleSubject.create();
    private SingleSubject<GeoPosition> destinationAddressChanged = SingleSubject.create();
    private boolean pickupDistanceVerified;
    private final CompositeSubscription eventSubscriptions = new CompositeSubscription();
    private CompositeSubscription addressLoadSubscription = new CompositeSubscription();
    private Subscription carCategoriesSubscription = Subscriptions.empty();
    private Subscription surgeAreasSubscription = Subscriptions.empty();
    private Subscription currentRiderSubscription = Subscriptions.unsubscribed();
    private Subscription nearestDriversSubscription = Subscriptions.empty();
    private Subscription offsetSubscription = Subscriptions.empty();
    private Subscription notRatedRideSubscription = Subscriptions.empty();
    private Subscription myLocationSubscription = Subscriptions.empty();
    private Subscription drawDirectionSubscription = Subscriptions.empty();
    private Subscription connectionSubscription = Subscriptions.empty();
    private Subscription rideRatedSubscription = Subscriptions.empty();
    private Subscription mapUpdateSubscription = Subscriptions.empty();
    private Subscription unpaidSubscription = Subscriptions.empty();
    private Subscription emailVerifiedSubscription = Subscriptions.empty();
    private Subscription campaignSubscription = Subscriptions.empty();
    private MapView mapView;
    private NearDriversManager nearDriversManager = new NearDriversManager();

    public final ObservableField<Float> controlsAlpha = new ObservableField<>(1f);
    public final ObservableBoolean googleServicesEnabled = new ObservableBoolean(true);
    public final ObservableBoolean hasServerConnection = new ObservableBoolean(true);
    public final ObservableBoolean showPrevRide = new ObservableBoolean(false);
    public final ObservableBoolean showCampaign = new ObservableBoolean(false);
    public final ObservableField<String> campaignLogoUrl = new ObservableField<>();
    public final ObservableField<String> campaignText = new ObservableField<>();
    public final ObservableBoolean hasUnmetRequirement = new ObservableBoolean(false);
    public final ObservableField<String> unmetRequirement = new ObservableField<>("");
    public final ObservableBoolean showCompletion = new ObservableBoolean(false);
    public final ObservableBoolean displayCompletionTime = new ObservableBoolean(false);
    public final ObservableField<String> completionTimeValue = new ObservableField<>("5 min");
    private UnmetRequirementType unmetRequirementType = UnmetRequirementType.NONE;
    private String unpaidReminderMessage;

    private volatile GeoPosition startAddress;
    private volatile GeoPosition destinationAddress;

    private int timeToNearestDriver;
    private Rider currentRider;
    private boolean roundUpPopupShown;
    private CachedDirection cachedDirection;
    private ObservableField<String> comment = new ObservableField<>("");
    private ObservableBoolean commentVisible = new ObservableBoolean(false);
    private String restoredComment = "";

    // prevent from frequent API calls with same parameters
    private CachedValue<LatLng> carTypesLocation = new CachedValue<>();
    private CachedValue<LatLng> surgeAreasLocation = new CachedValue<>();

    private String appName;
    private BehaviorSubject<String> toolbarTitle = BehaviorSubject.create();
    private Optional<Campaign> currentCampaign = Optional.empty();

    public MapViewModel(MapView mapView) {
        this.mapView = mapView;
        appName = App.getFormattedAppName();
        toolbarTitle.onNext(appName);
    }

    public void setMap(GoogleMap googleMap) {
        if (googleMap != null) {
            this.map = googleMap;
        }
        subscribeToBottomOffsetChange();
    }

    void setControlsAlpha(float alpha) {
        controlsAlpha.set(alpha);
    }

    void setGoogleServicesEnabled(boolean enabled) {
        googleServicesEnabled.set(enabled);
    }

    Optional<CampaignProvider> getCurrentCampaignProvider() {
        return currentCampaign.flatMap(campaign -> StreamSupport.stream(Optional.ofNullable(App.getConfigurationManager().getLastConfiguration())
                .map(GlobalConfig::getCampaignProviders)
                .orElse(Collections.emptyList()))
                .filter(provider -> provider.getId() == campaign.getId())
                .findAny());
    }

    private void subscribeToBottomOffsetChange() {
        offsetSubscription.unsubscribe();
        offsetSubscription = bottomOffset
                .distinctUntilChanged()
                .onBackpressureLatest()
                .subscribe(mapView::onBottomOffsetChanged);
    }

    public void postBottomOffset(final int bottomOffset) {
        this.bottomOffset.onNext(bottomOffset);
    }

    int getBottomOffset() {
        return bottomOffset.getValue() != null ? bottomOffset.getValue() : 0;
    }

    private void drawDriversOnMap(List<DriverLocation> driversList) {
        nearestDriversSubscription.unsubscribe();
        nearDriversManager.updatePositions(driversList, map);
    }

    void setFingerprintOnlyEnabled(boolean enabled) {
        App.getDataManager().setFingerprintedOnlyEnabled(enabled);
        if (pickupLocation != null) {
            loadDriversNearBy(pickupLocation, true);
            reloadCarTypes(pickupLocation);
        }
    }

    void setFemaleOnlyEnabled(boolean enabled) {
        App.getDataManager().setFemaleOnlyEnabled(enabled);
        if (pickupLocation != null) {
            loadDriversNearBy(pickupLocation, true);
            reloadCarTypes(pickupLocation);
        }
    }

    void reloadCarTypes(LatLng latLng) {
        Timber.d("::reloadCarTypes::" + "latLng = [" + latLng + "]");
        if (latLng == null) {
            return;
        }
        if (!carTypesLocation.cache(latLng, API_CALL_THRESHOLD_MS)) {
            // hey, you requested it recently, wait a bit
            return;
        }
        setPickupLocation(latLng);
    }

    void onCameraChange(CameraPosition cameraPosition) {
        if (cameraPosition != null && !mapView.isInRideRequest()
                && (startAddress == null || !LocationHelper.areLocationsEqual(pickupLocation, cameraPosition.target))) {
            setPickupLocation(cameraPosition.target);
            setPickupDistanceVerified(false);
            if (!isActiveRide()) {
                loadAddressForLocation(pickupLocation);
            }
        }
    }

    private void loadAddressForLocation(LatLng latLng) {
        resetTimeToNearestDriver();
        cancelAddressFetch();
        addressLoadSubscription.add(LocationHelper.loadBestAddress(latLng)
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(geoPosition -> {
                    setStartAddress(geoPosition);
                    mapView.onAddressAvailable(geoPosition.getPlaceName());
                    loadDriversNearBy(latLng, false);
                }, throwable -> {
                    Timber.e(throwable);
                    mapView.onAddressNotAvailable();
                }));
    }

    public boolean isPickAddressEntered() {
        return startAddress != null;
    }

    public boolean isPickAnDestinationEntered() {
        return startAddress != null && !TextUtils.isEmpty(startAddress.getAddressLine()) && destinationAddress != null && !TextUtils.isEmpty(destinationAddress.getAddressLine());
    }

    void drawDirectionFromCarToPickup(LatLng car, LatLng pickup) {
        if (prevRideLocation.getValue().isPresent()) {
            drawDirectionBetweenPoints(car, pickup, prevRideLocation.getValue().get());
        } else {
            drawDirectionBetweenPoints(car, pickup);
        }
    }

    void drawDirectionFromCarToDestination(LatLng car, LatLng destination) {
        drawDirectionBetweenPoints(car, destination);
    }

    private void drawDirectionBetweenPoints(LatLng start, LatLng end, LatLng... waypoints) {
        if (isDirectionCached(start, end, waypoints)) {
            mapView.drawDirectionOnMap(cachedDirection.getDirection());
            return;
        }
        if (isDirectionRequested(start, end, waypoints)) {
            // prevent cancel/subscribe on same request
            return;
        }
        requestDirection(start, end);
        cancelDrawDirection();

        drawDirectionSubscription = (waypoints.length > 0
                ? App.getDataManager().getDirectionWithWayPoints(start, end, waypoints)
                : App.getDataManager().getDirection(start, end))
                .observeOn(RxSchedulers.main())
                .subscribe(direction -> {
                    cacheDirection(start, end, direction, waypoints);
                    mapView.drawDirectionOnMap(direction);
                }, Timber::e);
    }

    void cancelDrawDirection() {
        drawDirectionSubscription.unsubscribe();
    }

    void reloadSurgeArea(final LatLng cameraPosition) {
        if (!surgeAreasLocation.cache(cameraPosition, API_CALL_THRESHOLD_MS)) {
            // hey, you requested it recently, wait a bit
            return;
        }
        Integer cityId = App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId();
        surgeAreasSubscription.unsubscribe();
        surgeAreasSubscription = App.getDataManager().getSurgeAreasService()
                .getSurgeAreasList(cameraPosition.latitude, cameraPosition.longitude, cityId)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<SurgeAreasResponse>() {
                    @Override
                    public void onNext(SurgeAreasResponse surgeAreasResponse) {
                        super.onNext(surgeAreasResponse);
                        if (surgeAreasResponse != null) {
                            App.getDataManager().setSurgeArea(surgeAreasResponse.surgeAreas);
                        } else {
                            App.getDataManager().setSurgeArea(Collections.emptyList());
                        }
                        if (mapView != null) {
                            mapView.onSurgeAreasChanged();
                        }
                    }

                    @Override
                    public void onError(BaseApiException e) {
                        App.getDataManager().setSurgeArea(Collections.emptyList());
                        Timber.e(e, e.getMessage());
                    }
                });
    }

    void loadDriversNearBy(LatLng pickupLocation, boolean resetCurrent) {
        if (pickupLocation == null) {
            return;
        }

        if (resetCurrent) {
            resetTimeToNearestDriver();
        }

        if (!isAvailableOnLocation(pickupLocation)) {
            resetTimeToNearestDriver();
            clearNearestCars();
            mapView.onNotAvailableAtLocation();
            return;
        }

        onTimeToNearestDriverRequested();
        nearestDriversSubscription.unsubscribe();
        nearestDriversSubscription = DriversNearByHelper.getAvailableDriversNearBy(pickupLocation)
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<List<DriverLocation>>(null) {
                    @Override
                    public void onNext(List<DriverLocation> drivers) {
                        drawDriversOnMap(drivers);
                        if (drivers.isEmpty()) {
                            setNoAvailableDrivers();
                            resetTimeToNearestDriver();
                        } else {
                            DriverLocation driverLocation = Collections.min(drivers, (lhs, rhs) -> lhs.getDrivingTimeToRider() - rhs.getDrivingTimeToRider());
                            int eta = driverLocation.getDrivingTimeToRider() / DateTimeConstants.SECONDS_PER_MINUTE;
                            setTimeToNearestDriver(eta);
                        }
                    }
                });
    }

    void resetTimeToNearestDriver() {
        timeToNearestDriver = TIME_HIDDEN;
        mapView.onTimeToPickupCleared();
        timeEstimateSubject.onNext(timeToNearestDriver);
        nearestDriversSubscription.unsubscribe();
    }

    void setTimeToNearestDriver(int timeToNearestDriver) {
        this.timeToNearestDriver = timeToNearestDriver;
        String value = timeToNearestDriver > 0 ? String.valueOf(timeToNearestDriver) : Constants.LESS_THAN_MIN;
        mapView.onTimeToPickupResult(String.valueOf(value));
        timeEstimateSubject.onNext(timeToNearestDriver);
    }

    private void onTimeToNearestDriverRequested() {
        if (timeToNearestDriver == TIME_HIDDEN) {
            mapView.onTimeToPickupRequested();
        }
    }

    Observable<Integer> getTimeEstimateObservable() {
        return timeEstimateSubject
                // RA-11402: avoid back-pressure on subject using buffer
                // Note: no need to serialize as subject pushed on main thread only
                .onBackpressureLatest()
                .asObservable();
    }

    private void setNoAvailableDrivers() {
        mapView.onNoDriversAvailable();
    }

    private boolean isAvailableOnLocation(@NonNull LatLng pickupLocation) {
        return App.getConfigurationManager().isInsideCityBoundaries(pickupLocation);
    }

    public LatLng getPickupLocation() {
        return pickupLocation;
    }

    void setPickupLocation(LatLng location) {
        if (!ObjectUtils.equals(pickupLocation, location)) {
            pickupLocation = location;
            fetchCarTypesSubject.onNext(location);
        }
    }

    public Observable<LatLng> observePickupLocation() {
        return fetchCarTypesSubject.asObservable().serialize().onBackpressureLatest();
    }

    boolean pickupEqualToStartAddress() {
        return startAddress != null
                && LocationHelper.areLocationsEqual(pickupLocation, startAddress.getLatLng());
    }

    Optional<RideStatusEvent> getCurrentRideStatusEvent() {
        return App.getStateManager().getLastRideStatus();
    }

    public void clearRideState() {
        App.getStateManager().clearRideState();
    }

    private boolean isActiveDriverExist() {
        return App.getStateManager().getLastRideStatus()
                .map(RideStatusEvent::getResp)
                .map(Ride::hasActiveDriver)
                .orElse(false);
    }

    boolean isDriverAssigned() {
        return isActiveDriverExist() && App.getStateManager().getLastRideStatus()
                .map(RideStatusEvent::getResp)
                .map(ride -> ride.hasOneOfStatuses(ACTIVE, DRIVER_ASSIGNED, DRIVER_REACHED))
                .orElse(false);
    }

    boolean isDriverReached() {
        return isActiveDriverExist() && App.getStateManager().getLastRideStatus()
                .map(RideStatusEvent::getResp)
                .map(ride -> ride.hasOneOfStatuses(ACTIVE, DRIVER_REACHED))
                .orElse(false);
    }

    boolean isRideStartedByDriver() {
        return isActiveDriverExist() && App.getStateManager().getLastRideStatus()
                .map(RideStatusEvent::getResp)
                .map(ride -> ride.hasOneOfStatuses(ACTIVE))
                .orElse(false);
    }

    boolean isRideRequested() {
        return App.getStateManager().getLastRideStatus()
                .map(RideStatusEvent::getResp)
                .map(ride -> ride.hasOneOfStatuses(REQUESTED))
                .orElse(false);
    }

    Observable<Optional<LatLng>> getPrevRideLocationObservable() {
        return prevRideLocation
                .distinctUntilChanged()
                .asObservable()
                .onBackpressureLatest();
    }

    @Override
    public void onStart() {
        super.onStart();
        subscribeToConfigurationChanges();
        subscribeToFilteredRideEvents();
        subscribeToCarTypesChange();
        subscribeToBottomOffsetChange();
        subscribeToConnectionStatus();
        subscribeToUnpaidBalance();
        subscribeToEmailVerification();
        subscribeToRideRatedEvents();
        subscribeToMapUpdates();
        subscribeToFavoriteUpdates();
        updateUnmetRequirements();
    }

    private void subscribeToFavoriteUpdates() {
        eventSubscriptions.add(App.getPrefs()
                .getFavoritePlaceChanged()
                .subscribe(s -> {
                    // force update custom names which may have changed
                    // see also MainMapFragment#refreshAddresses()
                    if (startAddress != null && startAddress.hasCustomName()) {
                        startAddress.setCustomName(null);
                    }
                    if (destinationAddress != null && destinationAddress.hasCustomName()) {
                        destinationAddress.setCustomName(null);
                    }
                }));
    }

    private void subscribeToMapUpdates() {
        mapUpdateSubscription = App.getStateManager().getMapStatus()
                .subscribe(mapUpdateEvent -> {
                    // Handle result there
                    if (!isActiveRide()) {
                        if (getPickupLocation() != null) {
                            loadDriversNearBy(getPickupLocation(), false);
                        } else {
                            Timber.d("::onMapUpdateEvent:: pickup location == null");
                        }
                    } else {
                        if (shouldUpdateActiveRide()) {
                            mapView.updateActiveRide();
                        }
                    }
                });

    }

    private void subscribeToRideRatedEvents() {
        rideRatedSubscription = App.getStateManager()
                .getRateStatus()
                .filter(DriverRatedEvent::isDriverRated)
                .subscribe(driverRatedEvent -> {
                    mapView.onRideRated(driverRatedEvent);
                    App.getStateManager().clearRideToRateState();
                });
    }

    private void subscribeToConnectionStatus() {
        connectionSubscription.unsubscribe();
        connectionSubscription = App.getInstance().getConnectionStatusManager()
                .getStatusObservable()
                .subscribe(this::doOnConnectionStatus);
    }

    private void subscribeToUnpaidBalance() {
        unpaidSubscription.unsubscribe();
        unpaidSubscription = App.getDataManager().getUnpaidBalanceObservable()
                // skip first value from subject
                // updateUnmetRequirements() is be called onStart()
                .skip(1)
                .subscribe(b -> updateUnmetRequirements());
    }

    private void subscribeToEmailVerification() {
        emailVerifiedSubscription.unsubscribe();
        emailVerifiedSubscription = App.getDataManager().getEmailVerifiedObservable()
                // skip first value from subject
                // updateUnmetRequirements() is be called onStart()
                .skip(1)
                .subscribe(b -> updateUnmetRequirements());
    }

    void updateUnmetRequirements() {
        if (!App.getDataManager().isEmailVerified()) {
            hasUnmetRequirement.set(true);
            unmetRequirement.set(App.getInstance().getString(R.string.notification_verify_email_before));
            unmetRequirementType = UnmetRequirementType.EMAIL_VERIFICATION;
        } else if (!App.getDataManager().hasPaymentMethods()) {
            hasUnmetRequirement.set(true);
            unmetRequirement.set(App.getInstance().getString(R.string.notification_set_payment_before));
            unmetRequirementType = UnmetRequirementType.PAYMENT_METHOD;
        } else if (App.getDataManager().isPrimaryCardExpired()) {
            hasUnmetRequirement.set(true);
            unmetRequirement.set(App.getInstance().getString(R.string.notification_primary_card_expired));
            unmetRequirementType = UnmetRequirementType.PRIMARY_CARD_EXPIRED;
        } else if (App.getDataManager().hasUnpaid()) {
            hasUnmetRequirement.set(true);
            unmetRequirement.set(unpaidReminderMessage);
            unmetRequirementType = UnmetRequirementType.UNPAID_BALANCE;
        } else {
            hasUnmetRequirement.set(false);
            unmetRequirement.set("");
            unmetRequirementType = UnmetRequirementType.NONE;
        }
    }

    public UnmetRequirementType getUnmetRequirementType() {
        return unmetRequirementType;
    }

    private void doOnConnectionStatus(ConnectionStatus status) {
        // RA-12366: do not show error when server is unreachable
        // only when there is no network in general
        hasServerConnection.set(status != ConnectionStatus.DISCONNECTED);
    }

    private void subscribeToCarTypesChange() {
        eventSubscriptions.add(fetchCarTypesSubject.subscribe(this::loadCarCategoriesAvailableAtLocation));
    }

    private void loadCarCategoriesAvailableAtLocation(LatLng latLng) {
        carCategoriesSubscription.unsubscribe();
        carCategoriesSubscription = CarCategoriesHelper
                .fetchCarCategoriesAvailableAtLocation(latLng)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(mapView::onCarTypesUpdated, throwable -> Timber.e(throwable, "Cannot load car types"));
    }

    private void subscribeToFilteredRideEvents() {
        eventSubscriptions.add(
                App.getStateManager().getRideStatus()
                        .doOnNext(event -> doOnRide(event.getResp()))
                        .distinctUntilChanged((previous, current) -> {
                            Timber.d(":::event::: %s", current.getData());
                            if (current.getData() == RideStatus.RIDE_REQUEST_ERROR) {
                                return false;
                            }
                            return previous.getData() == current.getData();
                        })
                        // RA-8890: prevent consumer from back pressure,
                        // see http://stackoverflow.com/a/40150152/6151244
                        // and use the latest event in this case.
                        .onBackpressureLatest()
                        .observeOn(RxSchedulers.main())
                        .subscribe(this::processRideEvent));
    }

    private void processRideEvent(RideStatusEvent rideStatusEvent) {
        switch (rideStatusEvent.getData()) {
            case REQUESTED:
                setStartAddressDuringRequest(rideStatusEvent.getResp().getStartPosition());
                setDestinationAddressDuringRequest(rideStatusEvent.getResp().getEndPosition());
                mapView.onRideRequested();
                toolbarTitle.onNext(App.getInstance().getString(R.string.map_title_requesting));
                break;
            case NO_AVAILABLE_DRIVER:
                mapView.onRideNoAvailableDriver();
                break;
            case ADMIN_CANCELLED:
                mapView.onRideAdminCancelled(rideStatusEvent);
                break;
            case DRIVER_CANCELLED:
                mapView.onRideDriverCancelled(rideStatusEvent);
                break;
            case RIDER_CANCELLED:
                mapView.onRideRiderCancelled(rideStatusEvent);
                break;
            case DRIVER_ASSIGNED:
                mapView.onRideDriverAssigned(rideStatusEvent);
                toolbarTitle.onNext(App.getInstance().getString(R.string.map_title_driver_on_way));
                break;
            case DRIVER_REACHED:
                mapView.onRideDriverReached(rideStatusEvent);
                toolbarTitle.onNext(App.getInstance().getString(R.string.map_title_driver_arrived));
                break;
            case ACTIVE:
                mapView.onRideActive(rideStatusEvent);
                toolbarTitle.onNext(appName);
                break;
            case COMPLETED:
                mapView.onRideCompleted(rideStatusEvent);
                break;
            case FINISHED:
                mapView.onRideFinished();
                break;
            case RIDE_REQUEST_ERROR:
                mapView.onRideRequestError(rideStatusEvent);
                break;
            case UNKNOWN:
                break;
        }
        mapView.manageOptionMenuButton();
    }

    private void setStartAddressDuringRequest(@Nullable GeoPosition startAddress) {
        if (startAddress == null) {
            return;
        }
        if (ObjectUtils.equals(this.startAddress, startAddress)) {
            return;
        }
        this.startAddress = startAddress;
        this.pickupLocation = startAddress.getLatLng();
        startAddressChanged.onNext(startAddress);
    }

    private void setDestinationAddressDuringRequest(@Nullable GeoPosition destinationAddress) {
        if (destinationAddress == null) {
            return;
        }
        if (ObjectUtils.equals(this.destinationAddress, destinationAddress)) {
            return;
        }
        this.destinationAddress = destinationAddress;
        App.getPrefs().setDestinationGeoPosition(destinationAddress);
        destinationAddressChanged.onNext(destinationAddress);
    }

    private void doOnRide(@Nullable Ride ride) {
        boolean hasPrevRide = ride != null
                && ride.getPrevRide() != null
                && RideStatus.DRIVER_ASSIGNED.toString().equals(ride.getStatus())
                && RideStatus.ACTIVE.toString().equals(ride.getPrevRide().getStatus());
        showPrevRide.set(hasPrevRide);
        if (hasPrevRide) {
            prevRideLocation.onNext(Optional.ofNullable(ride.getPrevRide().getEndLocation()));
        } else {
            prevRideLocation.onNext(Optional.empty());
        }
        updateCompletionTime(ride);
    }

    private void subscribeToConfigurationChanges() {
        eventSubscriptions.add(App.getConfigurationManager()
                .getLastAndRequestUpdates()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnConfig, Timber::e));
    }

    private void doOnConfig(GlobalConfig config) {
        if (!ObjectUtils.equals(config.getGeneralInformation().getApplicationNamePipe(), appName)) {
            appName = config.getGeneralInformation().getApplicationNamePipe();
            if (!isCustomToolbarTitle()) {
                toolbarTitle.onNext(appName);
            }
        }
        String unpaidMessage = Optional.ofNullable(config)
                .map(GlobalConfig::getUnpaidConfig)
                .map(UnpaidConfig::getWarningMessage)
                .orElse(App.getInstance().getString(R.string.unpaid_warning));
        if (!unpaidMessage.equals(unpaidReminderMessage)) {
            unpaidReminderMessage = unpaidMessage;
            if (unmetRequirementType == UnmetRequirementType.UNPAID_BALANCE) {
                unmetRequirement.set(unpaidReminderMessage);
            }
        }
    }

    private boolean isCustomToolbarTitle() {
        return App.getStateManager().getLastRideStatus()
                .map(RideStatusEvent::getResp)
                .map(ride -> ride.hasOneOfStatuses(REQUESTED, DRIVER_ASSIGNED, DRIVER_REACHED))
                .orElse(false);
    }

    void setRideRequestShown(boolean shown) {
        App.getPrefs().setRideRequestShown(shown);
        if (!isCustomToolbarTitle()) {
            if (shown) {
                toolbarTitle.onNext(App.getInstance().getString(R.string.map_title_confirmation));
            } else {
                toolbarTitle.onNext(appName);
            }
        }
        showCampaign.set(canShowCampaign());
    }

    Observable<String> getToolbarTitle() {
        return toolbarTitle.asObservable()
                .serialize()
                .onBackpressureLatest();
    }

    @Override
    public void onStop() {
        super.onStop();
        eventSubscriptions.clear();
        addressLoadSubscription.clear();
        carCategoriesSubscription.unsubscribe();
        surgeAreasSubscription.unsubscribe();
        offsetSubscription.unsubscribe();
        notRatedRideSubscription.unsubscribe();
        currentRiderSubscription.unsubscribe();
        connectionSubscription.unsubscribe();
        carTypesLocation.reset();
        surgeAreasLocation.reset();
        myLocationSubscription.unsubscribe();
        unpaidSubscription.unsubscribe();
        emailVerifiedSubscription.unsubscribe();
        rideRatedSubscription.unsubscribe();
        mapUpdateSubscription.unsubscribe();
        cancelDrawDirection();
        clearNearestCars();
    }

    void onDestroy() {
        campaignSubscription.unsubscribe();
    }

    private boolean shouldUpdateActiveRide() {
        return App.getStateManager().getLastRideStatus()
                .map(BaseEvent::getData)
                .filter(status -> status == RideStatus.DRIVER_ASSIGNED
                        || status == RideStatus.DRIVER_REACHED
                        || status == RideStatus.ACTIVE)
                .isPresent();
    }

    public boolean isActiveRide() {
        return App.getPrefs().getRideId() != RequestRideFragment.RIDE_NOT_EXISTS;
    }

    boolean shouldShowContact() {
        return isActiveDriverExist() && App.getStateManager().getLastRideStatus()
                .map(RideStatusEvent::getResp)
                .map(ride -> ride.hasOneOfStatuses(DRIVER_REACHED, DRIVER_ASSIGNED))
                .orElse(false);
    }

    public GeoPosition getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(GeoPosition destinationAddress) {
        setDestinationAddress(destinationAddress, false);
    }

    public void setDestinationAddress(GeoPosition destinationAddress, boolean fire) {
        this.destinationAddress = destinationAddress;
        App.getPrefs().setDestinationGeoPosition(destinationAddress);
        if (fire) {
            destinationAddressChanged.onNext(destinationAddress);
            checkCampaign();
        }
    }

    public GeoPosition getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(GeoPosition startAddress) {
        setStartAddress(startAddress, false);
    }

    public void setStartAddress(GeoPosition startAddress, boolean fire) {
        this.startAddress = startAddress;
        App.getPrefs().setPickupGeoPosition(startAddress);
        if (startAddress != null) {
            setPickupLocation(startAddress.getLatLng());
        }
        if (fire) {
            startAddressChanged.onNext(startAddress);
            checkCampaign();
        }
    }

    public void setPickupDistanceVerified(boolean verified) {
        pickupDistanceVerified = verified;
    }

    public boolean isPickupDistanceVerified() {
        return pickupDistanceVerified;
    }

    /**
     * Not intended for multiple subscriptions, based on {@link SingleSubject}
     */
    Observable<GeoPosition> getStartAddressChanged() {
        return startAddressChanged.asObservable();
    }

    /**
     * Not intended for multiple subscriptions, based on {@link SingleSubject}
     */
    Observable<GeoPosition> getDestinationAddressChanged() {
        return destinationAddressChanged.asObservable();
    }

    void checkCampaign() {
        if (startAddress != null && destinationAddress != null) {
            campaignSubscription.unsubscribe();
            campaignSubscription = App.getDataManager().getFareEstimate(startAddress.getLat(),
                    startAddress.getLng(),
                    destinationAddress.getLat(),
                    destinationAddress.getLng(),
                    App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId(),
                    false)
                    .retryWhen(new RetryWhenNoNetwork(1000))
                    .subscribe(response -> {
                        setCurrentCampaign(response.getCampaignInfo());
                    }, throwable -> {
                        Timber.e(throwable);
                        setCurrentCampaign(null);
                    });
        } else {
            setCurrentCampaign(null);
        }
    }

    void hideCampaign() {
        showCampaign.set(false);
    }

    void clearCampaign() {
        setCurrentCampaign(null);
    }

    private void setCurrentCampaign(@Nullable Campaign campaign) {
        long oldId = currentCampaign.map(Campaign::getId).orElse(0L);
        long newId = Optional.ofNullable(campaign).map(Campaign::getId).orElse(0L);
        if (oldId != newId) {
            currentCampaign = Optional.ofNullable(campaign);
            if (campaign != null) {
                showCampaign.set(canShowCampaign());
                campaignLogoUrl.set(campaign.getBannerIcon());
                campaignText.set(campaign.getBannerText());
            } else {
                showCampaign.set(false);
                campaignLogoUrl.set("");
                campaignText.set("");
            }
        }
    }

    private boolean canShowCampaign() {
        return currentCampaign.isPresent() && App.getPrefs().isRideRequestShown();
    }

    void cancelAddressFetch() {
        addressLoadSubscription.clear();
    }

    void checkUpdate() {
        addSubscription(App.getDataManager()
                .getAppInfoObservable()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(
                        configAppInfoResponse -> {
                            if (AppInfoUtil.isMandatoryRequired(configAppInfoResponse) &&
                                    AppInfoUtil.canShowUpdate()) {
                                mapView.onUpgradeNeeded();
                            }
                        },
                        throwable -> Timber.w("Cannot check app info after ride ended: " + throwable.getMessage(), throwable)));
    }

    /**
     * Check whether to show round up reminder.
     * Popup would be shown if all the following conditions are met:
     * a) check was initiated by user interaction or popup wasn't shown yet
     * b) map is in foreground
     * c) charity request is not in process
     * d) there is no active ride
     * e) there is no ride to rate
     * f) round up is disabled
     * g) popup show limit is not reached
     *
     * @param onUserInteraction indicates whether this check was caused by user interaction
     */
    void checkRoundUpPopup(boolean onUserInteraction) {
        if ((!roundUpPopupShown || onUserInteraction) // user interaction or not shown in this fragment
                && mapView.isInForeground() // map is in foreground
                && currentRiderSubscription.isUnsubscribed() // charity request is not in process
                && !isActiveRide() // not in active ride
                && App.getPrefs().getRideToRate() == 0L // no ride to rate
                && !App.getPrefs().isRoundUpEnabled() // marked as disabled in Prefs
                && !App.getPrefs().roundUpPopupReachedShowLimit()) { // limit for popup reminder not reached
            roundUpPopupShown = true;
            mapView.showRoundUpPopup();
        }
    }

    /**
     * Check current rider's charity.
     * Can initiate round up popup.
     *
     * @see MapViewModel#checkRoundUpPopup(boolean)
     */
    void checkRidersCharity() {
        if (currentRider == null) {
            currentRiderSubscription.unsubscribe();
            currentRiderSubscription = App.getDataManager().getCurrentRider()
                    .observeOn(RxSchedulers.main())
                    .subscribeOn(RxSchedulers.network())
                    .subscribe(this::doOnRiderComplete, Timber::e);
        } else {
            checkRoundUpPopup(false);
        }
    }

    void cancelRoundUp() {
        App.getPrefs().increaseRoundUpPopupShowCount();
    }

    private void doOnRiderComplete(@Nullable Rider rider) {
        currentRider = rider;
        if (currentRider != null) {
            App.getPrefs().setRoundUpEnabled(currentRider.getCharity() != null);
            checkRoundUpPopup(false);
        }
    }

    private void requestDirection(@NonNull LatLng start, @NonNull LatLng end, LatLng... waypoints) {
        cachedDirection = new CachedDirection(start, end, null, waypoints);
    }

    private void cacheDirection(@NonNull LatLng start, @NonNull LatLng end, List<LatLng> direction, LatLng... waypoints) {
        if (isDirectionRequested(start, end, waypoints)) {
            cachedDirection.setDirection(direction, waypoints);
        } else {
            cachedDirection = new CachedDirection(start, end, direction, waypoints);
        }
    }

    private boolean isDirectionRequested(@NonNull LatLng start, @NonNull LatLng end, LatLng... waypoints) {
        return cachedDirection != null && cachedDirection.isDirectionRequested(start, end, waypoints);
    }

    private boolean isDirectionCached(@NonNull LatLng start, @NonNull LatLng end, LatLng... waypoints) {
        return cachedDirection != null && cachedDirection.isDirectionCached(start, end, waypoints);
    }

    public void updateCommentsVisibility() {
        updateCommentsVisibility(false);
    }

    public void updateCommentsVisibility(boolean destinationInputOccurred) {
        // destination entered or request panel shown
        boolean shouldShowOnMap = !isActiveRide() && (destinationAddress != null || destinationInputOccurred || mapView.isInRideRequest());
        // from ride requested to ride started
        boolean shouldShowInRide = isActiveRide() && !isRideStartedByDriver();
        commentVisible.set(shouldShowOnMap || shouldShowInRide);
        if (!commentVisible.get()) {
            comment.set("");
        }
    }

    public Optional<String> getOptionalComments() {
        if (TextUtils.isEmpty(getComment().get().trim())) {
            return Optional.empty();
        } else {
            return Optional.of(getComment().get().trim());
        }
    }

    public ObservableField<String> getComment() {
        return comment;
    }

    public ObservableBoolean isCommentVisible() {
        return commentVisible;
    }

    void sendComment() {
        if (isActiveRide()) {
            App.getDataManager().saveComment(getOptionalComments(), getStartAddress());
            App.getDataManager().getRidesService()
                    .updateCurrentRide(App.getPrefs().getRideId(),
                            null,
                            null,
                            null,
                            null,
                            getOptionalComments().orElse(null))
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(new ApiSubscriber2<>(false));
        }
    }

    void restoreCommentsFromRide(final Ride ride) {
        Optional<String> comments = Optional.ofNullable(ride.getComment());
        restoredComment = comments.orElse("");
        comment.set(restoredComment);
    }

    void restoreCommentsFromCache() {
        // RA-10750: try to restore saved comment for that address
        // The following conditions should be met:
        // 1. user did not change the comment manually (or comment is empty)
        // 2. start address is not empty
        // 3. comment field is visible
        boolean canChange = comment.get().isEmpty() || comment.get().equals(restoredComment);
        boolean shouldRestore = startAddress != null && commentVisible.get();
        if (shouldRestore) {
            Optional<String> comments = App.getDataManager().getComment(startAddress);
            restoredComment = comments.orElse("");
        }
        if (canChange && shouldRestore) {
            comment.set(restoredComment);
        }
    }

    void clearNearestCars() {
        nearestDriversSubscription.unsubscribe();
        nearDriversManager.clearDrivers();
    }

    private void updateCompletionTime(@Nullable Ride ride) {
        boolean hasEstimatedCompletion = ride != null
                && RideStatus.ACTIVE.toString().equals(ride.getStatus())
                && ride.getEstimatedTimeCompletion() != null;
        if (hasEstimatedCompletion) {
            showCompletion.set(true);
            updateCompletionTimeText(ride.getEstimatedTimeCompletion());
        } else {
            showCompletion.set(false);
        }
    }

    private void updateCompletionTimeText(long timestamp) {
        if (displayCompletionTime.get()) {
            completionTimeValue.set(new SimpleDateFormat("h:mm a", Locale.US).format(new Date(timestamp)));
        } else {
            long seconds = (timestamp - TimeUtils.currentTimeMillis()) / 1000;
            completionTimeValue.set(LocalizeUtils.formatDriverEta(App.getInstance(), seconds));
        }
    }

    public void toggleCompletionTimeDisplay() {
        displayCompletionTime.set(!displayCompletionTime.get());
        getCurrentRideStatusEvent()
                .map(RideStatusEvent::getResp)
                .map(Ride::getEstimatedTimeCompletion)
                .filter(timestamp -> timestamp != null)
                .ifPresentOrElse(this::updateCompletionTimeText, () -> showCompletion.set(false));
    }
}
