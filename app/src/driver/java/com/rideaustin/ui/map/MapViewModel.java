package com.rideaustin.ui.map;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.widget.Toast;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.RideUpgrade;
import com.rideaustin.api.config.Variant;
import com.rideaustin.api.model.AvatarType;
import com.rideaustin.api.model.QueueResponse;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideSpecificUpgradeStatus;
import com.rideaustin.api.model.ServerMessage;
import com.rideaustin.api.model.UpgradeRequest;
import com.rideaustin.api.model.UpgradeRequestStatus;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.RequestedCarType;
import com.rideaustin.api.model.surgearea.SurgeArea;
import com.rideaustin.api.model.surgearea.SurgeAreasResponse;
import com.rideaustin.base.ApiSubscriber2;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.engine.BaseStateWithRide;
import com.rideaustin.engine.EngineState;
import com.rideaustin.engine.state.AcceptedState;
import com.rideaustin.engine.state.ArrivedState;
import com.rideaustin.manager.ConnectionStatus;
import com.rideaustin.manager.RideRequestManager;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.riderequest.RideRequestSummaryData;
import com.rideaustin.ui.utils.SurgeAreaUtils;
import com.rideaustin.utils.AppInfoUtil;
import com.rideaustin.utils.toast.RAToast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java8.util.Optional;
import java8.util.stream.StreamSupport;
import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

import static com.rideaustin.engine.EngineState.Type.OFFLINE;
import static com.rideaustin.engine.EngineState.Type.ONLINE;
import static com.rideaustin.utils.CommonConstants.DIRECT_CONNECT_DRIVER_TYPE;
import static com.rideaustin.utils.CommonConstants.UNEXPECTED_STATE_KEY;
import static com.rideaustin.utils.CommonConstants.WOMEN_ONLY_DRIVER_TYPE;


/**
 * Created by yshloma on 23.06.2016.
 */
public class MapViewModel extends BaseObservable {

    public final ObservableBoolean hasServerConnection = new ObservableBoolean(true);

    private CompositeSubscription subscriptions = new CompositeSubscription();
    private Subscription surgeUpdatedSubscription = Subscriptions.empty();
    private Subscription loadSurgeAreasSubscription = Subscriptions.unsubscribed();
    private Subscription activeAreaDetailsSubscription = Subscriptions.empty();
    private Subscription configSubscription = Subscriptions.empty();
    private Subscription uiStateSubscription = Subscriptions.empty();
    private MapViewListener listener;
    private BehaviorSubject<List<SurgeArea>> surgeAreasSubject = BehaviorSubject.create();
    private boolean trackDriverLocation = true;

    public MapViewModel(final MapViewListener listener) {
        this.listener = listener;
        this.surgeAreasSubject.onNext(new ArrayList<>());
        subscribeToSurgeAreaUpdates();
    }


    public void onStart() {
        subscribeToConnectionStatus();
        // NOTE: map may be not ready yet here
        // Please consider using onMapReady() if map required
    }

    public void onMapReady() {
        subscribeToConfigurationUpdate();
        subscribeToUiStateChanges();
    }

    private void subscribeToConnectionStatus() {
        subscriptions.add(App.getInstance().getConnectionStatusManager()
                .getStatusObservable()
                .subscribe(this::doOnConnectionStatus));
    }

    private void doOnConnectionStatus(ConnectionStatus status) {
        // RA-12366: do not show error when server is unreachable
        // only when there is no network in general
        hasServerConnection.set(status != ConnectionStatus.DISCONNECTED);
    }

    private void subscribeToUiStateChanges() {
        uiStateSubscription.unsubscribe();
        uiStateSubscription = App.getInstance().getStateManager()
                .getUIStateObservable()
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(baseEngineState -> {
                    listener.onUiStateChanged(baseEngineState);
                    if (baseEngineState.getType() == ONLINE) {
                        baseEngineState.getPreviousType().ifPresent(type -> {
                            if (type == OFFLINE) {
                                baseEngineState.setPreviousType(null);
                                showGoingOnlinePopup();
                            }
                        });
                    }
                });
    }

    private void showGoingOnlinePopup() {
        subscriptions.add(App.getConfigurationManager().getLiveConfig()
                .filter(config -> config.getOnline() != null && config.getOnline().getShouldShowPopup())
                .flatMap(config -> getRideRequestSummary())
                .observeOn(RxSchedulers.main())
                .subscribe(summaryData -> {
                    Context context = App.getInstance();
                    String title = App.getDataManager().getDriver()
                            .map(Driver::getSelectedCar)
                            .map(Optional::get)
                            .map(car -> context.getString(R.string.ride_request_online,
                                    car.getMake(), car.getModel()))
                            .orElse(context.getString(R.string.ride_request_you_are_online));

                    StringBuilder sb = new StringBuilder();
                    sb.append(context.getString(R.string.ride_request_description_online));
                    RideRequestManager manager = App.getInstance().getRideRequestManager();
                    if (manager.isDirectConnect()) {
                        sb.append(context.getString(R.string.ride_request_dc_only_online,
                                summaryData.getSelectedAsString()));
                    } else if (manager.isWomenOnly()) {
                        sb.append(context.getString(R.string.ride_request_female_only_online,
                                summaryData.getSelectedAsString()));
                    } else {
                        sb.append(summaryData.getSelectedAsString());
                        if (manager.isWomenOnlyAvailable()) {
                            sb.append(context.getString(R.string.ride_request_and));
                            sb.append(summaryData.getWomenOnlyAvailableAsString());
                            sb.append(context.getString(R.string.ride_request_female_driver_requests));
                        }
                        if (manager.isDirectConnectAvailable()) {
                            sb.append(context.getString(R.string.ride_request_and));
                            sb.append(summaryData.getDirectConnectAvailableAsString());
                            sb.append(context.getString(R.string.ride_request_dc_requests));
                        }
                    }

                    listener.showGoingOnlinePopup(title, sb.toString());
                }, Timber::e));
    }

    private Observable<RideRequestSummaryData> getRideRequestSummary() {
        RideRequestManager manager = App.getInstance().getRideRequestManager();
        return Observable.zip(manager.getAvailableCarTypes(false),
                manager.getSelectedCarTypes(false),
                manager.getCustomTypeCarTypes(WOMEN_ONLY_DRIVER_TYPE),
                manager.getCustomTypeCarTypes(DIRECT_CONNECT_DRIVER_TYPE),
                RideRequestSummaryData::new);
    }

    private void subscribeToActiveAreaDetails(final GlobalConfig globalConfig) {
        final Integer cityId = globalConfig.getCurrentCity().getCityId();
        activeAreaDetailsSubscription.unsubscribe();
        activeAreaDetailsSubscription = App.getDataManager().getDriverService().getActiveAreaDetails(cityId)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(queueResponses -> listener.settleQueueList(queueResponses), throwable -> Timber.e(throwable, "::getQueueList::"));
    }

    private void subscribeToSurgeAreaUpdates() {
        surgeUpdatedSubscription.unsubscribe();
        surgeUpdatedSubscription = App.getInstance().getStateManager()
                .getSurgeUpdateObservable()
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnSurgeAreaUpdated, this::doOnSurgeAreaError);
    }

    private void doOnSurgeAreaUpdated(List<SurgeArea> update) {
        List<SurgeArea> surgeAreas = surgeAreasSubject.getValue();
        for (int j = 0; j < update.size(); j++) {
            SurgeArea surgeArea = update.get(j);
            int index = -1;
            int lookUpId = surgeArea.getId();
            for (int i = 0; i < surgeAreas.size(); i++) {
                if (surgeAreas.get(i).getId() == lookUpId) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                surgeAreas.set(index, surgeArea);
            } else {
                surgeAreas.add(surgeArea);
            }
        }
        surgeAreasSubject.onNext(surgeAreas);
    }

    private void doOnSurgeAreaError(Throwable throwable) {
        Timber.e(throwable, "::getUpdateSurgeAreaEvent::");
        subscribeToSurgeAreaUpdates();
    }

    void loadSurgeAreas() {
        if (!loadSurgeAreasSubscription.isUnsubscribed()) {
            return;
        }
        List<SurgeArea> surgeAreas = surgeAreasSubject.getValue();
        if (surgeAreas != null && !surgeAreas.isEmpty()) {
            surgeAreasSubject.onNext(surgeAreas);
            return;
        }
        Timber.d("::loadSurgeAreas::");
        final Integer cityId = App.getConfigurationManager().getLastConfiguration().getCurrentCity().getCityId();
        loadSurgeAreasSubscription = App.getDataManager()
                .loadSurgeAreasResponse(cityId)
                .map(SurgeAreasResponse::getSurgeAreas)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber2<List<SurgeArea>>(false) {
                    @Override
                    public void onNext(List<SurgeArea> surgeAreas) {
                        surgeAreasSubject.onNext(surgeAreas);
                    }
                });
    }

    void clearSurgeAreas() {
        surgeAreasSubject.onNext(null);
    }

    Observable<List<SurgeArea>> getSurgeAreasObservable() {
        return surgeAreasSubject.asObservable()
                .serialize().onBackpressureLatest()
                .filter(surgeAreas -> surgeAreas != null)
                .map(this::applySurgeAreaFilters);
    }

    private List<SurgeArea> applySurgeAreaFilters(final List<SurgeArea> surgeAreas) {
        EngineState.Type currentType = App.getInstance().getStateManager().getCurrentEngineStateType();
        if (currentType != ONLINE && currentType != OFFLINE) {
            return new ArrayList<>();
        }
        List<SurgeArea> list = new ArrayList<>(surgeAreas);
        Set<String> categories = App.getInstance().getRideRequestManager().getSelectedCategories();
        filterSurgeAreaByRequestType(categories, list);
        return list;
    }

    /**
     * Filter is applied according to Driver preferences of ride request.
     */
    private void filterSurgeAreaByRequestType(Set<String> currentRideRequestTypes, List<SurgeArea> surgeAreas) {
        Iterator<SurgeArea> iterator = surgeAreas.iterator();
        while (iterator.hasNext()) {
            SurgeArea surgeArea = iterator.next();
            boolean isNeedRemove = true;
            for (String currentRideRequestType : currentRideRequestTypes) {
                if (SurgeAreaUtils.getPriceFactor(surgeArea, currentRideRequestType) > 1f) {
                    isNeedRemove = false;
                    break;
                }
            }
            if (isNeedRemove) {
                iterator.remove();
            }
        }
    }

    private void subscribeToConfigurationUpdate() {
        App.getConfigurationManager().checkMandatoryUpdateAndRefreshConfiguration();
        configSubscription.unsubscribe();
        configSubscription = App.getConfigurationManager()
                .getConfigurationUpdates()
                .subscribeOn(RxSchedulers.computation())
                .observeOn(RxSchedulers.main())
                .subscribe(globalConfig -> {
                    listener.onConfigurationUpdated(globalConfig);
                    subscribeToActiveAreaDetails(globalConfig);
                });
    }

    public void onMapClose() {
        configSubscription.unsubscribe();
        uiStateSubscription.unsubscribe();
    }

    public void onStop() {
        subscriptions.clear();
        loadSurgeAreasSubscription.unsubscribe();
        activeAreaDetailsSubscription.unsubscribe();
    }

    public void checkUpdate() {
        subscriptions.add(App.getDataManager()
                .getAppInfoObservable()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(
                        configAppInfoResponse -> {
                            if (AppInfoUtil.isMandatoryRequired(configAppInfoResponse) &&
                                    AppInfoUtil.canShowUpdate()) {
                                listener.onUpgradeNeeded();
                            }
                        },
                        throwable -> Timber.w("Cannot check app info after ride ended: " + throwable.getMessage(), throwable)));
    }

    public boolean isTrackingDriverLocation() {
        if (trackDriverLocation) {
            EngineState.Type type = App.getInstance().getStateManager().getCurrentEngineStateType();
            switch (type) {
                case INACTIVE:
                case OFFLINE:
                case ONLINE:
                    return true;
            }
        }
        return false;
    }

    public void setTrackDriverLocation(boolean trackDriverLocation) {
        this.trackDriverLocation = trackDriverLocation;
    }

    public void onDestroyView() {
        surgeUpdatedSubscription.unsubscribe();
    }

    public void onCancelUpgradeClicked() {
        final Ride ride = App.getInstance().getStateManager().getCurrentRideIfAny().orElse(null);
        if (ride != null) {
            subscriptions.add(App.getDataManager().getRidesService().cancelRideUpgrade(AvatarType.DRIVER.avatarType)
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(new ApiSubscriber2<ServerMessage>(listener.getCallback()) {
                        @Override
                        public void onNext(ServerMessage serverMessage) {
                            super.onNext(serverMessage);
                            setUpgradeStatus(ride.getId(), UpgradeRequestStatus.CANCELLED);
                            listener.onRideUpgradeCancelled(serverMessage.getMessage());
                        }
                    }));
        }
    }

    public void onUpgradeCarTypeClicked() {
        if (App.getInstance().getStateManager().isRideUpgradeStatusInRequested()) {
            listener.onRideUpgradeRequested();
        } else {
            String target = getUpgradeTarget().get();
            final Ride ride = App.getInstance().getStateManager().getCurrentRideIfAny().orElse(null);
            if (!TextUtils.isEmpty(target) && ride != null) {
                subscriptions.add(App.getDataManager().getRidesService().requestRideUpgrade(target)
                        .subscribeOn(RxSchedulers.network())
                        .observeOn(RxSchedulers.main())
                        .subscribe(new ApiSubscriber2<ServerMessage>(listener.getCallback()) {
                            @Override
                            public void onNext(ServerMessage serverMessage) {
                                super.onNext(serverMessage);
                                setUpgradeStatus(ride.getId(), UpgradeRequestStatus.REQUESTED);
                                listener.onRideUpgradeRequested();
                            }
                        }));
            } else {
                RAToast.show(R.string.no_type_available, Toast.LENGTH_SHORT);
                Exception e = new Exception(UNEXPECTED_STATE_KEY);
                Timber.e(e, "There is no upgrade possible, but menu is clickable somehow.");
            }
        }
    }

    private void setUpgradeStatus(long rideId, UpgradeRequestStatus status) {
        App.getInstance().getStateManager().getCurrentEngineStateIfAny()
                .filter(engineState -> engineState instanceof BaseStateWithRide)
                .ifPresent(engineState -> ((BaseStateWithRide) engineState).postUpgradeStatus(RideSpecificUpgradeStatus.create(rideId, status)));
    }

    public Optional<String> getUpgradeTarget() {
        List<Variant> variants = Optional.ofNullable(App.getConfigurationManager().getLastConfiguration())
                .map(GlobalConfig::getRideUpgrade)
                .map(RideUpgrade::getVariants)
                .orElse(Collections.emptyList());

        return App.getInstance().getStateManager().getCurrentRideIfAny()
                .map(Ride::getRequestedCarType)
                .map(RequestedCarType::getCarCategory)
                .flatMap(category -> StreamSupport.stream(variants)
                        .filter(variant -> category.equals(variant.getCarCategory()))
                        .filter(variant -> variant.getValidUpgrades() != null)
                        .findAny())
                .flatMap(variant -> {
                    // take intersection of valid upgrade types and user preferences
                    return StreamSupport.stream(App.getInstance().getRideRequestManager()
                            .getSelectedCategories())
                            .filter(possibleUpgrades -> {
                                for (String validUpgrade : variant.getValidUpgrades()) {
                                    if (possibleUpgrades.contains(validUpgrade)) {
                                        return true;
                                    }
                                }
                                return false;
                            })
                            .findFirst();
                });
    }

    /**
     * @return only false if Ride's Upgrade Status is null or NONE.
     * This is useful to track when app is just launched
     * and {@link BaseStateWithRide#rideUpgradeSubject} was not posted any event.
     */
    private boolean isUpgradeAllowedByRideState() {
        return App.getInstance().getStateManager().getCurrentRideIfAny()
                .map(Ride::getUpgradeRequest)
                .map(UpgradeRequest::getStatus)
                .map(UpgradeRequestStatus::valueOf).or(() -> Optional.of(UpgradeRequestStatus.NONE))
                .map(upgradeRequestStatus -> upgradeRequestStatus == UpgradeRequestStatus.NONE).get();
    }

    private boolean isUpgradeAllowedByEngineState() {
        return App.getInstance().getStateManager().getCurrentEngineStateIfAny()
                // only "Arrived" state is eligible
                .filter(engineState -> engineState instanceof ArrivedState)
                // upgrade status should be empty
                .filter(engineState -> ((BaseStateWithRide) engineState).getUpgradeStatus().getStatus() == UpgradeRequestStatus.NONE)
                .isPresent();
    }

    public boolean isUpgradeButtonEnabled() {
        // return true if ride upgrade status is `In Requested` we know this for sure, because we have fresh ride object here.
        return App.getInstance().getStateManager().isRideUpgradeStatusInRequested() ||
                // This is useful when we have fresh ride,
                // but out Ride object's Ride status stays `REQUESTED` for a long time, until ride ends.
                // We get fresh Ride object when Ride requested, (not started, arrived) and app is restarted.
                // so don't trust RideState in Ride object blindly.
                (isUpgradeAllowedByRideState()
                        // Instead we check if current app state is correct.
                        && isUpgradeAllowedByEngineState()
                        // and also we check if there is possible Upgrade Type according to user specifications, and owned car, and server configs.
                        && getUpgradeTarget().isPresent());
    }


    public interface MapViewListener {
        @UiThread
        void onConfigurationUpdated(final GlobalConfig globalConfig);

        @UiThread
        void onUiStateChanged(EngineState state);

        BaseActivityCallback getCallback();

        void onUpgradeNeeded();

        void onRideUpgradeRequested();

        void onRideUpgradeCancelled(String message);

        void showGoingOnlinePopup(String selectedCar, String message);

        void settleQueueList(List<QueueResponse> queues);
    }
}
