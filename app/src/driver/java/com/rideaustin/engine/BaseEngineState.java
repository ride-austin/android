package com.rideaustin.engine;

import android.support.annotation.CallSuper;
import android.support.annotation.StringRes;

import com.rideaustin.App;
import com.rideaustin.BaseApp;
import com.rideaustin.api.DataManager;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.RideAcceptance;
import com.rideaustin.api.model.AirportQueueParams;
import com.rideaustin.api.model.Event;
import com.rideaustin.api.model.GoOfflineParams;
import com.rideaustin.api.model.HandshakeParams;
import com.rideaustin.api.model.Ride;
import com.rideaustin.api.model.RideRequestParams;
import com.rideaustin.manager.location.RALocation;
import com.rideaustin.manager.location.RALocationManager;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.utils.SerializationHelper;
import com.rideaustin.utils.TimeUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import java8.util.Optional;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by rost on 8/11/16.
 */
public abstract class BaseEngineState implements EngineState {

    private StateManager stateManager;
    private Optional<Type> previousType = Optional.empty();
    private CompositeSubscription untilDetachSubscription = new CompositeSubscription();

    private final DriverTrackingType driverTrackingType;
    private boolean notBindUI = false;

    protected BaseEngineState(DriverTrackingType driverTrackingType) {
        this.driverTrackingType = driverTrackingType;
    }

    public Optional<Type> getPreviousType() {
        return previousType;
    }

    public void setPreviousType(Type previousType) {
        this.previousType = Optional.ofNullable(previousType);
    }

    final void activate(StateManager stateManager) {
        this.stateManager = stateManager;
        onActivated();
    }

    protected void processHandshake(Event event) {
        HandshakeParams params = SerializationHelper.deSerialize(event.getParameters(), HandshakeParams.class);
        long now = TimeUtils.currentTimeMillis();
        if (params != null && params.getHandshakeExpiration() >= now) {
            App.getDataManager().acknowledgeRide(params.getRideId())
                    .subscribe(o -> {}, throwable -> {
                        Exception e = new Exception("::RideAcknowledgeFailed::", throwable);
                        Timber.e(e, "Ride acknowledge failed, ride id=" + params.getRideId());
                    });
        } else {
            Exception e = new Exception("::HandshakeRequestExpired::");
            Timber.e(e, "Handshake request expired: event=[" + event + "], now=[" + now + "]");
        }
    }

    protected void processRideRequest(Event event) {
        boolean hasParams = event.getParameters() != null;

        RideRequestParams params = hasParams ? getRideRequestParams(event) : getDefaultRideRequestParams();
        long now = TimeUtils.currentTimeMillis();
        long acceptExpiration = params.getAcceptanceExpiration();

        // time for driver to make a decision
        RideAcceptance acceptance = App.getConfigurationManager().getLastConfiguration().getRideAcceptance();
        long threshold = acceptance != null && acceptance.getDecisionThreshold() != null
                ? acceptance.getDecisionThreshold() * 1000
                : 0; // RA-12181: no threshold by default

        // check if request is not expired
        if (now + threshold >= acceptExpiration) {
            Exception e = new Exception("::RideRequestExpired::");
            Timber.e(e, "Ride request expired: event=[" + event + "], now=[" + now + "], decisionThreshold=[" + threshold + "]");
            return;
        }

        boolean isStacked = event.getNextRide() != null;
        Ride ride = isStacked ? event.getNextRide() : event.getRide();
        Ride currentRide = isStacked && (this instanceof BaseStateWithRide)
                ? ((BaseStateWithRide) this).getRide()
                : null;
        // finally process the request
        NavigationDrawerActivity.getStartIntentForRideRequestLaunch();
        AcceptData acceptData = new AcceptData(acceptExpiration, ride, currentRide);
        switchState(getStateManager().createPendingAcceptState(acceptData));
    }

    private RideRequestParams getRideRequestParams(Event event) {
        RideRequestParams params = SerializationHelper.deSerialize(event.getParameters(), RideRequestParams.class);
        if (params == null) {
            params = getDefaultRideRequestParams();
        }
        return params;
    }

    private RideRequestParams getDefaultRideRequestParams() {
        RideAcceptance acceptance = App.getConfigurationManager().getLastConfiguration().getRideAcceptance();
        long acceptancePeriod = acceptance != null && acceptance.getAcceptancePeriod() != null
                ? acceptance.getAcceptancePeriod() * 1000
                : 10000; // 10 seconds by default
        long now = TimeUtils.currentTimeMillis();
        RideRequestParams params = new RideRequestParams();
        params.setAcceptanceExpiration(now + acceptancePeriod);
        return params;
    }

    final void deactivate() {
        Timber.d("deactivating: %s", getType());
        onDeactivated();
        untilDetachSubscription.unsubscribe();
    }

    protected void subscribeUntilDeactivated(Subscription subscription) {
        untilDetachSubscription.add(subscription);
    }

    @CallSuper
    protected void onActivated() {
        Timber.d("activating: %s", getType());
        App.getInstance().getDriverLocationManager().startTracking(driverTrackingType);
    }

    protected void onDeactivated() {
        App.getInstance().getDriverLocationManager().stopTracking();
    }

    protected LocalSerializer getSerializer() {
        return App.getInstance().getLocalSerializer();
    }

    protected DataManager getDataManager() {
        return App.getDataManager();
    }

    protected StateManager getStateManager() {
        return App.getInstance().getStateManager();
    }

    protected void switchState(BaseEngineState engineState) {
        engineState.setPreviousType(getType());
        stateManager.switchState(engineState);
    }

    public Observable<Object> switchNextWithPendingEvents(boolean forceSend) {
        return App.getInstance()
                .getPendingEventsManager()
                .tryToSend()
                .observeOn(RxSchedulers.main())
                .flatMap(response -> {
                    if (forceSend && !response.isSuccessful()) {
                        // If sending is mandatory for caller and sending failed,
                        // wrap status into error and let caller's stream handle it.
                        // Probably caller would show corresponding error
                        return Observable.error(response.getException());
                    } else {
                        // Otherwise, proceed with any response, just wrap it into SwitchNextData
                        // Let corresponding EngineState decide what to do next
                        return switchNext(response.getSwitchData());
                    }
                });
    }

    protected RALocationManager getDriverLocationManager() {
        return BaseApp.getLocationManager();
    }

    protected void requestNotBindUI() {
        notBindUI = true;
    }

    boolean isNotBindUI() {
        return notBindUI;
    }

    /**
     * Retrieves nearest drivers based on last live location
     * (waits for live location if needed)
     */
    private Observable<List<RALocation>> getNearDrivers() {
        return App.getLocationManager().getLastLocation(false, false)
                .switchMap(location -> {
                    GlobalConfig configuration = App.getConfigurationManager().getLastConfiguration();
                    final Integer cityId = configuration.getCurrentCity().getCityId();
                    return getDataManager().getNearestDrivers(location, cityId);
                });
    }

    public Observable<List<RALocation>> getNearDriversPeriodically(long interval, TimeUnit timeUnit) {
        return getNearDrivers()
                .repeatWhen(observable -> observable.delay(interval, timeUnit, RxSchedulers.computation()))
                .retryWhen(observable -> observable.delay(interval, timeUnit, RxSchedulers.computation()));
    }

    public Observable<Throwable> backgroundErrors() {
        return App.getInstance().getLongPollingManager().getErrors()
                .mergeWith(App.getInstance().getDriverLocationManager().getErrors());
    }

    protected void switchToCorrectState() {
        stateManager.switchToCorrectState();
    }

    public Observable<GoOfflineParams> goOfflineObservable() {
        return getStateManager().getGoOfflineObservable();
    }

    protected String getAirportQueueMessage(Event event, @StringRes int defaultResId) {
        return Optional.ofNullable(event.deserializeParams(AirportQueueParams.class))
                .map(AirportQueueParams::getMessage)
                .orElse(getString(defaultResId));
    }

    protected String getString(@StringRes int resId) {
        return App.getInstance().getString(resId);
    }

    protected String getString(@StringRes int resId, Object... args) {
        return App.getInstance().getString(resId, args);
    }

}
